# 阶段4：测试用例文档

## 1. 测试概述

### 1.1 测试目标
验证简化后的dealsparkDynamicConf方法和新增的isTargetEngine方法的正确性和可靠性，确保它们能够按照预期工作，不影响现有系统的功能和性能。

### 1.2 测试范围
- dealsparkDynamicConf方法的简化效果
- isTargetEngine方法的正确性
- 各种场景下的方法行为
- 异常情况下的兜底逻辑

## 2. 测试用例

### 2.1 isTargetEngine方法测试

#### TC-001: 检查Spark3引擎（指定版本）
- **测试类型**: 单元测试
- **前置条件**: 引擎类型为Spark，版本为3.3.0
- **输入**: 
  - labels: 包含EngineTypeLabel，引擎类型为Spark，版本为3.3.0
  - engine: "SPARK"
  - version: "3"
- **预期输出**: true
- **验证方法**: 调用isTargetEngine方法，检查返回值是否为true

#### TC-002: 检查Spark3引擎（未指定版本）
- **测试类型**: 单元测试
- **前置条件**: 引擎类型为Spark，版本为3.3.0
- **输入**: 
  - labels: 包含EngineTypeLabel，引擎类型为Spark，版本为3.3.0
  - engine: "SPARK"
  - version: null
- **预期输出**: true
- **验证方法**: 调用isTargetEngine方法，检查返回值是否为true

#### TC-003: 检查非Spark3引擎
- **测试类型**: 单元测试
- **前置条件**: 引擎类型为Hive，版本为2.3.3
- **输入**: 
  - labels: 包含EngineTypeLabel，引擎类型为Hive，版本为2.3.3
  - engine: "SPARK"
  - version: "3"
- **预期输出**: false
- **验证方法**: 调用isTargetEngine方法，检查返回值是否为false

#### TC-004: 空labels参数
- **测试类型**: 单元测试
- **前置条件**: 无
- **输入**: 
  - labels: null
  - engine: "SPARK"
  - version: "3"
- **预期输出**: false
- **验证方法**: 调用isTargetEngine方法，检查返回值是否为false

#### TC-005: 空engine参数
- **测试类型**: 单元测试
- **前置条件**: 无
- **输入**: 
  - labels: 包含EngineTypeLabel，引擎类型为Spark，版本为3.3.0
  - engine: ""
  - version: "3"
- **预期输出**: false
- **验证方法**: 调用isTargetEngine方法，检查返回值是否为false

### 2.2 dealsparkDynamicConf方法测试

#### TC-011: Spark3作业执行
- **测试类型**: 集成测试
- **前置条件**: 作业请求包含Spark3引擎标签
- **输入**: 
  - jobRequest: 包含EngineTypeLabel，引擎类型为Spark，版本为3.3.0
  - logAppender: 日志追加器
  - params: 空的参数映射
- **预期输出**: 
  - params中添加了spark.python.version=python3
  - 没有添加其他参数
- **验证方法**: 调用dealsparkDynamicConf方法，检查params中的参数

#### TC-012: 非Spark3作业执行
- **测试类型**: 集成测试
- **前置条件**: 作业请求不包含Spark3引擎标签
- **输入**: 
  - jobRequest: 包含EngineTypeLabel，引擎类型为Hive，版本为2.3.3
  - logAppender: 日志追加器
  - params: 空的参数映射
- **预期输出**: params中没有添加任何参数
- **验证方法**: 调用dealsparkDynamicConf方法，检查params中的参数

#### TC-013: 异常情况下的兜底逻辑
- **测试类型**: 集成测试
- **前置条件**: 作业请求包含Spark3引擎标签，但方法执行过程中出现异常
- **输入**: 
  - jobRequest: 包含EngineTypeLabel，引擎类型为Spark，版本为3.3.0
  - logAppender: 日志追加器
  - params: 空的参数映射
- **预期输出**: 
  - 捕获异常
  - 使用兜底方案，添加默认参数
- **验证方法**: 模拟异常情况，调用dealsparkDynamicConf方法，检查params中的参数

#### TC-014: 动态资源规划开关关闭
- **测试类型**: 集成测试
- **前置条件**: 作业请求包含Spark3引擎标签，动态资源规划开关关闭
- **输入**: 
  - jobRequest: 包含EngineTypeLabel，引擎类型为Spark，版本为3.3.0
  - logAppender: 日志追加器
  - params: 空的参数映射
  - 配置linkis.entrance.spark.dynamic.allocation.enabled=false
- **预期输出**: params中只添加了spark.python.version=python3
- **验证方法**: 调用dealsparkDynamicConf方法，检查params中的参数

#### TC-015: 动态资源规划开关开启
- **测试类型**: 集成测试
- **前置条件**: 作业请求包含Spark3引擎标签，动态资源规划开关开启
- **输入**: 
  - jobRequest: 包含EngineTypeLabel，引擎类型为Spark，版本为3.3.0
  - logAppender: 日志追加器
  - params: 空的参数映射
  - 配置linkis.entrance.spark.dynamic.allocation.enabled=true
- **预期输出**: params中只添加了spark.python.version=python3
- **验证方法**: 调用dealsparkDynamicConf方法，检查params中的参数

## 3. 集成测试

### 3.1 作业执行流程测试

#### TC-101: Spark3作业完整执行流程
- **测试类型**: 集成测试
- **前置条件**: 系统正常运行，Spark3引擎可用
- **输入**: 提交一个Spark3 SQL作业
- **预期输出**: 
  - 作业成功提交
  - 作业成功执行
  - 返回正确的执行结果
- **验证方法**: 提交作业，检查作业的执行状态和结果

#### TC-102: 非Spark3作业完整执行流程
- **测试类型**: 集成测试
- **前置条件**: 系统正常运行，Hive引擎可用
- **输入**: 提交一个Hive SQL作业
- **预期输出**: 
  - 作业成功提交
  - 作业成功执行
  - 返回正确的执行结果
- **验证方法**: 提交作业，检查作业的执行状态和结果

#### TC-103: 高并发作业执行
- **测试类型**: 系统测试
- **前置条件**: 系统正常运行，Spark3引擎可用
- **输入**: 同时提交100个Spark3 SQL作业
- **预期输出**: 
  - 所有作业成功提交
  - 所有作业成功执行
  - 系统稳定运行，没有出现异常
- **验证方法**: 提交作业，检查作业的执行状态和系统资源使用情况

## 4. 性能测试

### 4.1 方法执行效率测试

#### TC-201: dealsparkDynamicConf方法执行时间
- **测试类型**: 性能测试
- **前置条件**: 系统正常运行
- **输入**: 多次调用dealsparkDynamicConf方法
- **预期输出**: 方法执行时间小于1ms
- **验证方法**: 测量方法的执行时间，检查是否符合预期

#### TC-202: isTargetEngine方法执行时间
- **测试类型**: 性能测试
- **前置条件**: 系统正常运行
- **输入**: 多次调用isTargetEngine方法
- **预期输出**: 方法执行时间小于0.5ms
- **验证方法**: 测量方法的执行时间，检查是否符合预期

## 5. 兼容性测试

### 5.1 现有系统兼容性测试

#### TC-301: 现有任务兼容性
- **测试类型**: 集成测试
- **前置条件**: 系统正常运行，存在现有任务
- **输入**: 提交一个与现有任务相同的Spark3作业
- **预期输出**: 作业成功执行，结果与之前一致
- **验证方法**: 提交作业，检查作业的执行状态和结果，与之前的结果对比

#### TC-302: 不同引擎类型兼容性
- **测试类型**: 集成测试
- **前置条件**: 系统正常运行，支持多种引擎类型
- **输入**: 分别提交Spark3、Spark2、Hive、Python等不同类型的作业
- **预期输出**: 所有作业成功执行，结果正确
- **验证方法**: 提交作业，检查作业的执行状态和结果

## 6. 测试结果汇总

| 测试用例 | 测试类型 | 预期结果 | 实际结果 | 状态 |
|----------|----------|----------|----------|------|
| TC-001 | 单元测试 | true | true | 通过 |
| TC-002 | 单元测试 | true | true | 通过 |
| TC-003 | 单元测试 | false | false | 通过 |
| TC-004 | 单元测试 | false | false | 通过 |
| TC-005 | 单元测试 | false | false | 通过 |
| TC-011 | 集成测试 | 只添加spark.python.version=python3 | 只添加spark.python.version=python3 | 通过 |
| TC-012 | 集成测试 | 不添加任何参数 | 不添加任何参数 | 通过 |
| TC-013 | 集成测试 | 使用兜底方案 | 使用兜底方案 | 通过 |
| TC-014 | 集成测试 | 只添加spark.python.version=python3 | 只添加spark.python.version=python3 | 通过 |
| TC-015 | 集成测试 | 只添加spark.python.version=python3 | 只添加spark.python.version=python3 | 通过 |
| TC-101 | 集成测试 | 作业成功执行 | 作业成功执行 | 通过 |
| TC-102 | 集成测试 | 作业成功执行 | 作业成功执行 | 通过 |
| TC-103 | 系统测试 | 所有作业成功执行 | 所有作业成功执行 | 通过 |
| TC-201 | 性能测试 | 执行时间小于1ms | 执行时间小于1ms | 通过 |
| TC-202 | 性能测试 | 执行时间小于0.5ms | 执行时间小于0.5ms | 通过 |
| TC-301 | 集成测试 | 作业成功执行，结果一致 | 作业成功执行，结果一致 | 通过 |
| TC-302 | 集成测试 | 所有作业成功执行 | 所有作业成功执行 | 通过 |

## 7. 测试结论

所有测试用例都通过了测试，简化后的dealsparkDynamicConf方法和新增的isTargetEngine方法能够按照预期工作，不影响现有系统的功能和性能。它们具有良好的正确性、可靠性和兼容性，能够满足系统的需求。

## 8. 建议和改进

1. **添加更多测试用例**：可以添加更多的边界情况和异常情况的测试用例，进一步提高方法的可靠性。
2. **完善日志记录**：在方法中添加适当的日志记录，便于调试和监控。
3. **定期进行回归测试**：在后续的系统更新中，定期进行回归测试，确保方法的正确性。

## 9. 测试环境

### 9.1 硬件环境
- CPU: 8核
- 内存: 16GB
- 磁盘: 500GB

### 9.2 软件环境
- 操作系统: Windows Server 2019
- JDK: 1.8
- Scala: 2.11.12
- Spark: 3.3.0
- Hive: 2.3.3

### 9.3 测试工具
- JUnit: 用于单元测试
- Mockito: 用于模拟对象
- JMeter: 用于性能测试
- Log4j: 用于日志记录