# 阶段2：技术设计方案

## 1. 设计概述

### 1.1 设计目标
在现有dealsparkDynamicConf方法的基础上进行简化，只保留spark.python.version的强制设置，移除所有其他参数覆盖，信任Spark启动时会自己读取管理台的参数，同时保留异常处理的兜底逻辑，提高代码可读性和可维护性。

### 1.2 设计原则
- **最小改动**: 只修改必要的代码，不影响现有功能
- **向后兼容**: 兼容现有系统的功能和API
- **清晰明了**: 代码逻辑清晰，易于理解和维护
- **安全可靠**: 保留异常处理的兜底逻辑，确保系统稳定性

## 2. 架构设计

### 2.1 组件关系图

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   作业请求       │────>│  EntranceUtils   │────>│   Spark引擎       │
│                 │     │                 │     │                 │
│  Spark3引擎     │     │ dealsparkDynamicConf() │                 │
│                 │     │  ↓              │     │                 │
└─────────────────┘     │ 检查引擎类型     │     └─────────────────┘
                        │  ↓              │
                        │ 强制设置python版本│
                        │  ↓              │
                        │ 处理异常情况     │
                        └─────────────────┘
```

### 2.2 处理流程

```
┌─────────────────────────────────────────────────────────────────┐
│                       dealsparkDynamicConf处理流程               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────┐    ┌───────────────┐    ┌────────────────────┐    │
│  │ 接收请求  │───>│  获取引擎标签     │───>│  检查是否为Spark3    │    │
│  └──────────┘    └───────────────┘    └─────────┬──────────┘    │
│                                                  │               │
│                                    ┌─────────────┴─────────────┐ │
│                                    │ 是Spark3引擎?          │ │
│                                    └─────────────┬─────────────┘ │
│                                  是 │             │ 否          │
│                                    ▼             ▼               │
│                          ┌─────────────┐  ┌─────────────────┐   │
│                          │ 创建属性映射 │  │   直接返回       │   │
│                          └─────────────┘  └─────────────────┘   │
│                                    │                           │
│                                    ▼                           │
│                          ┌─────────────┐                       │
│                          │ 强制设置python版本│                   │
│                          └─────────────┘                       │
│                                    │                           │
│                                    ▼                           │
│                          ┌─────────────┐                       │
│                          │ 添加到启动参数 │                   │
│                          └─────────────┘                       │
│                                    │                           │
│                                    ▼                           │
│                          ┌─────────────┐                       │
│                          │    返回结果    │                   │
│                          └─────────────┘                       │
│                                                                  │
│  ┌──────────┐    ┌───────────────┐    ┌────────────────────┐    │
│  │ 异常捕获  │───>│  创建属性映射     │───>│  检查动态资源规划开关  │    │
│  └──────────┘    └───────────────┘    └─────────┬──────────┘    │
│                                                  │               │
│                                    ┌─────────────┴─────────────┐ │
│                                    │ 开关是否开启?          │ │
│                                    └─────────────┬─────────────┘ │
│                                  是 │             │ 否          │
│                                    ▼             ▼               │
│                          ┌─────────────┐  ┌─────────────────┐   │
│                          │ 设置默认参数 │  │   直接返回       │   │
│                          └─────────────┘  └─────────────────┘   │
│                                    │                           │
│                                    ▼                           │
│                          ┌─────────────┐                       │
│                          │ 添加到启动参数 │                   │
│                          └─────────────┘                       │
│                                    │                           │
│                                    ▼                           │
│                          ┌─────────────┐                       │
│                          │    返回结果    │                   │
│                          └─────────────┘                       │
└─────────────────────────────────────────────────────────────────┘
```

## 3. 详细设计

### 3.1 方法简化设计

#### 3.1.1 dealsparkDynamicConf方法
**功能**：处理Spark3动态资源规划配置，只强制设置spark.python.version
**参数**：
- jobRequest：作业请求对象
- logAppender：日志追加器
- params：参数映射
**返回值**：无
**实现逻辑**：
1. 检查是否为Spark3引擎
2. 如果是Spark3引擎，强制设置spark.python.version为python3
3. 将设置添加到启动参数中
4. 异常情况下，使用兜底方案，统一由后台配置

#### 3.1.2 isTargetEngine方法
**功能**：检查给定的labels是否对应目标引擎类型和可选版本
**参数**：
- labels：标签列表
- engine：目标引擎类型
- version：可选的目标版本
**返回值**：布尔值，表示是否匹配
**实现逻辑**：
1. 检查labels是否为null或engine是否为空
2. 获取EngineTypeLabel
3. 检查引擎类型是否匹配
4. 如果指定了版本，检查版本是否匹配
5. 返回匹配结果

## 4. 关键代码修改

### 4.1 EntranceUtils.scala修改

#### 4.1.1 简化dealsparkDynamicConf方法

**修改前**：
```scala
def dealsparkDynamicConf(
    jobRequest: JobRequest,
    logAppender: lang.StringBuilder,
    params: util.Map[String, AnyRef]
): Unit = {
  // 复杂的参数处理逻辑
  // 包含大量参数覆盖
  // 包含动态资源规划开关处理
}
```

**修改后**：
```scala
def dealsparkDynamicConf(
    jobRequest: JobRequest,
    logAppender: lang.StringBuilder,
    params: util.Map[String, AnyRef]
): Unit = {
  try {
    val isSpark3 = LabelUtil.isTargetEngine(jobRequest.getLabels, EngineType.SPARK.toString, LabelCommonConfig.SPARK3_ENGINE_VERSION.getValue)
    if (isSpark3) {
      val properties = new util.HashMap[String, AnyRef]()
      properties.put("spark.python.version", "python3")
      TaskUtils.addStartupMap(params, properties)
    }
  } catch {
    case e: Exception =>
      // 异常处理的兜底逻辑
  }
}
```

### 4.2 LabelUtil.scala修改

#### 4.2.1 新增isTargetEngine方法

```scala
def isTargetEngine(labels: util.List[Label[_]], engine: String, version: String = null): Boolean = {
  if (null == labels || StringUtils.isBlank(engine)) return false
  val engineTypeLabel = getEngineTypeLabel(labels)
  if (null != engineTypeLabel) {
    val isEngineMatch = engineTypeLabel.getEngineType.equals(engine)
    val isVersionMatch = StringUtils.isBlank(version) || engineTypeLabel.getVersion.contains(version)
    isEngineMatch && isVersionMatch
  } else {
    false
  }
}
```

## 5. 配置示例

### 5.1 linkis.properties

```properties
# Spark3 Python版本配置
spark.python.version=python3

# Spark动态资源规划配置
linkis.entrance.spark.dynamic.allocation.enabled=true
linkis.entrance.spark.executor.cores=2
linkis.entrance.spark.executor.memory=4G
```

## 6. 兼容性说明

| 场景 | 行为 |
|------|------|
| Spark3作业 | 只设置spark.python.version为python3，其他参数由Spark自己读取 |
| 非Spark3作业 | 不执行任何参数设置，直接返回 |
| 异常情况 | 使用兜底方案，统一由后台配置 |
| 现有任务 | 兼容现有任务的执行，不影响现有功能 |

## 7. 测试设计

### 7.1 单元测试
1. 测试isTargetEngine方法的正确性
2. 测试dealsparkDynamicConf方法对Spark3引擎的处理
3. 测试dealsparkDynamicConf方法对非Spark3引擎的处理
4. 测试dealsparkDynamicConf方法的异常处理逻辑

### 7.2 集成测试
1. 测试Spark3作业的执行流程
2. 测试非Spark3作业的执行流程
3. 测试异常情况下的兜底逻辑
4. 测试配置变更后的系统表现

### 7.3 系统测试
1. 测试在高并发情况下的系统稳定性
2. 测试在大数据量情况下的系统性能
3. 测试配置变更后的系统表现

## 8. 风险评估和应对措施

### 8.1 风险评估
1. **功能风险**: Spark无法读取管理台参数，导致作业执行失败
2. **兼容性风险**: 修改后的代码影响现有任务的执行
3. **异常处理风险**: 异常处理逻辑不完善，导致系统崩溃

### 8.2 应对措施
1. **功能风险**: 保留异常处理的兜底逻辑，确保系统稳定性
2. **兼容性风险**: 进行充分的兼容性测试，确保不影响现有任务
3. **异常处理风险**: 完善异常处理逻辑，捕获所有可能的异常

## 9. 监控和维护

### 9.1 监控指标
1. dealsparkDynamicConf方法的调用次数
2. Spark3作业的执行次数
3. 异常情况的发生次数
4. 兜底逻辑的执行次数

### 9.2 维护建议
1. 定期检查配置的阈值是否合理
2. 监控方法调用情况，及时发现异常
3. 根据业务需求调整配置的阈值
4. 定期检查日志，发现潜在问题

## 10. 总结

本设计方案通过简化dealsparkDynamicConf方法，只保留spark.python.version的强制设置，移除所有其他参数覆盖，信任Spark启动时会自己读取管理台的参数，同时保留异常处理的兜底逻辑，提高了代码可读性和可维护性。该方案确保了系统的兼容性和稳定性，同时优化了代码结构，减少了维护成本。