# Spark引擎支持设置executor参数 - 测试用例文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 需求ID | LINKIS-ENHANCE-SPARK-001 |
| 测试版本 | v1.0 |
| 需求类型 | 功能增强（ENHANCE） |
| 基础模块 | Spark引擎 |
| 当前版本 | dev-1.18.0-webank |
| 创建时间 | 2026-03-12 |
| 文档状态 | 待评审 |

---

# 📋 测试概览

## 测试范围

本项目为Spark引擎增加executor端参数设置能力，测试范围包括：
- 功能开关控制（启用/禁用）
- 参数设置到executor端
- 参数排除配置
- 异常处理机制
- 日志记录验证

## 测试环境

| 项目 | 内容 |
|------|------|
| 操作系统 | Linux |
| Spark版本 | 2.x / 3.x |
| Linkis版本 | dev-1.18.0-webank |
| 测试框架 | JUnit 5 |
| 模拟框架 | Mockito |

---

# 🔧 单元测试

## UT-001: 配置项默认值验证

**测试目标**: 验证新增配置项的默认值

**测试用例**:
```scala
@Test
def testSparkDriverParamsEnabledDefault(): Unit = {
  val enabled = SparkConfiguration.SPARK_DRIVER_PARAMS_ENABLED.getValue
  assertFalse(enabled, "SPARK_DRIVER_PARAMS_ENABLED should default to false")
}

@Test
def testSparkDriverParamsExcludeDefault(): Unit = {
  val exclude = SparkConfiguration.SPARK_DRIVER_PARAMS_EXCLUDE.getValue
  assertTrue(exclude.isEmpty, "SPARK_DRIVER_PARAMS_EXCLUDE should default to empty string")
}
```

**预期结果**: SPARK_DRIVER_PARAMS_ENABLED为false，SPARK_DRIVER_PARAMS_EXCLUDE为空字符串

---

## UT-002: 排除参数配置解析验证

**测试目标**: 验证排除配置的解析逻辑

**测试用例**:
```scala
@Test
def testSparkDriverParamsExcludeSplit(): Unit = {
  val testExclude = "spark.sql.shuffle.partitions,spark.dynamicAllocation.maxExecutors"
  val excludeParams = testExclude.split(",").map(_.trim).filter(_.nonEmpty).toSet
  assertEquals(2, excludeParams.size, "Should parse 2 excluded params")
}

@Test
def testSparkDriverParamsExcludeWithSpaces(): Unit = {
  val testExclude = "spark.executor.instances , spark.executor.memory"
  val excludeParams = testExclude.split(",").map(_.trim).filter(_.nonEmpty).toSet
  assertEquals(2, excludeParams.size, "Should parse 2 excluded params with spaces")
}
```

**预期结果**: 正确解析逗号分隔的排除参数，支持处理前后空格

---

# 🧪 功能测试

## FT-001: 功能开关关闭时，不执行任何参数设置

**前置条件**:
- 配置项: `wds.linkis.spark.executor.params.enabled=false`

**测试步骤**:
1. 启动Spark引擎
2. 执行Spark代码
3. 查看日志

**预期结果**:
- 日志包含: "Spark executor params setting is disabled"
- 不包含参数设置相关的日志

**验收标准**: AC-001

---

## FT-002: 功能开关开启时，正确设置所有Spark参数到executor端

**前置条件**:
- 配置项: `wds.linkis.spark.executor.params.enabled=true`
- 配置项: `wds.linkis.spark.executor.params.exclude=`

**测试步骤**:
1. 启动Spark引擎
2. 执行Spark代码: `spark.range(10).count()`
3. 查看日志

**预期结果**:
- 日志包含: "Spark executor params setting completed"
- 日志包含参数统计: total、success、skipped、failed
- success > 0

**验收标准**: AC-002

---

## FT-003: 配置排除参数时，排除的参数不会被设置

**前置条件**:
- 配置项: `wds.linkis.spark.executor.params.enabled=true`
- 配置项: `wds.linkis.spark.executor.params.exclude=spark.sql.shuffle.partitions,spark.dynamicAllocation.maxExecutors`

**测试步骤**:
1. 启动Spark引擎
2. 执行Spark代码
3. 验证排除参数未被设置

**预期结果**:
- 日志包含: "Spark executor params setting completed"
- 日志显示skipped参数数量 = 2

**验收标准**: AC-003

---

## FT-004: 参数设置失败时，记录WARNING日志并继续执行

**前置条件**:
- 配置项: `wds.linkis.spark.executor.params.enabled=true`

**测试步骤**:
1. 模拟无效参数（通过Mock SparkContext）
2. 触发参数设置
3. 检查日志

**预期结果**:
- 日志包含WARNING级别日志
- 日志包含: "Failed to set spark param"
- 参数设置流程继续执行，不中断

**验收标准**: AC-004

---

## FT-005: 验证参数设置在sc.setJobGroup后执行

**前置条件**:
- 配置项: `wds.linkis.spark.executor.params.enabled=true`

**测试步骤**:
1. 启动Spark引擎
2. 执行Spark代码
3. 查看代码执行日志顺序

**预期结果**:
- 日志顺序: "Set jobGroup to" → "Spark executor params setting completed" → 代码执行日志

**验收标准**: AC-005

---

## FT-006: 配置项位于linkis-engineconn.properties

**前置条件**:
- linkis-engineconn.properties文件存在

**测试步骤**:
1. 读取linkis-engineconn.properties
2. 搜索新增配置项

**预期结果**:
- 文件包含: `wds.linkis.spark.executor.params.enabled`
- 文件包含: `wds.linkis.spark.executor.params.exclude`

**验收标准**: AC-006

---

# 🔄 回归测试

## RT-001: 现有Spark功能不受影响

**测试目标**: 验证新增功能不影响现有Spark作业的执行

**测试步骤**:
1. 不配置executor参数功能开关（使用默认false）
2. 执行以下Spark任务:
   - SQL查询
   - DataFrame操作
   - RDD操作

**预期结果**: 所有任务正常执行，功能与变更前一致

---

## RT-002: 时区配置场景验证

**测试目标**: 验证常见使用场景（时区配置）正常工作

**前置条件**:
- 配置项: `wds.linkis.spark.executor.params.enabled=true`
- 配置项: `spark.sql.legacy.timeParserPolicy=LEGACY`（通过SparkConf传递）

**测试步骤**:
1. 执行时间解析代码
2. 验证时区配置生效

**预期结果**: 时区配置正确应用于executor端

---

## RT-003: 并发执行验证

**测试目标**: 验证多个任务并发执行时参数设置的正确性

**测试步骤**:
1. 提交多个Spark作业
2. 每个作业都应正确设置executor参数

**预期结果**: 所有作业的executor参数正确设置

---

# 📊 性能测试

## PT-001: 参数设置性能验证

**测试目标**: 验证参数设置操作的性能

**测试步骤**:
1. 配置100个Spark参数
2. 开启executor参数设置
3. 执行代码并测量参数设置时间

**预期结果**: 参数设置完成时间 < 100ms

**性能指标**: < 100ms

---

## PT-002: 功能开关关闭时的性能影响

**测试目标**: 验证功能开关关闭时无性能影响

**测试步骤**:
1. 配置项: `wds.linkis.spark.executor.params.enabled=false`
2. 执行Spark作业并测量执行时间

**预期结果**: 与未开启功能时的性能无差异

---

# 🔐 安全性测试

## ST-001: 日志不记录敏感参数值

**测试目标**: 验证日志不泄露敏感信息

**测试步骤**:
1. 配置包含敏感信息的Spark参数（如密码）
2. 开启executor参数设置
3. 检查日志

**预期结果**:
- 日志仅记录参数总数
- 日志不记录具体参数key和value

---

## ST-002: 敏感参数排除验证

**测试目标**: 验证可通过排除配置排除敏感参数

**测试步骤**:
1. 配置参数排除列表包含敏感参数名
2. 验证敏感参数未被设置

**预期结果**: 敏感参数在排除列表中，不会被设置到executor端

---

# 🎯 测试数据

## 测试配置示例

```properties
# linkis-engineconn.properties

# 场景1: 功能关闭
wds.linkis.spark.executor.params.enabled=false

# 场景2: 功能开启
wds.linkis.spark.executor.params.enabled=true

# 场景3: 排除参数
wds.linkis.spark.executor.params.enabled=true
wds.linkis.spark.executor.params.exclude=spark.sql.shuffle.partitions,spark.dynamicAllocation.maxExecutors

# 场景4: 完整配置
wds.linkis.spark.executor.params.enabled=true
wds.linkis.spark.executor.params.exclude=spark.executor.instances,spark.executor.memory
```

---

# 📋 测试执行清单

| 用例ID | 用例名称 | 状态 | 执行者 | 执行时间 |
|--------|---------|------|--------|---------|
| UT-001 | 配置项默认值验证 | ⏸️ 待执行 | | |
| UT-002 | 排除参数配置解析验证 | ⏸️ 待执行 | | |
| FT-001 | 功能开关关闭时不执行参数设置 | ⏸️ 待执行 | | |
| FT-002 | 功能开关开启时正确设置参数 | ⏸️ 待执行 | | |
| FT-003 | 配置排除参数时不设置 | ⏸️ 待执行 | | |
| FT-004 | 参数设置失败时记录WARNING | ⏸️ 待执行 | | |
| FT-005 | 参数设置在setJobGroup后执行 | ⏸️ 待执行 | | |
| FT-006 | 配置项位置验证 | ⏸️ 待执行 | | |
| RT-001 | 现有Spark功能不受影响 | ⏸️ 待执行 | | |
| RT-002 | 时区配置场景验证 | ⏸️ 待执行 | | |
| RT-003 | 并发执行验证 | ⏸️ 待执行 | | |
| PT-001 | 参数设置性能验证 | ⏸️ 待执行 | | |
| PT-002 | 开关关闭时的性能验证 | ⏸️ 待执行 | | |
| ST-001 | 日志不记录敏感参数值 | ⏸️ 待执行 | | |
| ST-002 | 敏感参数排除验证 | ⏸️ 待执行 | | |

---

# 🎯 验收标准对照表

| 验收标准 | 相关用例 | 状态 |
|---------|---------|------|
| AC-001: 功能开关关闭时不执行参数设置 | FT-001 | ⏸️ 待验证 |
| AC-002: 功能开关开启时正确设置参数 | FT-002 | ⏸️ 待验证 |
| AC-003: 排除配置中的参数不被设置 | FT-003 | ⏸️ 待验证 |
| AC-004: 参数设置失败记录WARNING | FT-004 | ⏸️ 待验证 |
| AC-005: 在sc.setJobGroup后执行 | FT-005 | ⏸️ 待验证 |
| AC-006: 配置项位置正确 | FT-006 | ⏸️ 待验证 |

---
