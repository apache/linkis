# Hive YARN Tag 用户名增强需求文档

## 文档信息

| 项目 | 内容 |
|------|------|
| **需求名称** | Hive传递给YARN的tag加上用户名 |
| **需求类型** | 功能增强（ENHANCE） |
| **关联模块** | linkis-engineconn-plugins/hive |
| **创建日期** | 2026-03-27 |
| **版本** | 1.19.0 |
| **负责人** | 待定 |

---

## 一、功能概述

### 1.1 功能名称
Hive引擎YARN任务标签用户名增强

### 1.2 一句话描述
在Hive引擎向YARN提交任务时，将任务ID和用户名同时传递给YARN的`mapreduce.job.tags`参数，以便在YARN界面上快速识别任务来源。

### 1.3 功能背景

**当前痛点**：
在日常Hive任务调度和监控中，运维和开发人员需要能够快速定位YARN集群上运行的任务来源。

**现有实现**：
当前Linkis在向YARN提交Hive任务时，已经设置了`mapreduce.job.tags`参数来标记任务ID，格式为：
```
LINKIS_{jobId}
```

**存在问题**：
- 缺少用户名信息
- 在YARN界面上难以区分是哪个用户提交的任务
- 多个用户同时提交任务时，无法快速定位任务归属

### 1.4 期望价值

**业务价值**：
1. **提升运维效率**：运维人员可以在YARN界面上快速识别任务所属用户
2. **便于故障排查**：当某个用户的任务出现问题时，可以快速定位并处理
3. **增强任务可追溯性**：通过YARN标签即可知道任务提交者，无需查看Linkis日志

**量化目标**：
- 运维人员定位任务来源的时间从 **5分钟** 降低到 **10秒**
- 任务识别准确率达到 **100%**

---

## 二、功能需求

### 2.1 核心功能P0

#### 2.1.1 用户名标签添加

**功能描述**：
在Hive引擎向YARN提交任务时，在现有的`mapreduce.job.tags`参数中增加用户名信息。

**实现位置**：
- `HiveEngineConnExecutor.scala`（第165-176行）
- `HiveEngineConcurrentConnExecutor.scala`（第144-148行）

**标签格式**：
```
LINKIS_{jobId},USER_{username}
```

**示例**：
- 原格式：`LINKIS_123456789`
- 新格式：`LINKIS_123456789,USER_zhangsan`

#### 2.1.2 用户名获取方式

**数据来源**：
```scala
engineExecutorContext.getProperties.get("execUser")
```

**备用方案**（如果execUser为空）：
```scala
ugi.getUserName
```

### 2.2 功能约束

#### 2.2.1 向后兼容性

**要求**：
- 完全向后兼容现有功能
- 不影响现有任务标签格式（LINKIS_{jobId}）
- 当无法获取用户名时，保持原有标签格式

**兼容性保证**：
1. 仅在成功获取到用户名时才添加USER标签
2. 用户名为空或null时，保持原有格式
3. 标签格式使用逗号分隔，YARN可以正确解析

#### 2.2.2 特殊字符处理

**处理规则**：
- 用户名中的特殊字符 **保持原样**，不进行转义或过滤
- 示例：用户名为`user@example.com`，标签为`USER_user@example.com`

**原因**：
- YARN标签本身支持特殊字符
- 保持用户名原始格式，便于识别

#### 2.2.3 域用户处理

**场景说明**：
- 当前环境 **不存在域用户**（已确认）
- 未来如果存在域用户，标签格式为`USER_domain\username`，YARN标签支持反斜杠

### 2.3 边界条件处理

| 场景 | 处理方式 | 标签示例 |
|------|----------|----------|
| 正常用户名 | 添加USER标签 | `LINKIS_123,USER_zhangsan` |
| 用户名为空字符串 | 保持原格式 | `LINKIS_123` |
| 用户名为null | 保持原格式 | `LINKIS_123` |
| execUser不存在 | 尝试使用ugi.getUserName | - |
| jobId为空 | 不设置标签 | - |
| 特殊字符用户名 | 保持原样 | `USER_user@example.com` |

---

## 三、技术方案

### 3.1 代码修改点

#### 3.1.1 HiveEngineConnExecutor.scala

**文件路径**：
```
linkis-engineconn-plugins/hive/src/main/scala/org/apache/linkis/engineplugin/hive/executor/HiveEngineConnExecutor.scala
```

**修改位置**：第165-176行

**现有代码**：
```scala
val jobId = JobUtils.getJobIdFromMap(engineExecutorContext.getProperties)

if (StringUtils.isNotBlank(jobId)) {
  val jobTags = JobUtils.getJobSourceTagsFromObjectMap(engineExecutorContext.getProperties)
  val tags = if (StringUtils.isAsciiPrintable(jobTags)) {
    s"LINKIS_$jobId,$jobTags"
  } else {
    s"LINKIS_$jobId"
  }
  LOG.info(s"set mapreduce.job.tags=$tags")
  hiveConf.set("mapreduce.job.tags", tags)
}
```

**修改后代码**：
```scala
val jobId = JobUtils.getJobIdFromMap(engineExecutorContext.getProperties)

if (StringUtils.isNotBlank(jobId)) {
  val jobTags = JobUtils.getJobSourceTagsFromObjectMap(engineExecutorContext.getProperties)

  // 获取用户名
  val execUser = if (engineExecutorContext.getProperties != null) {
    engineExecutorContext.getProperties.get("execUser") match {
      case user: String => user
      case _ => null
    }
  } else null

  // 构建标签，包含用户名信息
  val tags = if (StringUtils.isAsciiPrintable(jobTags)) {
    if (StringUtils.isNotBlank(execUser)) {
      s"LINKIS_$jobId,$jobTags,USER_$execUser"
    } else {
      s"LINKIS_$jobId,$jobTags"
    }
  } else {
    if (StringUtils.isNotBlank(execUser)) {
      s"LINKIS_$jobId,USER_$execUser"
    } else {
      s"LINKIS_$jobId"
    }
  }

  LOG.info(s"set mapreduce.job.tags=$tags")
  hiveConf.set("mapreduce.job.tags", tags)
}
```

#### 3.1.2 HiveEngineConcurrentConnExecutor.scala

**文件路径**：
```
linkis-engineconn-plugins/hive/src/main/scala/org/apache/linkis/engineplugin/hive/executor/HiveEngineConcurrentConnExecutor.scala
```

**修改位置**：第144-148行

**现有代码**：
```scala
val jobId = JobUtils.getJobIdFromMap(engineExecutorContext.getProperties)
if (StringUtils.isNotBlank(jobId)) {
  LOG.info(s"set mapreduce.job.tags=LINKIS_$jobId")
  hiveConf.set("mapreduce.job.tags", s"LINKIS_$jobId")
}
```

**修改后代码**：
```scala
val jobId = JobUtils.getJobIdFromMap(engineExecutorContext.getProperties)
if (StringUtils.isNotBlank(jobId)) {
  // 获取用户名
  val execUser = if (engineExecutorContext.getProperties != null) {
    engineExecutorContext.getProperties.get("execUser") match {
      case user: String => user
      case _ => null
    }
  } else null

  // 构建标签，包含用户名信息
  val tags = if (StringUtils.isNotBlank(execUser)) {
    s"LINKIS_$jobId,USER_$execUser"
  } else {
    s"LINKIS_$jobId"
  }

  LOG.info(s"set mapreduce.job.tags=$tags")
  hiveConf.set("mapreduce.job.tags", tags)
}
```

### 3.2 日志增强

**新增日志**：
```scala
LOG.info(s"Set mapreduce.job.tags with user info: jobId=$jobId, user=$execUser, tags=$tags")
```

**日志级别**：INFO
**日志位置**：设置标签后立即输出

### 3.3 依赖检查

**所需依赖**：
- `org.apache.commons.lang3.StringUtils`：已存在
- `org.apache.linkis.governance.common.utils.JobUtils`：已存在
- `engineExecutorContext.getProperties`：已存在

**无需新增依赖**

---

## 四、非功能性需求

### 4.1 性能要求

- **性能影响**：几乎无影响（仅增加一次属性获取和字符串拼接）
- **标签长度**：用户名通常不超过32字符，对YARN标签长度影响可忽略

### 4.2 可靠性要求

- **容错机制**：用户名获取失败时，保持原有标签格式
- **不影响任务执行**：标签设置失败不应影响任务正常运行

### 4.3 可维护性要求

- **代码可读性**：添加清晰的注释说明用户名标签的作用
- **日志完整性**：记录用户名信息，便于排查问题

---

## 五、测试验收标准

### 5.1 功能测试用例

| 用例编号 | 测试场景 | 输入条件 | 预期结果 |
|---------|---------|----------|----------|
| TC001 | 正常用户名 | execUser="zhangsan", jobId="123" | `LINKIS_123,USER_zhangsan` |
| TC002 | 用户名为空字符串 | execUser="", jobId="123" | `LINKIS_123` |
| TC003 | 用户名为null | execUser=null, jobId="123" | `LINKIS_123` |
| TC004 | jobId为空 | jobId="" | 不设置标签 |
| TC005 | 特殊字符用户名 | execUser="user@example.com", jobId="123" | `LINKIS_123,USER_user@example.com` |
| TC006 | 结合jobTags使用 | jobId="123", jobTags="EMR", execUser="zhangsan" | `LINKIS_123,EMR,USER_zhangsan` |

### 5.2 验证方式

**日志验证**：
1. 查看Linkis日志，确认输出`set mapreduce.job.tags=LINKIS_xxx,USER_xxx`
2. 确认用户名信息正确

**YARN界面验证**：
1. 登录YARN ResourceManager Web UI
2. 查看正在运行的Hive任务
3. 确认任务标签包含用户名信息

### 5.3 回归测试

**测试范围**：
- Hive引擎正常执行任务
- 现有任务标签格式不受影响
- 多用户并发场景下标签正确性

---

## 六、发布计划

### 6.1 影响范围

**影响模块**：
- `linkis-engineconn-plugins/hive`

**不影响模块**：
- 其他引擎插件（Spark、Flink等）
- Linkis公共服务模块

### 6.2 上线步骤

1. **代码开发**：完成代码修改
2. **单元测试**：完成单元测试
3. **集成测试**：在测试环境验证
4. **灰度发布**：选择部分用户灰度
5. **全量发布**：全量发布到生产环境

### 6.3 回滚方案

**回滚条件**：
- 发现任务执行异常
- YARN标签解析失败

**回滚方式**：
- 恢复原始代码
- 重新编译部署

---

## 七、参考资料

### 7.1 相关代码文件

- `HiveEngineConnExecutor.scala`：Hive引擎执行器
- `HiveEngineConcurrentConnExecutor.scala`：Hive并发执行器
- `JobUtils.scala`：任务工具类

### 7.2 相关文档

- YARN官方文档：https://hadoop.apache.org/docs/stable/hadoop-yarn/hadoop-yarn-site/YARN.html
- Hive配置文档：https://cwiki.apache.org/confluence/display/Hive/AdminManual+Configuration

---

## 八、附录

### 8.1 需求澄清记录

| 问题 | 回答 |
|-----|------|
| 1. 用户标识来源 | `engineExecutorContext.getProperties.get("execUser")` |
| 2. 域用户处理 | 不存在 |
| 3. 特殊字符 | 保持原样 |
| 4. 标签格式 | 前缀格式（USER_zhangsan） |
| 5. 标签顺序 | 固定顺序（LINKIS_{jobId},USER_{username}） |
| 6. 向后兼容 | 完全兼容 |
| 7. 存储 | 不存储 |

### 8.2 变更历史

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|----------|------|
| 1.0 | 2026-03-27 | 初始版本 | - |

---

**文档结束**
