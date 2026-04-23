# Keytab文件缓存优化 - 设计文档

| 版本 | 日期 | 作者 | 变更说明 |
|:----:|:----:|:----:|:--------|
| 1.0 | 2026-02-11 | DevSyncAgent | 初始版本 |

---

## 一、设计概述

### 1.1 设计目标
通过引入keytab文件缓存机制，解决`getLinkisUserKeytabFile`方法每次创建临时文件导致的Full GC问题。

### 1.2 设计原则
- **最小改动原则**：仅修改问题方法及相关辅助代码，不影响其他功能
- **复用现有机制**：尽量复用现有的`fileSystemCache`清理机制
- **线程安全**：确保并发访问场景下的正确性
- **向后兼容**：保持API接口不变

---

## 二、架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        HDFSUtils                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              UserGroupInformation 调用链                   │  │
│  ├───────────────────────────────────────────────────────────┤  │
│  │  getUserGroupInformation(userName, label)                 │  │
│  │        ├──> isKerberosEnabled(label)                      │  │
│  │        ├──> isKeytabProxyUserEnabled(label)               │  │
│  │        └──> getLinkisUserKeytabFile(userName, label)      │  │
│  │                  [问题方法，需要修改]                      │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    新增：缓存模块                          │  │
│  ├───────────────────────────────────────────────────────────┤  │
│  │  ┌─────────────────────────────────────────────────────┐   │  │
│  │  │  keytabFileCache: ConcurrentHashMap                 │   │  │
│  │  │  Key: userName_label        Value: Path              │   │  │
│  │  └─────────────────────────────────────────────────────┘   │  │
│  │                                                             │  │
│  │  ┌─────────────────────────────────────────────────────┐   │  │
│  │  │  createOrGetCachedKeytabFile(userName, label)       │   │  │
│  │  │    - 检查缓存                                       │   │  │
│  │  │    - 命中: 返回缓存路径                             │   │  │
│  │  │    - 未命中: 创建新临时文件并缓存                   │   │  │
│  │  └─────────────────────────────────────────────────────┘   │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              复用：现有清理机制                             │  │
│  ├───────────────────────────────────────────────────────────┤  │
│  │  现有的 fileSystemCache 清理定时任务                        │  │
│  │    - 60秒执行一次                                          │  │
│  │    - 可扩展增加 keytabFileCache 清理                       │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 核心类结构

#### 2.2.1 HDFSUtils缓存结构

```scala
object HDFSUtils extends Logging {

  // 现有：FileSystem缓存
  private val fileSystemCache: java.util.Map[String, HDFSFileSystemContainer] =
    new ConcurrentHashMap[String, HDFSFileSystemContainer]()

  // 新增：Keytab文件缓存
  private val keytabFileCache: java.util.Map[String, Path] =
    new ConcurrentHashMap[String, Path]()

  // 缓存Key生成
  private def createKeytabCacheKey(userName: String, label: String): String = {
    val normalizedLabel = if (label == null) DEFAULT_CACHE_LABEL else label
    userName + JOINT + normalizedLabel
  }
}
```

---

## 三、详细设计

### 3.1 核心方法设计

#### 3.1.1 createOrGetCachedKeytabFile - 新增方法

**职责**：创建或获取缓存的keytab临时文件

**方法签名**：
```scala
private def createOrGetCachedKeytabFile(userName: String, label: String): Path
```

**流程图**：
```
开始
  │
  ├─> 生成缓存Key：userName_label
  │
  ├─> 检查keytabFileCache中是否存在
  │
  ├─> 存在？
  │   ├─ 是 ─> 检查文件是否存在
  │   │         ├─ 存在 ─> 返回缓存路径
  │   │         └─ 不存在 ─> 重新创建（缓存失效场景）
  │   │
  │   └─ 否 ─> 创建新临时文件
  │             ├─ 读取加密keytab文件
  │             ├─ 解密内容
  │             ├─ 创建临时文件
  │             ├─ 设置权限 rw-------
  │             ├─ 写入解密内容
  │             └─ 缓存文件路径
  │
  └─ 返回文件路径
```

**伪代码**：
```scala
private def createOrGetCachedKeytabFile(userName: String, label: String): Path = {
  val cacheKey = createKeytabCacheKey(userName, label)

  // 检查缓存
  var cachedPath = keytabFileCache.get(cacheKey)
  if (cachedPath != null && Files.exists(cachedPath)) {
    logger.debug(s"Keytab cache hit for user: $userName, label: $label")
    return cachedPath
  }

  // 缓存未命中，创建新文件
  logger.debug(s"Keytab cache miss for user: $userName, label: $label, creating new file...")

  synchronized {
    // 双重检查，避免重复创建
    cachedPath = keytabFileCache.get(cacheKey)
    if (cachedPath != null && Files.exists(cachedPath)) {
      return cachedPath
    }

    // 创建临时文件
    val sourcePath = Paths.get(getLinkisKeytabPath(label), userName + KEYTAB_SUFFIX)
    val encryptedBytes = Files.readAllBytes(sourcePath)
    val decryptedBytes = AESUtils.decrypt(encryptedBytes, AESUtils.PASSWORD)

    val tempFile = Files.createTempFile(userName, KEYTAB_SUFFIX)
    Files.setPosixFilePermissions(tempFile, PosixFilePermissions.fromString("rw-------"))
    Files.write(tempFile, decryptedBytes)

    // 缓存文件路径
    keytabFileCache.put(cacheKey, tempFile)

    logger.info(s"Keytab file cached: $tempFile for user: $userName, label: $label")
    tempFile
  }
}
```

#### 3.1.2 getLinkisUserKeytabFile - 修改方法

**改动点**：
```scala
// 修改前
private def getLinkisUserKeytabFile(userName: String, label: String): String = {
  val path = if (LINKIS_KEYTAB_SWITCH) {
    val byte = Files.readAllBytes(Paths.get(getLinkisKeytabPath(label), userName + KEYTAB_SUFFIX))
    val encryptedContent = AESUtils.decrypt(byte, AESUtils.PASSWORD)
    val tempFile = Files.createTempFile(userName, KEYTAB_SUFFIX)
    Files.setPosixFilePermissions(tempFile, PosixFilePermissions.fromString("rw-------"))
    Files.write(tempFile, encryptedContent)
    tempFile.toString
  } else {
    new File(getKeytabPath(label), userName + KEYTAB_SUFFIX).getPath
  }
  path
}

// 修改后
private def getLinkisUserKeytabFile(userName: String, label: String): String = {
  val path = if (LINKIS_KEYTAB_SWITCH) {
    createOrGetCachedKeytabFile(userName, label).toString
  } else {
    new File(getKeytabPath(label), userName + KEYTAB_SUFFIX).getPath
  }
  path
}
```

### 3.2 缓存清理机制

#### 3.2.1 扩展现有清理逻辑

**位置**：HDFSUtils.scala 第59-95行（现有的fileSystemCache清理定时任务）

**改动方案**：在现有清理任务中增加keytabFileCache清理

```scala
if (HadoopConf.HDFS_ENABLE_CACHE && HadoopConf.HDFS_ENABLE_CACHE_CLOSE) {
  logger.info("HDFS Cache clear enabled ")
  Utils.defaultScheduler.scheduleAtFixedRate(
    new Runnable {
      override def run(): Unit = Utils.tryAndWarn {
        // ===== 现有逻辑：清理FileSystemCache =====
        fileSystemCache
          .values()
          .asScala
          .filter { hdfsFileSystemContainer =>
            hdfsFileSystemContainer.canRemove() && StringUtils.isNotBlank(
              hdfsFileSystemContainer.getUser
            )
          }
          .foreach { hdfsFileSystemContainer =>
            val locker = hdfsFileSystemContainer.getUser + LOCKER_SUFFIX
            locker.intern() synchronized {
              if (
                  hdfsFileSystemContainer.canRemove() && !HadoopConf.HDFS_ENABLE_NOT_CLOSE_USERS
                    .contains(hdfsFileSystemContainer.getUser)
              ) {
                fileSystemCache.remove(
                  hdfsFileSystemContainer.getUser + JOINT + hdfsFileSystemContainer.getLabel
                )
                IOUtils.closeQuietly(hdfsFileSystemContainer.getFileSystem)
                logger.info(
                  s"user${hdfsFileSystemContainer.getUser} to remove hdfsFileSystemContainer"
                )
              }
            }
          }

        // ===== 新增：清理KeytabFileCache =====
        cleanExpiredKeytabFiles()
      }
    },
    3 * 60 * 1000,
    60 * 1000,
    TimeUnit.MILLISECONDS
  )
}
```

#### 3.2.2 cleanExpiredKeytabFiles - 新增方法

**职责**：清理过期的keytab缓存文件

**方法签名**：
```scala
private def cleanExpiredKeytabFiles(): Unit
```

**实现**：
```scala
private def cleanExpiredKeytabFiles(): Unit = {
  if (keytabFileCache.isEmpty) return

  val now = System.currentTimeMillis()
  val idleTime = HadoopConf.HDFS_ENABLE_CACHE_IDLE_TIME

  keytabFileCache
    .keySet()
    .asScala
    .foreach { cacheKey =>
      val locker = cacheKey + "_KEYTAB"
      locker.intern() synchronized {
        try {
          val keytabPath = keytabFileCache.get(cacheKey)
          if (keytabPath != null && Files.exists(keytabPath)) {
            val lastModified = Files.getLastModifiedTime(keytabPath).toMillis
            if (now - lastModified > idleTime) {
              // 删除临时文件
              Files.deleteIfExists(keytabPath)
              keytabFileCache.remove(cacheKey)
              logger.info(s"Cleaned expired keytab file: $keytabPath (key: $cacheKey)")
            }
          }
        } catch {
          case e: Exception =>
            logger.warn(s"Failed to clean keytab cache for key: $cacheKey", e)
        }
      }
    }
}
```

---

## 四、类图与时序图

### 4.1 类图

```
┌─────────────────────────────────────────────────────────────────┐
│                         HDFSUtils (object)                       │
├─────────────────────────────────────────────────────────────────┤
│ - fileSystemCache: Map[String, HDFSFileSystemContainer]          │
│ - keytabFileCache: Map[String, Path]                               │
│ - DEFAULT_CACHE_LABEL: String                                     │
│ - JOINT: String                                                   │
├─────────────────────────────────────────────────────────────────┤
│ + getHDFSFileSystem(user, label): FileSystem                      │
│ + getUserGroupInformation(user, label): UserGroupInformation      │
│ - getLinkisUserKeytabFile(userName, label): String               │
│ - createOrGetCachedKeytabFile(userName, label): Path             │
│ - createKeytabCacheKey(userName, label): String                   │
│ - cleanExpiredKeytabFiles(): Unit                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 时序图 - 缓存命中场景

```
getUserGroupInformation     getLinkisUserKeytabFile  createOrGetCachedKeytabFile
        │                            │                          │
        ├─── 调用 ──────────────────>│                          │
        │                            │                          │
        │               检查 LINKIS_KEYTAB_SWITCH               │
        │                            │                          │
        │                    [ON]                           │
        │                            │                          │
        │                            ├─── 调用 ─────────────>│
        │                            │                          │
        │                            │               生成缓存Key
        │                            │                          │
        │                            │                   检查缓存
        │                            │                          │
        │                            │                   [命中]
        │                            │                          │
        │                            │               返回缓存路径
        │                            │                   │
        │                            │<─── 返回 ────────────────│
        │                            │                          │
        │                            │                   │
        │<─── 返回 ────────────────────│                          │
        │                            │                          │
```

### 4.3 时序图 - 缓存未命中场景

```
getUserGroupInformation     getLinkisUserKeytabFile  createOrGetCachedKeytabFile    AESUtils
        │                            │                          │                      │
        ├─── 调用 ──────────────────>│                          │                      │
        │                            │                          │                      │
        │               检查 LINKIS_KEYTAB_SWITCH               │                      │
        │                            │                          │                      │
        │                    [ON]                           │                      │
        │                            │                          │                      │
        │                            ├─── 调用 ─────────────>│                      │
        │                            │                          │                      │
        │                            │               生成缓存Key                     │
        │                            │                          │                      │
        │                            │                   检查缓存                     │
        │                            │                          │                      │
        │                            │                   [未命中]                     │
        │                            │                          │                      │
        │                            │               读取加密文件                     │
        │                            │                          │                      │
        │                            │               调用解密 ─────────────────────> │
        │                            │                          │                      │
        │                            │               创建临时文件                     │
        │                            │                          │                      │
        │                            │               设置文件权限                     │
        │                            │                          │                      │
        │                            │               写入解密内容                     │
        │                            │                          │                      │
        │                            │               缓存文件路径                     │
        │                            │                          │                      │
        │                            │<─── 返回 ────────────────│                      │
        │                            │                          │                      │
        │<─── 返回 ────────────────────│                          │                      │
        │                            │                          │                      │
```

---

## 五、异常处理设计

### 5.1 异常场景与处理策略

| 场景 | 异常类型 | 处理策略 |
|:----|:--------|:--------|
| 文件读取失败 | IOException | 记录ERROR日志，抛出异常给上层处理 |
| 解密失败 | AESUtils异常 | 记录ERROR日志，抛出异常给上层处理 |
| 临时文件创建失败 | IOException | 记录ERROR日志，抛出异常给上层处理 |
| 文件权限设置失败 | IOException | 记录WARN日志，尝试删除临时文件 |
| 缓存文件不存在 | - | 重新创建（缓存失效场景） |
| 清理任务失败 | Exception | 记录WARN日志，不影响主流程 |

### 5.2 日志设计

| 级别 | 场景 | 日志格式 |
|:----:|:----|:---------|
| DEBUG | 缓存命中 | `Keytab cache hit for user: {userName}, label: {label}` |
| DEBUG | 缓存未命中 | `Keytab cache miss for user: {userName}, label: {label}, creating new file...` |
| INFO | 新缓存创建 | `Keytab file cached: {path} for user: {userName}, label: {label}` |
| INFO | 缓存清理 | `Cleaned expired keytab file: {path} (key: {cacheKey})` |
| WARN | 清理失败 | `Failed to clean keytab cache for key: {cacheKey}, error: {msg}` |
| ERROR | 文件操作失败 | `Failed to read keytab file: {path}, error: {msg}` |

---

## 六、测试设计

### 6.1 单元测试用例

| 用例ID | 测试场景 | 输入 | 预期结果 |
|:------:|:--------|:-----|:---------|
| TC-01 | 首次调用创建缓存 | userName="user1", label=null | 创建临时文件并缓存 |
| TC-02 | 二次调用复用缓存 | userName="user1", label=null | 返回第一次的文件路径 |
| TC-03 | 不同用户不同缓存 | userName="user2", label=null | 返回不同的文件路径 |
| TC-04 | 不同label不同缓存 | userName="user1", label="cluster1" | 返回不同的文件路径 |
| TC-05 | LINKIS_KEYTAB_SWITCH关闭 | 设置开关为false | 返回源文件路径 |
| TC-06 | 并发调用 | 10个线程同用户 | 所有线程返回相同路径 |
| TC-07 | 缓存文件被删除 | 删除缓存文件后调用 | 重新创建临时文件 |

### 6.2 集成测试用例

| 用例ID | 测试场景 | 测试内容 |
|:------:|:--------|:---------|
| IT-01 | 完整调用链 | getUserGroupInformation → getLinkisUserKeytabFile |
| IT-02 | 缓存清理 | 验证过期缓存能被清理 |
| IT-03 | 避免Full GC | 对比修复前后的GC次数 |

---

## 七、配置项设计

### 7.1 复用现有配置

| 配置项 | 说明 | 默认值 |
|:------|:-----|:------|
| `wds.linkis.hadoop.hdfs.cache.enable` | 是否启用缓存清理 | false |
| `linkis.hadoop.hdfs.cache.close.enable` | 是否启用关闭机制 | true |
| `wds.linkis.hadoop.hdfs.cache.idle.time` | 缓存空闲时间（毫秒） | 180000 (3分钟) |
| `linkis.hadoop.hdfs.cache.not.close.users` | 不清理的用户列表 | "hadoop" |

### 7.2 新增配置（可选）

| 配置项 | 说明 | 默认值 | 可选 |
|:------|:-----|:------|:----:|
| `linkis.keytab.cache.enable` | 是否启用keytab缓存 | true | 是 |

---

## 八、实施计划

### 8.1 任务分解

| 任务ID | 任务名称 | 负责人 | 预计工时 |
|:------:|:--------|:------:|:--------:|
| T-01 | 创建缓存数据结构 | - | 0.5h |
| T-02 | 实现createOrGetCachedKeytabFile方法 | - | 1h |
| T-03 | 修改getLinkisUserKeytabFile方法 | - | 0.5h |
| T-04 | 实现cleanExpiredKeytabFiles方法 | - | 0.5h |
| T-05 | 集成到现有清理任务 | - | 0.5h |
| T-06 | 编写单元测试 | - | 1.5h |
| T-07 | 编写集成测试 | - | 1h |
| T-08 | 代码审查 | - | 0.5h |
| T-09 | 性能测试（GC对比） | - | 1h |

### 8.2 验收标准

- [ ] 所有单元测试通过
- [ ] 所有集成测试通过
- [ ] Full GC频率降低80%以上
- [ ] 无新增CheckStyle/Warn
- [ ] 代码审查通过

---

## 九、回滚方案

### 9.1 回滚条件

- Full GC频率未明显降低
- 影响Kerberos认证功能
- 出现新的稳定性问题

### 9.2 回滚步骤

1. 回滚代码修改
2. 重新编译部署
3. 验证原有功能正常
4. 分析问题并重新设计

---

## 十、附录

### 10.1 参考资料

1. HDFSUtils.scala - 现有缓存实现
2. HadoopConf.scala - 配置项定义
3. Apache Hadoop Kerberos认证文档

### 10.2 关键代码片段

见第三章详细设计部分。