# Keytab文件缓存优化 - 需求文档

| 版本 | 日期 | 作者 | 变更说明 |
|:----:|:----:|:----:|:--------|
| 1.0 | 2026-02-11 | DevSyncAgent | 初始版本 |

---

## 一、功能概述

### 1.1 功能名称
Keytab文件缓存优化 - 修复Full GC问题

### 1.2 一句话描述
通过添加keytab文件缓存机制，解决`HDFSUtils.getLinkisUserKeytabFile`方法每次创建临时文件导致的Full GC问题

### 1.3 功能类型
Bug修复 (FIX)

---

## 二、问题分析

### 2.1 问题现象
当启用`LINKIS_KEYTAB_SWITCH`配置后，系统频繁触发Full GC，严重影响系统性能。

### 2.2 问题定位

| 分析维度 | 详情 |
|:--------|------|
| **问题文件** | `linkis-commons/linkis-hadoop-common/src/main/scala/org/apache/linkis/hadoop/common/utils/HDFSUtils.scala` |
| **问题方法** | `getLinkisUserKeytabFile (userName: String, label: String)` |
| **代码行** | 第383-397行 |
| **调用频率** | 高 - 每次`getUserGroupInformation`获取FileSystem时都会调用 |

### 2.3 根本原因

| 根因编号 | 根因描述 | 影响 |
|:--------:|:--------|:-----|
| RC-1 | 每次调用都创建新临时文件 (`Files.createTempFile`) | 大量File对象分配，增加GC压力 |
| RC-2 | 没有缓存机制，同一用户keytab反复读取/解密/写入 | I/O和CPU资源浪费 |
| RC-3 | 临时文件不清理 | 内存泄漏风险 |
| RC-4 | 调用频率高 | 放大上述问题的影响 |

### 2.4 问题代码

```scala
private def getLinkisUserKeytabFile(userName: String, label: String): String = {
  val path = if (LINKIS_KEYTAB_SWITCH) {
    // 读取文件
    val byte = Files.readAllBytes(Paths.get(getLinkisKeytabPath(label), userName + KEYTAB_SUFFIX))
    // 解密内容
    val encryptedContent = AESUtils.decrypt(byte, AESUtils.PASSWORD)
    val tempFile = Files.createTempFile(userName, KEYTAB_SUFFIX)  // 问题核心：每次都创建新临时文件
    Files.setPosixFilePermissions(tempFile, PosixFilePermissions.fromString("rw-------"))
    Files.write(tempFile, encryptedContent)
    tempFile.toString
  } else {
    new File(getKeytabPath(label), userName + KEYTAB_SUFFIX).getPath
  }
  path
}
```

### 2.5 调用点分析

| 调用位置 | 行号 | 调用场景 |
|:--------|:----:|:--------|
| `getUserGroupInformation(userName: String, label: String)` | 276 | 普通用户keytab登录 |
| `getUserGroupInformation(userName: String, label: String)` | 282 | Proxy用户keytab登录 |

---

## 三、解决方案设计

### 3.1 核心方案

添加keytab文件缓存机制，参考现有的`fileSystemCache`模式，实现：
1. 缓存keytab临时文件（以`userName_label`为key）
2. 复用已解密的临时文件
3. 延迟清理临时文件（复用fileSystemCache的清理机制）
4. 线程安全的实现

### 3.2 架构设计

#### 3.2.1 缓存数据结构

```scala
// Keytab文件缓存
private val keytabFileCache: java.util.Map[String, Path] = new ConcurrentHashMap[String, Path]()

// 缓存Key：userName_label
private def createKeytabCacheKey(userName: String, label: String): String = {
  val normalizedLabel = if (label == null) DEFAULT_CACHE_LABEL else label
  userName + JOINT + normalizedLabel
}
```

#### 3.2.2 缓存清理策略

复用现有的`fileSystemCache`清理定时任务：
- 清理条件：文件未被使用超过 `HDFS_ENABLE_CACHE_IDLE_TIME`（3分钟）
- 清理频率：60秒一次
- 清理操作：删除临时文件 + 移除缓存条目

### 3.3 约束条件

| 约束类型 | 要求 |
|:--------|:-----|
| **安全性** | 必须保持临时文件权限为 `rw-------` |
| **兼容性** | 保持现有API接口不变 |
| **线程安全** | 使用`ConcurrentHashMap`保证并发安全 |
| **配置兼容** | 复用现有HadoopConf的缓存配置项 |

---

## 四、功能需求

### 4.1 核心功能 (P0)

| ID | 功能描述 | 验收标准 |
|:--:|:--------|:--------|
| F-01 | 实现keytab文件缓存 | 同一用户首次调用后，后续调用返回已缓存的文件路径 |
| F-02 | 集成到现有清理机制 | 超过空闲时间的临时文件能被自动清理 |
| F-03 | 保持文件权限正确 | 缓存的临时文件权限为`rw-------` |
| F-04 | 线程安全 | 多线程并发调用不会导致问题 |

### 4.2 重要功能 (P1)

| ID | 功能描述 | 验收标准 |
|:--:|:--------|:--------|
| F-05 | 缓存命中率日志 | 定期输出缓存命中率统计日志 |
| F-06 | 异常处理 | 处理缓存读取失败等边界情况 |

### 4.3 辅助功能 (P2)

| ID | 功能描述 | 验收标准 |
|:--:|:--------|:--------|
| F-07 | 监控指标 | 暴露缓存大小、命中率等监控指标 |

---

## 五、非功能需求

### 5.1 性能需求

| 指标 | 目标值 | 测量方法 |
|:----|:------|:--------|
| Full GC频率 | 降低80%以上 | 对比修复前后Full GC次数 |
| 临时文件创建次数 | 减少90%以上 | 统计`createTempFile`调用次数 |
| 方法响应时间 | 降低50%以上 | 对比修复前后调用耗时 |

### 5.2 可靠性需求

| 需求 | 说明 |
|:----|:-----|
| 缓存失效保护 | 缓存失效时，应回退到原有逻辑，不影响业务 |
| 文件完整性 | 确保解密后的文件内容正确 |

### 5.3 可维护性需求

| 需求 | 说明 |
|:----|:-----|
| 代码可读性 | 添加清晰的注释说明缓存逻辑 |
| 日志完善 | 关键操作记录DEBUG级别日志 |

---

## 六、验收标准

### 6.1 功能验收

- [ ] F-01: 同一用户的keytab文件能被正确缓存和复用
- [ ] F-02: 空闲的临时文件能被及时清理
- [ ] F-03: 缓存临时文件权限正确
- [ ] F-04: 并发场景下测试通过

### 6.2 性能验收

- [ ] Full GC频率降低80%以上
- [ ] 临时文件创建次数减少90%以上
- [ ] 方法响应时间降低50%以上

### 6.3 兼容性验收

- [ ] API接口保持不变
- [ ] 现有配置项无需修改
- [ ] LINKIS_KEYTAB_SWITCH关闭时行为不变

---

## 七、风险与预案

| 风险 | 影响 | 概率 | 应对措施 |
|:----|:----|:----:|:--------|
| R-01 | 缓存清理时机不当导致文件被过早删除 | 高 | 低 | 增加文件使用状态跟踪 |
| R-02 | 并发访问导致缓存数据不一致 | 中 | 低 | 使用ConcurrentHashMap保证线程安全 |
| R-03 | 缓存失效导致业务异常 | 高 | 低 | 增加降级逻辑，缓存失效时回退到原有逻辑 |

---

## 八、待确认问题

| 问题ID | 问题描述 | 优先级 | 状态 |
|:------|:--------|:------:|:----:|
| Q-01 | 是否需要新的配置项控制keytab缓存开关？ | P2 | 待确认 |
| Q-02 | 缓存清理是否需要独立的配置项？ | P2 | 待确认 |

---

## 九、参考文档

1. 现有代码：`HDFSUtils.scala` 第44-94行（fileSystemCache实现）
2. 配置文件：`HadoopConf.scala` 缓存相关配置
3. Hadoop Kerberos认证文档