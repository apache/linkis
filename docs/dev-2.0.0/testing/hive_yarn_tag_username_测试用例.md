# Hive YARN Tag 用户名增强 - 测试用例文档

## 文档信息

| 项目 | 内容 |
|------|------|
| **测试名称** | Hive传递给YARN的tag加上用户名 |
| **测试类型** | 功能增强测试（ENHANCE） |
| **关联需求** | [hive_yarn_tag_username_需求.md](../requirements/hive_yarn_tag_username_需求.md) |
| **关联设计** | [hive_yarn_tag_username_设计.md](../design/hive_yarn_tag_username_设计.md) |
| **创建日期** | 2026-03-27 |
| **版本** | 1.0 |
| **测试负责人** | 待定 |

---

## 一、测试概述

### 1.1 测试目标

验证Hive引擎在向YARN提交任务时，能够正确地将任务ID和用户名同时传递给YARN的`mapreduce.job.tags`参数，确保：

1. **核心功能验证**：标签格式正确，符合`LINKIS_{jobId},USER_{username}`规范
2. **向后兼容性**：现有功能不受影响，用户名获取失败时保持原有格式
3. **边界条件处理**：正确处理空值、null值、特殊字符等边界场景
4. **并发安全性**：多用户并发场景下标签正确性

### 1.2 测试范围

| 测试类型 | 覆盖内容 | 优先级 |
|---------|---------|:------:|
| **功能测试** | 标签格式、用户名获取、jobTags组合 | P0 |
| **边界测试** | 空值、null、特殊字符处理 | P0 |
| **兼容性测试** | 向后兼容性、现有功能影响 | P0 |
| **并发测试** | 多用户并发场景 | P1 |
| **回归测试** | 现有Hive任务执行 | P0 |

### 1.3 测试策略

**测试方法**：
- **单元测试**：验证标签构建逻辑
- **集成测试**：验证Hive任务提交到YARN的完整流程
- **日志验证**：验证日志输出正确性
- **YARN界面验证**：验证YARN Web UI中标签显示

**测试环境**：
- Hadoop 3.3.4
- Hive 2.3.3
- Linkis 1.19.0
- YARN ResourceManager正常运行

---

## 二、功能测试用例

### 2.1 核心功能测试

#### TC001：正常用户名 - 无jobTags场景

**来源**：需求文档 - 五、测试验收标准

**测试类型**：功能测试

**前置条件**：
- Hive引擎服务已启动
- YARN ResourceManager正常运行
- execUser="zhangsan"，jobId="123456789"

**测试步骤**：
1. 提交Hive任务，execUser设置为"zhangsan"
2. 设置jobId为"123456789"
3. 不设置jobTags
4. 执行任务
5. 查看Linkis日志输出
6. 登录YARN Web UI查看任务标签

**预期结果**：
- 日志输出：`set mapreduce.job.tags=LINKIS_123456789,USER_zhangsan`
- YARN Web UI显示标签：`LINKIS_123456789,USER_zhangsan`
- Hive任务执行成功

**测试数据**：
```scala
execUser = "zhangsan"
jobId = "123456789"
jobTags = null
```

**优先级**：P0
**测试类型**：功能测试

---

#### TC002：正常用户名 - 有jobTags场景

**来源**：需求文档 - 五、测试验收标准

**测试类型**：功能测试

**前置条件**：
- Hive引擎服务已启动
- YARN ResourceManager正常运行
- execUser="zhangsan"，jobId="123456789"，jobTags="EMR"

**测试步骤**：
1. 提交Hive任务，execUser设置为"zhangsan"
2. 设置jobId为"123456789"
3. 设置jobTags为"EMR"
4. 执行任务
5. 查看Linkis日志输出
6. 登录YARN Web UI查看任务标签

**预期结果**：
- 日志输出：`set mapreduce.job.tags=LINKIS_123456789,EMR,USER_zhangsan`
- YARN Web UI显示标签：`LINKIS_123456789,EMR,USER_zhangsan`
- Hive任务执行成功

**测试数据**：
```scala
execUser = "zhangsan"
jobId = "123456789"
jobTags = "EMR"
```

**优先级**：P0
**测试类型**：功能测试

---

### 2.2 边界条件测试

#### TC003：用户名为空字符串

**来源**：需求文档 - 五、测试验收标准

**测试类型**：边界测试

**前置条件**：
- Hive引擎服务已启动
- execUser=""（空字符串），jobId="123456789"

**测试步骤**：
1. 提交Hive任务，execUser设置为空字符串
2. 设置jobId为"123456789"
3. 执行任务
4. 查看Linkis日志输出

**预期结果**：
- 日志输出：`set mapreduce.job.tags=LINKIS_123456789`（保持原格式）
- 不包含USER标签
- Hive任务执行成功

**测试数据**：
```scala
execUser = ""
jobId = "123456789"
```

**优先级**：P0
**测试类型**：边界测试

---

#### TC004：用户名为null

**来源**：需求文档 - 五、测试验收标准

**测试类型**：边界测试

**前置条件**：
- Hive引擎服务已启动
- execUser=null，jobId="123456789"

**测试步骤**：
1. 提交Hive任务，execUser设置为null
2. 设置jobId为"123456789"
3. 执行任务
4. 查看Linkis日志输出

**预期结果**：
- 日志输出：`set mapreduce.job.tags=LINKIS_123456789`（保持原格式）
- 不包含USER标签
- Hive任务执行成功

**测试数据**：
```scala
execUser = null
jobId = "123456789"
```

**优先级**：P0
**测试类型**：边界测试

---

#### TC005：jobId为空

**来源**：需求文档 - 五、测试验收标准

**测试类型**：边界测试

**前置条件**：
- Hive引擎服务已启动
- jobId=""（空字符串）

**测试步骤**：
1. 提交Hive任务，jobId为空字符串
2. 执行任务
3. 查看Linkis日志输出

**预期结果**：
- 不设置mapreduce.job.tags参数
- 日志中无"set mapreduce.job.tags"输出
- Hive任务正常执行

**测试数据**：
```scala
jobId = ""
```

**优先级**：P1
**测试类型**：边界测试

---

#### TC006：特殊字符用户名

**来源**：需求文档 - 五、测试验收标准

**测试类型**：边界测试

**前置条件**：
- Hive引擎服务已启动
- execUser="user@example.com"，jobId="123456789"

**测试步骤**：
1. 提交Hive任务，execUser设置为"user@example.com"
2. 设置jobId为"123456789"
3. 执行任务
4. 查看Linkis日志输出
5. 登录YARN Web UI查看任务标签

**预期结果**：
- 日志输出：`set mapreduce.job.tags=LINKIS_123456789,USER_user@example.com`
- 特殊字符@和.保持原样，不转义
- YARN Web UI正确显示标签
- Hive任务执行成功

**测试数据**：
```scala
execUser = "user@example.com"
jobId = "123456789"
```

**优先级**：P1
**测试类型**：边界测试

---

#### TC007：用户名包含下划线

**来源**：需求文档 - 设计延伸

**测试类型**：边界测试

**前置条件**：
- Hive引擎服务已启动
- execUser="user_name"，jobId="123456789"

**测试步骤**：
1. 提交Hive任务，execUser设置为"user_name"
2. 设置jobId为"123456789"
3. 执行任务
4. 查看Linkis日志输出

**预期结果**：
- 日志输出：`set mapreduce.job.tags=LINKIS_123456789,USER_user_name`
- 用户名中的下划线保持原样
- Hive任务执行成功

**测试数据**：
```scala
execUser = "user_name"
jobId = "123456789"
```

**优先级**：P2
**测试类型**：边界测试

---

#### TC008：execUser不存在于properties

**来源**：需求文档 - 五、测试验收标准

**测试类型**：边界测试

**前置条件**：
- Hive引擎服务已启动
- properties中不包含"execUser"键
- jobId="123456789"

**测试步骤**：
1. 提交Hive任务，properties中不设置execUser
2. 设置jobId为"123456789"
3. 执行任务
4. 查看Linkis日志输出

**预期结果**：
- 日志输出：`set mapreduce.job.tags=LINKIS_123456789`（保持原格式）
- 不包含USER标签
- Hive任务执行成功

**测试数据**：
```scala
execUser = null // properties中不存在此键
jobId = "123456789"
```

**优先级**：P0
**测试类型**：边界测试

---

#### TC020：YARN标签长度超限

**来源**：测试用例文档Review建议

**测试类型**：边界测试

**前置条件**：
- Hive引擎服务已启动
- YARN ResourceManager正常运行

**测试步骤**：
1. 提交Hive任务，execUser设置为200字符的超长字符串
2. 设置jobId为50字符的字符串
3. 执行任务
4. 查看Linkis日志输出
5. 登录YARN Web UI查看任务标签

**预期结果**：
- 标签总长度不超过YARN限制（256字符）
- 如果超长，应有警告日志
- YARN Web UI正确显示标签
- Hive任务正常执行

**测试数据**：
```scala
execUser = "a" * 200  // 200字符
jobId = "b" * 50       // 50字符
// 预期标签长度: 7(LINKIS_) + 50 + 1(,) + 5(USER_) + 200 = 263字符（超限）
```

**优先级**：P2
**测试类型**：边界测试

---

#### TC021：超长用户名处理

**来源**：测试用例文档Review建议

**测试类型**：边界测试

**前置条件**：
- Hive引擎服务已启动
- YARN ResourceManager正常运行

**测试步骤**：
1. 提交Hive任务，execUser设置为100字符的字符串
2. 设置jobId为正常的"123456789"
3. 执行任务
4. 查看Linkis日志输出
5. 登录YARN Web UI查看任务标签

**预期结果**：
- 日志输出正常
- YARN Web UI正确显示标签
- 无标签截断或解析错误
- Hive任务执行成功

**测试数据**：
```scala
execUser = "very_long_username_" + "x" * 80  // 约100字符
jobId = "123456789"
// 预期标签长度: 7(LINKIS_) + 9 + 1(,) + 5(USER_) + 100 = 122字符（正常）
```

**优先级**：P2
**测试类型**：边界测试

---

### 2.3 兼容性测试

#### TC009：向后兼容性 - 无execUser场景

**来源**：需求文档 - 二、功能需求

**测试类型**：兼容性测试

**前置条件**：
- 使用未增强的Hive引擎（或execUser为null）
- YARN ResourceManager正常运行

**测试步骤**：
1. 提交Hive任务，execUser为null
2. 验证任务标签格式与增强前一致
3. 验证Hive任务执行结果

**预期结果**：
- 标签格式与增强前完全一致
- 任务执行结果与增强前一致
- 无任何错误或异常

**优先级**：P0
**测试类型**：兼容性测试

---

#### TC010：向后兼容性 - 有jobTags无execUser场景

**来源**：需求文档 - 二、功能需求

**测试类型**：兼容性测试

**前置条件**：
- Hive引擎服务已启动
- execUser=null，jobTags="EMR"，jobId="123456789"

**测试步骤**：
1. 提交Hive任务，设置jobTags为"EMR"
2. execUser为null
3. 执行任务
4. 查看Linkis日志输出

**预期结果**：
- 日志输出：`set mapreduce.job.tags=LINKIS_123456789,EMR`
- 标签格式与增强前完全一致
- Hive任务执行成功

**测试数据**：
```scala
execUser = null
jobId = "123456789"
jobTags = "EMR"
```

**优先级**：P0
**测试类型**：兼容性测试

---

#### TC011：现有任务标签格式不受影响

**来源**：需求文档 - 二、功能需求

**测试类型**：回归测试

**前置条件**：
- Hive引擎服务已启动
- 存在历史Hive任务记录

**测试步骤**：
1. 查询历史Hive任务的标签格式
2. 提交新的Hive任务
3. 对比新旧任务的标签格式

**预期结果**：
- 历史任务标签格式不受影响
- 新任务标签格式符合预期
- 无任务执行异常

**优先级**：P0
**测试类型**：回归测试

---

### 2.4 并发测试

#### TC012：多用户并发场景

**来源**：需求文档 - 二、功能需求

**测试类型**：并发测试

**前置条件**：
- Hive引擎服务已启动
- 准备3个不同的用户：zhangsan、lisi、wangwu

**测试步骤**：
1. 同时提交3个Hive任务，分别使用不同的execUser
2. 任务1：execUser="zhangsan"，jobId="001"
3. 任务2：execUser="lisi"，jobId="002"
4. 任务3：execUser="wangwu"，jobId="003"
5. 查看各任务的日志输出
6. 登录YARN Web UI查看各任务标签

**预期结果**：
- 任务1标签：`LINKIS_001,USER_zhangsan`
- 任务2标签：`LINKIS_002,USER_lisi`
- 任务3标签：`LINKIS_003,USER_wangwu`
- 各任务标签互不干扰
- 所有任务执行成功

**测试数据**：
```scala
任务1: execUser="zhangsan", jobId="001"
任务2: execUser="lisi", jobId="002"
任务3: execUser="wangwu", jobId="003"
```

**优先级**：P1
**测试类型**：并发测试

---

### 2.5 性能测试

#### TC013：标签构建性能测试

**来源**：需求文档 - 四、非功能性需求

**测试类型**：性能测试

**前置条件**：
- Hive引擎服务已启动

**测试步骤**：
1. 提交1000个Hive任务
2. 记录每个任务的标签构建时间
3. 统计平均构建时间

**预期结果**：
- 标签构建时间 < 1ms
- 对任务执行时间无显著影响
- 无性能下降

**优先级**：P1
**测试类型**：性能测试

---

### 2.6 日志验证测试

#### TC014：日志输出完整性验证

**来源**：需求文档 - 三、技术方案

**测试类型**：功能测试

**前置条件**：
- Hive引擎服务已启动
- 日志级别设置为INFO

**测试步骤**：
1. 提交Hive任务，execUser="testuser"
2. 查看Linkis日志文件
3. 搜索"set mapreduce.job.tags"关键词

**预期结果**：
- 日志中包含：`set mapreduce.job.tags=LINKIS_xxx,USER_testuser`
- 日志级别为INFO
- 日志内容清晰可读

**优先级**：P0
**测试类型**：功能测试

---

## 三、YARN界面验证测试

### 3.1 YARN Web UI标签显示测试

#### TC015：YARN Web UI标签显示验证

**来源**：需求文档 - 五、测试验收标准

**测试类型**：集成测试

**前置条件**：
- YARN ResourceManager Web UI可访问
- Hive引擎服务已启动

**测试步骤**：
1. 提交Hive任务，execUser="testuser"
2. 登录YARN ResourceManager Web UI
3. 查找对应的Hive任务
4. 查看任务的标签信息

**预期结果**：
- YARN Web UI显示标签：`LINKIS_xxx,USER_testuser`
- 标签格式正确，显示清晰
- 可以通过标签快速识别任务来源用户

**优先级**：P0
**测试类型**：集成测试

---

#### TC016：YARN标签搜索功能验证

**来源**：需求文档 - 一、功能概述

**测试类型**：集成测试

**前置条件**：
- YARN ResourceManager Web UI可访问
- 已提交多个Hive任务

**测试步骤**：
1. 在YARN Web UI中搜索"USER_testuser"
2. 验证搜索结果

**预期结果**：
- 可以通过用户名标签快速定位任务
- 搜索结果准确
- 运维效率提升

**优先级**：P1
**测试类型**：集成测试

---

## 四、单元测试

### 4.1 标签构建逻辑测试

#### TC017：HiveEngineConnExecutor标签构建 - 正常场景

**来源**：代码变更 - HiveEngineConnExecutor.scala

**测试类型**：单元测试

**测试目标**：验证HiveEngineConnExecutor的标签构建逻辑

**测试方法**：
```scala
test("should build correct tags with execUser and without jobTags") {
  // Given
  val properties = new java.util.HashMap[String, Object]()
  properties.put("execUser", "testuser")
  properties.put("jobId", "123456789")

  // When
  val tags = buildTags(properties)

  // Then
  assert(tags == "LINKIS_123456789,USER_testuser")
}

test("should build correct tags with execUser and jobTags") {
  // Given
  val properties = new java.util.HashMap[String, Object]()
  properties.put("execUser", "testuser")
  properties.put("jobId", "123456789")
  properties.put("jobTags", "EMR")

  // When
  val tags = buildTags(properties)

  // Then
  assert(tags == "LINKIS_123456789,EMR,USER_testuser")
}
```

**优先级**：P0
**测试类型**：单元测试

---

#### TC018：HiveEngineConnExecutor标签构建 - 边界场景

**来源**：代码变更 - HiveEngineConnExecutor.scala

**测试类型**：单元测试

**测试目标**：验证边界条件下的标签构建逻辑

**测试方法**：
```scala
test("should handle empty execUser") {
  // Given
  val properties = new java.util.HashMap[String, Object]()
  properties.put("execUser", "")
  properties.put("jobId", "123456789")

  // When
  val tags = buildTags(properties)

  // Then
  assert(tags == "LINKIS_123456789")
}

test("should handle null execUser") {
  // Given
  val properties = new java.util.HashMap[String, Object]()
  properties.put("jobId", "123456789")

  // When
  val tags = buildTags(properties)

  // Then
  assert(tags == "LINKIS_123456789")
}

test("should handle special characters in username") {
  // Given
  val properties = new java.util.HashMap[String, Object]()
  properties.put("execUser", "user@example.com")
  properties.put("jobId", "123456789")

  // When
  val tags = buildTags(properties)

  // Then
  assert(tags == "LINKIS_123456789,USER_user@example.com")
}
```

**优先级**：P0
**测试类型**：单元测试

---

#### TC019：HiveEngineConcurrentConnExecutor标签构建 - 正常场景

**来源**：代码变更 - HiveEngineConcurrentConnExecutor.scala

**测试类型**：单元测试

**测试目标**：验证HiveEngineConcurrentConnExecutor的标签构建逻辑

**测试方法**：
```scala
test("should build correct tags with execUser") {
  // Given
  val properties = new java.util.HashMap[String, Object]()
  properties.put("execUser", "testuser")
  properties.put("jobId", "123456789")

  // When
  val tags = buildTagsForConcurrent(properties)

  // Then
  assert(tags == "LINKIS_123456789,USER_testuser")
}

test("should handle null execUser") {
  // Given
  val properties = new java.util.HashMap[String, Object]()
  properties.put("jobId", "123456789")

  // When
  val tags = buildTagsForConcurrent(properties)

  // Then
  assert(tags == "LINKIS_123456789")
}
```

**优先级**：P0
**测试类型**：单元测试

---

## 五、测试数据管理

### 5.1 测试数据准备

| 测试场景 | execUser | jobId | jobTags | 预期标签 |
|---------|----------|-------|---------|----------|
| TC001 | "zhangsan" | "123456789" | null | `LINKIS_123456789,USER_zhangsan` |
| TC002 | "zhangsan" | "123456789" | "EMR" | `LINKIS_123456789,EMR,USER_zhangsan` |
| TC003 | "" | "123456789" | null | `LINKIS_123456789` |
| TC004 | null | "123456789" | null | `LINKIS_123456789` |
| TC005 | "testuser" | "" | null | 不设置标签 |
| TC006 | "user@example.com" | "123456789" | null | `LINKIS_123456789,USER_user@example.com` |
| TC010 | null | "123456789" | "EMR" | `LINKIS_123456789,EMR` |

### 5.2 测试环境配置

**Hadoop配置**：
```xml
<property>
  <name>yarn.resourcemanager.hostname</name>
  <value>localhost</value>
</property>
```

**Hive配置**：
```xml
<property>
  <name>hive.execution.engine</name>
  <value>mr</value>
</property>
```

---

## 六、测试执行计划

### 6.1 测试执行顺序

```
第1轮：单元测试（TC017-TC019）
  ↓
第2轮：功能测试（TC001-TC002）
  ↓
第3轮：边界测试（TC003-TC008）
  ↓
第4轮：兼容性测试（TC009-TC011）
  ↓
第5轮：并发测试（TC012）
  ↓
第6轮：性能测试（TC013）
  ↓
第7轮：YARN界面验证（TC015-TC016）
  ↓
第8轮：日志验证（TC014）
```

### 6.2 测试通过标准

| 测试类型 | 通过标准 |
|---------|---------|
| **单元测试** | 所有测试用例通过，代码覆盖率 ≥ 80% |
| **功能测试** | 所有P0测试用例通过 |
| **边界测试** | 所有P0测试用例通过 |
| **兼容性测试** | 所有P0测试用例通过 |
| **并发测试** | 所有任务标签正确，无混乱 |
| **性能测试** | 标签构建时间 < 1ms |
| **集成测试** | YARN界面标签显示正确 |

---

## 七、缺陷管理

### 7.1 缺陷严重级别定义

| 级别 | 定义 | 示例 |
|-----|------|------|
| **P0-致命** | 功能无法实现，影响核心业务 | 标签格式错误，任务无法提交 |
| **P1-严重** | 主要功能受影响，但有绕过方案 | 用户名为null时抛出异常 |
| **P2-一般** | 次要功能受影响 | 特殊字符处理不当 |
| **P3-轻微** | 文档、日志等小问题 | 日志格式不清晰 |

### 7.2 缺陷报告模板

```
缺陷编号：BUG-XXX
缺陷标题：[简要描述]
严重级别：P0/P1/P2/P3
发现阶段：单元测试/功能测试/集成测试
复现步骤：
1.
2.
3.
预期结果：
实际结果：
环境信息：
附件：
```

---

## 八、测试报告

### 8.1 测试统计

| 统计项 | 数量 | 占比 |
|-------|-----|------|
| **测试用例总数** | 21 | 100% |
| **P0用例** | 12 | 57% |
| **P1用例** | 5 | 24% |
| **P2用例** | 4 | 19% |
| **通过用例** | - | - |
| **失败用例** | - | - |
| **阻塞用例** | - | - |

### 8.2 测试覆盖率

| 覆盖类型 | 覆盖率 | 目标 | 状态 |
|---------|-------|------|------|
| **需求覆盖率** | 100% | 100% | ✅ |
| **功能覆盖率** | 100% | 100% | ✅ |
| **代码覆盖率** | - | ≥80% | ⏳ |
| **场景覆盖率** | 100% | 100% | ✅ |

---

## 九、风险评估

### 9.1 测试风险

| 风险 | 影响 | 概率 | 缓解措施 |
|-----|------|------|---------|
| YARN环境不可用 | 高 | 低 | 提前检查YARN状态 |
| 测试数据准备不足 | 中 | 低 | 使用Mock数据 |
| 并发测试不稳定 | 中 | 中 | 增加重试机制 |
| 性能测试环境差异 | 低 | 中 | 使用专用测试环境 |

### 9.2 质量风险

| 风险 | 影响 | 概率 | 缓解措施 |
|-----|------|------|---------|
| 特殊字符处理不当 | 高 | 低 | 增加特殊字符测试 |
| 并发场景标签混乱 | 高 | 低 | 增加并发测试 |
| 向后兼容性问题 | 高 | 低 | 增加兼容性测试 |

---

## 十、验收标准

### 10.1 功能验收标准

- [ ] TC001-TC002：正常用户名场景测试通过
- [ ] TC003-TC008：边界条件测试通过
- [ ] TC009-TC011：兼容性测试通过
- [ ] TC012：并发测试通过
- [ ] TC013：性能测试通过
- [ ] TC015-TC016：YARN界面验证通过
- [ ] TC017-TC019：单元测试通过
- [ ] TC020-TC021：边界扩展测试通过（可选）

### 10.2 质量验收标准

- [ ] 所有P0测试用例通过
- [ ] 代码覆盖率 ≥ 80%
- [ ] 无P0、P1级别缺陷
- [ ] 性能指标达标（标签构建时间 < 1ms）
- [ ] 向后兼容性验证通过

---

## 十一、附录

### 11.1 相关文档

- [需求文档](../requirements/hive_yarn_tag_username_需求.md)
- [设计文档](../design/hive_yarn_tag_username_设计.md)
- [Feature文件](../features/hive_yarn_tag_username.feature)

### 11.2 测试环境信息

| 组件 | 版本 |
|-----|------|
| Hadoop | 3.3.4 |
| Hive | 2.3.3 |
| Linkis | 1.19.0 |
| Java | 1.8 |
| Scala | 2.11.12 / 2.12.17 |

### 11.3 测试命令参考

```bash
# 编译Hive引擎
mvn clean package -pl linkis-engineconn-plugins/hive

# 运行单元测试
mvn test -pl linkis-engineconn-plugins/hive

# 查看Linkis日志
tail -f /path/to/linkis/logs/linkis-hive-engine.log | grep "mapreduce.job.tags"

# 提交Hive测试任务
hive -e "SELECT count(*) FROM test_table;"
```

---

**文档结束**
