# Linkis SQL 查询增加周变量 - 需求文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 需求ID | LINKIS-FEATURE-WEEK-VAR-001 |
| 需求名称 | Linkis SQL 查询增加周变量 |
| 需求类型 | 新增功能（FEATURE） |
| 基础模块 | linkis-commons / linkis-entrance |
| 当前版本 | dev-1.18.0-webank |
| 创建时间 | 2026-04-09 |
| 文档状态 | 待评审 |

---

## 一、功能概述

### 1.1 功能名称

Linkis SQL 查询增加周变量支持

### 1.2 功能描述

在 Linkis 现有日期变量系统（日期、月份、季度、半年、年度）基础上，新增**周相关变量**，支持：
- 基于运行日期（run_date）计算周相关的系统变量
- 中国习惯周计算方式
- 提供周数、周开始日期、周结束日期等变量
- 支持周变量的算术运算（如 `${run_week - 1}`）
- 与现有变量系统完全兼容

### 1.3 一句话描述

为 Linkis 变量系统增加周变量，支持按周进行数据查询和周期性任务调度。

---

## 二、功能背景

### 2.1 当前痛点

**当前遇到的问题**：

Linkis 现有变量系统已支持：
- 日期变量：`run_date`、`run_today` 等
- 月份变量：`run_month_begin`、`run_month_end` 等
- 季度变量：`run_quarter_begin`、`run_quarter_end` 等
- 年度变量：`run_year_begin`、`run_year_end` 等

但在实际业务场景中，**周维度**的数据查询和分析非常常见：
- 周报数据查询：每周一统计上周数据
- 周期性任务：每周执行的数据分析任务
- 周同比分析：本周数据与上周数据对比
- 周滚动窗口：最近 N 周的数据聚合

**期望达到的目标**：

提供标准化的周变量，支持用户通过简单的变量语法实现周维度数据查询，无需手动计算周相关的日期。

### 2.2 现有功能

**当前实现**：
- 变量替换机制：`VariableUtils.scala`
- 变量语法：`${变量名}` 或 `${变量名 运算符 数值}`
- 变量类型：DateType、MonthType、QuarterType、YearType、HourType
- 代码位置：`linkis-commons/linkis-common/src/main/scala/org/apache/linkis/common/utils/VariableUtils.scala`

**功能定位**：
- 本需求是对现有变量系统的扩展
- 新增 WeekType 变量类型
- 集成到 `initAllDateVars` 方法中自动初始化

---

## 三、核心功能

### 3.1 功能优先级

| 优先级 | 功能点 | 说明 |
|--------|--------|------|
| P0 | 周日期范围变量 | run_week_begin、run_week_begin_std、run_week_end、run_week_end_std |
| P1 | 周变量算术运算 | 支持 run_week_begin + 1 等运算 |

### 3.2 功能详细规格

#### 3.2.1 P0功能：周日期范围变量

**变量列表**：

| 变量名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| `run_week_begin` | DateType | 周开始日期 | 20260406 |
| `run_week_begin_std` | DateType | 周开始日期标准格式 | 2026-04-06 |
| `run_week_end` | DateType | 周结束日期 | 20260412 |
| `run_week_end_std` | DateType | 周结束日期标准格式 | 2026-04-12 |

**计算规则**：
- 周一为每周的第一天
- 周日为每周的最后一天
- 基于 `run_date` 计算所属周的开始和结束日期

#### 3.2.2 P0功能：周变量使用示例

**SQL 示例**：

```sql
-- 查询本周数据（基于 run_date 所属周）
SELECT * FROM orders
WHERE dt >= '${run_week_begin}' AND dt <= '${run_week_end}'

-- 查询上周数据
SELECT * FROM orders
WHERE dt >= '${run_week_begin - 7}' AND dt <= '${run_week_end - 7}'

-- 本周和上周数据对比
SELECT
  SUM(amount) AS current_week_amount
FROM orders
WHERE dt >= '${run_week_begin}' AND dt <= '${run_week_end}'
UNION ALL
SELECT
  SUM(amount) AS last_week_amount
FROM orders
WHERE dt >= '${run_week_begin - 7}' AND dt <= '${run_week_end - 7}'

-- 使用标准格式日期
SELECT * FROM orders
WHERE dt >= '${run_week_begin_std}' AND dt <= '${run_week_end_std}'
```

---

## 四、技术方案

### 4.1 修改 VariableUtils

**文件位置**：
`linkis-commons/linkis-common/src/main/scala/org/apache/linkis/common/utils/VariableUtils.scala`

**修改点 1**：添加周变量常量
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
}
```

**修改点 2**：在 `initAllDateVars` 方法中添加周变量初始化
```scala
private def initAllDateVars(
    run_date: CustomDateType,
    nameAndType: mutable.Map[String, variable.VariableType]
): Unit = {
  // ... 现有代码 ...

  // 新增：初始化周变量
  val runDateStr = run_date.toString
  val weekBegin = calculateWeekBegin(runDateStr)
  val weekEnd = calculateWeekEnd(runDateStr)

  nameAndType("run_week_begin") = variable.DateType(new CustomDateType(weekBegin, false))
  nameAndType("run_week_begin_std") = variable.DateType(new CustomDateType(weekBegin, true))
  nameAndType("run_week_end") = variable.DateType(new CustomDateType(weekEnd, false))
  nameAndType("run_week_end_std") = variable.DateType(new CustomDateType(weekEnd, true))
}
```

### 4.2 新增周日期计算方法

**在 VariableUtils 中添加以下方法**：

```scala
/**
 * 计算周开始日期（周一）
 * @param dateStr 日期字符串 yyyyMMdd 或 yyyy-MM-dd
 * @return 周一日期字符串 yyyyMMdd
 */
private def calculateWeekBegin(dateStr: String): String = {
  val dateFormat = new SimpleDateFormat("yyyyMMdd")
  val date = if (dateStr.contains("-")) {
    new SimpleDateFormat("yyyy-MM-dd").parse(dateStr)
  } else {
    dateFormat.parse(dateStr)
  }

  val calendar = Calendar.getInstance()
  calendar.setTime(date)
  val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

  // 调整到周一
  val daysToMonday = dayOfWeek - Calendar.MONDAY
  if (daysToMonday < 0) {
    calendar.add(Calendar.DAY_OF_MONTH, -7 - daysToMonday)
  } else {
    calendar.add(Calendar.DAY_OF_MONTH, -daysToMonday)
  }

  dateFormat.format(calendar.getTime)
}

/**
 * 计算周结束日期（周日）
 * @param dateStr 日期字符串 yyyyMMdd 或 yyyy-MM-dd
 * @return 周日日期字符串 yyyyMMdd
 */
private def calculateWeekEnd(dateStr: String): String = {
  val dateFormat = new SimpleDateFormat("yyyyMMdd")
  val date = if (dateStr.contains("-")) {
    new SimpleDateFormat("yyyy-MM-dd").parse(dateStr)
  } else {
    dateFormat.parse(dateStr)
  }

  val calendar = Calendar.getInstance()
  calendar.setTime(date)
  val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

  // 调整到周日
  val daysToSunday = Calendar.SUNDAY - dayOfWeek
  if (daysToSunday >= 0) {
    calendar.add(Calendar.DAY_OF_MONTH, daysToSunday)
  } else {
    calendar.add(Calendar.DAY_OF_MONTH, 7 + daysToSunday)
  }

  dateFormat.format(calendar.getTime)
}
```

**文件位置**：
`linkis-commons/linkis-common/src/main/scala/org/apache/linkis/common/utils/VariableUtils.scala`

**修改点 1**：添加周变量常量
```scala
object VariableUtils extends Logging {
  val RUN_DATE = "run_date"
  val RUN_TODAY_H = "run_today_h"
  val RUN_TODAY_HOUR = "run_today_hour"

  // 新增：周变量常量
  val RUN_WEEK = "run_week"
  val RUN_WEEK_STD = "run_week_std"
  val RUN_WEEK_NUM = "run_week_num"
  val RUN_WEEK_YEAR = "run_week_year"
  // ... 其他周变量常量
}
```

**修改点 2**：在 `initAllDateVars` 方法中添加周变量初始化
```scala
private def initAllDateVars(
    run_date: CustomDateType,
    nameAndType: mutable.Map[String, variable.VariableType]
): Unit = {
  // ... 现有代码 ...

  // 新增：初始化周变量
  val run_week = new CustomWeekType(run_date.toString, false)
  nameAndType("run_week") = WeekType(run_week)
  nameAndType("run_week_std") = WeekType(new CustomWeekType(run_week.getStandardFormat, true))
  nameAndType("run_week_num") = variable.DoubleValue(run_week.getWeekNum.toDouble)
  nameAndType("run_week_year") = variable.DoubleValue(run_week.getYear.toDouble)
  nameAndType("run_week_begin") = variable.DateType(new CustomDateType(run_week.getWeekBegin, false))
  nameAndType("run_week_begin_std") = variable.DateType(new CustomDateType(run_week.getWeekBegin, true))
  nameAndType("run_week_end") = variable.DateType(new CustomDateType(run_week.getWeekEnd, false))
  nameAndType("run_week_end_std") = variable.DateType(new CustomDateType(run_week.getWeekEnd, true))

  // 本周变量
  val run_today = new CustomDateType(getToday(false, run_date + 1), false)
  val run_week_now = new CustomWeekType(run_today.toString, false)
  nameAndType("run_week_now") = WeekType(run_week_now)
  nameAndType("run_week_now_std") = WeekType(new CustomWeekType(run_week_now.getStandardFormat, true))
  nameAndType("run_week_now_begin") = variable.DateType(new CustomDateType(run_week_now.getWeekBegin, false))
  nameAndType("run_week_now_end") = variable.DateType(new CustomDateType(run_week_now.getWeekEnd, false))

  // 上周变量
  val run_last_week = run_week - 1
  nameAndType("run_last_week") = WeekType(run_last_week)
  nameAndType("run_last_week_begin") = variable.DateType(new CustomDateType(run_last_week.getWeekBegin, false))
  nameAndType("run_last_week_end") = variable.DateType(new CustomDateType(run_last_week.getWeekEnd, false))
}
```

---

## 五、非功能需求

### 5.1 性能要求

- 变量初始化性能：周变量计算不应超过 50ms
- 不影响现有变量系统的性能

### 5.2 兼容性要求

- 向后兼容：不影响现有日期、月份、季度等变量
- 代码兼容：支持 Java 8+

### 5.3 安全性要求

- 周变量不涉及敏感信息
- 日志记录符合现有安全规范

### 5.4 可维护性要求

- 遵循 Linkis 项目编码规范
- 添加详细的代码注释
- 提供单元测试覆盖

---

## 六、验收标准

| ID | 验收项 | 验证方式 | 优先级 |
|-----|-------|---------|--------|
| AC-001 | run_week_begin 正确返回周一日期 | SQL 查询验证 | P0 |
| AC-002 | run_week_end 正确返回周日日期 | SQL 查询验证 | P0 |
| AC-003 | run_week_begin_std 返回标准格式日期 | SQL 查询验证 | P0 |
| AC-004 | run_week_end_std 返回标准格式日期 | SQL 查询验证 | P0 |
| AC-005 | 支持周变量算术运算 | SQL 中使用 ${run_week_begin + 7} | P1 |
| AC-006 | 不影响现有变量系统 | 执行现有 SQL 验证 | P0 |
| AC-007 | 周一为每周第一天 | 验证周一日期为周开始 | P0 |

---

## 七、测试场景

### 7.1 功能测试

| 场景 | 输入 | 预期结果 |
|------|------|---------|
| 正常周查询 | run_date=2026-04-09 | run_week_begin=20260406, run_week_end=20260412 |
| 年初周查询 | run_date=2026-01-03 | run_week_begin=20260101（正确处理跨年周） |
| 年末周查询 | run_date=2025-12-31 | run_week_end=20260102（正确处理跨年周） |
| 算术运算 | ${run_week_begin + 7} | 返回下周一日期 |

### 7.2 边界测试

| 场景 | 输入 | 预期结果 |
|------|------|---------|
| 闰年2月 | run_date=2024-02-29 | 正确计算周范围 |
| 年末 | run_date=2025-12-31 | 正确处理 |
| 年初 | run_date=2026-01-01 | 正确处理 |

### 7.3 兼容性测试

| 场景 | 预期结果 |
|------|---------|
| 现有变量仍可用 | run_date、run_month 等正常工作 |
| 混合使用变量 | SQL 中同时使用日期和周变量 |

---

## 八、风险与依赖

### 9.1 风险

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| 跨年周处理错误 | 高 | 充分测试边界场景 |
| 性能影响 | 低 | 算法优化，性能测试验证 |

### 9.2 依赖

- Java 8+（java.time API）
- Linkis 1.18.0+
- 现有 VariableUtils 框架

---

## 九、实施计划

| 阶段 | 内容 | 预计时间 |
|------|------|---------|
| 需求评审 | 需求文档评审确认 | 0.5天 |
| 设计评审 | 技术方案评审确认 | 0.5天 |
| 开发实现 | 在 VariableUtils 中添加周变量支持 | 1天 |
| 单元测试 | 周日期计算逻辑单元测试 | 1天 |
| 集成测试 | 功能测试和兼容性测试 | 1天 |
| 代码评审 | Code Review | 0.5天 |

---

## 附录

### 附录A：变量完整列表

| 变量名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| run_week_begin | DateType | 周开始日期 | 20260406 |
| run_week_begin_std | DateType | 周开始日期标准格式 | 2026-04-06 |
| run_week_end | DateType | 周结束日期 | 20260412 |
| run_week_end_std | DateType | 周结束日期标准格式 | 2026-04-12 |

### 附录B：参考代码位置

- VariableUtils: `linkis-commons/linkis-common/src/main/scala/org/apache/linkis/common/utils/VariableUtils.scala`
- CustomDateType: `linkis-commons/linkis-common/src/main/scala/org/apache/linkis/common/variable/CustomDateType.scala`
