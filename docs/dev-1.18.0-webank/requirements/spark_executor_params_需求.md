# Spark引擎支持设置executor参数 - 需求文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 需求ID | LINKIS-ENHANCE-SPARK-001 |
| 需求名称 | Spark引擎支持设置executor参数 |
| 需求类型 | 功能增强（ENHANCE） |
| 基础模块 | Spark引擎 |
| 当前版本 | dev-1.18.0-webank |
| 创建时间 | 2026-03-12 |
| 文档状态 | 待评审 |

---

## 一、功能概述

### 1.1 功能名称

spark引擎支持设置executor参数

### 1.2 功能描述

为Linkis Spark引擎增加executor端参数设置能力，通过调用`sc.setLocalProperty`方法将Spark运行时参数动态设置到executor端。支持：
- 总开关控制（默认关闭）
- 参数排除配置（逗号分隔）
- 异常容错（失败记录WARNING日志，继续执行）
- 轻量级日志记录（开关状态和参数总数）

### 1.3 一句话描述

为Spark Engine增加executor端参数设置能力，支持开关控制和参数排除，实现Spark运行时参数的动态配置。

---

## 二、功能背景

### 2.1 当前痛点

**当前遇到的问题**：
部分Spark运行参数无法通过常规方式传递到executor端，导致以下场景无法实现：
- 时区配置：无法设置`spark.sql.legacy.timeParserPolicy`来兼容旧版时区解析
- SQL行为调优：无法动态调整Spark SQL的运行时行为
- 兼容性设置：无法为特定场景配置兼容参数

**期望达到的目标**：
提供一种标准化的方式，将Spark运行时参数动态设置到executor端，支持：
- 用户通过简单配置即可启用/禁用参数设置功能
- 通过排除列表灵活控制哪些参数不被设置
- 兼容现有Spark配置机制，不影响已有功能

### 2.2 现有功能

**当前实现**：
- Spark引擎可以通过`sc.getConf`获取所有Spark参数
- 已有参考实现：`getAllWithPrefix("spark.hadoop.")`方式批量获取参数并设置
- 当前位置：executeLine方法中，通过`sc.setJobGroup(jobGroup, _code, true)`设置作业组

**功能定位**：
- 本需求是对现有参数设置能力的增强
- 通过`sc.setLocalProperty`方法将参数设置到executor端
- 集成到executeLine方法，在`sc.setJobGroup`后执行

---

## 三、核心功能

### 3.1 功能优先级

| 优先级 | 功能点 | 说明 |
|--------|--------|------|
| P0 | 参数设置到executor端 | 通过sc.setLocalProperty设置Spark参数 |
| P0 | 功能开关控制 | 支持启用/禁用参数设置功能 |
| P1 | 参数排除配置 | 支持配置排除列表，指定不设置的参数 |

### 3.2 功能详细规格

#### 3.2.1 P0功能：参数设置到executor端

**功能描述**：
在SparkEngineConnExecutor的executeLine方法中，在`sc.setJobGroup`之后，遍历所有Spark参数，通过`sc.setLocalProperty`方法设置到executor端。

**实现方式**：
```scala
sc.getAll
  .foreach { case (key, value) =>
    if (!excludeParams.contains(key)) {
      sc.setLocalProperty(key, value)
    }
  }
```

**触发条件**：
- 功能开关开启（`wds.linkis.spark.executor.params.enabled=true`）
- 每次执行代码时触发（executeLine方法调用时）

#### 3.2.2 P0功能：功能开关控制

**配置项**：
- 配置文件：`linkis-engineconn.properties`
- 配置项：`wds.linkis.spark.executor.params.enabled`
- 类型：Boolean
- 默认值：`false`（默认关闭）

**控制逻辑**：
```scala
if (SparkConfiguration.SPARK_EXECUTOR_PARAMS_ENABLED.getValue) {
  // 执行参数设置
  setSparkExecutorParams(sc)
} else {
  // 不执行任何操作
  logger.info("Spark executor params setting is disabled")
}
```

#### 3.2.3 P1功能：参数排除配置

**配置项**：
- 配置文件：`linkis-engineconn.properties`
- 配置项：`wds.linkis.spark.executor.params.exclude`
- 类型：String（逗号分隔的参数列表）
- 默认值：空字符串（不排除任何参数）

**配置示例**：
```properties
# 排除并行度相关参数
wds.linkis.spark.executor.params.exclude=spark.sql.shuffle.partitions,spark.dynamicAllocation.maxExecutors
```

**实现逻辑**：
```scala
val excludeParams = SparkConfiguration.SPARK_EXECUTOR_PARAMS_EXCLUDE.getValue
  .split(",")
  .map(_.trim)
  .filter(_.nonEmpty)
  .toSet
```

---

## 四、配置设计

### 4.1 新增配置项

| 配置项 | 配置文件 | 类型 | 默认值 | 说明 |
|--------|---------|------|--------|------|
| wds.linkis.spark.executor.params.enabled | linkis-engineconn.properties | Boolean | false | 是否启用executor端参数设置 |
| wds.linkis.spark.executor.params.exclude | linkis-engineconn.properties | String | 空 | 排除的参数列表（逗号分隔） |

### 4.2 配置示例

```properties
# 启用executor端参数设置
wds.linkis.spark.executor.params.enabled=true

# 排除不需要设置的参数
wds.linkis.spark.executor.params.exclude=spark.sql.shuffle.partitions,spark.dynamicAllocation.maxExecutors,spark.executor.instances
```

---

## 五、技术方案

### 5.1 集成位置

**修改文件**：
- `linkis-engineconn-plugins/spark/src/main/scala/org/apache/linkis/engineplugin/spark/executor/SparkEngineConnExecutor.scala`

**集成点**：
在executeLine方法中，`sc.setJobGroup(jobGroup, _code, true)`之后添加参数设置逻辑。

**代码位置**：
```scala
// 现有代码（第203行）
sc.setJobGroup(jobGroup, _code, true)

// 新增代码开始
// 设置executor参数
Utils.tryAndWarn(setSparkexecutorParams(sc))
// 新增代码结束
```

### 5.2 新增配置类

**修改文件**：
- `linkis-engineconn-plugins/spark/src/main/scala/org/apache/linkis/engineplugin/spark/config/SparkConfiguration.scala`

**新增配置**：
```scala
val SPARK_EXECUTOR_PARAMS_ENABLED = CommonVars[Boolean](
  "wds.linkis.spark.executor.params.enabled",
  false,
  "Enable spark executor params setting to executor side（启用Spark executor参数设置）"
)

val SPARK_EXECUTOR_PARAMS_EXCLUDE = CommonVars[String](
  "wds.linkis.spark.executor.params.exclude",
  "",
  "Exclude params from setting to executor side, split by comma（排除的executor参数，逗号分隔）"
)
```

### 5.3 参数设置方法

**新增方法**（在SparkEngineConnExecutor.scala中）：
```scala
/**
 * Set spark params to executor side via setLocalProperty
 * @param sc SparkContext
 */
private def setSparkDriverParams(sc: SparkContext): Unit = {
  if (!SparkConfiguration.SPARK_EXECUTOR_PARAMS_ENABLED.getValue) {
    return
  }

  val excludeParams = SparkConfiguration.SPARK_EXECUTOR_PARAMS_EXCLUDE.getValue
    .split(",")
    .map(_.trim)
    .filter(_.nonEmpty)
    .toSet

  var totalParams = 0
  var skippedParams = 0
  var successCount = 0
  var failCount = 0

  sc.getAll.foreach { case (key, value) =>
    totalParams += 1
    if (excludeParams.contains(key)) {
      skippedParams += 1
    } else {
      Utils.tryCatch {
        sc.setLocalProperty(key, value)
        successCount += 1
      } {
        case e: Exception =>
          logger.warn(s"Failed to set spark param: $key, error: ${e.getMessage}", e)
          failCount += 1
      }
    }
  }

  logger.info(s"Spark executor params setting completed - total: $totalParams, " +
    s"skipped: $skippedParams, success: $successCount, failed: $failCount")
}
```

### 5.4 异常处理策略

**策略**：跳过该参数，继续设置其他参数，仅记录WARNING日志

**实现**：
- 使用`Utils.tryCatch`捕获异常
- 记录WARNING级别日志，包含参数key和异常信息
- 继续处理下一个参数

**优势**：
- 避免单个参数设置失败影响整体功能
- 通过日志定位问题参数
- 不影响Spark作业正常执行

### 5.5 日志记录

**记录内容**：
- 开关状态：启用/禁用
- 设置的参数总数
- 成功数量、失败数量、跳过数量

**不记录**：
- 详细参数列表（避免敏感信息泄露）
- 参数值（避免敏感信息泄露）

**日志示例**：
```
INFO  - Spark executor params setting completed - total: 45, skipped: 3, success: 42, failed: 0
WARN  - Failed to set spark param: spark.invalid.param, error: Invalid parameter name
```

---

## 六、非功能需求

### 6.1 性能要求

- 参数设置操作应在100ms内完成
- 不影响executeLine方法的整体性能

### 6.2 兼容性要求

- 功能默认关闭，不影响现有Spark配置
- 向后兼容：关闭时与现有行为完全一致
- 配置项使用现有的`linkis-engineconn.properties`配置文件

### 6.3 安全性要求

- 日志不记录敏感参数值
- 支持排除敏感参数（如密码、token等）

### 6.4 可维护性要求

- 代码遵循Linkis项目编码规范
- 日志使用SLF4J框架
- 配置项添加详细注释

---

## 七、验收标准

| ID | 验收项 | 验证方式 | 优先级 |
|-----|-------|---------|--------|
| AC-001 | 功能开关关闭时，不执行任何参数设置 | 验证日志无设置记录 | P0 |
| AC-002 | 功能开关开启时，正确设置所有Spark参数到executor端 | 验证日志记录参数总数 | P0 |
| AC-003 | 排除配置中的参数不会被设置 | 验证排除参数不在日志中 | P1 |
| AC-004 | 参数设置失败时，记录WARNING日志并继续 | 模拟参数设置失败场景 | P0 |
| AC-005 | 在sc.setJobGroup后执行参数设置 | 代码位置验证 | P0 |
| AC-006 | 配置项位于linkis-engineconn.properties | 配置文件验证 | P0 |

---

## 八、测试场景

### 8.1 功能测试

| 场景 | 配置 | 预期结果 |
|------|------|---------|
| 开关关闭 | wds.linkis.spark.executor.params.enabled=false | 不执行参数设置，日志记录禁用状态 |
| 开关启用 | wds.linkis.spark.executor.params.enabled=true | 执行参数设置，日志记录参数总数 |
| 排除参数 | 配置exclude参数 | 排除的参数不会被设置 |
| 参数设置失败 | 模拟无效参数 | 记录WARNING日志，继续执行 |

### 8.2 兼容性测试

| 场景 | 预期结果 |
|------|---------|
| 关闭开关 | 与现有Spark行为完全一致 |
| 不配置开关（使用默认值） | 功能关闭，不影响现有功能 |

### 8.3 性能测试

| 场景 | 预期结果 |
|------|---------|
| 100个Spark参数 | 设置时间 < 100ms |

---

## 九、风险与依赖

### 9.1 风险

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| 某些参数设置可能导致Spark不稳定 | 高 | 默认关闭，异常捕获+WARNING日志 |
| 排除配置填写错误 | 中 | 提供配置示例和注释 |

### 9.2 依赖

- Spark 2.x 或 3.x
- Linkis Spark引擎
- 现有`linkis-engineconn.properties`配置文件

---

## 十、实施计划

| 阶段 | 内容 | 预计时间 |
|------|------|---------|
| 需求评审 | 需求文档评审确认 | 1天 |
| 设计评审 | 技术方案评审确认 | 1天 |
| 开发实现 | 代码实现 | 2天 |
| 单元测试 | 单元测试用例编写 | 1天 |
| 集成测试 | 功能测试和兼容性测试 | 1天 |
| 代码评审 | Code Review | 1天 |

---

## 附录

### 附录A：参考代码位置

- SparkEngineConnExecutor: `linkis-engineconn-plugins/spark/src/main/scala/org/apache/linkis/engineplugin/spark/executor/SparkEngineConnExecutor.scala`
- SparkConfiguration: `linkis-engineconn-plugins/spark/src/main/scala/org/apache/linkis/engineplugin/spark/config/SparkConfiguration.scala`

### 附录B：相关链接

- Spark setLocalProperty API: https://spark.apache.org/docs/latest/api/scala/org/apache/spark/SparkContext.html#setLocalProperty(key:String,value:String):Unit
