# Linkis Manager 智能队列选择 - 测试用例文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 需求ID | LINKIS-FEATURE-MANAGER-SECONDARY-QUEUE-001 |
| 需求名称 | Linkis Manager 智能队列选择 |
| 测试版本 | v1.0 |
| 创建时间 | 2026-04-09 |
| 测试类型 | 功能测试、单元测试、集成测试 |
| 测试范围 | 队列选择逻辑、异常处理、配置验证 |

---

## 一、测试概述

### 1.1 测试目标

验证 Linkis Manager 智能队列选择功能的正确性、稳定性和可靠性，确保：
- 功能按照需求文档正确工作
- 异常情况下能够安全降级
- 配置项能够正确生效
- 性能满足要求

### 1.2 测试范围

| 模块 | 测试内容 | 优先级 |
|------|---------|--------|
| 队列选择逻辑 | 根据资源使用率选择队列 | P0 |
| 配置管理 | 功能开关、阈值、引擎类型、Creator 过滤 | P0 |
| 异常处理 | Yarn API 异常、Label 解析异常等 | P0 |
| 引擎集成 | Spark 引擎队列传递 | P0 |
| 多引擎过滤 | Hive、Flink 等引擎过滤 | P1 |
| 性能测试 | 队列查询耗时、并发性能 | P1 |

### 1.3 测试策略

**测试类型**：
- 单元测试：覆盖核心队列选择逻辑
- 集成测试：验证与 Yarn ResourceManager 的集成
- 功能测试：端到端验证队列选择功能
- 异常测试：验证各种异常场景的降级处理
- 性能测试：验证队列查询性能

**测试环境**：
- 开发环境：单元测试
- 测试环境：集成测试和功能测试
- 预发环境：性能测试和压力测试

---

## 二、功能测试用例

### TC001：备用队列可用时选择备用队列

**优先级**：P0

**前置条件**：
- Linkis Manager 服务正常启动
- Yarn ResourceManager 可访问
- 配置了主队列 `root.primary` 和备用队列 `root.backup`
- 功能开关已启用：`wds.linkis.rm.secondary.yarnqueue.enable=true`
- 阈值配置为 0.9

**测试步骤**：
1. 提交 Spark 引擎创建请求，配置如下：
   ```json
   {
     "userCreatorLabel": {
       "user": "testuser",
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
2. 模拟备用队列 `root.backup` 资源使用情况：
   - 已使用内存：72 GB
   - 最大内存：100 GB
   - 使用率：72%
3. 执行队列选择逻辑

**预期结果**：
- 备用队列使用率 72% <= 阈值 90%
- 系统选择备用队列 `root.backup`
- properties 中 `wds.linkis.rm.yarnqueue` 被更新为 `root.backup`
- 日志输出：
  ```
  INFO: Queue selection enabled: primary=root.primary, secondary=root.backup, threshold=0.9
  INFO: Request info: engineType=spark, creator=IDE
  INFO: Resource usage details for queue root.backup (threshold: 90.00%):
  INFO:   Memory: 72.00% ✓ OK
  INFO:   CPU: 45.00% ✓ OK
  INFO:   Instances: 60.00% ✓ OK
  INFO: Secondary queue available: all dimensions under threshold, use secondary queue: root.backup
  INFO: Updated queue config: root.backup
  ```

**测试数据**：
```json
{
  "primaryQueue": "root.primary",
  "secondaryQueue": "root.backup",
  "engineType": "spark",
  "creator": "IDE",
  "threshold": 0.9,
  "usedMemory": 73728,
  "maxMemory": 102400,
  "usedCores": 45,
  "maxCores": 100,
  "usedInstances": 18,
  "maxInstances": 30
}
```

**清理数据**：无需清理

---

### TC002：备用队列不可用时选择主队列（内存超阈值）

**优先级**：P0

**前置条件**：
- Linkis Manager 服务正常启动
- Yarn ResourceManager 可访问
- 配置了主队列 `root.primary` 和备用队列 `root.backup`
- 功能开关已启用
- 阈值配置为 0.9

**测试步骤**：
1. 提交 Spark 引擎创建请求
2. 模拟备用队列 `root.backup` 资源使用情况：
   - 已使用内存：95 GB
   - 最大内存：100 GB
   - 内存使用率：95%
3. 执行队列选择逻辑

**预期结果**：
- 备用队列内存使用率 95% > 阈值 90%
- 系统选择主队列 `root.primary`
- properties 中 `wds.linkis.rm.yarnqueue` 保持为 `root.primary`
- 日志输出：
  ```
  INFO: Resource usage details for queue root.backup (threshold: 90.00%):
  INFO:   Memory: 95.00% ✗ OVER
  INFO:   CPU: 50.00% ✓ OK
  INFO:   Instances: 65.00% ✓ OK
  INFO: Secondary queue not available: Memory over threshold, use primary queue: root.primary
  ```

**测试数据**：
```json
{
  "primaryQueue": "root.primary",
  "secondaryQueue": "root.backup",
  "threshold": 0.9,
  "usedMemory": 97280,
  "maxMemory": 102400,
  "usedCores": 50,
  "maxCores": 100,
  "usedInstances": 19,
  "maxInstances": 30
}
```

---

### TC003：备用队列不可用时选择主队列（CPU超阈值）

**优先级**：P0

**前置条件**：同 TC002

**测试步骤**：
1. 提交 Spark 引擎创建请求
2. 模拟备用队列资源使用情况：
   - 内存使用率：85%（正常）
   - CPU 使用率：95%（超阈值）
   - 实例数使用率：70%（正常）
3. 执行队列选择逻辑

**预期结果**：
- CPU 使用率 95% > 阈值 90%
- 系统选择主队列 `root.primary`
- 日志明确显示 CPU 超过阈值

**测试数据**：
```json
{
  "usedMemory": 87040,
  "maxMemory": 102400,
  "usedCores": 95,
  "maxCores": 100,
  "usedInstances": 21,
  "maxInstances": 30
}
```

---

### TC004：备用队列不可用时选择主队列（实例数超阈值）

**优先级**：P0

**测试步骤**：
1. 提交 Spark 引擎创建请求
2. 模拟备用队列资源使用情况：
   - 内存使用率：85%（正常）
   - CPU 使用率：80%（正常）
   - 实例数使用率：95%（超阈值）
3. 执行队列选择逻辑

**预期结果**：
- 实例数使用率 95% > 阈值 90%
- 系统选择主队列 `root.primary`
- 日志明确显示实例数超过阈值

**测试数据**：
```json
{
  "usedMemory": 87040,
  "maxMemory": 102400,
  "usedCores": 80,
  "maxCores": 100,
  "usedInstances": 28,
  "maxInstances": 30
}
```

---

### TC005：多个维度同时超阈值

**优先级**：P0

**测试步骤**：
1. 提交 Spark 引擎创建请求
2. 模拟备用队列资源使用情况：
   - 内存使用率：95%（超阈值）
   - CPU 使用率：92%（超阈值）
   - 实例数使用率：88%（正常）
3. 执行队列选择逻辑

**预期结果**：
- 内存和 CPU 都超过阈值
- 系统选择主队列 `root.primary`
- 日志显示所有超阈值的维度：
  ```
  INFO: Secondary queue not available: Memory, CPU over threshold, use primary queue: root.primary
  ```

**测试数据**：
```json
{
  "usedMemory": 97280,
  "maxMemory": 102400,
  "usedCores": 92,
  "maxCores": 100,
  "usedInstances": 26,
  "maxInstances": 30
}
```

---

### TC006：未配置备用队列时使用主队列

**优先级**：P0

**前置条件**：
- Linkis Manager 服务正常启动
- 仅配置主队列 `root.primary`
- 未配置备用队列

**测试步骤**：
1. 提交 Spark 引擎创建请求，配置如下：
   ```json
   {
     "properties": {
       "wds.linkis.rm.yarnqueue": "root.primary"
     }
   }
   ```
2. 执行队列选择逻辑

**预期结果**：
- 系统检测到未配置备用队列
- 直接使用主队列 `root.primary`
- 不调用 Yarn API 查询队列资源
- 日志输出：
  ```
  DEBUG: Secondary queue not configured or disabled, use primary queue from properties
  ```

---

### TC007：功能禁用时使用主队列

**优先级**：P0

**前置条件**：
- Linkis Manager 服务正常启动
- 功能开关关闭：`wds.linkis.rm.secondary.yarnqueue.enable=false`

**测试步骤**：
1. 提交 Spark 引擎创建请求，配置了主队列和备用队列
2. 执行队列选择逻辑

**预期结果**：
- 系统检测到功能已禁用
- 直接使用主队列 `root.primary`
- 不调用 Yarn API 查询队列资源
- 不检查引擎类型和 Creator

---

### TC008：Spark 引擎通过过滤

**优先级**：P0

**前置条件**：
- 配置的支持引擎列表：`spark`
- 配置的支持 Creator 列表：`IDE`

**测试步骤**：
1. 提交 Spark 引擎创建请求，Creator 为 IDE
2. 执行队列选择逻辑

**预期结果**：
- 引擎类型 `spark` 在支持列表中
- Creator `IDE` 在支持列表中
- 继续执行队列选择逻辑（查询备用队列资源）

---

### TC009：Hive 引擎被过滤（使用主队列）

**优先级**：P1

**前置条件**：
- 配置的支持引擎列表：`spark`（仅支持 Spark）

**测试步骤**：
1. 提交 Hive 引擎创建请求：
   ```json
   {
     "engineTypeLabel": {
       "engineType": "hive"
     },
     "properties": {
       "wds.linkis.rm.yarnqueue": "root.primary",
       "wds.linkis.rm.secondary.yarnqueue": "root.backup"
     }
   }
   ```
2. 执行队列选择逻辑

**预期结果**：
- 引擎类型 `hive` 不在支持列表中
- 使用主队列 `root.primary`
- 不调用 Yarn API 查询队列资源
- 日志输出：
  ```
  INFO: Engine type 'hive' not in supported list: spark, use primary queue: root.primary
  ```

---

### TC010：SHELL Creator 被过滤（使用主队列）

**优先级**：P1

**前置条件**：
- 配置的支持 Creator 列表：`IDE`（仅支持 IDE）

**测试步骤**：
1. 提交 Spark 引擎创建请求，Creator 为 SHELL
2. 执行队列选择逻辑

**预期结果**：
- Creator `SHELL` 不在支持列表中
- 使用主队列 `root.primary`
- 不调用 Yarn API 查询队列资源
- 日志输出：
  ```
  INFO: Creator 'SHELL' not in supported list: IDE, use primary queue: root.primary
  ```

---

### TC011：阈值边界测试（等于阈值）

**优先级**：P2

**测试步骤**：
1. 提交 Spark 引擎创建请求，阈值配置为 0.9
2. 模拟备用队列资源使用率恰好为 90%
3. 执行队列选择逻辑

**预期结果**：
- 使用率 90% <= 阈值 90%（使用 <= 判断）
- 系统选择备用队列 `root.backup`
- 验证边界条件正确

**测试数据**：
```json
{
  "threshold": 0.9,
  "usedMemory": 92160,
  "maxMemory": 102400,
  "usedCores": 90,
  "maxCores": 100,
  "usedInstances": 27,
  "maxInstances": 30
}
```

---

### TC012：阈值边界测试（略高于阈值）

**优先级**：P2

**测试步骤**：
1. 提交 Spark 引擎创建请求，阈值配置为 0.9
2. 模拟备用队列资源使用率为 90.1%
3. 执行队列选择逻辑

**预期结果**：
- 使用率 90.1% > 阈值 90%
- 系统选择主队列 `root.primary`

**测试数据**：
```json
{
  "threshold": 0.9,
  "usedMemory": 92262,
  "maxMemory": 102400
}
```

---

## 三、边界测试用例

### TC101：资源使用率为 0%（空队列）

**优先级**：P2

**测试步骤**：
1. 提交 Spark 引擎创建请求
2. 模拟备用队列完全空闲（使用率 0%）
3. 执行队列选择逻辑

**预期结果**：
- 使用率 0% <= 阈值 90%
- 系统选择备用队列 `root.backup`
- 验证空队列场景正确处理

**测试数据**：
```json
{
  "usedMemory": 0,
  "maxMemory": 102400,
  "usedCores": 0,
  "maxCores": 100,
  "usedInstances": 0,
  "maxInstances": 30
}
```

---

### TC102：资源使用率为 100%（满队列）

**优先级**：P2

**测试步骤**：
1. 提交 Spark 引擎创建请求
2. 模拟备用队列完全满载（使用率 100%）
3. 执行队列选择逻辑

**预期结果**：
- 使用率 100% > 阈值 90%
- 系统选择主队列 `root.primary`
- 验证满队列场景正确处理

**测试数据**：
```json
{
  "usedMemory": 102400,
  "maxMemory": 102400,
  "usedCores": 100,
  "maxCores": 100,
  "usedInstances": 30,
  "maxInstances": 30
}
```

---

### TC103：最大资源为 0 的异常情况

**优先级**：P2

**测试步骤**：
1. 提交 Spark 引擎创建请求
2. 模拟备用队列最大资源为 0（异常配置）
3. 执行队列选择逻辑

**预期结果**：
- 系统检测到 maxResource 为 0 或 null
- 使用率计算结果为 0.0（避免除以 0）
- 根据 0.0 <= threshold 判断
- 系统选择备用队列（因为 0.0 <= 0.9）
- 日志中有相应的提示信息

**测试数据**：
```json
{
  "usedMemory": 0,
  "maxMemory": 0
}
```

---

### TC104：CPU 核心数为 0 的情况

**优先级**：P2

**测试步骤**：
1. 提交 Spark 引擎创建请求
2. 模拟备用队列 CPU 最大核心数为 0
3. 执行队列选择逻辑

**预期结果**：
- CPU 使用率计算为 0.0（避免除以 0）
- CPU 维度判定为未超过阈值
- 根据其他维度（内存、实例数）进行判断

**测试数据**：
```json
{
  "usedCores": 0,
  "maxCores": 0,
  "usedMemory": 73728,
  "maxMemory": 102400
}
```

---

### TC105：实例数为 0 的情况

**优先级**：P2

**测试步骤**：
1. 提交 Spark 引擎创建请求
2. 模拟备用队列最大实例数为 0
3. 执行队列选择逻辑

**预期结果**：
- 实例数使用率计算为 0.0（避免除以 0）
- 实例数维度判定为未超过阈值
- 根据其他维度进行判断

**测试数据**：
```json
{
  "usedInstances": 0,
  "maxInstances": 0,
  "usedMemory": 73728,
  "maxMemory": 102400
}
```

---

### TC106：阈值为 0.0（最小阈值）

**优先级**：P2

**测试步骤**：
1. 配置阈值为 0.0
2. 提交 Spark 引擎创建请求
3. 模拟备用队列有任何使用（> 0%）
4. 执行队列选择逻辑

**预期结果**：
- 任何使用率 > 0 都会超过阈值 0.0
- 系统选择主队列 `root.primary`
- 验证最小阈值配置正确工作

---

### TC107：阈值为 1.0（最大阈值）

**优先级**：P2

**测试步骤**：
1. 配置阈值为 1.0（100%）
2. 提交 Spark 引擎创建请求
3. 模拟备用队列使用率为 99%
4. 执行队列选择逻辑

**预期结果**：
- 使用率 99% <= 阈值 100%
- 系统选择备用队列 `root.backup`
- 验证最大阈值配置正确工作

---

## 四、异常测试用例

### TC201：Yarn 连接失败（自动降级）

**优先级**：P0

**前置条件**：
- Yarn ResourceManager 服务不可用或网络不通

**测试步骤**：
1. 提交 Spark 引擎创建请求
2. 尝试查询备用队列资源
3. Yarn API 调用失败（ConnectException）

**预期结果**：
- 捕获 ConnectException
- 记录 ERROR 日志，包含完整异常堆栈：
  ```
  ERROR: Exception during queue resource check for secondary queue: root.backup, fallback to primary queue: root.primary
  java.net.ConnectException: Connection refused
  ```
- 使用主队列 `root.primary`
- 引擎继续创建，不受影响
- 任务正常执行

---

### TC202：队列不存在（自动降级）

**优先级**：P0

**测试步骤**：
1. 提交 Spark 引擎创建请求，配置不存在的队列 `nonexistent_queue`
2. 尝试查询队列资源
3. Yarn 返回 404 错误

**预期结果**：
- 捕获队列不存在异常
- 记录 ERROR 日志
- 使用主队列 `root.primary`
- 引擎继续创建

---

### TC203：Label 解析失败（自动降级）

**优先级**：P0

**测试步骤**：
1. 提交引擎创建请求，Labels 格式错误或缺失
2. 尝试解析引擎类型和 Creator
3. Label 解析抛出异常

**预期结果**：
- 捕获 Label 解析异常
- 记录 ERROR 日志：
  ```
  ERROR: Failed to parse labels for queue selection, fallback to primary queue
  ```
- 使用主队列 `root.primary`
- 引擎继续创建

---

### TC204：Yarn API 超时（自动降级）

**优先级**：P1

**前置条件**：
- Yarn ResourceManager 响应缓慢（> 3秒）

**测试步骤**：
1. 提交 Spark 引擎创建请求
2. Yarn API 调用超时
3. 触发超时异常

**预期结果**：
- 捕获超时异常
- 记录 ERROR 日志，包含超时信息
- 使用主队列 `root.primary`
- 引擎继续创建
- 总耗时不超过 4 秒（3 秒超时 + 处理时间）

---

### TC205：配置格式错误（自动降级）

**优先级**：P1

**测试步骤**：
1. 配置阈值为非法值（如 "abc"）
2. 提交引擎创建请求
3. 尝试解析配置

**预期结果**：
- 捕获配置解析异常
- 使用默认配置或降级到主队列
- 记录 ERROR 日志
- 引擎继续创建

---

### TC206：空指针异常（自动降级）

**优先级**：P1

**测试步骤**：
1. 模拟 properties 为 null 的情况
2. 提交引擎创建请求

**预期结果**：
- 代码中有 null 检查，避免空指针
- 如果发生空指针异常，最外层 try-catch 捕获
- 使用主队列 `root.primary`
- 记录 ERROR 日志
- 引擎继续创建

---

### TC207：并发请求异常隔离

**优先级**：P1

**测试步骤**：
1. 同时提交 10 个引擎创建请求
2. 其中部分请求的 Yarn API 调用失败
3. 验证异常隔离

**预期结果**：
- 失败的请求降级到主队列
- 成功的请求正常选择队列
- 各请求互不影响
- 没有异常扩散到其他请求

---

### TC208：properties 为 null

**优先级**：P1

**测试步骤**：
1. 提交引擎创建请求，engineCreateRequest.getProperties() 返回 null
2. 执行队列选择逻辑

**预期结果**：
- 代码中有 null 检查，创建新的 HashMap
- 使用主队列（因为没有配置备用队列）
- 不抛出空指针异常

---

### TC209：primaryQueue 为空字符串

**优先级**：P1

**测试步骤**：
1. 提交引擎创建请求，配置 `wds.linkis.rm.yarnqueue` 为空字符串
2. 执行队列选择逻辑

**预期结果**：
- StringUtils.isBlank() 检测到空字符串
- 跳过智能队列选择
- 使用原始配置（空字符串）
- 日志记录：
  ```
  DEBUG: Secondary queue not configured or disabled, use primary queue from properties
  ```

---

### TC210：secondaryQueue 为空字符串

**优先级**：P1

**测试步骤**：
1. 提交引擎创建请求，配置 `wds.linkis.rm.secondary.yarnqueue` 为空字符串
2. 执行队列选择逻辑

**预期结果**：
- StringUtils.isBlank() 检测到空字符串
- 跳过智能队列选择
- 使用主队列

---

## 五、性能测试用例

### TC301：队列查询耗时测试

**优先级**：P1

**测试目标**：验证 Yarn API 调用耗时满足性能要求

**前置条件**：
- Yarn ResourceManager 正常运行
- 网络延迟正常（< 50ms）

**测试步骤**：
1. 提交 100 次引擎创建请求
2. 记录每次 Yarn API 调用耗时
3. 统计 P50、P95、P99 耗时

**预期结果**：
- P50 耗时 < 200ms
- P95 耗时 < 500ms
- P99 耗时 < 1000ms
- 满足性能要求

**性能指标**：
| 指标 | 目标值 | 实际值 | 是否通过 |
|------|--------|--------|----------|
| P50 耗时 | < 200ms | ___ | ___ |
| P95 耗时 | < 500ms | ___ | ___ |
| P99 耗时 | < 1000ms | ___ | ___ |

---

### TC302：引擎创建总耗时测试

**优先级**：P1

**测试目标**：验证队列选择逻辑不显著增加引擎创建时间

**前置条件**：
- 准备两组测试：
  - 对照组：功能禁用时的引擎创建耗时
  - 实验组：功能启用时的引擎创建耗时

**测试步骤**：
1. 禁用智能队列选择，记录 50 次引擎创建的平均耗时
2. 启用智能队列选择，记录 50 次引擎创建的平均耗时
3. 对比两者差异

**预期结果**：
- 增加的耗时 < 1s
- 增加比例 < 20%

**性能指标**：
| 场景 | 平均耗时 | 增加耗时 | 增加比例 |
|------|---------|---------|----------|
| 功能禁用 | ___ ms | - | - |
| 功能启用 | ___ ms | ___ ms | ___ % |

---

### TC303：并发队列选择测试

**优先级**：P1

**测试目标**：验证并发场景下的性能和正确性

**前置条件**：
- Yarn ResourceManager 正常运行

**测试步骤**：
1. 同时提交 10 个引擎创建请求
2. 观察各请求的队列选择结果
3. 验证并发正确性

**预期结果**：
- 各请求独立进行队列选择
- 没有请求阻塞或超时
- 没有并发安全问题
- 各请求选择正确的队列

**并发指标**：
| 指标 | 目标值 | 实际值 | 是否通过 |
|------|--------|--------|----------|
| 并发请求数 | 10 | 10 | - |
| 成功率 | 100% | ___ % | ___ |
| 平均响应时间 | < 1s | ___ ms | ___ |
| 最大响应时间 | < 3s | ___ ms | ___ |

---

### TC304：高并发压力测试

**优先级**：P2

**测试目标**：验证系统在高并发下的稳定性

**前置条件**：
- Yarn ResourceManager 正常运行

**测试步骤**：
1. 以 50 QPS 的速率提交引擎创建请求
2. 持续 1 分钟
3. 观察系统状态

**预期结果**：
- 系统稳定运行，无崩溃
- 错误率 < 1%
- 平均响应时间 < 2s
- Yarn ResourceManager 无异常

**压力指标**：
| 指标 | 目标值 | 实际值 | 是否通过 |
|------|--------|--------|----------|
| QPS | 50 | 50 | - |
| 持续时间 | 60s | 60s | - |
| 总请求数 | 3000 | ___ | - |
| 错误率 | < 1% | ___ % | ___ |
| 平均响应时间 | < 2s | ___ ms | ___ |

---

## 六、单元测试用例

### TC401：队列选择逻辑 - 正常选择备用队列

**优先级**：P0

**测试方法**：`testQueueSelection_WhenSecondaryAvailable`

**Mock 对象**：
- ExternalResourceService：返回备用队列资源信息

**测试步骤**：
```scala
// Given
val labels = createLabels(engineType = "spark", creator = "IDE")
val properties = new HashMap[String, String]()
properties.put("wds.linkis.rm.yarnqueue", "root.primary")
properties.put("wds.linkis.rm.secondary.yarnqueue", "root.backup")
val request = new EngineCreateRequest()
request.setProperties(properties)

// Mock: 备用队列使用率 72%
val mockQueueInfo = createMockQueueInfo(
  usedMemory = 73728,
  maxMemory = 102400,
  usedCores = 45,
  maxCores = 100,
  usedInstances = 18,
  maxInstances = 30
)
when(externalResourceService.getResource(any(), any(), any()))
  .thenReturn(mockQueueInfo)

// When
requestResourceService.canRequest(labels, resource, request)

// Then
assert(request.getProperties.get("wds.linkis.rm.yarnqueue") == "root.backup")
```

**预期结果**：
- properties 中的队列被更新为 `root.backup`

---

### TC402：队列选择逻辑 - 内存超阈值选择主队列

**优先级**：P0

**测试方法**：`testQueueSelection_WhenMemoryOverThreshold`

**Mock 对象**：
- ExternalResourceService：返回内存使用率 95% 的队列信息

**测试步骤**：
```scala
// Given
val labels = createLabels(engineType = "spark", creator = "IDE")
val properties = new HashMap[String, String]()
properties.put("wds.linkis.rm.yarnqueue", "root.primary")
properties.put("wds.linkis.rm.secondary.yarnqueue", "root.backup")
val request = new EngineCreateRequest()
request.setProperties(properties)

// Mock: 备用队列内存使用率 95%
val mockQueueInfo = createMockQueueInfo(
  usedMemory = 97280,
  maxMemory = 102400
)
when(externalResourceService.getResource(any(), any(), any()))
  .thenReturn(mockQueueInfo)

// When
requestResourceService.canRequest(labels, resource, request)

// Then
assert(request.getProperties.get("wds.linkis.rm.yarnqueue") == "root.primary")
```

**预期结果**：
- properties 中的队列保持为 `root.primary`

---

### TC403：队列选择逻辑 - CPU超阈值选择主队列

**优先级**：P0

**测试方法**：`testQueueSelection_WhenCPUOverThreshold`

**Mock 对象**：返回 CPU 使用率 95% 的队列信息

**预期结果**：
- 选择主队列

---

### TC404：队列选择逻辑 - 实例数超阈值选择主队列

**优先级**：P0

**测试方法**：`testQueueSelection_WhenInstancesOverThreshold`

**Mock 对象**：返回实例数使用率 95% 的队列信息

**预期结果**：
- 选择主队列

---

### TC405：配置验证 - 未配置备用队列

**优先级**：P0

**测试方法**：`testQueueSelection_WhenSecondaryNotConfigured`

**测试步骤**：
```scala
// Given
val properties = new HashMap[String, String]()
properties.put("wds.linkis.rm.yarnqueue", "root.primary")
// 不配置 secondary.yarnqueue
val request = new EngineCreateRequest()
request.setProperties(properties)

// When
requestResourceService.canRequest(labels, resource, request)

// Then
assert(request.getProperties.get("wds.linkis.rm.yarnqueue") == "root.primary")
// 验证没有调用 Yarn API
verify(externalResourceService, never()).getResource(any(), any(), any())
```

**预期结果**：
- 使用主队列
- 没有调用 Yarn API

---

### TC406：配置验证 - 功能禁用

**优先级**：P0

**测试方法**：`testQueueSelection_WhenDisabled`

**Mock 配置**：
- `RMConfiguration.SECONDARY_QUEUE_ENABLED.getValue` 返回 false

**测试步骤**：
```scala
// Given: 功能禁用
when(RMConfiguration.SECONDARY_QUEUE_ENABLED.getValue).thenReturn(false)

val properties = new HashMap[String, String]()
properties.put("wds.linkis.rm.yarnqueue", "root.primary")
properties.put("wds.linkis.rm.secondary.yarnqueue", "root.backup")

// When
requestResourceService.canRequest(labels, resource, request)

// Then
assert(request.getProperties.get("wds.linkis.rm.yarnqueue") == "root.primary")
verify(externalResourceService, never()).getResource(any(), any(), any())
```

**预期结果**：
- 使用主队列
- 没有调用 Yarn API

---

### TC407：引擎类型过滤 - Spark 通过

**优先级**：P0

**测试方法**：`testEngineTypeFilter_Spark_Pass`

**Mock 配置**：
- `RMConfiguration.SECONDARY_QUEUE_ENGINES.getValue` 返回 "spark"

**测试步骤**：
```scala
// Given
val labels = createLabels(engineType = "spark", creator = "IDE")
val properties = new HashMap[String, String]()
properties.put("wds.linkis.rm.yarnqueue", "root.primary")
properties.put("wds.linkis.rm.secondary.yarnqueue", "root.backup")

// When
requestResourceService.canRequest(labels, resource, request)

// Then
// 验证调用了 Yarn API（说明通过了引擎类型过滤）
verify(externalResourceService, times(1)).getResource(any(), any(), any())
```

**预期结果**：
- Spark 引擎通过过滤
- 调用 Yarn API 查询队列资源

---

### TC408：引擎类型过滤 - Hive 被过滤

**优先级**：P0

**测试方法**：`testEngineTypeFilter_Hive_Filtered`

**Mock 配置**：
- `RMConfiguration.SECONDARY_QUEUE_ENGINES.getValue` 返回 "spark"

**测试步骤**：
```scala
// Given
val labels = createLabels(engineType = "hive", creator = "IDE")
val properties = new HashMap[String, String]()
properties.put("wds.linkis.rm.yarnqueue", "root.primary")
properties.put("wds.linkis.rm.secondary.yarnqueue", "root.backup")

// When
requestResourceService.canRequest(labels, resource, request)

// Then
assert(request.getProperties.get("wds.linkis.rm.yarnqueue") == "root.primary")
verify(externalResourceService, never()).getResource(any(), any(), any())
```

**预期结果**：
- Hive 引擎被过滤
- 没有调用 Yarn API
- 使用主队列

---

### TC409：Creator 过滤 - IDE 通过

**优先级**：P0

**测试方法**：`testCreatorFilter_IDE_Pass`

**Mock 配置**：
- `RMConfiguration.SECONDARY_QUEUE_CREATORS.getValue` 返回 "IDE"

**预期结果**：
- IDE Creator 通过过滤
- 调用 Yarn API

---

### TC410：Creator 过滤 - SHELL 被过滤

**优先级**：P0

**测试方法**：`testCreatorFilter_SHELL_Filtered`

**Mock 配置**：
- `RMConfiguration.SECONDARY_QUEUE_CREATORS.getValue` 返回 "IDE"

**测试步骤**：
```scala
// Given
val labels = createLabels(engineType = "spark", creator = "SHELL")
val properties = new HashMap[String, String]()
properties.put("wds.linkis.rm.yarnqueue", "root.primary")
properties.put("wds.linkis.rm.secondary.yarnqueue", "root.backup")

// When
requestResourceService.canRequest(labels, resource, request)

// Then
assert(request.getProperties.get("wds.linkis.rm.yarnqueue") == "root.primary")
verify(externalResourceService, never()).getResource(any(), any(), any())
```

**预期结果**：
- SHELL Creator 被过滤
- 没有调用 Yarn API
- 使用主队列

---

### TC411：异常处理 - Yarn API 异常

**优先级**：P0

**测试方法**：`testExceptionHandling_YarnAPIException`

**Mock 对象**：
- ExternalResourceService 抛出异常

**测试步骤**：
```scala
// Given
when(externalResourceService.getResource(any(), any(), any()))
  .thenThrow(new ConnectException("Connection refused"))

val properties = new HashMap[String, String]()
properties.put("wds.linkis.rm.yarnqueue", "root.primary")
properties.put("wds.linkis.rm.secondary.yarnqueue", "root.backup")

// When
requestResourceService.canRequest(labels, resource, request)

// Then
// 验证降级到主队列，不抛出异常
assert(request.getProperties.get("wds.linkis.rm.yarnqueue") == "root.primary")
```

**预期结果**：
- 捕获异常
- 降级到主队列
- 不向上抛出异常

---

### TC412：异常处理 - Label 解析异常

**优先级**：P0

**测试方法**：`testExceptionHandling_LabelParseException`

**测试步骤**：
```scala
// Given: Labels 为 null 或格式错误
val labels: util.List[Label[_]] = null

val properties = new HashMap[String, String]()
properties.put("wds.linkis.rm.yarnqueue", "root.primary")
properties.put("wds.linkis.rm.secondary.yarnqueue", "root.backup")

// When
requestResourceService.canRequest(labels, resource, request)

// Then
// 验证降级到主队列
assert(request.getProperties.get("wds.linkis.rm.yarnqueue") == "root.primary")
```

**预期结果**：
- 捕获异常
- 降级到主队列
- 不向上抛出异常

---

### TC413：边界值 - 使用率等于阈值

**优先级**：P2

**测试方法**：`testBoundary_UsageEqualsThreshold`

**测试步骤**：
```scala
// Given: 阈值 0.9，使用率 0.9
val mockQueueInfo = createMockQueueInfo(
  usedMemory = 92160,
  maxMemory = 102400
)

// When
requestResourceService.canRequest(labels, resource, request)

// Then
// 使用率 <= 阈值，选择备用队列
assert(request.getProperties.get("wds.linkis.rm.yarnqueue") == "root.backup")
```

**预期结果**：
- 使用率等于阈值时，选择备用队列

---

### TC414：边界值 - 最大资源为 0

**优先级**：P2

**测试方法**：`testBoundary_MaxResourceIsZero`

**测试步骤**：
```scala
// Given: 最大资源为 0
val mockQueueInfo = createMockQueueInfo(
  usedMemory = 0,
  maxMemory = 0
)

// When
requestResourceService.canRequest(labels, resource, request)

// Then
// 使用率计算为 0.0，选择备用队列
assert(request.getProperties.get("wds.linkis.rm.yarnqueue") == "root.backup")
```

**预期结果**：
- 避免除以 0
- 使用率为 0.0
- 选择备用队列

---

## 七、集成测试用例

### TC501：端到端队列选择流程

**优先级**：P0

**测试目标**：验证完整的队列选择流程

**前置条件**：
- Linkis Manager 服务正常启动
- Yarn ResourceManager 可访问
- 配置正确的主队列和备用队列

**测试步骤**：
1. 用户通过 IDE 提交 Spark 任务
2. 配置主队列 `root.primary` 和备用队列 `root.backup`
3. Linkis Manager 接收引擎创建请求
4. 执行队列选择逻辑
5. 查询备用队列资源
6. 根据阈值选择队列
7. 更新 properties
8. Spark 引擎使用选定的队列创建

**预期结果**：
- 备用队列可用时，Spark 引擎使用备用队列创建
- 备用队列不可用时，Spark 引擎使用主队列创建
- 引擎正常创建并执行任务
- Yarn 中可以看到任务提交到正确的队列

**验证点**：
- [ ] Linkis Manager 日志显示队列选择过程
- [ ] Spark 引擎配置的队列正确
- [ ] Yarn ResourceManager 中任务在正确的队列

---

### TC502：多引擎集成测试

**优先级**：P1

**测试目标**：验证不同引擎的队列选择行为

**前置条件**：
- Linkis Manager 服务正常启动
- 配置支持引擎列表：`spark`

**测试步骤**：
1. 提交 Spark 任务，验证执行队列选择
2. 提交 Hive 任务，验证不执行队列选择
3. 提交 Flink 任务，验证不执行队列选择

**预期结果**：
- Spark 任务：执行队列选择，使用选定队列
- Hive 任务：跳过队列选择，使用主队列
- Flink 任务：跳过队列选择，使用主队列

**验证点**：
- [ ] Spark 任务日志有队列选择信息
- [ ] Hive 任务日志显示 "Engine type 'hive' not in supported list"
- [ ] Flink 任务日志显示 "Engine type 'flink' not in supported list"

---

### TC503：多 Creator 集成测试

**优先级**：P1

**测试目标**：验证不同 Creator 的队列选择行为

**前置条件**：
- Linkis Manager 服务正常启动
- 配置支持 Creator 列表：`IDE`

**测试步骤**：
1. 通过 IDE 提交 Spark 任务
2. 通过 NOTEBOOK 提交 Spark 任务
3. 通过 SHELL 提交 Spark 任务

**预期结果**：
- IDE Creator：执行队列选择
- NOTEBOOK Creator：跳过队列选择（不在支持列表）
- SHELL Creator：跳过队列选择（不在支持列表）

---

### TC504：Yarn 故障恢复测试

**优先级**：P1

**测试目标**：验证 Yarn 故障时的降级处理

**前置条件**：
- Linkis Manager 服务正常启动
- Yarn ResourceManager 可以启停

**测试步骤**：
1. 正常状态下提交任务，验证队列选择正常
2. 停止 Yarn ResourceManager
3. 提交任务，验证降级到主队列
4. 重启 Yarn ResourceManager
5. 提交任务，验证队列选择恢复正常

**预期结果**：
- 步骤 1：正常选择队列
- 步骤 3：降级到主队列，引擎创建成功
- 步骤 5：恢复正常队列选择

---

## 八、测试数据准备

### 8.1 Yarn 队列资源数据

**队列配置**：
```json
{
  "primaryQueue": {
    "name": "root.primary",
    "maxMemory": 204800,
    "maxCores": 200,
    "maxInstances": 50
  },
  "secondaryQueue": {
    "name": "root.backup",
    "maxMemory": 102400,
    "maxCores": 100,
    "maxInstances": 30
  }
}
```

**不同使用率场景**：
| 场景 | 已使用内存 | 最大内存 | 使用率 | 预期队列 |
|------|----------|---------|--------|---------|
| 资源充足 | 73728 | 102400 | 72% | 备用队列 |
| 资源紧张 | 97280 | 102400 | 95% | 主队列 |
| 边界值 | 92160 | 102400 | 90% | 备用队列 |
| 空队列 | 0 | 102400 | 0% | 备用队列 |
| 满队列 | 102400 | 102400 | 100% | 主队列 |

### 8.2 配置数据

**系统配置**：
```properties
# 功能开关
wds.linkis.rm.secondary.yarnqueue.enable=true

# 阈值配置
wds.linkis.rm.secondary.yarnqueue.threshold=0.9

# 引擎类型过滤
wds.linkis.rm.secondary.yarnqueue.engines=spark

# Creator 过滤
wds.linkis.rm.secondary.yarnqueue.creators=IDE
```

**用户配置**（任务参数）：
```json
{
  "properties": {
    "wds.linkis.rm.yarnqueue": "root.primary",
    "wds.linkis.rm.secondary.yarnqueue": "root.backup"
  }
}
```

### 8.3 测试用户数据

| 用户名 | Creator | 引擎类型 | 主队列 | 备用队列 |
|--------|---------|---------|--------|----------|
| testuser | IDE | spark | root.primary | root.backup |
| testuser2 | NOTEBOOK | spark | root.primary | root.backup |
| testuser3 | SHELL | spark | root.primary | root.backup |
| testuser4 | IDE | hive | root.primary | root.backup |

---

## 九、验收标准覆盖检查

### 9.1 功能验收标准

| 验收项 | 对应用例 | 覆盖状态 |
|-------|---------|---------|
| AC-001: 队列选择功能可配置 | TC006, TC007 | ✅ |
| AC-002: 资源充足时使用第二队列 | TC001 | ✅ |
| AC-003: 资源紧张时使用主队列 | TC002, TC003, TC004 | ✅ |
| AC-004: 未配置时使用主队列 | TC006 | ✅ |
| AC-005: 阈值可配置 | TC011, TC012 | ✅ |
| AC-006: 功能开关可配置 | TC007 | ✅ |
| AC-007: Spark 引擎生效 | TC001, TC008 | ✅ |
| AC-008: 其他引擎自动过滤 | TC009 | ✅ |
| AC-010: 引擎类型过滤生效 | TC008, TC009 | ✅ |
| AC-011: Creator 过滤生效 | TC010 | ✅ |
| AC-012: 异常时自动降级 | TC201-TC210 | ✅ |
| AC-013: 异常时不影响引擎创建 | TC201-TC210 | ✅ |

**覆盖率**：13/13 (100%) ✅

### 9.2 性能验收标准

| 验收项 | 对应用例 | 覆盖状态 |
|-------|---------|---------|
| AC-PERF-001: 队列查询耗时 P95 < 500ms | TC301 | ✅ |
| AC-PERF-002: 引擎创建总耗时增加 < 1s | TC302 | ✅ |
| AC-PERF-003: Yarn API 调用超时 3s | TC204 | ✅ |

**覆盖率**：3/3 (100%) ✅

### 9.3 并发验收标准

| 验收项 | 对应用例 | 覆盖状态 |
|-------|---------|---------|
| AC-CONC-001: 多任务并发队列选择 | TC303 | ✅ |
| AC-CONC-002: 高并发资源查询 | TC304 | ✅ |

**覆盖率**：2/2 (100%) ✅

---

## 十、测试执行计划

### 10.1 测试阶段

| 阶段 | 测试类型 | 预计时间 | 负责人 |
|------|---------|---------|--------|
| 第一阶段 | 单元测试 | 2 天 | 开发人员 |
| 第二阶段 | 功能测试 | 2 天 | 测试人员 |
| 第三阶段 | 异常测试 | 1 天 | 测试人员 |
| 第四阶段 | 性能测试 | 1 天 | 测试人员 |
| 第五阶段 | 集成测试 | 2 天 | 测试人员 |

**总计**：约 8 个工作日

### 10.2 测试优先级执行顺序

**第一轮**（P0 用例）：
- TC001-TC010：核心功能测试
- TC201-TC203：核心异常测试
- TC401-TC412：核心单元测试

**第二轮**（P1 用例）：
- TC011-TC012：边界测试
- TC204-TC210：异常测试
- TC301-TC303：性能测试
- TC501-TC504：集成测试

**第三轮**（P2 用例）：
- TC101-TC107：边界测试
- TC304：高并发测试
- TC413-TC414：边界单元测试

### 10.3 测试环境

| 环境 | 用途 | 状态 |
|------|------|------|
| 开发环境 | 单元测试 | ✅ 就绪 |
| 测试环境 | 功能、异常测试 | ⏳ 待准备 |
| 预发环境 | 性能、集成测试 | ⏳ 待准备 |

---

## 十一、缺陷记录模板

### 缺陷报告

| 缺陷ID | 标题 | 严重程度 | 状态 | 发现用例 |
|--------|------|---------|------|---------|
| BUG-001 | [待填写] | [P0/P1/P2/P3] | [Open/Fixed/Closed] | TC___ |

**严重程度定义**：
- P0：阻塞性缺陷，影响核心功能
- P1：严重缺陷，影响重要功能
- P2：一般缺陷，影响次要功能
- P3：轻微缺陷，界面或提示问题

---

## 十二、测试总结报告模板

### 12.1 测试执行统计

| 统计项 | 数值 |
|--------|------|
| 用例总数 | ___ |
| 执行用例数 | ___ |
| 通过用例数 | ___ |
| 失败用例数 | ___ |
| 阻塞用例数 | ___ |
| 用例通过率 | ___ % |

### 12.2 缺陷统计

| 严重程度 | 数量 | 已修复 | 未修复 |
|---------|------|--------|--------|
| P0 | ___ | ___ | ___ |
| P1 | ___ | ___ | ___ |
| P2 | ___ | ___ | ___ |
| P3 | ___ | ___ | ___ |
| **总计** | ___ | ___ | ___ |

### 12.3 测试结论

**通过标准**：
- P0 用例 100% 通过
- P1 用例 >= 95% 通过
- P2 用例 >= 90% 通过
- 无 P0、P1 级未修复缺陷

**测试结论**：
- [ ] ✅ 通过 - 可以发布
- [ ] ⚠️ 有条件通过 - 需修复部分缺陷后发布
- [ ] ❌ 不通过 - 需要重新测试

---

## 附录

### A. 测试用例编号规则

**编号格式**：TC[类型编号][用例序号]

**类型编号**：
- 0xx：功能测试
- 1xx：边界测试
- 2xx：异常测试
- 3xx：性能测试
- 4xx：单元测试
- 5xx：集成测试

### B. 参考文档

- 需求文档：`docs/project-knowledge/requirements/linkis_manager_secondary_queue_需求.md`
- 设计文档：`docs/project-knowledge/design/linkis_manager_secondary_queue_设计.md`
- 代码实现：`linkis-computation-governance/linkis-manager/linkis-application-manager/src/main/scala/org/apache/linkis/manager/rm/service/RequestResourceService.scala`
- 配置类：`linkis-computation-governance/linkis-manager/linkis-manager-common/src/main/java/org/apache/linkis/manager/common/conf/RMConfiguration.java`

### C. 术语表

| 术语 | 说明 |
|------|------|
| 主队列（Primary Queue） | 用户配置的主要队列 |
| 备用队列（Secondary Queue） | 第二队列，资源充足时优先使用 |
| 阈值（Threshold） | 触发队列切换的资源使用率临界值 |
| Creator | Linkis 任务创建来源标识（IDE、NOTEBOOK、CLIENT、SHELL 等） |
| Yarn ResourceManager | Hadoop Yarn 资源管理器 |
| Linkis Manager | Linkis 资源管理服务 |

---

**文档结束**
