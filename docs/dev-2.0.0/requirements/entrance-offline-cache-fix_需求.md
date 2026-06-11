# Entrance Offline Cache Fix 问题分析报告

**需求类型**: FIX（Bug修复）
**问题等级**: P1-高
**文档版本**: v1.0
**创建日期**: 2026-04-02

---

## 📋 问题速览

| 维度 | 内容 |
|-----|------|
| **问题概述** | Entrance实例offline后，ParallelGroup缓存未更新，导致用户任务并发数计算错误 |
| **紧急程度** | P1-高 |
| **影响范围** | 所有使用多Entrance实例部署的环境 |
| **问题模块** | linkis-entrance（EntranceGroupFactory） |
| **复现概率** | 必现 |
| **发现环境** | 生产环境 |

> 💡 **阅读指引**：速览问题看本卡片 → 复现步骤看第三章 → 根因分析看第四章 → 技术实现看设计文档

---

## 1. 【核心】问题定位

### 1.1 问题背景

Apache Linkis支持多Entrance实例部署以提高系统可用性和并发处理能力。在多实例环境下，每个Entrance实例需要根据当前在线的Entrance实例数量动态计算用户的任务并发数。

当某个Entrance实例被标记为offline（隔离）时，该实例应该从总数中排除，确保剩余在线实例的并发数计算正确。

### 1.2 问题现象

**具体场景**：
- 集群有4台Entrance服务：A、B、C、D
- 用户并发设置为100（期望每个实例处理25个并发任务）
- Entrance C上有正在运行的作业
- 管理员将Entrance C标记为offline（隔离）
- **实际结果**：单台在线Entrance并发 = 100/4 = 25（仍按4台计算）
- **期望结果**：单台在线Entrance并发 = 100/3 = 33（按3台计算）

**用户影响**：
- 任务提交失败（并发槽位不足）
- 用户体验下降
- 系统资源利用率下降

### 1.3 【核心】问题复现步骤

| 步骤 | 操作 | 预期结果 | 实际结果 |
|:----:|------|---------|---------|
| 1 | 部署4个Entrance实例，配置用户并发数为100 | 系统正常运行，每个实例并发数为25 | ✓ 正常 |
| 2 | 在某个Entrance实例（如C）上有正在运行的任务时，标记该实例为offline | 并发数自动调整为 100/3 = 33 | ❌ 并发数仍为 100/4 = 25 |
| 3 | 提交新任务 | 任务应该成功提交到剩余的3个实例 | ❌ 任务提交失败，提示并发数已满 |
| 4 | 等待50分钟后（缓存过期）提交新任务 | 任务成功提交 | ✓ 任务成功提交（缓存自动更新） |

**复现条件**：
- 多Entrance实例部署（>=2个）
- 某个Entrance实例offline时有正在运行的任务
- 缓存未过期（默认50分钟）

### 1.4 问题代码位置

**核心问题代码**：`linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/scheduler/EntranceGroupFactory.scala`

**问题方法**：
```scala
// Line 72-128
override def getOrCreateGroup(event: SchedulerEvent): Group = {
  // ...
  val cacheGroup = groupNameToGroups.getIfPresent(groupName)
  if (null == cacheGroup) synchronized {
    // 第一次执行时计算并发数并缓存
    val entranceNum = EntranceUtils.getRunningEntranceNumber()
    val maxRunningJobs = userDefinedRunningJobs / entranceNum
    // ...
    groupNameToGroups.put(groupName, group)
  } else {
    cacheGroup // 后续直接返回缓存，不重新计算
  }
}
```

**并发数计算方法**：`linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/utils/EntranceUtils.scala`

```scala
// Line 114-137
def getRunningEntranceNumber(): Int = {
  val entranceNum = Sender.getInstances(...).length
  // 获取offline实例
  val offlineIns = InstanceLabelClient.getInstance.getInstanceFromLabel(labelList)
  // 计算实际在线实例数
  entranceNum - offlineIns.length
}
```

**缓存配置**：`linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/conf/EntranceConfiguration.scala`

```scala
// Line 202-204
val GROUP_CACHE_MAX = CommonVars("wds.linkis.consumer.group.cache.capacity", 5000)
val GROUP_CACHE_EXPIRE_TIME = CommonVars("wds.linkis.consumer.group.expire.time", 50) // 50分钟
```

---

## 2. 【核心】根因分析

### 2.1 5Why根因分析表

| 层级 | 问题 | 答案 |
|:----:|------|------|
| **Why 1** | 为什么Entrance offline后并发数计算错误？ | 因为ParallelGroup缓存未更新，仍使用offline前的并发数 |
| **Why 2** | 为什么缓存未更新？ | 因为getOrCreateGroup()只在第一次执行时计算并发数，后续直接从Guava Cache读取 |
| **Why 3** | 为什么没有缓存更新机制？ | 因为当前实现依赖缓存过期（50分钟），没有主动失效缓存的机制 |
| **Why 4** | 为什么没有主动失效机制？ | 因为多实例间缺少缓存状态同步机制 |
| **Why 5** | 根本原因是什么？ | **缺少Entrance offline事件的广播机制，导致各实例缓存不一致** |

### 2.2 根因确认

#### 2.2.1 直接原因

**技术层面**：
1. **缓存只写一次**：`EntranceGroupFactory.getOrCreateGroup()` 在第一次执行时计算并发数并放入Guava Cache，后续请求直接返回缓存对象
2. **缓存过期时间过长**：默认50分钟，导致offline后长时间缓存不更新
3. **缺少主动失效机制**：没有在Entrance offline时主动清除缓存

#### 2.2.2 根本原因

**架构层面**：
1. **缺少实例状态变更通知机制**：多实例间没有同步Entrance状态变更的通道
2. **缓存策略设计缺陷**：仅依赖被动过期（TTL），缺少主动失效（Invalidation）
3. **分布式缓存一致性问题**：Guava Cache是本地缓存，多实例间无法自动同步

#### 2.2.3 相关代码

| 文件 | 方法/类 | 作用 |
|-----|---------|------|
| `EntranceGroupFactory.scala` | `getOrCreateGroup()` | 创建并缓存ParallelGroup对象 |
| `EntranceGroupFactory.scala` | `groupNameToGroups` | Guava缓存，存储Group对象 |
| `EntranceUtils.scala` | `getRunningEntranceNumber()` | 计算在线Entrance实例数 |
| `EntranceConfiguration.scala` | `GROUP_CACHE_EXPIRE_TIME` | 缓存过期时间配置 |

---

## 3. 【核心】修复方案

### 3.1 方案概述

**方案名称**：基于RPC广播的缓存主动失效机制

**核心思路**：
1. 复用Linkis现有的BroadcastProtocol和BroadcastRPCInterceptor
2. 在Entrance offline时广播缓存清除消息
3. 各实例接收广播后清除本地Group缓存
4. 下次任务提交时重新计算并发数

### 3.2 临时方案（Hot Fix）

**方案描述**：手动清除缓存

**实施步骤**：
1. 在Entrance offline后，手动调用管理API清除缓存
2. 或者重启所有Entrance实例

**优点**：
- 实施简单，无需代码修改

**缺点**：
- 需要人工介入，自动化程度低
- 重启实例会影响正在运行的任务
- 不适合生产环境

**不推荐使用此方案**

### 3.3 根本方案

#### 3.3.1 方案设计

**方案A - 主动失效缓存（推荐）**

**核心组件**：
1. **广播协议**：`EntranceGroupCacheClearBroadcast`（继承BroadcastProtocol）
2. **广播监听器**：`EntranceGroupCacheClearBroadcastListener`
3. **触发点**：Entrance offline事件

**实现步骤**：

**步骤1**：创建广播协议类
```scala
// 广播消息，通知所有实例清除Group缓存
case class EntranceGroupCacheClearBroadcast(
  instance: String,
  timestamp: Long
) extends BroadcastProtocol
```

**步骤2**：创建广播监听器
```scala
class EntranceGroupCacheClearBroadcastListener extends BroadcastListener {
  override def onBroadcastEvent(event: BroadcastProtocol): Unit = {
    event match {
      case clear: EntranceGroupCacheClearBroadcast =>
        // 清除所有Group缓存
        groupNameToGroups.invalidateAll()
        logger.info(s"Cleared all Group cache due to entrance offline: ${clear.instance}")
    }
  }
}
```

**步骤3**：在offline时发送广播

**触发点**：`linkis-computation-governance/linkis-entrance/src/main/java/org/apache/linkis/entrance/server/DefaultEntranceServer.java`

在`shutdownEntrance()`方法中添加广播发送逻辑：

```java
@EventListener
private void shutdownEntrance(ContextClosedEvent event) {
  if (shutdownFlag) {
    logger.warn("event has been handled");
  } else {
    // ============ 新增代码：发送广播清除缓存 ============
    try {
      // 发送广播消息，通知所有其他实例清除Group缓存
      EntranceGroupCacheClearBroadcast broadcast = new EntranceGroupCacheClearBroadcast(
          Sender.getThisInstance(),
          System.currentTimeMillis()
      );
      Sender.send(broadcast); // BroadcastRPCInterceptor会自动广播到所有实例
      logger.info("Successfully sent cache clear broadcast for entrance shutdown: " + Sender.getThisInstance());
    } catch (Exception e) {
      // 广播失败不影响shutdown流程，只记录日志
      logger.error("Failed to send cache clear broadcast", e);
    }
    // ============ 新增代码结束 ============

    if (EntranceConfiguration.ENTRANCE_SHUTDOWN_FAILOVER_CONSUME_QUEUE_ENABLED()) {
      logger.warn("Entrance exit to update and clean all ConsumeQueue task instances");
    }

    logger.warn("Entrance exit to stop all job");
    // ... 原有逻辑
  }
}
```

**步骤4**：注册监听器
```scala
// 在Spring配置中注册
RPCSpringBeanCache.addBroadcastListener(new EntranceGroupCacheClearBroadcastListener())
```

#### 3.3.2 修复内容清单

| 序号 | 修改项 | 文件路径 | 修改内容 |
|:----:|-------|---------|---------|
| 1 | 新增广播协议 | `linkis-entrance/src/main/scala/org/apache/linkis/entrance/protocol/EntranceGroupCacheClearBroadcast.scala` | 创建`EntranceGroupCacheClearBroadcast`广播消息类 |
| 2 | 新增广播监听器 | `linkis-entrance/src/main/scala/org/apache/linkis/entrance/listener/EntranceGroupCacheClearBroadcastListener.scala` | 创建监听器，接收广播后清除缓存 |
| 3 | 添加清除方法 | `EntranceGroupFactory.scala` | 添加`clearAllGroupCache()`公共方法供监听器调用 |
| 4 | 触发广播 | `DefaultEntranceServer.java` - `shutdownEntrance()`方法 | 在Spring `ContextClosedEvent`事件监听器中发送广播 |
| 5 | 注册监听器 | Spring配置 | 通过`RPCSpringBeanCache.addBroadcastListener()`注册监听器 |

#### 3.3.3 涉及文件

- [ ] `linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/scheduler/EntranceGroupFactory.scala` - 添加`clearAllGroupCache()`公共方法
- [ ] `linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/protocol/EntranceGroupCacheClearBroadcast.scala` - 新建广播消息类
- [ ] `linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/listener/EntranceGroupCacheClearBroadcastListener.scala` - 新建广播监听器
- [ ] `linkis-computation-governance/linkis-entrance/src/main/java/org/apache/linkis/entrance/server/DefaultEntranceServer.java` - 在`shutdownEntrance()`方法中触发广播

### 3.4 测试验证计划

#### 3.4.1 单元测试

| 测试项 | 测试内容 | 验证点 |
|-------|---------|-------|
| 广播消息序列化 | 验证广播消息可以正确序列化和反序列化 | 消息传输正确 |
| 缓存清除 | 验证invalidateAll()能正确清除缓存 | 缓存为空 |
| 并发清除 | 验证多线程同时清除缓存的安全性 | 无异常 |

#### 3.4.2 集成测试

| 测试场景 | 测试步骤 | 验证点 |
|---------|---------|-------|
| 单实例offline | 1. 启动4个实例 2. 标记1个offline 3. 提交任务 | 并发数正确为33 |
| 多实例offline | 1. 启动4个实例 2. 标记2个offline 3. 提交任务 | 并发数正确为50 |
| offline后online | 1. offline 2. 等待清除 3. online 4. 提交任务 | 并发数正确更新 |
| 广播失败处理 | 1. 模拟部分实例不可用 2. offline 3. 验证 | 日志记录失败，不影响offline |

#### 3.4.3 回归测试

| 测试范围 | 测试内容 |
|---------|---------|
| 基本功能 | 任务提交、执行、查询 |
| 并发控制 | 多用户并发提交任务 |
| 实例管理 | 实例上线、下线、隔离 |

#### 3.4.4 性能测试

| 测试指标 | 测试方法 | 验收标准 |
|---------|---------|---------|
| 广播延迟 | 发送广播到所有实例接收的时间 | < 5秒 |
| 缓存清除时间 | invalidateAll()执行时间 | < 100ms |
| 并发影响 | 广播期间对任务提交的影响 | 无明显影响 |

### 3.5 发布策略

**发布方式**：灰度发布

**发布步骤**：
1. **第一阶段**：部署到测试环境，验证功能
2. **第二阶段**：部署到1个生产实例，观察24小时
3. **第三阶段**：全量部署

**监控指标**：
- 广播发送成功率
- 广播接收成功率
- 缓存清除次数
- 任务提交成功率

**发布窗口**：低峰期（如凌晨2-4点）

### 3.6 回滚方案

**回滚条件**：
- 出现严重Bug导致任务提交大面积失败
- 广播机制导致性能严重下降
- 出现死锁或资源泄露

**回滚步骤**：
1. 停止新版本部署
2. 回滚代码到上一版本
3. 重启所有Entrance实例
4. 验证系统恢复正常

**回滚时间**：预计10分钟

---

## 4. 【重要】非功能需求

### 4.1 性能需求

| 需求项 | 要求 | 说明 |
|-------|------|------|
| 广播延迟 | < 5秒 | 从发送到所有实例接收 |
| 缓存清除时间 | < 100ms | 单次清除所有缓存 |
| 内存影响 | < 1MB | 新增代码和对象 |
| CPU影响 | < 1% | 广播处理线程 |

### 4.2 可靠性需求

| 需求项 | 要求 | 说明 |
|-------|------|------|
| 广播可靠性 | 至少一次 | 确保消息被处理 |
| 失败处理 | 记录日志，不中断 | 广播失败不影响offline |
| 幂等性 | 支持 | 重复清除缓存无副作用 |

### 4.3 可维护性需求

| 需求项 | 要求 | 说明 |
|-------|------|------|
| 日志记录 | 记录关键操作 | 发送、接收、清除 |
| 监控指标 | 提供指标 | 缓存清除次数、耗时 |
| 配置项 | 可配置 | 缓存过期时间等 |

### 4.4 兼容性需求

| 需求项 | 要求 | 说明 |
|-------|------|------|
| 版本兼容 | 兼容1.3.0+ | 不影响旧版本 |
| 协议兼容 | 向后兼容 | 新增广播协议 |

---

## 5. 【重要】防范措施

### 5.1 测试改进

- [ ] 补充测试用例：Entrance offline场景测试
- [ ] 补充测试用例：广播机制测试
- [ ] 提高覆盖率：EntranceGroupFactory覆盖率从60%提升到80%
- [ ] 增加边界测试：实例频繁上下线、并发offline等

### 5.2 监控改进

- [ ] 增加监控指标：Group缓存大小
- [ ] 增加监控指标：缓存清除次数
- [ ] 增加监控指标：广播发送/接收成功率
- [ ] 完善告警规则：广播失败率超过10%时告警
- [ ] 增加日志输出：记录缓存清除的详细信息

### 5.3 流程改进

- [ ] 代码规范强化：分布式缓存一致性检查清单
- [ ] Code Review检查点：广播机制的正确性、异常处理
- [ ] 发布流程优化：增加灰度验证步骤

### 5.4 文档完善

- [ ] 更新架构文档：说明缓存机制
- [ ] 更新运维手册：说明offline流程
- [ ] 添加故障排查指南：缓存相关问题

---

## 6. 验收标准（三段式）

### 6.1 验收条件

| 验证阶段 | 验收条件 | Given-When-Then |
|:--------:|---------|----------------|
| 【输入验证】 | AC1: Entrance实例offline事件能正确识别 | **Given**: 集群有4个Entrance实例在线  <br> **When**: 管理员通过管理台或API标记Entrance C为offline  <br> **Then**: 系统触发`ContextClosedEvent`或调用`shutdownEntrance()`方法，并准备发送广播 |
| 【处理验证】 | AC2: offline事件触发后，广播消息在5秒内发送到所有实例 | **Given**: Entrance实例offline事件已触发  <br> **When**: 系统构造`EntranceGroupCacheClearBroadcast`广播消息并通过`Sender.send()`发送  <br> **Then**: 所有其他Entrance实例在5秒内接收到广播消息 |
| 【处理验证】 | AC3: 各实例接收广播后，Group缓存被正确清除（invalidateAll） | **Given**: Entrance实例接收到`EntranceGroupCacheClearBroadcast`广播  <br> **When**: `EntranceGroupCacheClearBroadcastListener`监听器调用`groupNameToGroups.invalidateAll()`  <br> **Then**: Guava Cache中的所有ParallelGroup缓存被清除，缓存大小变为0 |
| 【输出验证】 | AC4: 缓存清除后，新任务提交时并发数计算正确（排除offline实例） | **Given**: Entrance Group缓存已被清除  <br> **When**: 用户提交新任务，调用`getOrCreateGroup()`方法  <br> **Then**: 系统重新计算并发数为 100/3 = 33（排除offline实例），任务成功提交 |
| 【输出验证】 | AC5: 广播失败时记录ERROR日志，不影响offline流程和任务执行 | **Given**: 部分Entrance实例不可达  <br> **When**: 发送广播消息时部分实例RPC调用失败  <br> **Then**: 系统记录ERROR日志包含失败实例信息，offline流程继续执行，不抛出异常 |

---

## 7. 【参考】风险识别

### 7.1 技术风险

| 风险 | 可能性 | 影响 | 缓解措施 |
|-----|:------:|:----:|---------|
| 广播消息丢失 | 中 | 高 | 使用至少一次语义；失败重试 |
| 广播性能影响 | 低 | 中 | 异步发送；超时控制 |
| 并发清除冲突 | 低 | 低 | Guava Cache线程安全 |
| 部分实例不可达 | 高 | 低 | 记录失败日志；不中断流程 |

### 7.2 业务风险

| 风险 | 可能性 | 影响 | 缓解措施 |
|-----|:------:|:----:|---------|
| 缓存清除期间任务提交失败 | 低 | 中 | 清除操作快速（<100ms）；异步处理 |
| 正在运行任务受影响 | 极低 | 高 | 只清除缓存，不影响运行中任务 |

### 7.3 发布风险

| 风险 | 可能性 | 影响 | 缓解措施 |
|-----|:------:|:----:|---------|
| 版本不兼容 | 低 | 高 | 向后兼容；灰度发布 |
| 配置错误 | 中 | 中 | 配置校验；文档完善 |

---

## 8. 【参考】关联影响分析

### 8.1 功能模块影响

| 影响模块 | 影响等级 | 影响描述 |
|---------|:--------:|---------|
| Entrance任务调度 | 🟡 重要影响 | 并发数计算逻辑变更 |
| Entrance实例管理 | 🟡 重要影响 | 新增offline广播机制 |
| RPC广播框架 | 🟢 轻微影响 | 复用现有框架，无变更 |

### 8.2 数据模型影响

| 影响项 | 影响等级 | 影响描述 |
|-------|:--------:|---------|
| 数据库 | 🟢 无影响 | 不涉及数据库变更 |
| 缓存 | 🟡 重要影响 | Guava Cache清除策略变更 |

### 8.3 安全与权限影响

| 影响项 | 影响等级 | 影响描述 |
|-------|:--------:|---------|
| RPC通信 | 🟢 轻微影响 | 复用现有RPC鉴权 |
| 实例操作 | 🟢 轻微影响 | 现有权限控制不变 |

### 8.4 用户体验影响

| 影响项 | 影响等级 | 影响描述 |
|-------|:--------:|---------|
| 任务提交 | 🟢 正面影响 | 并发数计算更准确，提升成功率 |
| 实例管理 | 🟢 正面影响 | offline更流畅，无需重启 |

### 8.5 上下游与三方依赖影响

| 影响项 | 影响等级 | 影响描述 |
|-------|:--------:|---------|
| 上游系统 | 🟢 无影响 | 无上游依赖 |
| 下游系统 | 🟢 无影响 | 不影响EngineConn等 |
| 三方依赖 | 🟢 无影响 | 复用Linkis现有组件 |

---

## 附录A：参考资料

### A.1 代码位置

| 模块 | 路径 |
|-----|------|
| EntranceGroupFactory | `linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/scheduler/EntranceGroupFactory.scala` |
| ParallelGroup | `linkis-commons/linkis-scheduler/src/main/scala/org/apache/linkis/scheduler/queue/parallelqueue/ParallelGroup.scala` |
| BroadcastRPCInterceptor | `linkis-commons/linkis-rpc/src/main/scala/org/apache/linkis/rpc/interceptor/common/BroadcastRPCInterceptor.scala` |
| BroadcastProtocol | `linkis-commons/linkis-protocol/src/main/scala/org/apache/linkis/protocol/BroadcastProtocol.scala` |

### A.2 相关文档

- [Linkis Scheduler模块文档](https://linkis.apache.org/zh-CN/docs/latest/architecture/scheduler/)
- [Linkis RPC通信文档](https://linkis.apache.org/zh-CN/docs/latest/architecture/rpc/)
- [Guava Cache官方文档](https://github.com/google/guava/wiki/CachesExplained)

---

**文档结束**
