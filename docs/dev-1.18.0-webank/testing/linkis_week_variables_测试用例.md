# Linkis SQL 查询增加周变量 - 测试用例文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 需求ID | LINKIS-FEATURE-WEEK-VAR-001 |
| 需求名称 | Linkis SQL 查询增加周变量 |
| 测试类型 | 单元测试 + 集成测试 + 功能测试 |
| 测试版本 | 1.0 |
| 创建时间 | 2026-04-09 |
| 文档状态 | 待执行 |

**关联需求文档**：`docs/project-knowledge/requirements/linkis_week_variables_需求.md`
**关联设计文档**：`docs/project-knowledge/design/linkis_week_variables_设计.md`

---

## 一、测试概述

### 1.1 测试目标

验证 Linkis 周变量功能的正确性、稳定性和兼容性,确保:
- 周日期计算准确(周一为每周第一天)
- 支持标准格式和非标准格式
- 异常处理机制有效
- 功能开关控制正常
- 与现有变量系统兼容

### 1.2 测试范围

| 测试类型 | 测试内容 | 优先级 |
|---------|---------|-------|
| 单元测试 | DateTypeUtils.getWeekBegin()、getWeekEnd() 方法 | P0 |
| 单元测试 | 周变量初始化逻辑 | P0 |
| 集成测试 | 变量替换功能 | P0 |
| 边界测试 | 跨年周、闰年、年初年末 | P0 |
| 异常测试 | 异常处理和降级逻辑 | P1 |
| 功能开关测试 | linkis.variable.week.enabled 配置 | P1 |
| 兼容性测试 | 与现有变量系统共存 | P0 |

### 1.3 测试环境

| 环境 | 配置 |
|------|------|
| 操作系统 | Windows 11 / Linux |
| Java 版本 | 1.8+ |
| Scala 版本 | 2.11.12 / 2.12.17 |
| 构建工具 | Maven 3.x |
| 测试框架 | ScalaTest / JUnit |

---

## 二、单元测试用例

### 2.1 DateTypeUtils.getWeekBegin() 方法测试

#### TC001：getWeekBegin - 周四返回本周一

**来源**：代码变更分析 - DateTypeUtils.scala, getWeekBegin()方法

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-09(周四)
2. 调用 `DateTypeUtils.getWeekBegin(std = false, date)`
3. 验证返回值

**预期结果**：
- 返回 "20260406" (2026-04-06 是周一)

**测试数据**：
```
输入日期: 2026-04-09 (周四)
预期输出: 20260406
```

**优先级**：P0

**覆盖场景**：关键路径 - 正常流程

---

#### TC002：getWeekBegin - 周一返回自身

**来源**：代码变更分析 - DateTypeUtils.scala, getWeekBegin()方法

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-06(周一)
2. 调用 `DateTypeUtils.getWeekBegin(std = false, date)`
3. 验证返回值

**预期结果**：
- 返回 "20260406" (自身)

**测试数据**：
```
输入日期: 2026-04-06 (周一)
预期输出: 20260406
```

**优先级**：P0

**覆盖场景**：边界场景 - 周一当天

---

#### TC003：getWeekBegin - 周日返回本周一

**来源**：代码变更分析 - DateTypeUtils.scala, getWeekBegin()方法

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-12(周日)
2. 调用 `DateTypeUtils.getWeekBegin(std = false, date)`
3. 验证返回值

**预期结果**：
- 返回 "20260406" (本周一)

**测试数据**：
```
输入日期: 2026-04-12 (周日)
预期输出: 20260406
```

**优先级**：P0

**覆盖场景**：边界场景 - 周日当天

---

#### TC004：getWeekBegin - 标准格式

**来源**：代码变更分析 - DateTypeUtils.scala, getWeekBegin()方法

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-09(周四)
2. 调用 `DateTypeUtils.getWeekBegin(std = true, date)`
3. 验证返回值格式

**预期结果**：
- 返回 "2026-04-06" (yyyy-MM-dd 格式)

**测试数据**：
```
输入日期: 2026-04-09 (周四)
预期输出: 2026-04-06
```

**优先级**：P0

**覆盖场景**：功能验证 - 标准格式

---

### 2.2 DateTypeUtils.getWeekEnd() 方法测试

#### TC005：getWeekEnd - 周四返回本周日

**来源**：代码变更分析 - DateTypeUtils.scala, getWeekEnd()方法

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-09(周四)
2. 调用 `DateTypeUtils.getWeekEnd(std = false, date)`
3. 验证返回值

**预期结果**：
- 返回 "20260412" (2026-04-12 是周日)

**测试数据**：
```
输入日期: 2026-04-09 (周四)
预期输出: 20260412
```

**优先级**：P0

**覆盖场景**：关键路径 - 正常流程

---

#### TC006：getWeekEnd - 周日返回自身

**来源**：代码变更分析 - DateTypeUtils.scala, getWeekEnd()方法

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-12(周日)
2. 调用 `DateTypeUtils.getWeekEnd(std = false, date)`
3. 验证返回值

**预期结果**：
- 返回 "20260412" (自身)

**测试数据**：
```
输入日期: 2026-04-12 (周日)
预期输出: 20260412
```

**优先级**：P0

**覆盖场景**：边界场景 - 周日当天

---

#### TC007：getWeekEnd - 周一返回本周日

**来源**：代码变更分析 - DateTypeUtils.scala, getWeekEnd()方法

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-06(周一)
2. 调用 `DateTypeUtils.getWeekEnd(std = false, date)`
3. 验证返回值

**预期结果**：
- 返回 "20260412" (本周日)

**测试数据**：
```
输入日期: 2026-04-06 (周一)
预期输出: 20260412
```

**优先级**：P0

**覆盖场景**：边界场景 - 周一当天

---

### 2.3 边界场景测试

#### TC008：跨年周 - 年末(2025-12-31 周四)

**来源**：需求文档验收标准 AC-007 - 周一为每周第一天

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2025-12-31(周四)
2. 调用 `getWeekBegin(std = false, date)` 和 `getWeekEnd(std = false, date)`
3. 验证返回值

**预期结果**：
- run_week_begin = "20251228" (2025-12-28 周一)
- run_week_end = "20260103" (2026-01-03 周日, 跨年)

**测试数据**：
```
输入日期: 2025-12-31 (周四)
预期输出: begin=20251228, end=20260103
```

**优先级**：P0

**覆盖场景**：边界场景 - 跨年周(年末)

---

#### TC009：跨年周 - 年初(2026-01-01 周五)

**来源**：需求文档验收标准 AC-007 - 周一为每周第一天

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-01-01(周五)
2. 调用 `getWeekBegin(std = false, date)` 和 `getWeekEnd(std = false, date)`
3. 验证返回值

**预期结果**：
- run_week_begin = "20251228" (2025-12-28 周一, 跨年)
- run_week_end = "20260103" (2026-01-03 周日)

**测试数据**：
```
输入日期: 2026-01-01 (周五)
预期输出: begin=20251228, end=20260103
```

**优先级**：P0

**覆盖场景**：边界场景 - 跨年周(年初)

---

#### TC010：闰年 - 2024-02-29(闰日, 周四)

**来源**：设计文档边界场景 6.2 - 闰年处理

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2024-02-29(闰日, 周四)
2. 调用 `getWeekBegin(std = false, date)` 和 `getWeekEnd(std = false, date)`
3. 验证返回值

**预期结果**：
- run_week_begin = "20240226" (2024-02-26 周一)
- run_week_end = "20240303" (2024-03-03 周日)

**测试数据**：
```
输入日期: 2024-02-29 (闰日, 周四)
预期输出: begin=20240226, end=20240303
```

**优先级**：P0

**覆盖场景**：边界场景 - 闰年

---

#### TC011：闰年 - 2020-02-29(闰日, 周六)

**来源**：设计文档边界场景 6.2 - 闰年处理

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2020-02-29(闰日, 周六)
2. 调用 `getWeekBegin(std = false, date)` 和 `getWeekEnd(std = false, date)`
3. 验证返回值

**预期结果**：
- run_week_begin = "20200224" (2020-02-24 周一)
- run_week_end = "20200301" (2020-03-01 周日)

**测试数据**：
```
输入日期: 2020-02-29 (闰日, 周六)
预期输出: begin=20200224, end=20200301
```

**优先级**：P0

**覆盖场景**：边界场景 - 闰年(周六)

---

#### TC012：非闰年 - 2023-02-28(周二)

**来源**：设计文档边界场景 6.2 - 闰年处理

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2023-02-28(周二)
2. 调用 `getWeekBegin(std = false, date)` 和 `getWeekEnd(std = false, date)`
3. 验证返回值

**预期结果**：
- run_week_begin = "20230227" (2023-02-27 周一)
- run_week_end = "20230305" (2023-03-05 周日)

**测试数据**：
```
输入日期: 2023-02-28 (周二)
预期输出: begin=20230227, end=20230305
```

**优先级**：P0

**覆盖场景**：边界场景 - 非闰年2月

---

### 2.4 每日测试(周一到周日)

#### TC013：周一测试

**来源**：代码变更分析 - 完整覆盖每周每天

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-06(周一)
2. 调用 getWeekBegin 和 getWeekEnd
3. 验证返回值

**预期结果**：
- run_week_begin = "20260406"
- run_week_end = "20260412"

**测试数据**：
```
输入: 2026-04-06 (周一)
输出: begin=20260406, end=20260412
```

**优先级**：P0

---

#### TC014：周二测试

**来源**：代码变更分析 - 完整覆盖每周每天

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-07(周二)
2. 调用 getWeekBegin 和 getWeekEnd
3. 验证返回值

**预期结果**：
- run_week_begin = "20260406"
- run_week_end = "20260412"

**测试数据**：
```
输入: 2026-04-07 (周二)
输出: begin=20260406, end=20260412
```

**优先级**：P0

---

#### TC015：周三测试

**来源**：代码变更分析 - 完整覆盖每周每天

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-08(周三)
2. 调用 getWeekBegin 和 getWeekEnd
3. 验证返回值

**预期结果**：
- run_week_begin = "20260406"
- run_week_end = "20260412"

**测试数据**：
```
输入: 2026-04-08 (周三)
输出: begin=20260406, end=20260412
```

**优先级**：P0

---

#### TC016：周四测试

**来源**：代码变更分析 - 完整覆盖每周每天

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-09(周四)
2. 调用 getWeekBegin 和 getWeekEnd
3. 验证返回值

**预期结果**：
- run_week_begin = "20260406"
- run_week_end = "20260412"

**测试数据**：
```
输入: 2026-04-09 (周四)
输出: begin=20260406, end=20260412
```

**优先级**：P0

---

#### TC017：周五测试

**来源**：代码变更分析 - 完整覆盖每周每天

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-10(周五)
2. 调用 getWeekBegin 和 getWeekEnd
3. 验证返回值

**预期结果**：
- run_week_begin = "20260406"
- run_week_end = "20260412"

**测试数据**：
```
输入: 2026-04-10 (周五)
输出: begin=20260406, end=20260412
```

**优先级**：P0

---

#### TC018：周六测试

**来源**：代码变更分析 - 完整覆盖每周每天

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-11(周六)
2. 调用 getWeekBegin 和 getWeekEnd
3. 验证返回值

**预期结果**：
- run_week_begin = "20260406"
- run_week_end = "20260412"

**测试数据**：
```
输入: 2026-04-11 (周六)
输出: begin=20260406, end=20260412
```

**优先级**：P0

---

#### TC019：周日测试

**来源**：代码变更分析 - 完整覆盖每周每天

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 准备测试日期：2026-04-12(周日)
2. 调用 getWeekBegin 和 getWeekEnd
3. 验证返回值

**预期结果**：
- run_week_begin = "20260406"
- run_week_end = "20260412"

**测试数据**：
```
输入: 2026-04-12 (周日)
输出: begin=20260406, end=20260412
```

**优先级**：P0

---

### 2.5 异常处理测试

#### TC020：getWeekBegin - 异常处理(降级逻辑)

**来源**：代码变更分析 - DateTypeUtils.scala getWeekBegin() 异常处理

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 模拟异常场景(传入 null 日期)
2. 调用 `getWeekBegin(std = false, date)`
3. 验证降级处理

**预期结果**：
- 捕获异常不抛出
- 返回当前日期作为降级值
- 记录错误日志

**测试数据**：
```
输入: null 或无效日期
预期: 降级返回当前日期,不抛出异常
```

**优先级**：P1

**覆盖场景**：异常场景 - 降级处理

---

#### TC021：getWeekEnd - 异常处理(降级逻辑)

**来源**：代码变更分析 - DateTypeUtils.scala getWeekEnd() 异常处理

**测试类型**：单元测试

**前置条件**：
- DateTypeUtils 已正确初始化

**测试步骤**：
1. 模拟异常场景(传入 null 日期)
2. 调用 `getWeekEnd(std = false, date)`
3. 验证降级处理

**预期结果**：
- 捕获异常不抛出
- 返回当前日期作为降级值
- 记录错误日志

**测试数据**：
```
输入: null 或无效日期
预期: 降级返回当前日期,不抛出异常
```

**优先级**：P1

**覆盖场景**：异常场景 - 降级处理

---

### 2.6 功能开关测试

#### TC022：功能开关 - 启用状态

**来源**：代码变更分析 - VariableUtils.scala WEEK_VARIABLE_ENABLED

**测试类型**：单元测试

**前置条件**：
- 配置 linkis.variable.week.enabled = true (默认)

**测试步骤**：
1. 设置配置 linkis.variable.week.enabled = true
2. 调用 VariableUtils.replace(),传入 run_date
3. 验证周变量被正确初始化

**预期结果**：
- 周变量被正确初始化
- run_week_begin、run_week_begin_std、run_week_end、run_week_end_std 都可用
- 日志输出 "Week variables initialized successfully"

**测试数据**：
```
配置: linkis.variable.week.enabled=true
输入: run_date=20260409
预期: 4个周变量都被初始化
```

**优先级**：P1

**覆盖场景**：功能开关 - 启用

---

#### TC023：功能开关 - 禁用状态

**来源**：代码变更分析 - VariableUtils.scala WEEK_VARIABLE_ENABLED

**测试类型**：单元测试

**前置条件**：
- 配置 linkis.variable.week.enabled = false

**测试步骤**：
1. 设置配置 linkis.variable.week.enabled = false
2. 调用 VariableUtils.replace(),传入 run_date
3. 验证周变量未被初始化

**预期结果**：
- 周变量未被初始化
- nameAndType 中不包含 run_week_begin 等变量
- 日志输出 "Week variables are disabled by configuration"

**测试数据**：
```
配置: linkis.variable.week.enabled=false
输入: run_date=20260409
预期: 周变量未被初始化
```

**优先级**：P1

**覆盖场景**：功能开关 - 禁用

---

## 三、集成测试用例

### 3.1 变量替换功能测试

#### TC024：周变量替换 - 基本功能

**来源**：需求文档验收标准 AC-001、AC-002

**测试类型**：集成测试

**前置条件**：
- VariableUtils 已正确初始化

**测试步骤**：
1. 准备 SQL: `SELECT * FROM orders WHERE dt >= '${run_week_begin}' AND dt <= '${run_week_end}'`
2. 设置变量: run_date = "20260409"
3. 调用 VariableUtils.replace(sql, variables)
4. 验证替换结果

**预期结果**：
- SQL 被正确替换
- run_week_begin 被替换为 "20260406"
- run_week_end 被替换为 "20260412"
- 最终 SQL: `SELECT * FROM orders WHERE dt >= '20260406' AND dt <= '20260412'`

**测试数据**：
```sql
输入 SQL:
SELECT * FROM orders WHERE dt >= '${run_week_begin}' AND dt <= '${run_week_end}'

变量:
run_date = 20260409

预期输出:
SELECT * FROM orders WHERE dt >= '20260406' AND dt <= '20260412'
```

**优先级**：P0

**覆盖场景**：关键路径 - 周变量替换

---

#### TC025：周变量替换 - 标准格式

**来源**：需求文档验收标准 AC-003、AC-004

**测试类型**：集成测试

**前置条件**：
- VariableUtils 已正确初始化

**测试步骤**：
1. 准备 SQL: `SELECT * FROM orders WHERE dt >= '${run_week_begin_std}' AND dt <= '${run_week_end_std}'`
2. 设置变量: run_date = "20260409"
3. 调用 VariableUtils.replace(sql, variables)
4. 验证替换结果

**预期结果**：
- SQL 被正确替换
- run_week_begin_std 被替换为 "2026-04-06"
- run_week_end_std 被替换为 "2026-04-12"

**测试数据**：
```sql
输入 SQL:
SELECT * FROM orders WHERE dt >= '${run_week_begin_std}' AND dt <= '${run_week_end_std}'

变量:
run_date = 20260409

预期输出:
SELECT * FROM orders WHERE dt >= '2026-04-06' AND dt <= '2026-04-12'
```

**优先级**：P0

**覆盖场景**：关键路径 - 标准格式替换

---

#### TC026：周变量算术运算 - 上周

**来源**：需求文档验收标准 AC-005

**测试类型**：集成测试

**前置条件**：
- VariableUtils 已正确初始化

**测试步骤**：
1. 准备 SQL: `SELECT * FROM orders WHERE dt >= '${run_week_begin - 7}' AND dt <= '${run_week_end - 7}'`
2. 设置变量: run_date = "20260409"
3. 调用 VariableUtils.replace(sql, variables)
4. 验证替换结果

**预期结果**：
- SQL 被正确替换
- run_week_begin - 7 被替换为 "20260330" (2026-03-30 周一)
- run_week_end - 7 被替换为 "20260405" (2026-04-05 周日)

**测试数据**：
```sql
输入 SQL:
SELECT * FROM orders WHERE dt >= '${run_week_begin - 7}' AND dt <= '${run_week_end - 7}'

变量:
run_date = 20260409

预期输出:
SELECT * FROM orders WHERE dt >= '20260330' AND dt <= '20260405'
```

**优先级**：P1

**覆盖场景**：功能验证 - 算术运算

---

#### TC027：周变量算术运算 - 下周

**来源**：需求文档验收标准 AC-005

**测试类型**：集成测试

**前置条件**：
- VariableUtils 已正确初始化

**测试步骤**：
1. 准备 SQL: `SELECT * FROM orders WHERE dt >= '${run_week_begin + 7}' AND dt <= '${run_week_end + 7}'`
2. 设置变量: run_date = "20260409"
3. 调用 VariableUtils.replace(sql, variables)
4. 验证替换结果

**预期结果**：
- SQL 被正确替换
- run_week_begin + 7 被替换为 "20260413" (2026-04-13 周一)
- run_week_end + 7 被替换为 "20260419" (2026-04-19 周日)

**测试数据**：
```sql
输入 SQL:
SELECT * FROM orders WHERE dt >= '${run_week_begin + 7}' AND dt <= '${run_week_end + 7}'

变量:
run_date = 20260409

预期输出:
SELECT * FROM orders WHERE dt >= '20260413' AND dt <= '20260419'
```

**优先级**：P1

**覆盖场景**：功能验证 - 算术运算

---

#### TC028：周变量混合使用

**来源**：需求文档测试场景 7.3

**测试类型**：集成测试

**前置条件**：
- VariableUtils 已正确初始化

**测试步骤**：
1. 准备 SQL:
```sql
SELECT * FROM orders
WHERE dt >= '${run_week_begin}' AND dt <= '${run_week_end}'
  AND month >= '${run_month_begin}'
```
2. 设置变量: run_date = "20260409"
3. 调用 VariableUtils.replace(sql, variables)
4. 验证替换结果

**预期结果**：
- 所有变量都被正确替换
- run_week_begin → "20260406"
- run_week_end → "20260412"
- run_month_begin → "20260401"

**测试数据**：
```sql
输入 SQL:
SELECT * FROM orders
WHERE dt >= '${run_week_begin}' AND dt <= '${run_week_end}'
  AND month >= '${run_month_begin}'

变量:
run_date = 20260409

预期输出:
SELECT * FROM orders
WHERE dt >= '20260406' AND dt <= '20260412'
  AND month >= '20260401'
```

**优先级**：P0

**覆盖场景**：兼容性验证 - 混合使用变量

---

#### TC029：周变量对比分析

**来源**：需求文档使用示例

**测试类型**：集成测试

**前置条件**：
- VariableUtils 已正确初始化

**测试步骤**：
1. 准备 SQL:
```sql
SELECT
  SUM(amount) AS current_week_amount
FROM orders
WHERE dt >= '${run_week_begin}' AND dt <= '${run_week_end}'
UNION ALL
SELECT
  SUM(amount) AS last_week_amount
FROM orders
WHERE dt >= '${run_week_begin - 7}' AND dt <= '${run_week_end - 7}'
```
2. 设置变量: run_date = "20260409"
3. 调用 VariableUtils.replace(sql, variables)
4. 验证替换结果

**预期结果**：
- 第一组: run_week_begin → "20260406", run_week_end → "20260412"
- 第二组: run_week_begin - 7 → "20260330", run_week_end - 7 → "20260405"

**测试数据**：
```sql
输入 SQL:
SELECT
  SUM(amount) AS current_week_amount
FROM orders
WHERE dt >= '${run_week_begin}' AND dt <= '${run_week_end}'
UNION ALL
SELECT
  SUM(amount) AS last_week_amount
FROM orders
WHERE dt >= '${run_week_begin - 7}' AND dt <= '${run_week_end - 7}'

变量:
run_date = 20260409

预期输出:
SELECT
  SUM(amount) AS current_week_amount
FROM orders
WHERE dt >= '20260406' AND dt <= '20260412'
UNION ALL
SELECT
  SUM(amount) AS last_week_amount
FROM orders
WHERE dt >= '20260330' AND dt <= '20260405'
```

**优先级**：P1

**覆盖场景**：功能验证 - 数据对比分析

---

### 3.2 兼容性测试

#### TC030：不影响现有变量 - run_date

**来源**：需求文档验收标准 AC-006

**测试类型**：集成测试

**前置条件**：
- VariableUtils 已正确初始化

**测试步骤**：
1. 准备 SQL: `SELECT * FROM orders WHERE dt = '${run_date}'`
2. 设置变量: run_date = "20260409"
3. 调用 VariableUtils.replace(sql, variables)
4. 验证替换结果

**预期结果**：
- run_date 被正确替换为 "20260409"
- 现有变量功能不受影响

**测试数据**：
```sql
输入 SQL:
SELECT * FROM orders WHERE dt = '${run_date}'

变量:
run_date = 20260409

预期输出:
SELECT * FROM orders WHERE dt = '20260409'
```

**优先级**：P0

**覆盖场景**：兼容性验证 - 现有变量

---

#### TC031：不影响现有变量 - run_month_begin

**来源**：需求文档验收标准 AC-006

**测试类型**：集成测试

**前置条件**：
- VariableUtils 已正确初始化

**测试步骤**：
1. 准备 SQL: `SELECT * FROM orders WHERE dt >= '${run_month_begin}' AND dt <= '${run_month_end}'`
2. 设置变量: run_date = "20260409"
3. 调用 VariableUtils.replace(sql, variables)
4. 验证替换结果

**预期结果**：
- run_month_begin 被正确替换为 "20260401"
- run_month_end 被正确替换为 "20260430"
- 现有月份变量功能不受影响

**测试数据**：
```sql
输入 SQL:
SELECT * FROM orders WHERE dt >= '${run_month_begin}' AND dt <= '${run_month_end}'

变量:
run_date = 20260409

预期输出:
SELECT * FROM orders WHERE dt >= '20260401' AND dt <= '20260430'
```

**优先级**：P0

**覆盖场景**：兼容性验证 - 月份变量

---

#### TC032：不影响现有变量 - run_quarter_begin

**来源**：需求文档验收标准 AC-006

**测试类型**：集成测试

**前置条件**：
- VariableUtils 已正确初始化

**测试步骤**：
1. 准备 SQL: `SELECT * FROM orders WHERE dt >= '${run_quarter_begin}' AND dt <= '${run_quarter_end}'`
2. 设置变量: run_date = "20260409"
3. 调用 VariableUtils.replace(sql, variables)
4. 验证替换结果

**预期结果**：
- run_quarter_begin 被正确替换为 "20260401" (Q2开始)
- run_quarter_end 被正确替换为 "20260630" (Q2结束)
- 现有季度变量功能不受影响

**测试数据**：
```sql
输入 SQL:
SELECT * FROM orders WHERE dt >= '${run_quarter_begin}' AND dt <= '${run_quarter_end}'

变量:
run_date = 20260409

预期输出:
SELECT * FROM orders WHERE dt >= '20260401' AND dt <= '20260630'
```

**优先级**：P0

**覆盖场景**：兼容性验证 - 季度变量

---

#### TC033：不影响现有变量 - run_year_begin

**来源**：需求文档验收标准 AC-006

**测试类型**：集成测试

**前置条件**：
- VariableUtils 已正确初始化

**测试步骤**：
1. 准备 SQL: `SELECT * FROM orders WHERE dt >= '${run_year_begin}' AND dt <= '${run_year_end}'`
2. 设置变量: run_date = "20260409"
3. 调用 VariableUtils.replace(sql, variables)
4. 验证替换结果

**预期结果**：
- run_year_begin 被正确替换为 "20260101"
- run_year_end 被正确替换为 "20261231"
- 现有年度变量功能不受影响

**测试数据**：
```sql
输入 SQL:
SELECT * FROM orders WHERE dt >= '${run_year_begin}' AND dt <= '${run_year_end}'

变量:
run_date = 20260409

预期输出:
SELECT * FROM orders WHERE dt >= '20260101' AND dt <= '20261231'
```

**优先级**：P0

**覆盖场景**：兼容性验证 - 年度变量

---

## 四、性能测试用例

### 4.1 性能基准测试

#### TC034：周变量计算性能

**来源**：设计文档第八章 - 性能分析

**测试类型**：性能测试

**前置条件**：
- 使用 JMH (Java Microbenchmark Harness) 框架
- 预热完成

**测试步骤**：
1. 使用 JMH 运行 getWeekBegin() 性能测试
2. 使用 JMH 运行 getWeekEnd() 性能测试
3. 记录平均执行时间

**预期结果**：
- getWeekBegin() 平均执行时间 < 50ms
- getWeekEnd() 平均执行时间 < 50ms

**测试数据**：
```
测试方法: JMH @Benchmark
预热迭代: 10
测量迭代: 100
预期: < 50ms
```

**优先级**：P1

**覆盖场景**：性能验证 - 计算性能

---

#### TC035：变量替换性能

**来源**：设计文档第八章 - 性能分析

**测试类型**：性能测试

**前置条件**：
- 使用 JMH 框架
- 预热完成

**测试步骤**：
1. 准备包含周变量的 SQL
2. 使用 JMH 运行 VariableUtils.replace() 性能测试
3. 记录平均执行时间

**预期结果**：
- 变量替换总时间 < 100ms
- 内存占用增量 < 1KB

**测试数据**：
```
SQL: SELECT * FROM orders WHERE dt >= '${run_week_begin}' AND dt <= '${run_week_end}'
变量: run_date = 20260409
预期: < 100ms
```

**优先级**：P1

**覆盖场景**：性能验证 - 替换性能

---

## 五、测试用例统计

### 5.1 按优先级统计

| 优先级 | 用例数 | 占比 |
|--------|-------|------|
| P0 | 27 | 77.1% |
| P1 | 8 | 22.9% |
| 总计 | 35 | 100% |

### 5.2 按测试类型统计

| 测试类型 | 用例数 | 占比 |
|---------|-------|------|
| 单元测试 | 23 | 65.7% |
| 集成测试 | 10 | 28.6% |
| 性能测试 | 2 | 5.7% |
| 总计 | 35 | 100% |

### 5.3 按覆盖场景统计

| 覆盖场景 | 用例数 | 占比 |
|---------|-------|------|
| 关键路径 - 正常流程 | 10 | 28.6% |
| 边界场景 | 13 | 37.1% |
| 异常场景 | 2 | 5.7% |
| 功能验证 | 6 | 17.1% |
| 兼容性验证 | 4 | 11.5% |
| 总计 | 35 | 100% |

---

## 六、验收标准覆盖检查

### 6.1 需求文档验收标准覆盖

| 验收标准 | 对应用例 | 状态 |
|---------|---------|:----:|
| AC-001: run_week_begin 正确返回周一日期 | TC001, TC013-019 | ✅ |
| AC-002: run_week_end 正确返回周日日期 | TC005, TC013-019 | ✅ |
| AC-003: run_week_begin_std 返回标准格式 | TC004, TC025 | ✅ |
| AC-004: run_week_end_std 返回标准格式 | TC025 | ✅ |
| AC-005: 支持周变量算术运算 | TC026, TC027 | ✅ |
| AC-006: 不影响现有变量系统 | TC030-TC033 | ✅ |
| AC-007: 周一为每周第一天 | TC001-TC003, TC008-TC009 | ✅ |

**覆盖率**：7/7 (100%)

---

## 七、测试执行计划

### 7.1 测试执行顺序

1. **阶段1：单元测试** (预计1小时)
   - 执行 TC001-TC023
   - 重点：DateTypeUtils 方法测试、边界场景、异常处理

2. **阶段2：集成测试** (预计1小时)
   - 执行 TC024-TC033
   - 重点：变量替换功能、兼容性验证

3. **阶段3：性能测试** (预计0.5小时)
   - 执行 TC034-TC035
   - 重点：性能基准测试

### 7.2 测试环境准备

| 项目 | 要求 |
|------|------|
| 代码分支 | dev-1.18.0-webank |
| 测试框架 | ScalaTest / JUnit |
| Java 版本 | 1.8+ |
| 配置文件 | linkis.variable.week.enabled=true |

### 7.3 测试数据准备

| 测试数据 | 用途 |
|---------|------|
| 2026-04-09 (周四) | 正常场景 |
| 2025-12-31 (周四) | 跨年周(年末) |
| 2026-01-01 (周五) | 跨年周(年初) |
| 2024-02-29 (周四) | 闰年 |
| 2020-02-29 (周六) | 闰年边界 |

---

## 八、缺陷报告模板

### 8.1 缺陷等级定义

| 等级 | 定义 | 示例 |
|------|------|------|
| 严重 | 核心功能无法使用 | 周变量计算错误导致系统崩溃 |
| 重要 | 主要功能受影响 | 跨年周计算错误 |
| 一般 | 次要功能受影响 | 日志输出不正确 |
| 轻微 | 不影响功能 | 文档注释错误 |

### 8.2 缺陷报告格式

```
缺陷ID: WEEK-BUG-XXX
标题: [缺陷标题]
发现日期: 2026-04-09
缺陷等级: [严重/重要/一般/轻微]
测试用例: TCXXX
重现步骤:
1. [步骤1]
2. [步骤2]
3. [步骤3]
实际结果: [实际发生的结果]
预期结果: [期望发生的结果]
环境信息: [测试环境]
附件: [截图/日志]
```

---

## 九、附录

### 9.1 周变量完整列表

| 变量名 | 类型 | 格式 | 说明 | 示例 |
|--------|------|------|------|------|
| run_week_begin | DateType | yyyyMMdd | 周开始日期(周一) | 20260406 |
| run_week_begin_std | DateType | yyyy-MM-dd | 周开始日期标准格式 | 2026-04-06 |
| run_week_end | DateType | yyyyMMdd | 周结束日期(周日) | 20260412 |
| run_week_end_std | DateType | yyyy-MM-dd | 周结束日期标准格式 | 2026-04-12 |

### 9.2 测试用例编号索引

| 编号范围 | 测试类型 | 说明 |
|---------|---------|------|
| TC001-TC007 | 单元测试 | getWeekBegin/getWeekEnd 基础测试 |
| TC008-TC012 | 单元测试 | 边界场景测试 |
| TC013-TC019 | 单元测试 | 每日测试(周一到周日) |
| TC020-TC021 | 单元测试 | 异常处理测试 |
| TC022-TC023 | 单元测试 | 功能开关测试 |
| TC024-TC029 | 集成测试 | 变量替换功能测试 |
| TC030-TC033 | 集成测试 | 兼容性测试 |
| TC034-TC035 | 性能测试 | 性能基准测试 |

---

**文档版本**：v1.0
**最后更新**：2026-04-09
**作者**：测试用例生成Agent
**审核状态**：待审核
