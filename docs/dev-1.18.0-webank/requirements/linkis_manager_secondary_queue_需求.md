# Linkis Manager 智能队列选择 - 需求文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 需求ID | LINKIS-FEATURE-MANAGER-SECONDARY-QUEUE-001 |
| 需求名称 | Linkis Manager 智能队列选择 |
| 需求类型 | 新增功能（FEATURE） |
| 基础模块 | linkis-computation-governance/linkis-manager |
| 当前版本 | dev-1.18.0-hadoop3-sup |
| 创建时间 | 2026-04-09 |
| 文档状态 | 待评审 |

---

## 一、功能概述

### 1.1 功能名称

Linkis Manager 统一智能队列选择

### 1.2 功能描述

在 Linkis Manager 资源管理层面增加**智能队列选择**功能，支持：
- 为用户配置主队列和备用队列（第二队列）
- 在引擎创建时自动查询备用队列资源使用情况
- 当备用队列资源使用率低于阈值时，优先使用备用队列
- **当前仅支持 Spark 引擎，后续可扩展至其他引擎**
- 通过配置灵活控制队列选择策略（支持的引擎类型、Creator）

### 1.3 一句话描述

在 Linkis Manager 层面实现统一的智能队列选择，根据 Yarn 队列资源使用情况自动选择最优队列，当前仅支持 Spark 引擎。

---

## 二、功能背景

### 2.1 当前痛点

**现有架构分析**：

Linkis 采用两层资源管理架构：
1. **Linkis Manager 层**：负责全局资源管理和调度
   - 通过 `YarnResourceRequester` 查询 Yarn 队列资源
   - 决定是否允许创建引擎
   - 管理用户资源配额

2. **引擎插件层**：负责具体的任务执行
   - Spark、Hive、Flink 等各自引擎
   - 使用固定配置的队列提交任务

**存在的问题**：

| 问题 | 说明 | 影响 |
|------|------|------|
| 队列配置固定 | 每个引擎只能配置一个队列 | 资源利用率低 |
| 重复实现 | 如需智能队列选择，需在每个引擎实现 | 维护成本高 |
| 策略不统一 | 不同引擎可能有不同的队列策略 | 难以管理 |
| 无法复用 | 已有的 Yarn 资源查询能力未能充分利用 | 浪费资源 |

**业务场景痛点**：

1. **资源浪费**：低优先级任务占用高优先级队列资源
2. **队列冲突**：所有任务竞争同一队列，导致排队等待
3. **扩展困难**：新增引擎需要单独实现队列选择逻辑
4. **管理复杂**：队列策略分散在各个引擎中

### 2.2 现有功能

**Linkis Manager 已有能力**：

| 组件 | 功能 | 文件位置 |
|------|------|---------|
| YarnResourceRequester | 通过 Yarn REST API 查询队列资源 | YarnResourceRequester.java |
| ExternalResourceService | 外部资源服务接口 | ExternalResourceService.java |
| RequestResourceService | 资源请求服务 | RequestResourceService.scala |

**已获取的资源信息**：
```java
YarnQueueInfo {
    maxResource      // 队列最大资源
    usedResource     // 已使用资源
    maxApps          // 最大应用数
    numPendingApps   // 等待中的应用数
    numActiveApps    // 运行中的应用数
}
```

### 2.3 架构优势

**在 Linkis Manager 层面实现的优势**：

✅ **统一管理**：队列选择逻辑集中在一个地方
✅ **易于扩展**：设计支持所有引擎（Spark、Hive、Flink 等），当前仅支持 Spark
✅ **复用能力**：直接使用现有的 YarnResourceRequester
✅ **架构合理**：资源管理应该在 Manager 层面
✅ **易于维护**：修改一处，全局生效
✅ **配置灵活**：可以按用户、Creator、引擎类型配置

---

## 三、核心功能

### 3.1 功能优先级

| 优先级 | 功能点 | 说明 |
|--------|--------|------|
| P0 | 第二队列配置 | 支持配置主队列和备用队列 |
| P0 | 队列选择逻辑 | 根据资源使用率自动选择队列 |
| P0 | 引擎集成 | 将选定的队列传递给引擎 |
| P1 | 多维度配置 | 支持按用户、Creator、引擎类型配置 |
| P1 | 队列选择日志 | 记录队列选择决策过程 |

### 3.2 功能详细规格

#### 3.2.1 P0功能：第二队列配置

**配置项**：

**用户配置**（通过任务参数传入）：

| 配置项 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `wds.linkis.rm.yarnqueue` | String | ✅ | 主队列名称 |
| `wds.linkis.rm.secondary.yarnqueue` | String | ❌ | 第二队列名称（可选） |

**系统配置**（Linkis 配置）：

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `wds.linkis.rm.secondary.yarnqueue.enable` | Boolean | true | 是否启用智能队列选择功能 |
| `wds.linkis.rm.secondary.yarnqueue.threshold` | Double | 0.9 | 资源使用率阈值（0-1） |
| `wds.linkis.rm.secondary.yarnqueue.engines` | String | `spark` | 支持的引擎类型（逗号分隔），当前仅支持 Spark |
| `wds.linkis.rm.secondary.yarnqueue.creators` | String | `IDE,NOTEBOOK,CLIENT` | 支持的 Creator（逗号分隔） |

**配置方式**：

用户在提交任务时，只需传入两个队列名称。阈值和功能开关由 Linkis 系统配置控制。

**配置示例**：

```json
{
  "userCreatorLabel": {
    "user": "user1",
    "creator": "IDE"
  },
  "engineTypeLabel": {
    "engineType": "spark"
  },
  "properties": {
    "wds.linkis.rm.yarnqueue": "root.primary",
    "wds.linkis.rm.secondary.yarnqueue": "root.backup"
  }
}
```

**多任务配置示例**：

- 任务A（高优先级）：只使用主队列
  ```json
  {
    "properties": {
      "wds.linkis.rm.yarnqueue": "root.high-priority"
    }
  }
  ```

- 任务B（低优先级）：使用智能队列选择
  ```json
  {
    "properties": {
      "wds.linkis.rm.yarnqueue": "root.primary",
      "wds.linkis.rm.secondary.yarnqueue": "root.backup",
      "wds.linkis.rm.secondary.yarnqueue.threshold": "0.9"
    }
  }
  ```

- 任务C（测试任务）：使用独立的备用队列
  ```json
  {
    "properties": {
      "wds.linkis.rm.yarnqueue": "root.dev",
      "wds.linkis.rm.secondary.yarnqueue": "root.test",
      "wds.linkis.rm.secondary.yarnqueue.threshold": "0.8"
    }
  }
  ```

#### 3.2.2 P0功能：队列选择逻辑

**决策流程**：

```
┌─────────────────────────────────────────────────────────┐
│              引擎创建请求到达 Linkis Manager             │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
         ┌────────────────────────────┐
         │ 获取配置信息                  │
         │ - 用户配置：主队列、第二队列   │
         │ - 系统配置：阈值、功能开关     │
         │ - 引擎类型、Creator           │
         └─────────────┬──────────────┘
                       │
           ┌───────────┴───────────┐
           │                       │
           │ 未配置第二队列或功能关闭 │ 已配置且启用
           ▼                       ▼
    ┌──────────────┐    ┌──────────────────────────┐
    │ 使用主队列    │    │ 检查引擎类型和Creator     │
    │ (primary)    │    │ 是否在支持列表中           │
    └──────────────┘    └──────────┬───────────────┘
                                    │
                        ┌───────────┴───────────┐
                        │                       │
                        │ 不在支持列表           │ 在支持列表
                        ▼                       ▼
                 ┌──────────────┐    ┌──────────────────────────┐
                 │ 使用主队列    │    │ 查询第二队列资源使用率    │
                 │ (primary)    │    └──────────┬───────────────┘
                 └──────────────┘                 │
                                                 ▼
                                      ┌──────────────────────┐
                                      │ 资源使用率 <= 阈值?    │
                                      └──────────┬───────────┘
                                                 │
                                     ┌───────────┴───────────┐
                                     │                       │
                                     │ Yes                   │ No
                                     ▼                       ▼
                              ┌──────────────┐     ┌──────────────┐
                              │ 使用第二队列  │     │ 使用主队列    │
                              │ (secondary)  │     │ (primary)    │
                              └──────────────┘     └──────────────┘
                                       │                     │
                                       └──────────┬──────────┘
                                                  ▼
                                      ┌──────────────────────┐
                                      │ 更新 properties            │
                                      │ - 覆盖队列配置              │
                                      └──────────────────────┘
```

**资源使用率判断逻辑**：

```
使用备用队列的条件：所有维度（内存、CPU、实例数）的使用率都 <= 阈值
切回主队列的条件：有任何一个维度的使用率 > 阈值
```

**实现说明**：

采用**三维度独立判断**方式：

```scala
// 分别计算各维度使用率
val memoryUsage = usedResource.getQueueMemory / maxResource.getQueueMemory
val cpuUsage = usedResource.getQueueCores / maxResource.getQueueCores
val instancesUsage = usedResource.getQueueInstances / maxResource.getQueueInstances

// 判断：所有维度都必须在阈值以下
val allUnderThreshold = memoryUsage <= threshold &&
                       cpuUsage <= threshold &&
                       instancesUsage <= threshold

if (allUnderThreshold) {
  使用备用队列
} else {
  使用主队列（记录哪些维度超过阈值）
}
```

**判断原则**：
- **保守策略**：只要有一个维度超过阈值，就认为资源紧张，使用主队列
- **详细日志**：记录每个维度的使用率和是否超过阈值，便于问题排查
- **容错处理**：某个维度的最大资源为 0 时，该维度不参与判断

#### 3.2.3 P0功能：引擎集成

**集成方式**：

通过 `EngineCreateRequest.getProperties()` 传递选定的队列，**无需修改 EngineCreateRequest 类结构**。

```java
public class EngineCreateRequest {
    private Map<String, String> properties;  // 已有字段，无需修改

    // 直接使用 properties 传递队列信息
}
```

**实现方式**：

在 Linkis Manager 资源请求服务中，将选定的队列放入 properties：

```scala
override def requestResource(
    labels: util.List[Label[_]],
    resource: NodeResource,
    engineCreateRequest: EngineCreateRequest,
    wait: Long
): ResultResource = {

    // ... 现有代码 ...

    // 新增：智能队列选择
    val selectedQueue = queueSelectionService.selectQueue(
        labelContainer.getUserCreatorLabel,
        labelContainer.getEngineTypeLabel
    )

    // 将选定的队列放入 properties（覆盖原有配置）
    val properties = engineCreateRequest.getProperties
    if (properties == null) {
        engineCreateRequest.setProperties(new util.HashMap[String, String]())
    }
    engineCreateRequest.getProperties.put("wds.linkis.rm.yarnqueue", selectedQueue)

    logger.info(s"Selected queue for engine: $selectedQueue")

    // ... 继续现有流程 ...
}
```

**引擎插件改动**：

**方案1：无需修改（推荐）✅**

各引擎插件已经从 `options` 中读取队列配置：

```scala
// Spark 引擎（现有代码）
val options = engineCreationContext.getOptions
sparkConfig.setQueue(LINKIS_QUEUE_NAME.getValue(options))
// LINKIS_QUEUE_NAME = CommonVars[String]("wds.linkis.rm.yarnqueue", "default")
```

Manager 只需要在 properties 中设置 `wds.linkis.rm.yarnqueue` 的值，引擎会自动使用。

**方案2：使用新的配置键（可选）**

如果需要保留原始队列配置，可以使用新的配置键：

```scala
// Manager 设置
properties.put("wds.linkis.rm.selected.yarnqueue", selectedQueue)

// 引擎插件读取
val selectedQueue = LINKIS_SELECTED_QUEUE.getValue(options)
sparkConfig.setQueue(selectedQueue)
```

**优势**：
- ✅ 无需修改 EngineCreateRequest 类
- ✅ 复用现有的 properties 传递机制
- ✅ 引擎插件无需修改或仅需最小修改
- ✅ 向后兼容，不影响现有功能

---

## 四、技术方案

### 4.1 整体架构

```
用户提交任务（带队列配置参数）
         ↓
┌─────────────────────────────────────────────────────────┐
│              Linkis Manager - RequestResourceService       │
│  ┌──────────────────────────────────────────────────┐   │
│  │ 1. 从 engineCreateRequest.properties 获取配置      │   │
│  │    - primaryQueue（主队列）                        │   │
│  │    - secondaryQueue（第二队列）                    │   │
│  │    - threshold（阈值）                             │   │
│  └──────────────────────────────────────────────────┘   │
│                         ↓                                │
│  ┌──────────────────────────────────────────────────┐   │
│  │2. 查询第二队列资源使用率                           │   │
│  │   YarnResourceRequester.requestResourceInfo()     │   │
│  └──────────────────────────────────────────────────┘   │
│                         ↓                                │
│  ┌──────────────────────────────────────────────────┐   │
│  │3. 判断使用哪个队列                                 │   │
│  │   if (usage <= threshold) 用第二队列              │   │
│  │   else 用主队列                                   │   │
│  └──────────────────────────────────────────────────┘   │
│                         ↓                                │
│  ┌──────────────────────────────────────────────────┐   │
│  │4. 更新 properties                                 │   │
│  │   properties.put("wds.linkis.rm.yarnqueue", selectedQueue)│
│  └──────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ↓
        ┌──────────────────────────────────────┐
        │  各引擎插件（Spark、Hive、Flink）     │
        │  - 从 options 读取队列配置             │
        │  - 使用选定的队列提交任务              │
        └──────────────────────────────────────┘
```

### 4.2 修改 RequestResourceService

**文件位置**：
`linkis-computation-governance/linkis-manager/linkis-application-manager/src/main/scala/org/apache/linkis/manager/rm/service/RequestResourceService.scala`

**修改内容**：

在 `requestResource` 方法中增加队列选择逻辑：

```scala
override def requestResource(
    labels: util.List[Label[_]],
    resource: NodeResource,
    engineCreateRequest: EngineCreateRequest,
    wait: Long
): ResultResource = {

  // ... 现有资源检查逻辑 ...

  // ========== 新增：智能队列选择逻辑 ==========
  // 重要：任何异常都不能影响任务执行，异常时直接使用主队列
  try {
    // 1. 获取用户配置（从任务参数）
    val properties = if (engineCreateRequest.getProperties != null) {
      engineCreateRequest.getProperties
    } else {
      new util.HashMap[String, String]()
    }

    // 2. 获取队列配置（用户配置）
    val primaryQueue = properties.get("wds.linkis.rm.yarnqueue")
    val secondaryQueue = properties.get("wds.linkis.rm.secondary.yarnqueue")

    // 3. 获取系统配置（Linkis 配置）
    val enabled = RMConfiguration.SECONDARY_QUEUE_ENABLED.getValue
    val threshold = RMConfiguration.SECONDARY_QUEUE_THRESHOLD.getValue
    val supportedEngines = RMConfiguration.SECONDARY_QUEUE_ENGINES.getValue.split(",").map(_.trim).toSet
    val supportedCreators = RMConfiguration.SECONDARY_QUEUE_CREATORS.getValue.split(",").map(_.trim).toSet

    // 4. 检查是否启用第二队列功能
    if (enabled && StringUtils.isNotBlank(secondaryQueue) &&
        StringUtils.isNotBlank(primaryQueue)) {

      // 5. 获取引擎类型和 Creator（从 Labels）
      var engineType: String = null
      var creator: String = null

      try {
        val labelContainer = LabelUtils.parseLabel(labels)
        if (labelContainer.getEngineTypeLabel != null) {
          engineType = labelContainer.getEngineTypeLabel.getEngineType
        }
        if (labelContainer.getUserCreatorLabel != null) {
          creator = labelContainer.getUserCreatorLabel.getCreator
        }
      } catch {
        case e: Exception =>
          logger.error("Failed to parse labels, fallback to primary queue", e)
          // Label 解析失败，直接使用主队列，不影响任务
      }

      logger.info(s"Queue selection enabled: primary=$primaryQueue, secondary=$secondaryQueue, threshold=$threshold")
      logger.info(s"Request info: engineType=$engineType, creator=$creator")

      // 6. 检查引擎类型和 Creator 是否在支持列表中
      val engineMatched = engineType == null || supportedEngines.contains(engineType.toLowerCase)
      val creatorMatched = creator == null || supportedCreators.contains(creator.toUpperCase)

      if (engineMatched && creatorMatched) {
        try {
          // 7. 查询第二队列资源使用率
          val queueInfo = externalResourceService.requestResourceInfo(
            new YarnResourceIdentifier(secondaryQueue),
            externalResourceProvider
          )

          if (queueInfo != null) {
            val usedResource = queueInfo.getUsedResource
            val maxResource = queueInfo.getMaxResource

            // 8. 计算资源使用率
            val usedPercentage = if (maxResource != null && maxResource > 0) {
              usedResource.getMaxMemory.toDouble / maxResource.getMaxMemory.toDouble
            } else {
              0.0
            }

            // 9. 判断使用哪个队列
            val selectedQueue = if (usedPercentage <= threshold) {
              logger.info(s"Secondary queue available: usage=${(usedPercentage * 100).formatted("%.2f%%")} <= ${(threshold * 100).formatted("%.2f%%")}, use secondary queue: $secondaryQueue")
              secondaryQueue
            } else {
              logger.info(s"Secondary queue not available: usage=${(usedPercentage * 100).formatted("%.2f%%")} > ${(threshold * 100).formatted("%.2f%%")}, use primary queue: $primaryQueue")
              primaryQueue
            }

            // 10. 更新 properties
            properties.put("wds.linkis.rm.yarnqueue", selectedQueue)

          } else {
            logger.warn(s"Failed to get queue info for $secondaryQueue, use primary queue: $primaryQueue")
          }

        } catch {
          case e: Exception =>
            // 异常处理：记录详细错误日志，使用主队列，确保不影响任务执行
            logger.error(s"Exception during queue resource check, fallback to primary queue: $primaryQueue", e)
        }
      } else {
        // 引擎类型或 Creator 不在支持列表中
        if (!engineMatched) {
          logger.info(s"Engine type '$engineType' not in supported list: ${supportedEngines.mkString(",")}, use primary queue: $primaryQueue")
        }
        if (!creatorMatched) {
          logger.info(s"Creator '$creator' not in supported list: ${supportedCreators.mkString(",")}, use primary queue: $primaryQueue")
        }
      }
    } else {
      logger.debug("Secondary queue not configured or disabled, use primary queue from properties")
    }

  } catch {
    case e: Exception =>
      // 最外层异常捕获：确保任何异常都不影响任务执行
      logger.error("Unexpected error in queue selection logic, task will continue with primary queue", e)
      // 不做任何处理，让任务继续使用原始配置的主队列
  }
  // ========== 队列选择逻辑结束 ==========

  // ... 继续现有流程 ...

  // 返回结果
}
```

### 4.3 代码说明

#### 4.3.1 队列选择逻辑

**核心逻辑**：

```scala
// 判断是否启用第二队列
if (enabled && secondaryQueue != null && !secondaryQueue.isEmpty) {
  // 获取引擎类型和 Creator
  val engineType = labelContainer.getEngineTypeLabel.getEngineType
  val creator = labelContainer.getUserCreatorLabel.getCreator

  // 检查引擎类型和 Creator 是否在支持列表中
  val engineMatched = supportedEngines.contains(engineType.toLowerCase)
  val creatorMatched = supportedCreators.contains(creator.toUpperCase)

  if (engineMatched && creatorMatched) {
    // 查询第二队列资源
    val queueInfo = externalResourceService.requestResourceInfo(secondaryQueue, ...)

    // 计算资源使用率
    val usage = usedResource / maxResource

    // 判断是否使用第二队列
    if (usage <= threshold) {
      selectedQueue = secondaryQueue  // 使用第二队列
    } else {
      selectedQueue = primaryQueue    // 使用主队列
    }

    // 更新 properties
    properties.put("wds.linkis.rm.yarnqueue", selectedQueue)
  } else {
    // 引擎类型或 Creator 不在支持列表中，使用主队列
    selectedQueue = primaryQueue
  }
}
```

#### 4.3.2 配置获取

**用户配置**（从任务参数）：

```scala
val properties = engineCreateRequest.getProperties
val primaryQueue = properties.get("wds.linkis.rm.yarnqueue")
val secondaryQueue = properties.get("wds.linkis.rm.secondary.yarnqueue")
```

**系统配置**（从 Linkis 配置）：

```scala
import org.apache.linkis.manager.common.conf.RMConfiguration

val enabled = RMConfiguration.SECONDARY_QUEUE_ENABLED.getValue
val threshold = RMConfiguration.SECONDARY_QUEUE_THRESHOLD.getValue
val supportedEngines = RMConfiguration.SECONDARY_QUEUE_ENGINES.getValue.split(",").map(_.trim).toSet
val supportedCreators = RMConfiguration.SECONDARY_QUEUE_CREATORS.getValue.split(",").map(_.trim).toSet
```

#### 4.3.3 异常处理

**核心原则**：**任何异常都不能影响任务执行**

**多层异常捕获策略**：

1. **最外层异常捕获**（确保任务继续）
   ```scala
   try {
     // 所有队列选择逻辑
   } catch {
     case e: Exception =>
       logger.error("Unexpected error in queue selection logic, task will continue with primary queue", e)
       // 不做任何处理，让任务继续
   }
   ```

2. **Label 解析异常捕获**
   ```scala
   try {
     val labelContainer = LabelUtils.parseLabel(labels)
     // ...
   } catch {
     case e: Exception =>
       logger.error("Failed to parse labels, fallback to primary queue", e)
       // 直接使用主队列
   }
   ```

3. **Yarn API 调用异常捕获**
   ```scala
   try {
     val queueInfo = externalResourceService.requestResourceInfo(...)
     // ...
   } catch {
     case e: Exception =>
       logger.error(s"Exception during queue resource check, fallback to primary queue: $primaryQueue", e)
       // 使用主队列
   }
   ```

**异常处理要求**：

- ✅ 所有异常都必须记录 ERROR 级别日志
- ✅ 日志必须包含完整的异常堆栈信息
- ✅ 异常时自动降级到主队列
- ✅ 确保任务继续执行，不受任何影响

**系统配置定义**：

需要在 `RMConfiguration` 中新增配置项：

```java
// linkis-manager-common/src/main/java/org/apache/linkis/manager/common/conf/RMConfiguration.java

public class RMConfiguration {
    // 是否启用第二队列功能
    public static final CommonVars<Boolean> SECONDARY_QUEUE_ENABLED =
        CommonVars.apply("wds.linkis.rm.secondary.yarnqueue.enable", Boolean.class, true);

    // 第二队列资源使用率阈值
    public static final CommonVars<Double> SECONDARY_QUEUE_THRESHOLD =
        CommonVars.apply("wds.linkis.rm.secondary.yarnqueue.threshold", Double.class, 0.9);

    // 支持的引擎类型（逗号分隔），当前仅支持 Spark
    public static final CommonVars<String> SECONDARY_QUEUE_ENGINES =
        CommonVars.apply("wds.linkis.rm.secondary.yarnqueue.engines", "spark");

    // 支持的 Creator（逗号分隔）
    public static final CommonVars<String> SECONDARY_QUEUE_CREATORS =
        CommonVars.apply("wds.linkis.rm.secondary.yarnqueue.creators", "IDE,NOTEBOOK,CLIENT");
}
```

#### 4.3.3 异常处理

### 4.4 引擎插件

**无需修改** ✅

各引擎插件已经从 `options` 中读取队列配置：

```scala
// Spark 引擎（现有代码）
val options = engineCreationContext.getOptions
sparkConfig.setQueue(LINKIS_QUEUE_NAME.getValue(options))
// LINKIS_QUEUE_NAME = CommonVars[String]("wds.linkis.rm.yarnqueue", "default")
```

Manager 更新 properties 后，引擎自动使用选定的队列。

### 4.5 YarnResourceRequester

**现有方法，无需修改** ✅

直接使用现有的 `requestResourceInfo` 方法：

```java
public NodeResource requestResourceInfo(
    ExternalResourceIdentifier identifier,
    ExternalResourceProvider provider
) {
    String rmWebAddress = getAndUpdateActiveRmWebAddress(provider);
    String queueName = ((YarnResourceIdentifier) identifier).getQueueName();

    YarnQueueInfo resources = getResources(rmWebAddress, realQueueName, queueName, provider);

    CommonNodeResource nodeResource = new CommonNodeResource();
    nodeResource.setMaxResource(resources.getMaxResource());
    nodeResource.setUsedResource(resources.getUsedResource());
    return nodeResource;
}
```
    engineCreateRequest.setSelectedQueue(selectedQueue)

    logger.info(s"Selected queue for engine: $selectedQueue")

    // ... 继续现有流程 ...
}
```

### 4.5 配置获取

**用户配置**：从任务提交参数中获取

用户在提交任务时，通过 `properties` 传入队列配置：

```json
{
  "properties": {
    "wds.linkis.rm.yarnqueue": "root.primary",
    "wds.linkis.rm.secondary.yarnqueue": "root.backup"
  }
}
```

**系统配置**：从 Linkis 配置文件获取

阈值和功能开关由 Linkis 系统配置：

```properties
# linkis.properties 或 linkis-engineconn.properties
wds.linkis.rm.secondary.yarnqueue.enable=true
wds.linkis.rm.secondary.yarnqueue.threshold=0.9
```

**配置获取逻辑**：

```scala
// 1. 获取用户配置（从任务参数）
val properties = engineCreateRequest.getProperties
val primaryQueue = properties.get("wds.linkis.rm.yarnqueue")
val secondaryQueue = properties.get("wds.linkis.rm.secondary.yarnqueue")

// 2. 获取系统配置（从 Linkis 配置）
val threshold = RMConfiguration.SECONDARY_QUEUE_THRESHOLD.getValue  // 0.9
val enabled = RMConfiguration.SECONDARY_QUEUE_ENABLED.getValue      // true
```

---

## 五、非功能需求

### 5.1 性能要求

| 指标 | 要求 | 说明 |
|------|------|------|
| 队列查询耗时 | < 500ms | Yarn REST API 调用，P95 < 500ms |
| 引擎创建影响 | < 1s | 增加的启动时间，相比原有流程增加 < 1s |
| 并发支持 | 10 QPS | 支持 10 个并发任务同时进行队列选择 |
| 超时控制 | 3s | Yarn API 调用超时时间 |

### 5.2 兼容性要求

| 项目 | 要求 |
|------|------|
| 向后兼容 | 未配置第二队列时，行为与原来一致 |
| 引擎兼容 | 所有基于 Yarn 的引擎都能使用 |
| 版本兼容 | 支持 Hadoop 2.x / 3.x |

### 5.3 可靠性要求

| 项目 | 要求 |
|------|------|
| 异常降级 | **任何异常都不能影响任务执行**，异常时直接使用主队列 |
| 日志记录 | 记录队列选择决策过程和所有异常信息 |
| 超时控制 | Yarn API 调用设置合理超时 |
| 多层异常捕获 | 在关键操作处（Label 解析、Yarn API 调用）都进行异常捕获 |

**异常处理原则**：

```
队列选择异常 → 记录 ERROR 日志 → 切回主队列 → 任务继续执行
```

**核心要求**：

1. **任务执行优先**：智能队列选择是增强功能，不能因为任何异常导致任务失败
2. **多层异常捕获**：
   - Label 解析异常 → 使用主队列，记录日志
   - Yarn API 调用异常 → 使用主队列，记录详细错误栈
   - 任何未预期异常 → 使用主队列，记录错误栈
3. **详细日志记录**：所有异常都必须记录 ERROR 级别日志，包含异常堆栈

### 5.4 可维护性要求

| 项目 | 要求 |
|------|------|
| 代码规范 | 遵循 Linkis 项目编码规范 |
| 单元测试 | 核心逻辑单元测试覆盖率 > 80% |

---

## 六、验收标准

### 6.1 功能验收

| ID | 验收项 | 验证方式 | 优先级 |
|-----|-------|---------|--------|
| AC-001 | 队列选择功能可配置 | 修改配置后生效 | P0 |
| AC-002 | 资源充足时使用第二队列 | 资源 < 阈值时使用第二队列 | P0 |
| AC-003 | 资源紧张时使用主队列 | 资源 > 阈值时使用主队列 | P0 |
| AC-004 | 未配置时使用主队列 | 不配置时行为与原来一致 | P0 |
| AC-005 | 阈值可配置 | 修改阈值后生效 | P1 |
| AC-006 | 功能开关可配置 | 可通过配置禁用功能 | P1 |
| AC-007 | Spark 引擎生效 | Spark 引擎使用选定队列 | P0 |
| AC-008 | 其他引擎自动过滤 | Hive、Flink 等引擎使用主队列 | P1 |
| AC-010 | 引擎类型过滤生效 | 不在支持列表的引擎使用主队列 | P1 |
| AC-011 | Creator 过滤生效 | 不在支持列表的 Creator 使用主队列 | P1 |
| AC-012 | 异常时自动降级 | 异常情况下使用主队列 | P0 |
| AC-013 | 异常时不影响引擎创建 | 异常时引擎仍能正常创建 | P0 |

### 6.2 性能验收

| ID | 验收项 | 指标 | 验证方式 | 优先级 |
|-----|-------|------|---------|--------|
| AC-PERF-001 | 队列资源查询耗时 | P95 < 500ms | 压测验证 | P1 |
| AC-PERF-002 | 引擎创建总耗时增加 | < 1s | 对比测试 | P1 |
| AC-PERF-003 | Yarn API 调用超时 | 3s 超时控制 | 功能测试 | P1 |

### 6.3 并发验收

| ID | 验收项 | 场景 | 预期结果 | 优先级 |
|-----|-------|------|---------|--------|
| AC-CONC-001 | 多任务并发队列选择 | 10个并发任务 | 各任务独立选择队列，互不影响 | P1 |
| AC-CONC-002 | 高并发资源查询 | 50 QPS | 系统稳定，无异常 | P2 |

---

## 七、测试场景

### 7.1 功能测试

| 场景 | 用户配置 | 系统配置 | 预期结果 |
|------|---------|---------|---------|
| 第二队列可用 | secondary=queue2 | 阈值=0.9 | 使用第二队列 |
| 第二队列不可用 | secondary=queue2 | 阈值=0.9, 资源95% | 使用主队列 |
| 未配置第二队列 | secondary 为空 | - | 使用主队列 |
| 禁用功能 | secondary=queue2 | enabled=false | 使用主队列 |
| 系统阈值调整 | secondary=queue2 | 阈值=0.8 | 按 80% 阈值判断 |
| 引擎类型过滤 | secondary=queue2, hive引擎 | engines=spark | 使用主队列 |
| 引擎类型通过 | secondary=queue2, spark引擎 | engines=spark | 正常判断 |
| Creator 过滤 | secondary=queue2, CLIENT | creators=IDE,NOTEBOOK | 使用主队列 |
| Creator 通过 | secondary=queue2, IDE | creators=IDE,NOTEBOOK | 正常判断 |

### 7.2 多引擎测试

| 引擎 | 验证方式 | 预期结果 |
|------|---------|---------|
| Spark | 提交 Spark 任务 | 使用选定队列 |
| Hive | 提交 Hive 任务 | 使用主队列（不在支持列表） |
| Flink | 提交 Flink 任务 | 使用主队列（不在支持列表） |
| Python | 提交 Python 任务 | 使用主队列（不在支持列表） |

### 7.3 异常测试

| 场景 | 预期结果 | 日志要求 |
|------|---------|---------|
| Yarn 连接失败 | 使用主队列，引擎正常创建 | 记录 ERROR 日志 + 异常堆栈 |
| 队列不存在 | 使用主队列，引擎正常创建 | 记录 ERROR 日志 + 异常堆栈 |
| 配置格式错误 | 使用主队列，引擎正常创建 | 记录 ERROR 日志 + 异常堆栈 |
| Label 解析失败 | 使用主队列，引擎正常创建 | 记录 ERROR 日志 + 异常堆栈 |
| Yarn API 超时 | 使用主队列，引擎正常创建 | 记录 ERROR 日志 + 超时信息 |
| 空指针异常 | 使用主队列，引擎正常创建 | 记录 ERROR 日志 + 异常堆栈 |
| 网络异常 | 使用主队列，引擎正常创建 | 记录 ERROR 日志 + 异常堆栈 |
| 配置解析异常 | 使用主队列，引擎正常创建 | 记录 ERROR 日志 + 异常堆栈 |

**异常测试核心要求**：

- ✅ **任务执行不受影响**：任何异常情况下，任务都能正常创建和执行
- ✅ **自动降级**：异常时自动切换到主队列
- ✅ **详细日志**：所有异常都记录 ERROR 级别日志，包含完整异常堆栈
- ✅ **用户无感知**：异常不影响用户体验，任务正常执行

---

## 八、风险与依赖

### 8.1 风险

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| Yarn API 调用失败导致引擎创建失败 | 高 | 异常捕获，降级使用主队列 |
| 高并发下资源查询性能问题 | 中 | 3秒超时控制，异常降级 |
| Yarn ResourceManager 压力增大 | 中 | 后续可增加本地缓存（TTL 5秒） |
| 队列资源信息实时性 | 低 | 已接受，无需额外措施 |

### 8.1.1 高并发风险详细说明

**风险描述**：

大量引擎创建请求同时查询 Yarn 队列资源，可能导致：
- Yarn ResourceManager 压力增大
- 请求响应时间增加
- 影响系统整体性能

**缓解措施**：

1. **当前实现**：
   - ✅ 3秒超时控制，避免长时间等待
   - ✅ 异常自动降级，不影响任务执行
   - ✅ 支持并发场景（AC-CONC-001）

2. **后续优化**：
   - 📋 增加本地缓存（TTL 5秒），减少重复查询
   - 📋 监控 Yarn API 调用频率，必要时增加限流
   - 📋 考虑异步查询方式，避免阻塞主流程
| 多引擎测试覆盖不足 | 中 | 充分测试各引擎 |

### 8.2 依赖

| 依赖项 | 版本要求 | 说明 |
|--------|---------|------|
| Hadoop | 2.x / 3.x | Yarn REST API |
| Yarn ResourceManager | 运行中 | 需要可访问 |
| Spring Framework | 现有版本 | 依赖注入 |

---

## 九、实施计划

| 阶段 | 内容 | 预计时间 |
|------|------|---------|
| 需求评审 | 需求文档评审确认 | 0.5天 |
| 设计评审 | 技术方案评审确认 | 0.5天 |
| 开发实现 | RequestResourceService 集成队列选择逻辑 | 2天 |
| YarnResourceRequester 增强 | 增加批量查询方法 | 0.5天 |
| 引擎验证 | 验证各引擎使用选定队列 | 1天 |
| 单元测试 | 核心逻辑单元测试 | 1天 |
| 集成测试 | 功能测试和多引擎测试 | 1天 |
| 代码评审 | Code Review | 0.5天 |
| 文档更新 | 使用文档和配置说明 | 0.5天 |

**总计**：约 7.5 个工作日

---

## 附录

### 附录A：队列选择决策日志示例

**场景一**：第二队列可用
```
2026-04-09 10:30:15 INFO  RequestResourceService:100 - Received engine create request from user1, IDE, spark
2026-04-09 10:30:15 INFO  RequestResourceService:105 - User queue config: primary=root.primary, secondary=root.backup
2026-04-09 10:30:15 INFO  RequestResourceService:110 - System config: enabled=true, threshold=0.9
2026-04-09 10:30:15 INFO  YarnResourceRequester:120 - Getting metrics for queue: root.backup
2026-04-09 10:30:17 INFO  YarnResourceRequester:140 - Queue metrics: used=720.0, max=1000.0, usage=72.00%, available=280.0
2026-04-09 10:30:17 INFO  RequestResourceService:115 - Secondary queue available (72.00% <= 90.00%), selected: root.backup
2026-04-09 10:30:17 INFO  RequestResourceService:120 - Updated properties: {wds.linkis.rm.yarnqueue=root.backup}
```

**场景二**：第二队列不可用
```
2026-04-09 10:35:10 INFO  RequestResourceService:100 - Received engine create request from user1, IDE, spark
2026-04-09 10:35:10 INFO  RequestResourceService:105 - User queue config: primary=root.primary, secondary=root.backup
2026-04-09 10:35:10 INFO  RequestResourceService:110 - System config: enabled=true, threshold=0.9
2026-04-09 10:35:10 INFO  YarnResourceRequester:120 - Getting metrics for queue: root.backup
2026-04-09 10:35:12 INFO  YarnResourceRequester:140 - Queue metrics: used=950.0, max=1000.0, usage=95.00%, available=50.0
2026-04-09 10:35:12 INFO  RequestResourceService:115 - Secondary queue not available (95.00% > 90.00%), use primary queue
2026-04-09 10:35:12 INFO  RequestResourceService:120 - Keep primary queue: root.primary
```

**场景三**：引擎类型过滤
```
2026-04-09 10:40:20 INFO  RequestResourceService:100 - Received engine create request from user1, IDE, hive
2026-04-09 10:40:20 INFO  RequestResourceService:105 - User queue config: primary=root.primary, secondary=root.backup
2026-04-09 10:40:20 INFO  RequestResourceService:110 - System config: enabled=true, threshold=0.9
2026-04-09 10:40:20 INFO  RequestResourceService:112 - Request info: engineType=hive, creator=IDE
2026-04-09 10:40:20 INFO  RequestResourceService:115 - Engine type 'hive' not in supported list: spark, use primary queue: root.primary
```

**场景四**：Creator 过滤
```
2026-04-09 10:45:10 INFO  RequestResourceService:100 - Received engine create request from user1, SHELL, spark
2026-04-09 10:45:10 INFO  RequestResourceService:105 - User queue config: primary=root.primary, secondary=root.backup
2026-04-09 10:45:10 INFO  RequestResourceService:110 - System config: enabled=true, threshold=0.9
2026-04-09 10:45:10 INFO  RequestResourceService:112 - Request info: engineType=spark, creator=SHELL
2026-04-09 10:45:10 INFO  RequestResourceService:117 - Creator 'SHELL' not in supported list: IDE,NOTEBOOK,CLIENT, use primary queue: root.primary
```

**场景五**：Yarn 连接异常（自动降级）
```
2026-04-09 10:50:20 INFO  RequestResourceService:100 - Received engine create request from user1, IDE, spark
2026-04-09 10:50:20 INFO  RequestResourceService:105 - User queue config: primary=root.primary, secondary=root.backup
2026-04-09 10:50:20 INFO  RequestResourceService:110 - System config: enabled=true, threshold=0.9
2026-04-09 10:50:20 INFO  RequestResourceService:112 - Request info: engineType=spark, creator=IDE
2026-04-09 10:50:22 ERROR YarnResourceRequester:150 - Failed to get queue metrics for root.backup
java.net.ConnectException: Connection refused: http://yarn-resourcemanager:8088/ws/v1/cluster/queue/root.backup
    at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1623)
    at org.apache.linkis.manager.rm.external.yarn.YarnResourceRequester.getResources(YarnResourceRequester.java:145)
    ... 10 more
2026-04-09 10:50:22 ERROR RequestResourceService:130 - Exception during queue resource check, fallback to primary queue: root.primary
org.apache.linkis.common.exception.LinkisRuntimeException: Failed to connect to Yarn ResourceManager
    at org.apache.linkis.manager.rm.external.yarn.YarnResourceRequester.requestResourceInfo(YarnResourceRequester.java:178)
    at org.apache.linkis.manager.rm.service.RequestResourceService.requestResource(RequestResourceService.scala:125)
    ... 5 more
2026-04-09 10:50:22 INFO  RequestResourceService:140 - Task continues with primary queue: root.primary
2026-04-09 10:50:23 INFO  DefaultResourceManager:200 - Engine created successfully with queue: root.primary
```

**异常处理说明**：
- ✅ Yarn 连接失败被捕获
- ✅ 记录完整的异常堆栈信息
- ✅ 自动降级到主队列
- ✅ 任务继续执行，引擎创建成功

### 附录B：参考代码位置

| 文件 | 路径 |
|------|------|
| YarnResourceRequester | linkis-manager/linkis-application-manager/src/main/java/org/apache/linkis/manager/rm/external/yarn/YarnResourceRequester.java |
| RequestResourceService | linkis-manager/linkis-application-manager/src/main/scala/org/apache/linkis/manager/rm/service/RequestResourceService.scala |
| ExternalResourceService | linkis-manager/linkis-application-manager/src/main/java/org/apache/linkis/manager/rm/external/service/ExternalResourceService.java |
| DefaultResourceManager | linkis-manager/linkis-application-manager/src/main/scala/org/apache/linkis/manager/rm/service/impl/DefaultResourceManager.scala |

### 附录C：术语表

| 术语 | 说明 |
|------|------|
| 主队列（Primary Queue） | 用户配置的主要队列，通过 `wds.linkis.rm.yarnqueue` 配置 |
| 第二队列（Secondary Queue） | 备用队列，资源充足时优先使用，通过 `wds.linkis.rm.secondary.yarnqueue` 配置 |
| 阈值（Threshold） | 触发队列切换的资源使用率临界值，通过 `wds.linkis.rm.secondary.yarnqueue.threshold` 配置 |
| 支持的引擎类型 | 当前仅支持 Spark 引擎，通过 `wds.linkis.rm.secondary.yarnqueue.engines` 配置，后续可扩展支持 Hive、Flink 等 |
| 支持的 Creator | 可配置支持的 Creator 列表，通过 `wds.linkis.rm.secondary.yarnqueue.creators` 配置 |
| YarnResourceRequester | Yarn 资源请求器，通过 REST API 查询 Yarn 队列资源 |
| ExternalResourceService | 外部资源服务接口，用于获取 Yarn 队列信息 |
| RequestResourceService | 资源请求服务，在资源请求流程中集成队列选择逻辑 |
| Creator | Linkis 任务创建来源标识（IDE、NOTEBOOK、CLIENT 等） |
