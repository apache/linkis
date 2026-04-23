# Entrance Offline Cache Fix - 测试用例文档

## 文档信息
- **需求类型**: FIX（Bug修复）
- **需求ID**: entrance-offline-cache-fix
- **文档版本**: v1.0
- **创建日期**: 2026-04-02
- **相关文档**:
  - [需求文档](../requirements/entrance-offline-cache-fix_需求.md)
  - [设计文档](../design/entrance-offline-cache-fix_设计.md)
  - [Feature文件](../features/entrance-offline-cache-fix.feature)

---

## 📋 测试概述

### 测试目标
验证多Entrance实例环境下，Entrance实例offline时Group缓存能被正确清除，确保并发数计算准确。

### 测试范围
- **Bug复现**: 验证修复前的问题确实存在
- **修复验证**: 验证修复后的正确行为
- **回归测试**: 确保修复不影响其他功能
- **性能测试**: 验证修复的性能影响在可接受范围

### 测试环境要求
- **最低配置**: 4个Entrance实例（A/B/C/D）
- **网络要求**: RPC网络正常
- **数据库**: Linkis元数据库正常
- **依赖服务**: Eureka服务发现、Configuration Server

### 核心验收标准（AC）
| AC编号 | 验收条件 | 优先级 |
|:------:|---------|:------:|
| AC1 | Entrance实例offline事件能正确识别 | P0 |
| AC2 | offline事件触发后，广播消息在5秒内发送到所有实例 | P0 |
| AC3 | 各实例接收广播后，Group缓存被正确清除（invalidateAll） | P0 |
| AC4 | 缓存清除后，新任务提交时并发数计算正确（排除offline实例） | P0 |
| AC5 | 广播失败时记录ERROR日志，不影响offline流程 | P1 |

---

## 📊 Feature覆盖率统计

### Feature文件覆盖
| Feature文件 | Scenario总数 | 已生成测试用例 | 覆盖率 | 状态 |
|------------|-------------|--------------|-------|------|
| entrance-offline-cache-fix.feature | 15 | 15 | 100% | ✅ 完全覆盖 |

### 覆盖详情
- ✅ Scenario 1: 复现Bug - Entrance offline后并发数未更新 → TC001
- ✅ Scenario 2: 复现Bug - 缓存50分钟后自动更新 → TC002
- ✅ Scenario 3: 修复后 - Entrance offline时缓存立即清除 → TC003
- ✅ Scenario 4: 修复后 - offline后新任务并发数正确 → TC004
- ✅ Scenario 5: 不同数量的Entrance offline后并发数计算正确（3个数据组合） → TC005-1/2/3
- ✅ Scenario 6: 正常情况下任务提交仍正常 → TC006
- ✅ Scenario 7: 实例频繁上下线场景 → TC007
- ✅ Scenario 8: 多个实例同时offline → TC008
- ✅ Scenario 9: 广播失败不影响offline流程 → TC009
- ✅ Scenario 10-12: 性能测试（广播延迟、缓存清除性能、广播期间任务提交） → TC010/011/012
- ✅ Scenario 13-15: 监控与日志验证 → TC013/014/015

### 验收标准覆盖检查
| 验收标准 | 测试用例覆盖 | 状态 |
|---------|------------|------|
| AC1: Entrance实例offline事件能正确识别 | TC003, TC009 | ✅ |
| AC2: 广播消息在5秒内发送到所有实例 | TC003, TC010 | ✅ |
| AC3: Group缓存被正确清除 | TC003, TC011 | ✅ |
| AC4: 并发数计算正确（排除offline实例） | TC004, TC005-1/2/3 | ✅ |
| AC5: 广播失败记录ERROR日志，不影响offline流程 | TC009, TC015 | ✅ |

**验收标准覆盖率**: 5/5 (100%)

---

## 🧪 测试用例详情

### 第一部分：Bug复现测试（2个用例）

#### TC001：复现Bug - Entrance offline后并发数未更新

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 1

**标签**: @bug @reproduction @skip

**测试类型**: 功能测试

**前置条件**:
- 集群有4个Entrance实例: A, B, C, D
- Entrance C上有正在运行的任务
- 已提交任务建立了Group缓存（并发数为25）

**测试步骤**:
1. 检查当前Group缓存，确认并发数为25（100/4）
2. 管理员通过管理台或API将Entrance C标记为offline
   ```
   GET /entrance/operation/label/markoffline?instance=entrance-2
   ```
3. 在Entrance A上提交新任务
4. 观察任务提交结果和并发数

**预期结果**:
- 系统仍按4个实例计算并发数（25个槽位）
- 任务应该提交失败，提示"并发数已满"
- Group缓存未清除，缓存大小仍为1

**测试数据**:
```json
{
  "instances": ["entrance-0", "entrance-1", "entrance-2", "entrance-3"],
  "offlineInstance": "entrance-2",
  "userConcurrency": 100,
  "expectedConcurrency": 25,
  "actualConcurrency": 25
}
```

**优先级**: P1
**执行频率**: 每次修复前验证
**备注**: 此测试用于验证Bug确实存在，修复后应该失败

---

#### TC002：复现Bug - 缓存50分钟后自动更新

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 2

**标签**: @bug @reproduction @skip

**测试类型**: 功能测试

**前置条件**:
- 集群有4个Entrance实例: A, B, C, D
- Entrance C已offline超过50分钟
- Group缓存已过期

**测试步骤**:
1. 等待50分钟，确保Group缓存过期
   ```
   默认过期时间: wds.linkis.consumer.group.expire.time = 50分钟
   ```
2. 提交新任务到Entrance A
3. 观察任务提交结果和并发数

**预期结果**:
- 系统按3个实例计算并发数（33个槽位）
- 任务应该成功提交
- 新的Group缓存被创建

**测试数据**:
```json
{
  "instances": ["entrance-0", "entrance-1", "entrance-2", "entrance-3"],
  "offlineInstance": "entrance-2",
  "userConcurrency": 100,
  "cacheExpireTime": 50,
  "expectedConcurrency": 33
}
```

**优先级**: P2
**执行频率**: 仅在需要验证缓存过期机制时执行
**备注**: 此测试验证缓存过期机制仍然有效

---

### 第二部分：修复验证测试（4个用例）

#### TC003：修复后 - Entrance offline时缓存立即清除

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 3

**标签**: @bugfix @critical @smoke

**测试类型**: 接口测试

**接口信息**:
- HTTP方法：GET
- 接口路径：/entrance/operation/label/markoffline
- 接口描述：标记Entrance实例为offline

**前置条件**:
- 集群有4个Entrance实例: A, B, C, D
- Entrance C上有正在运行的任务
- 已提交任务建立了Group缓存（并发数为25）

**测试步骤**:
1. 检查Entrance A/B/D的Group缓存大小，确认缓存存在
2. 通过API将Entrance C标记为offline
   ```bash
   curl -X GET "http://{gateway}/entrance/operation/label/markoffline" \
     -H "Authorization: Bearer {token}" \
     -d "instance=entrance-2"
   ```
3. 在5秒内检查所有实例的日志，确认广播消息被接收
4. 检查各实例的Group缓存，确认缓存已被清除

**预期结果**:
- 所有Entrance实例（A/B/D）应该收到广播消息
- 各实例的Group缓存应该在5秒内清除
- 缓存清除应该被记录到日志
- 日志应包含: "Received cache clear broadcast from entrance-2"

**测试数据**:
```json
{
  "offlineInstance": "entrance-2",
  "onlineInstances": ["entrance-0", "entrance-1", "entrance-3"],
  "expectedBroadcastRecipients": 3,
  "expectedCacheSizeAfterClear": 0,
  "expectedMaxWaitTime": 5000
}
```

**优先级**: P0
**测试类型**: 接口测试
**覆盖场景**: 正向场景 - 修复核心功能验证

---

#### TC004：修复后 - offline后新任务并发数正确

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 4

**标签**: @bugfix @critical

**测试类型**: 功能测试

**前置条件**:
- 集群有4个Entrance实例: A, B, C, D
- Entrance C已offline
- Group缓存已清除

**测试步骤**:
1. 确认Entrance C已offline（通过管理台或API查询）
2. 确认所有实例的Group缓存已清除（大小为0）
3. 用户提交新任务到Entrance A
4. 观察任务提交结果和并发数

**预期结果**:
- 系统按3个实例计算并发数（33个槽位）
- 任务应该成功提交
- 不应该出现并发数已满错误
- 新的Group缓存被创建，maxRunningJobs=33

**测试数据**:
```json
{
  "instances": ["entrance-0", "entrance-1", "entrance-2", "entrance-3"],
  "offlineInstance": "entrance-2",
  "userConcurrency": 100,
  "onlineCount": 3,
  "expectedConcurrency": 33,
  "expectedResult": "success"
}
```

**优先级**: P0
**测试类型**: 功能测试
**覆盖场景**: 正向场景 - 业务逻辑验证

---

#### TC005-1：不同数量的Entrance offline后并发数计算正确（1个offline）

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 5

**标签**: @bugfix

**测试类型**: 功能测试

**前置条件**:
- 集群有4个Entrance实例
- 有1个Entrance实例已offline
- Group缓存已清除

**测试步骤**:
1. 标记Entrance C为offline
2. 等待广播消息并确认缓存清除
3. 提交新任务
4. 检查并发数计算结果

**预期结果**:
- 系统按3个实例计算并发数
- 每个实例并发数应该是33

**测试数据**:
```json
{
  "offlineCount": 1,
  "onlineCount": 3,
  "userConcurrency": 100,
  "expectedParallelismPerInstance": 33
}
```

**优先级**: P1
**测试类型**: 功能测试
**备注**: 参数化测试场景1

---

#### TC005-2：不同数量的Entrance offline后并发数计算正确（2个offline）

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 5

**标签**: @bugfix

**测试类型**: 功能测试

**前置条件**:
- 集群有4个Entrance实例
- 有2个Entrance实例已offline
- Group缓存已清除

**测试步骤**:
1. 标记Entrance C和D为offline
2. 等待广播消息并确认缓存清除
3. 提交新任务
4. 检查并发数计算结果

**预期结果**:
- 系统按2个实例计算并发数
- 每个实例并发数应该是50

**测试数据**:
```json
{
  "offlineCount": 2,
  "onlineCount": 2,
  "userConcurrency": 100,
  "expectedParallelismPerInstance": 50
}
```

**优先级**: P1
**测试类型**: 功能测试
**备注**: 参数化测试场景2

---

#### TC005-3：不同数量的Entrance offline后并发数计算正确（3个offline）

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 5

**标签**: @bugfix

**测试类型**: 功能测试

**前置条件**:
- 集群有4个Entrance实例
- 有3个Entrance实例已offline
- Group缓存已清除

**测试步骤**:
1. 标记Entrance B、C、D为offline
2. 等待广播消息并确认缓存清除
3. 提交新任务
4. 检查并发数计算结果

**预期结果**:
- 系统按1个实例计算并发数
- 每个实例并发数应该是100

**测试数据**:
```json
{
  "offlineCount": 3,
  "onlineCount": 1,
  "userConcurrency": 100,
  "expectedParallelismPerInstance": 100
}
```

**优先级**: P1
**测试类型**: 功能测试
**备注**: 参数化测试场景3

---

### 第三部分：回归测试（4个用例）

#### TC006：正常情况下任务提交仍正常

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 6

**标签**: @regression @critical

**测试类型**: 功能测试

**前置条件**:
- 集群有4个Entrance实例: A, B, C, D
- 所有实例都在线

**测试步骤**:
1. 确认所有4个Entrance实例都在线
2. 提交新任务
3. 观察任务提交结果和并发数

**预期结果**:
- 系统按4个实例计算并发数（25个槽位）
- 任务应该成功提交
- 行为应该与修复前完全一致

**测试数据**:
```json
{
  "instances": ["entrance-0", "entrance-1", "entrance-2", "entrance-3"],
  "onlineCount": 4,
  "userConcurrency": 100,
  "expectedParallelismPerInstance": 25
}
```

**优先级**: P0
**测试类型**: 功能测试
**覆盖场景**: 回归测试 - 确保修复不影响正常流程

---

#### TC007：实例频繁上下线场景

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 7

**标签**: @regression

**测试类型**: 功能测试

**前置条件**:
- 集群有4个Entrance实例

**测试步骤**:
1. 将Entrance C标记为offline
2. 立即将Entrance C标记为online
3. 再次将Entrance C标记为offline
4. 提交新任务
5. 检查缓存状态和并发数

**预期结果**:
- 缓存应该正确更新
- 并发数应该反映当前在线实例数量
- 不应该出现异常或错误日志
- 每次offline都应该发送广播

**测试数据**:
```json
{
  "instances": ["entrance-0", "entrance-1", "entrance-2", "entrance-3"],
  "operationSequence": ["offline", "online", "offline"],
  "targetInstance": "entrance-2",
  "expectedBroadcastCount": 2
}
```

**优先级**: P1
**测试类型**: 功能测试
**覆盖场景**: 边界场景 - 频繁状态变更

---

#### TC008：多个实例同时offline

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 8

**标签**: @regression

**测试类型**: 功能测试

**前置条件**:
- 集群有4个Entrance实例: A, B, C, D

**测试步骤**:
1. 同时将Entrance C和D标记为offline（在10秒内完成）
2. 检查各实例收到的广播消息数量
3. 检查Group缓存状态
4. 提交新任务并检查并发数

**预期结果**:
- 所有实例应该收到2条广播消息
- Group缓存应该被清除2次（幂等操作，无副作用）
- 并发数应该按2个实例计算（50个槽位）
- 不应该出现异常或错误

**测试数据**:
```json
{
  "instances": ["entrance-0", "entrance-1", "entrance-2", "entrance-3"],
  "offlineInstances": ["entrance-2", "entrance-3"],
  "expectedBroadcastCount": 2,
  "onlineCount": 2,
  "expectedParallelismPerInstance": 50
}
```

**优先级**: P1
**测试类型**: 功能测试
**覆盖场景**: 边界场景 - 并发offline

---

#### TC009：广播失败不影响offline流程

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 9

**标签**: @regression @critical

**测试类型**: 功能测试

**前置条件**:
- 集群有4个Entrance实例: A, B, C, D
- Entrance D的RPC服务不可用（模拟网络故障）

**测试步骤**:
1. 模拟Entrance D的RPC通信失败（如停止网络或防火墙阻断）
2. 管理员将Entrance C标记为offline
3. 检查Entrance A和B是否收到广播消息
4. 检查Entrance D的通信失败日志
5. 验证Entrance C的offline流程是否成功完成

**预期结果**:
- Entrance A和B应该收到广播消息并清除缓存
- Entrance D通信失败应该被记录到ERROR日志
- Entrance C的offline流程应该成功完成
- 不应该抛出异常或中断offline流程
- 日志应包含: "Failed to send cache clear broadcast"

**测试数据**:
```json
{
  "instances": ["entrance-0", "entrance-1", "entrance-2", "entrance-3"],
  "offlineInstance": "entrance-2",
  "unreachableInstance": "entrance-3",
  "expectedBroadcastRecipients": 2,
  "expectedErrorLog": true,
  "expectedOfflineSuccess": true
}
```

**优先级**: P0
**测试类型**: 功能测试
**覆盖场景**: 异常场景 - 部分实例不可达

---

### 第四部分：性能测试（3个用例）

#### TC010：广播延迟测试

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 10

**标签**: @performance

**测试类型**: 性能测试

**前置条件**:
- 集群有4个Entrance实例
- RPC网络正常

**测试步骤**:
1. 记录当前时间戳T1
2. 触发Entrance offline广播
3. 在所有实例上记录接收广播的时间戳T2
4. 计算广播端到端延迟 = T2 - T1

**预期结果**:
- 所有实例应该在5秒内收到广播
- 广播总耗时应该小于5秒
- 平均延迟应该小于2秒

**测试数据**:
```json
{
  "instances": 4,
  "expectedMaxLatency": 5000,
  "expectedAverageLatency": 2000,
  "measurementPoints": ["sendTime", "receiveTime"]
}
```

**优先级**: P1
**测试类型**: 性能测试
**性能指标**: 广播端到端延迟 < 5秒

---

#### TC011：缓存清除性能测试

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 11

**标签**: @performance

**测试类型**: 性能测试

**前置条件**:
- 缓存中有5000个Group（接近最大容量）

**测试步骤**:
1. 预先创建5000个Group对象并放入缓存
2. 记录当前时间戳T1
3. 执行缓存清除操作 `clearAllGroupCache()`
4. 记录完成时间戳T2
5. 计算清除耗时 = T2 - T1
6. 监控CPU使用率

**预期结果**:
- 清除操作应该在100ms内完成
- CPU使用率不应该显著增加（< 5%）
- 不应该出现内存泄漏

**测试数据**:
```json
{
  "cacheSize": 5000,
  "expectedMaxTime": 100,
  "expectedCpuIncrease": 5,
  "expectedMemoryIncrease": 0
}
```

**优先级**: P1
**测试类型**: 性能测试
**性能指标**: 缓存清除耗时 < 100ms

---

#### TC012：广播期间任务提交不受影响

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 12

**标签**: @performance

**测试类型**: 性能测试

**前置条件**:
- 集群有4个Entrance实例
- 系统正在处理正常的任务提交

**测试步骤**:
1. 开始发送广播消息
2. 同时在广播发送期间提交10个任务
3. 记录每个任务的响应时间
4. 对比无广播时的基准响应时间

**预期结果**:
- 任务提交应该正常处理
- 响应时间不应该明显增加（< 10%差异）
- 所有任务都应该成功
- 不应该出现超时或失败

**测试数据**:
```json
{
  "concurrentTasks": 10,
  "expectedResponseTimeIncrease": 10,
  "expectedSuccessRate": 100,
  "baselineResponseTime": 100
}
```

**优先级**: P2
**测试类型**: 性能测试
**性能指标**: 响应时间增长 < 10%

---

### 第五部分：监控与日志验证（3个用例）

#### TC013：广播发送日志记录

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 13

**标签**: @monitoring

**测试类型**: 功能测试

**前置条件**:
- 集群有4个Entrance实例
- 日志级别为INFO

**测试步骤**:
1. 触发Entrance offline广播
2. 检查offline实例的日志文件
3. 搜索广播发送相关的日志

**预期结果**:
- 日志应该记录"Successfully sent cache clear broadcast"
- 日志应该包含offline实例信息
- 日志应该包含时间戳
- 日志级别应该为INFO

**测试数据**:
```json
{
  "expectedLogPattern": "Successfully sent cache clear broadcast",
  "expectedLogLevel": "INFO",
  "expectedFields": ["instance", "timestamp"]
}
```

**优先级**: P1
**测试类型**: 功能测试
**覆盖场景**: 监控验证 - 日志完整性

---

#### TC014：广播接收日志记录

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 14

**标签**: @monitoring

**测试类型**: 功能测试

**前置条件**:
- 集群有4个Entrance实例
- 日志级别为INFO

**测试步骤**:
1. 触发Entrance offline广播
2. 检查所有在线实例的日志文件
3. 搜索广播接收相关的日志

**预期结果**:
- 每个实例应该记录"Received cache clear broadcast"
- 日志应该记录缓存清除操作
- 日志应该包含广播来源实例信息
- 日志应该包含时间戳

**测试数据**:
```json
{
  "expectedLogPattern": "Received cache clear broadcast",
  "expectedLogPattern2": "Successfully cleared all Group cache",
  "expectedLogLevel": "INFO",
  "expectedFields": ["from", "timestamp", "cacheSize"]
}
```

**优先级**: P1
**测试类型**: 功能测试
**覆盖场景**: 监控验证 - 日志完整性

---

#### TC015：广播失败日志记录

**来源**: Feature文件 - entrance-offline-cache-fix.feature, Scenario 15

**标签**: @monitoring

**测试类型**: 功能测试

**前置条件**:
- 集群有4个Entrance实例
- 某个实例不可达（RPC通信失败）

**测试步骤**:
1. 模拟某个实例的RPC通信失败
2. 触发Entrance offline广播
3. 检查offline实例的日志文件
4. 搜索广播失败相关的日志

**预期结果**:
- 应该记录"Broadcast to <instance> failed"的ERROR日志
- 日志应该包含失败原因（如Connection refused）
- 日志级别应该为ERROR
- offline流程应该继续执行

**测试数据**:
```json
{
  "expectedLogPattern": "Failed to send cache clear broadcast",
  "expectedLogLevel": "ERROR",
  "expectedFields": ["exception", "failedInstance"],
  "expectedOfflineSuccess": true
}
```

**优先级**: P1
**测试类型**: 功能测试
**覆盖场景**: 监控验证 - 异常日志

---

## 📈 测试用例统计

### 按测试类型分类
| 测试类型 | 用例数量 | 占比 |
|---------|---------|:----:|
| 功能测试 | 9 | 60% |
| 接口测试 | 1 | 7% |
| 性能测试 | 3 | 20% |
| 回归测试 | 2 | 13% |
| **总计** | **15** | **100%** |

### 按优先级分类
| 优先级 | 用例数量 | 占比 |
|-------|---------|:----:|
| P0 | 5 | 33% |
| P1 | 8 | 53% |
| P2 | 2 | 14% |
| **总计** | **15** | **100%** |

### 按场景分类
| 场景分类 | 用例数量 |
|---------|---------|
| Bug复现 | 2 |
| 修复验证 | 4 |
| 回归测试 | 4 |
| 性能测试 | 3 |
| 监控验证 | 3 |

---

## 🎯 测试执行计划

### 阶段1：Bug复现（修复前）
**目标**: 验证Bug确实存在
- TC001: 复现Bug - Entrance offline后并发数未更新
- TC002: 复现Bug - 缓存50分钟后自动更新

**预期结果**: TC001应该失败（符合Bug行为），TC002应该成功

### 阶段2：修复验证（修复后）
**目标**: 验证修复生效
- TC003: Entrance offline时缓存立即清除
- TC004: offline后新任务并发数正确
- TC005-1/2/3: 不同数量的Entrance offline后并发数计算正确

**预期结果**: 所有测试应该成功

### 阶段3：回归测试
**目标**: 确保修复不影响其他功能
- TC006: 正常情况下任务提交仍正常
- TC007: 实例频繁上下线场景
- TC008: 多个实例同时offline
- TC009: 广播失败不影响offline流程

**预期结果**: 所有测试应该成功

### 阶段4：性能测试
**目标**: 验证修复的性能影响
- TC010: 广播延迟测试
- TC011: 缓存清除性能测试
- TC012: 广播期间任务提交不受影响

**预期结果**: 所有性能指标应该满足要求

### 阶段5：监控验证
**目标**: 验证监控和日志完整性
- TC013: 广播发送日志记录
- TC014: 广播接收日志记录
- TC015: 广播失败日志记录

**预期结果**: 所有日志应该正确记录

---

## 📋 测试数据准备

### 环境配置
```properties
# Entrance配置
wds.linkis.consumer.group.cache.capacity=5000
wds.linkis.consumer.group.expire.time=50

# 用户并发配置
wds.linkis.entrance.running.job=100

# 实例配置
entrance.instances=4
```

### 测试用户
```json
{
  "users": [
    {"username": "testuser1", "creator": "IDE"},
    {"username": "testuser2", "creator": "IDE"},
    {"username": "testuser3", "creator": "SCRIPT"}
  ]
}
```

### 测试任务
```json
{
  "taskTypes": ["SQL", "PYTHON", "SHELL"],
  "taskDurations": ["short", "medium", "long"]
}
```

---

## 🚨 风险与注意事项

### 测试风险
| 风险 | 影响 | 缓解措施 |
|-----|-----|---------|
| 测试环境不稳定 | 测试结果不可靠 | 确保测试环境独立，避免与开发环境共享 |
| RPC网络波动 | 广播测试失败 | 多次执行测试，取平均值 |
| 缓存过期时间过长 | 测试耗时过久 | 可以临时调短过期时间用于测试 |

### 测试注意事项
1. **测试隔离**: 每个测试用例执行前应该清理环境，确保测试独立性
2. **并发控制**: TC008（多个实例同时offline）需要精确控制时间，建议使用脚本
3. **日志检查**: 监控验证类测试需要检查所有实例的日志，建议使用日志收集工具
4. **性能基准**: TC010/011/012需要建立性能基准，建议在相同环境多次执行
5. **kill -9场景**: 设计文档中提到kill -9无法触发广播，这是预期行为，不需要测试用例覆盖

### 清理数据
每个测试用例执行后需要清理：
1. 清除所有Group缓存
2. 重置所有实例为online状态
3. 清理测试任务
4. 归档测试日志

---

## 📝 测试报告模板

### 测试执行摘要
- **测试日期**: YYYY-MM-DD
- **测试环境**: 测试环境 / 生产环境
- **测试人员**: XXX
- **测试版本**: v1.0

### 测试结果统计
| 统计项 | 数值 |
|-------|-----|
| 总用例数 | 15 |
| 执行用例数 | 15 |
| 通过用例数 | 15 |
| 失败用例数 | 0 |
| 通过率 | 100% |

### 缺陷列表
| 缺陷ID | 缺陷描述 | 严重程度 | 状态 |
|-------|---------|:-------:|-----|
| - | - | - | - |

---

## 附录

### A. 相关文档
- [需求文档](../requirements/entrance-offline-cache-fix_需求.md)
- [设计文档](../design/entrance-offline-cache-fix_设计.md)
- [Feature文件](../features/entrance-offline-cache-fix.feature)

### B. 代码位置
| 文件 | 路径 |
|-----|------|
| EntranceGroupFactory | linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/scheduler/EntranceGroupFactory.scala |
| EntranceLabelRestfulApi | linkis-computation-governance/linkis-entrance/src/main/java/org/apache/linkis/entrance/restful/EntranceLabelRestfulApi.java |
| EntranceGroupCacheClearBroadcast | linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/protocol/EntranceGroupCacheClearBroadcast.scala |
| EntranceGroupCacheClearBroadcastListener | linkis-computation-governance/linkis-entrance/src/main/scala/org/apache/linkis/entrance/listener/EntranceGroupCacheClearBroadcastListener.scala |

### C. 测试命令参考
```bash
# 标记实例为offline
curl -X GET "http://{gateway}/entrance/operation/label/markoffline?instance={instance}" \
  -H "Authorization: Bearer {token}"

# 标记实例为online
curl -X GET "http://{gateway}/entrance/operation/label/markonline?instance={instance}" \
  -H "Authorization: Bearer {token}"

# 查询实例状态
curl -X GET "http://{gateway}/entrance/operation/label/status?instance={instance}" \
  -H "Authorization: Bearer {token}"

# 提交测试任务
curl -X POST "http://{gateway}/entrance/submit" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"executeApplication":"spark","executionCode":"select 1","params":{}}'
```

---

**文档结束**
