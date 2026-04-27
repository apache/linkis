# Linkis SQL 查询增加周变量 - 设计文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 需求ID | LINKIS-FEATURE-WEEK-VAR-001 |
| 需求名称 | Linkis SQL 查询增加周变量 |
| 设计类型 | 功能增强设计 (ENHANCE) |
| 基础模块 | linkis-commons / linkis-entrance |
| 设计版本 | 1.0 |
| 创建时间 | 2026-04-09 |
| 设计状态 | 待评审 |

**关联需求文档**：`docs/project-knowledge/requirements/linkis_week_variables_需求.md`

---

## 一、设计概述

### 1.1 设计目标

在 Linkis 现有变量系统（日期、月份、季度、半年、年度）基础上，新增**周相关变量**，支持基于运行日期（run_date）计算周相关的系统变量。

### 1.2 设计范围

本设计涵盖以下内容：
- 在 VariableUtils 中添加周变量常量定义
- 在 initAllDateVars 方法中添加周变量初始化逻辑
- 在 DateTypeUtils 中添加周日期计算方法
- 周变量类型定义和算术运算支持

### 1.3 设计原则

1. **最小侵入原则**：基于现有架构扩展，不修改现有逻辑
2. **一致性原则**：遵循现有变量系统的命名和实现规范
3. **向后兼容**：不影响现有日期、月份、季度等变量功能
4. **性能优先**：周变量计算不应超过 50ms

---

## 二、架构设计

### 2.1 现有架构分析

#### 2.1.1 变量系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      VariableUtils                           │
│  ┌─────────────────────────────────────────────────────────┐│
│  │ replace() - 入口方法                                    ││
│  │   ├── 解析 run_date                                     ││
│  │   ├── 调用 initAllDateVars() 初始化所有日期变量        ││
│  │   └── 调用 parserVar() 执行变量替换                    ││
│  └─────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │ initAllDateVars() - 初始化所有日期变量                  ││
│  │   ├── run_date, run_date_std                            ││
│  │   ├── run_month_begin/end + std                         ││
│  │   ├── run_quarter_begin/end + std                       ││
│  │   ├── run_half_year_begin/end + std                     ││
│  │   ├── run_year_begin/end + std                          ││
│  │   ├── run_today + std                                   ││
│  │   └── run_mon + std (月度周期变量)                      ││
│  │  [新增] run_week_begin/end + std                        ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                            │
                            │ 调用工具类方法
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      DateTypeUtils                           │
│  ├── getToday() / getYesterday()                            │
│  ├── getMonth() - 月日期计算                                │
│  ├── getQuarter() - 季度日期计算                            │
│  ├── getHalfYear() - 半年日期计算                           │
│  ├── getYear() - 年日期计算                                 │
│  └── [新增] getWeek() - 周日期计算                          │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ 定义类型
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    CustomDateType.scala                      │
│  ├── class CustomDateType - 日期类型                        │
│  ├── class CustomMonthType - 月度类型                       │
│  ├── class CustomQuarterType - 季度类型                     │
│  ├── class CustomHalfYearType - 半年类型                    │
│  ├── class CustomYearType - 年度类型                        │
│  └── [新增] class CustomWeekType - 周类型                   │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ 包装为 VariableType
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     VariableType.scala                       │
│  ├── case class DateType                                    │
│  ├── case class MonthType                                   │
│  ├── case class QuarterType                                 │
│  ├── case class HalfYearType                                │
│  ├── case class YearType                                    │
│  └── [新增] case class WeekType                             │
└─────────────────────────────────────────────────────────────┘
```

#### 2.1.2 现有变量模式分析

通过分析现有代码，发现 Linkis 变量系统遵循以下模式：

**模式1：双重变量命名**
- 普通格式：`run_xxx_begin` → `20260406` (yyyyMMdd)
- 标准格式：`run_xxx_begin_std` → `2026-04-06` (yyyy-MM-dd)

**模式2：类型定义**
- 自定义类型（CustomXxxType）：负责日期计算和格式转换
- 包装类型（XxxType VariableType）：负责算术运算和变量替换

**模式3：算术运算**
- 支持 `+` 和 `-` 运算符
- 运算结果继承原类型的格式

### 2.2 周变量设计方案

#### 2.2.1 周变量定义

**遵循现有模式，定义以下周变量**：

| 变量名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| `run_week_begin` | DateType | 周开始日期（周一） | 20260406 |
| `run_week_begin_std` | DateType | 周开始日期标准格式 | 2026-04-06 |
| `run_week_end` | DateType | 周结束日期（周日） | 20260412 |
| `run_week_end_std` | DateType | 周结束日期标准格式 | 2026-04-12 |

**计算规则**：
- 周一为每周的第一天（中国习惯）
- 周日为每周的最后一天
- 基于 `run_date` 计算所属周的开始和结束日期
- 支持跨年周处理（如 2025-12-31 属于 2026-01-01 所属周）

#### 2.2.2 不需要创建 CustomWeekType

**设计决策**：经过分析现有代码，发现：
- `run_month_begin/end` 等变量使用的是 `DateType` + `CustomDateType`，而不是独立的 `MonthType` + `CustomMonthType`
- `MonthType` + `CustomMonthType` 仅用于 `run_mon` 系列变量（月度周期变量）

因此，周变量实现方案：
- **复用 `DateType` + `CustomDateType`**
- 在 `DateTypeUtils` 中添加静态方法 `getWeekBegin()` 和 `getWeekEnd()`
- 不需要创建新的 `CustomWeekType` 和 `WeekType`

---

## 三、详细设计

### 3.1 VariableUtils 修改

#### 3.1.1 添加周变量常量

**文件位置**：`linkis-commons/linkis-common/src/main/scala/org/apache/linkis/common/utils/VariableUtils.scala`

**修改位置**：在 `object VariableUtils extends Logging` 中添加

```scala
object VariableUtils extends Logging {

  val RUN_DATE = "run_date"
  val RUN_TODAY_H = "run_today_h"
  val RUN_TODAY_HOUR = "run_today_hour"

  // 新增：周变量常量
  val RUN_WEEK_BEGIN = "run_week_begin"
  val RUN_WEEK_BEGIN_STD = "run_week_begin_std"
  val RUN_WEEK_END = "run_week_end"
  val RUN_WEEK_END_STD = "run_week_end_std"

  // ... 现有代码 ...
}
```

#### 3.1.2 修改 initAllDateVars 方法

**修改位置**：在 `initAllDateVars` 方法中，在 `run_year_end_std` 初始化之后添加

```scala
private def initAllDateVars(
    run_date: CustomDateType,
    nameAndType: mutable.Map[String, variable.VariableType]
): Unit = {
  val run_date_str = run_date.toString

  // ... 现有变量初始化代码（run_date_std, run_month_xxx, run_quarter_xxx, run_half_year_xxx, run_year_xxx, run_today_xxx, run_mon_xxx）...

  // 新增：初始化周变量（放在所有变量初始化之后）
  // 使用 DateTypeUtils 计算周开始和结束日期
  val weekBegin = DateTypeUtils.getWeekBegin(std = false, run_date.getDate)
  val weekBeginStd = DateTypeUtils.getWeekBegin(std = true, run_date.getDate)
  val weekEnd = DateTypeUtils.getWeekEnd(std = false, run_date.getDate)
  val weekEndStd = DateTypeUtils.getWeekEnd(std = true, run_date.getDate)

  nameAndType("run_week_begin") = variable.DateType(new CustomDateType(weekBegin, false))
  nameAndType("run_week_begin_std") = variable.DateType(new CustomDateType(weekBeginStd, true))
  nameAndType("run_week_end") = variable.DateType(new CustomDateType(weekEnd, false))
  nameAndType("run_week_end_std") = variable.DateType(new CustomDateType(weekEndStd, true))
}
```

### 3.2 DateTypeUtils 修改

**文件位置**：`linkis-commons/linkis-common/src/main/scala/org/apache/linkis/common/variable/DateTypeUtils.scala`

**添加方法**：

```scala
/**
 * 获取周开始日期（周一）
 *
 * @param std  是否使用标准格式（true: yyyy-MM-dd, false: yyyyMMdd）
 * @param date 基准日期
 * @return 周一日期字符串
 */
def getWeekBegin(std: Boolean = true, date: Date): String = {
  val dateFormat = dateFormatLocal.get()
  val dateFormat_std = dateFormatStdLocal.get()
  val cal: Calendar = Calendar.getInstance()
  cal.setTime(date)

  // 获取当前是星期几（Calendar.SUNDAY=1, Calendar.MONDAY=2, ..., Calendar.SATURDAY=7）
  val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

  // 计算到周一的天数差
  // 周日(1) 需要回退 6 天到上周一
  // 周一(2) 不需要调整
  // 周二(3) 需要回退 1 天
  // ...
  // 周六(7) 需要回退 5 天
  val daysToMonday = if (dayOfWeek == Calendar.SUNDAY) {
    -6  // 周日回退6天到本周一
  } else {
    Calendar.MONDAY - dayOfWeek  // 其他日期回退到本周一
  }

  cal.add(Calendar.DAY_OF_MONTH, daysToMonday)

  if (std) {
    dateFormat_std.format(cal.getTime)
  } else {
    dateFormat.format(cal.getTime)
  }
}

/**
 * 获取周结束日期（周日）
 *
 * @param std  是否使用标准格式（true: yyyy-MM-dd, false: yyyyMMdd）
 * @param date 基准日期
 * @return 周日日期字符串
 */
def getWeekEnd(std: Boolean = true, date: Date): String = {
  val dateFormat = dateFormatLocal.get()
  val dateFormat_std = dateFormatStdLocal.get()
  val cal: Calendar = Calendar.getInstance()
  cal.setTime(date)

  // 获取当前是星期几
  val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

  // 计算到周日的天数差
  // 周日(1) 不需要调整
  // 周一(2) 需要前进 6 天
  // 周二(3) 需要前进 5 天
  // ...
  // 周六(7) 需要前进 1 天
  val daysToSunday = if (dayOfWeek == Calendar.SUNDAY) {
    0  // 周日不需要调整
  } else {
    Calendar.SUNDAY - dayOfWeek + 7  // 其他日期前进到本周日
  }

  cal.add(Calendar.DAY_OF_MONTH, daysToSunday)

  if (std) {
    dateFormat_std.format(cal.getTime)
  } else {
    dateFormat.format(cal.getTime)
  }
}
```

---

## 四、代码变更清单

### 4.1 文件变更列表

| 序号 | 文件路径 | 变更类型 | 变更说明 |
|------|---------|---------|---------|
| 1 | `linkis-commons/linkis-common/src/main/scala/org/apache/linkis/common/utils/VariableUtils.scala` | 修改 | 添加周变量常量、修改 initAllDateVars 方法 |
| 2 | `linkis-commons/linkis-common/src/main/scala/org/apache/linkis/common/variable/DateTypeUtils.scala` | 修改 | 添加 getWeekBegin() 和 getWeekEnd() 方法 |

### 4.2 变更代码行数估算

| 文件 | 新增行数 | 修改行数 | 删除行数 | 总计 |
|------|---------|---------|---------|------|
| VariableUtils.scala | 20 | 5 | 0 | 25 |
| DateTypeUtils.scala | 60 | 0 | 0 | 60 |
| 合计 | 80 | 5 | 0 | 85 |

---

## 五、数据流设计

### 5.1 周变量计算流程

```
用户提交SQL（包含 ${run_week_begin}）
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ VariableUtils.replace()                                     │
│  1. 解析 run_date 变量（如 2026-04-09）                     │
│  2. 创建 CustomDateType("2026-04-09", false)               │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ VariableUtils.initAllDateVars()                             │
│  3. 调用 DateTypeUtils.getWeekBegin(false, date)           │
│     → 返回 "20260406" (本周一)                              │
│  4. 调用 DateTypeUtils.getWeekBegin(true, date)            │
│     → 返回 "2026-04-06" (本周一标准格式)                    │
│  5. 调用 DateTypeUtils.getWeekEnd(false, date)             │
│     → 返回 "20260412" (本周日)                              │
│  6. 调用 DateTypeUtils.getWeekEnd(true, date)              │
│     → 返回 "2026-04-12" (本周日标准格式)                    │
│  7. 创建 DateType 并存入 nameAndType 映射                   │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ VariableUtils.parserVar()                                   │
│  8. 解析 ${run_week_begin} 表达式                           │
│  9. 从 nameAndType 获取 DateType                            │
│  10. 调用 DateType.getValue() 获取值 "20260406"            │
│  11. 替换 SQL 中的变量为实际值                               │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ 返回替换后的SQL                                              │
│  SELECT * FROM orders                                       │
│  WHERE dt >= '20260406' AND dt <= '20260412'               │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 周变量算术运算流程

```
用户SQL：${run_week_begin - 7} (上周一)
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ VariableUtils.parserVar()                                   │
│  1. 解析表达式：run_week_begin - 7                         │
│  2. 识别变量名：run_week_begin                              │
│  3. 识别运算符：-                                           │
│  4. 识别右值：7                                             │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ DateType.calculator()                                       │
│  5. 获取 DateType(CustomDateType("20260406", false))      │
│  6. 调用 CustomDateType.-(7)                                │
│     → 使用 DateUtils.addDays() 计算 20260406 - 7 天        │
│     → 返回 "20260330" (2026-03-30 所在周一)                 │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ 返回替换后的SQL                                              │
│  SELECT * FROM orders WHERE dt >= '20260330'               │
└─────────────────────────────────────────────────────────────┘
```

---

## 六、边界场景处理

### 6.1 跨年周处理

**场景1：2025-12-31（周四）**
```
输入：run_date = 2025-12-31
预期：
  run_week_begin = 20251228 (2025-12-28 周一)
  run_week_end = 20260103 (2026-01-03 周日，跨年)
```

**场景2：2026-01-01（周五）**
```
输入：run_date = 2026-01-01
预期：
  run_week_begin = 20251228 (2025-12-28 周一，跨年)
  run_week_end = 20260103 (2026-01-03 周日)
```

**实现逻辑**：
- 使用 `Calendar.add(Calendar.DAY_OF_MONTH, days)` 自动处理跨年
- 无需特殊逻辑，Java Calendar API 自动处理

### 6.2 闰年处理

**场景：2024-02-29（闰日，周四）**
```
输入：run_date = 2024-02-29
预期：
  run_week_begin = 20240226 (2024-02-26 周一)
  run_week_end = 20240303 (2024-03-03 周日)
```

**实现逻辑**：
- 使用 Java Calendar API 自动处理闰年
- 无需特殊逻辑

### 6.3 年初年末处理

**场景：2026-01-01（周四）**
```
输入：run_date = 2026-01-01
预期：
  run_week_begin = 20251228 (2025-12-28 周一，跨年)
  run_week_end = 20260103 (2026-01-03 周日)
```

---

## 七、测试设计

### 7.1 单元测试

**测试类**：`DateTypeUtilsTest`

**测试用例**：

```scala
class DateTypeUtilsTest extends AnyFunSuite {

  test("getWeekBegin - 周四") {
    val date = DateTypeUtils.dateFormatLocal.get().parse("20260409")
    val result = DateTypeUtils.getWeekBegin(std = false, date)
    assert(result === "20260406") // 2026-04-09 是周四，周一是 04-06
  }

  test("getWeekBegin - 周一") {
    val date = DateTypeUtils.dateFormatLocal.get().parse("20260406")
    val result = DateTypeUtils.getWeekBegin(std = false, date)
    assert(result === "20260406") // 2026-04-06 是周一，应返回自身
  }

  test("getWeekBegin - 周日") {
    val date = DateTypeUtils.dateFormatLocal.get().parse("20260412")
    val result = DateTypeUtils.getWeekBegin(std = false, date)
    assert(result === "20260406") // 2026-04-12 是周日，周一是 04-06
  }

  test("getWeekEnd - 周四") {
    val date = DateTypeUtils.dateFormatLocal.get().parse("20260409")
    val result = DateTypeUtils.getWeekEnd(std = false, date)
    assert(result === "20260412") // 2026-04-09 是周四，周日是 04-12
  }

  test("跨年周 - 年末") {
    val date = DateTypeUtils.dateFormatLocal.get().parse("20251231")
    val begin = DateTypeUtils.getWeekBegin(std = false, date)
    val end = DateTypeUtils.getWeekEnd(std = false, date)
    assert(begin === "20251228") // 2025-12-28 周一
    assert(end === "20260103")   // 2026-01-03 周日（跨年）
  }

  test("跨年周 - 年初") {
    val date = DateTypeUtils.dateFormatLocal.get().parse("20260101")
    val begin = DateTypeUtils.getWeekBegin(std = false, date)
    val end = DateTypeUtils.getWeekEnd(std = false, date)
    assert(begin === "20251228") // 2025-12-28 周一（跨年）
    assert(end === "20260103")   // 2026-01-03 周日
  }

  test("标准格式") {
    val date = DateTypeUtils.dateFormatLocal.get().parse("20260409")
    val beginStd = DateTypeUtils.getWeekBegin(std = true, date)
    val endStd = DateTypeUtils.getWeekEnd(std = true, date)
    assert(beginStd === "2026-04-06")
    assert(endStd === "2026-04-12")
  }
}
```

### 7.2 集成测试

**测试类**：`VariableUtilsTest`

**测试用例**：

```scala
class VariableUtilsTest extends AnyFunSuite {

  test("周变量替换 - 基本功能") {
    val sql = "SELECT * FROM orders WHERE dt >= '${run_week_begin}' AND dt <= '${run_week_end}'"
    val variables = new util.HashMap[String, Any]()
    variables.put("run_date", "20260409")

    val result = VariableUtils.replace(sql, variables)

    assert(result.contains("20260406"))
    assert(result.contains("20260412"))
  }

  test("周变量替换 - 标准格式") {
    val sql = "SELECT * FROM orders WHERE dt >= '${run_week_begin_std}'"
    val variables = new util.HashMap[String, Any]()
    variables.put("run_date", "20260409")

    val result = VariableUtils.replace(sql, variables)

    assert(result.contains("2026-04-06"))
  }

  test("周变量算术运算 - 上周") {
    val sql = "SELECT * FROM orders WHERE dt >= '${run_week_begin - 7}'"
    val variables = new util.HashMap[String, Any]()
    variables.put("run_date", "20260409")

    val result = VariableUtils.replace(sql, variables)

    // 20260406 - 7 = 20260330 (2026-03-30 是周一)
    assert(result.contains("20260330"))
  }

  test("周变量兼容性 - 不影响现有变量") {
    val sql = "SELECT * FROM orders WHERE dt >= '${run_month_begin}' AND dt <= '${run_month_end}'"
    val variables = new util.HashMap[String, Any]()
    variables.put("run_date", "20260409")

    val result = VariableUtils.replace(sql, variables)

    assert(result.contains("20260401")) // 4月1日
  }

  test("周变量混合使用") {
    val sql = """
      SELECT * FROM orders
      WHERE dt >= '${run_week_begin}' AND dt <= '${run_week_end}'
        AND month >= '${run_month_begin}'
    """
    val variables = new util.HashMap[String, Any]()
    variables.put("run_date", "20260409")

    val result = VariableUtils.replace(sql, variables)

    assert(result.contains("20260406"))
    assert(result.contains("20260412"))
    assert(result.contains("20260401"))
  }
}
```

### 7.3 功能测试

**测试场景**：

| 场景 | SQL示例 | 预期结果 |
|------|---------|---------|
| 本周数据查询 | `WHERE dt >= '${run_week_begin}'` | 正确替换为本周一日期 |
| 上周数据查询 | `WHERE dt >= '${run_week_begin - 7}'` | 正确替换为上周一日期 |
| 本周和上周对比 | `${run_week_begin}` 和 `${run_week_begin - 7}` | 两个变量正确计算 |
| 标准格式使用 | `${run_week_begin_std}` | 返回 yyyy-MM-dd 格式 |
| 混合使用 | `${run_week_begin}` 和 `${run_month_begin}` | 两个变量都正确替换 |

---

## 八、性能分析

### 8.1 性能目标

| 指标 | 目标值 | 测量方法 |
|------|--------|---------|
| 周变量计算时间 | < 50ms | JMH 基准测试 |
| 变量替换总时间 | < 100ms | JMH 基准测试 |
| 内存占用增量 | < 1KB | JConsole 监控 |

### 8.2 性能优化措施

1. **复用 SimpleDateFormat**：使用 ThreadLocal 避免重复创建
2. **减少对象创建**：复用 Calendar 实例
3. **避免不必要的转换**：直接使用 Calendar 操作日期

### 8.3 性能测试计划

**测试工具**：JMH (Java Microbenchmark Harness)

**测试代码示例**：

```scala
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class WeekVariablePerformanceTest {

  @Benchmark
  def testWeekBeginCalculation(): Unit = {
    val date = new Date()
    DateTypeUtils.getWeekBegin(std = false, date)
  }

  @Benchmark
  def testWeekEndCalculation(): Unit = {
    val date = new Date()
    DateTypeUtils.getWeekEnd(std = false, date)
  }

  @Benchmark
  def testVariableReplacement(): Unit = {
    val sql = "SELECT * FROM orders WHERE dt >= '${run_week_begin}'"
    val variables = new util.HashMap[String, Any]()
    variables.put("run_date", "20260409")
    VariableUtils.replace(sql, variables)
  }
}
```

---

## 九、兼容性设计

### 9.1 向后兼容性

**影响范围**：
- 不修改现有变量功能
- 不修改现有方法签名
- 仅新增方法和常量

**验证方法**：
- 运行现有单元测试套件
- 执行回归测试

### 9.2 版本兼容性

**最低支持版本**：Linkis 1.18.0+

**依赖**：
- Java 8+（java.util.Calendar 和 java.text.SimpleDateFormat）
- Scala 2.11.x / 2.12.x
- Spring Boot 2.7.x（无需修改）

### 9.3 部署兼容性

**部署方式**：无特殊要求，遵循现有部署流程

**配置变更**：无需修改配置文件

---

## 十、风险评估与缓解

### 10.1 技术风险

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|---------|
| 跨年周计算错误 | 高 | 低 | 充分测试边界场景，使用 Java Calendar API 自动处理 |
| 性能回归 | 中 | 低 | 进行性能基准测试，确保不超过 50ms |
| 与现有变量冲突 | 低 | 低 | 遵循现有命名规范，避免冲突 |

### 10.2 业务风险

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|---------|
| 用户习惯不同（周日为第一天） | 中 | 中 | 明确文档说明周一起始，后续可扩展支持配置 |
| 时区问题 | 低 | 低 | 使用系统默认时区，与现有变量保持一致 |

### 10.3 缓解措施详解

**跨年周计算验证**：
```scala
// 边界测试用例
val testCases = Seq(
  ("20251231", "20251228", "20260103"), // 年末周四
  ("20260101", "20251228", "20260103"), // 年初周五
  ("20200101", "20191230", "20200105"), // 2020年初周三
  ("20191231", "20191230", "20200105")  // 2019年末周二
)

testCases.foreach { case (runDate, expectedBegin, expectedEnd) =>
  val date = DateTypeUtils.dateFormatLocal.get().parse(runDate)
  val begin = DateTypeUtils.getWeekBegin(std = false, date)
  val end = DateTypeUtils.getWeekEnd(std = false, date)
  assert(begin == expectedBegin, s"$runDate: begin mismatch")
  assert(end == expectedEnd, s"$runDate: end mismatch")
}
```

---

## 十一、实施计划

### 11.1 开发阶段

| 阶段 | 任务 | 预计时间 | 交付物 |
|------|------|---------|--------|
| 1 | 在 DateTypeUtils 中添加 getWeekBegin() 和 getWeekEnd() 方法 | 1小时 | 代码实现 |
| 2 | 在 VariableUtils 中添加周变量常量 | 0.5小时 | 代码实现 |
| 3 | 在 initAllDateVars 中添加周变量初始化 | 1小时 | 代码实现 |
| 4 | 编写单元测试 | 1小时 | 测试代码 |
| 5 | 本地功能验证 | 0.5小时 | 验证报告 |

**总计**：约 4 小时

### 11.2 测试阶段

| 阶段 | 任务 | 预计时间 | 交付物 |
|------|------|---------|--------|
| 1 | 单元测试 | 1小时 | 单元测试报告 |
| 2 | 集成测试 | 1小时 | 集成测试报告 |
| 3 | 性能测试 | 0.5小时 | 性能测试报告 |
| 4 | 兼容性测试 | 0.5小时 | 兼容性测试报告 |

**总计**：约 3 小时

### 11.3 评审与发布

| 阶段 | 任务 | 预计时间 | 交付物 |
|------|------|---------|--------|
| 1 | 代码评审 | 1小时 | 评审意见 |
| 2 | 文档更新 | 0.5小时 | 更新后的文档 |
| 3 | 发布说明 | 0.5小时 | Release Notes |

**总计**：约 2 小时

### 11.4 总时间估算

- **开发**：4 小时
- **测试**：3 小时
- **评审与发布**：2 小时
- **总计**：约 9 小时（1-2个工作日）

---

## 十二、附录

### 12.1 周变量完整列表

| 变量名 | 类型 | 格式 | 说明 | 示例 |
|--------|------|------|------|------|
| run_week_begin | DateType | yyyyMMdd | 周开始日期（周一） | 20260406 |
| run_week_begin_std | DateType | yyyy-MM-dd | 周开始日期标准格式 | 2026-04-06 |
| run_week_end | DateType | yyyyMMdd | 周结束日期（周日） | 20260412 |
| run_week_end_std | DateType | yyyy-MM-dd | 周结束日期标准格式 | 2026-04-12 |

### 12.2 使用示例

```sql
-- 示例1：查询本周数据
SELECT * FROM orders
WHERE dt >= '${run_week_begin}' AND dt <= '${run_week_end}'

-- 示例2：查询上周数据
SELECT * FROM orders
WHERE dt >= '${run_week_begin - 7}' AND dt <= '${run_week_end - 7}'

-- 示例3：本周和上周数据对比
SELECT
  SUM(amount) AS current_week_amount
FROM orders
WHERE dt >= '${run_week_begin}' AND dt <= '${run_week_end}'
UNION ALL
SELECT
  SUM(amount) AS last_week_amount
FROM orders
WHERE dt >= '${run_week_begin - 7}' AND dt <= '${run_week_end - 7}'

-- 示例4：使用标准格式日期
SELECT * FROM orders
WHERE dt >= '${run_week_begin_std}' AND dt <= '${run_week_end_std}'

-- 示例5：查询最近两周数据
SELECT * FROM orders
WHERE dt >= '${run_week_begin - 7}' AND dt <= '${run_week_end}'
```

### 12.3 相关文档

- 需求文档：`docs/project-knowledge/requirements/linkis_week_variables_需求.md`
- VariableUtils 源码：`linkis-commons/linkis-common/src/main/scala/org/apache/linkis/common/utils/VariableUtils.scala`
- DateTypeUtils 源码：`linkis-commons/linkis-common/src/main/scala/org/apache/linkis/common/variable/DateTypeUtils.scala`

---

**文档版本**：v1.0
**最后更新**：2026-04-09
**作者**：DevSyncAgent
**审核状态**：待审核
