/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.common.variable

import java.util.Calendar

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class DateTypeUtilsTest {

  @Test def testGetCurHour(): Unit = {
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val runDateStr = "20220617"
    val cal: Calendar = Calendar.getInstance()
    val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
    val hourOfDayStd = if (hourOfDay < 10) "0" + hourOfDay else "" + hourOfDay
    val hour = runDateStr + hourOfDayStd
    val curHour = DateTypeUtils.getCurHour(false, runDateStr)
    assertEquals(hour, curHour)
  }

  // ========== Week Variable Tests ==========

  @Test def testGetWeekBegin_Thursday(): Unit = {
    // TC001: getWeekBegin - 周四返回本周一
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20260409") // 2026-04-09 is Thursday
    val result = DateTypeUtils.getWeek(std = false, isEnd = false, date)
    assertEquals("20260406", result) // Monday is 2026-04-06
  }

  @Test def testGetWeekBegin_Monday(): Unit = {
    // TC002: getWeekBegin - 周一返回自身
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20260406") // 2026-04-06 is Monday
    val result = DateTypeUtils.getWeek(std = false, isEnd = false, date)
    assertEquals("20260406", result) // Should return itself
  }

  @Test def testGetWeekBegin_Sunday(): Unit = {
    // TC003: getWeekBegin - 周日返回本周一
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20260412") // 2026-04-12 is Sunday
    val result = DateTypeUtils.getWeek(std = false, isEnd = false, date)
    assertEquals("20260406", result) // Monday is 2026-04-06
  }

  @Test def testGetWeekBegin_StandardFormat(): Unit = {
    // TC004: getWeekBegin - 标准格式
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20260409") // 2026-04-09 is Thursday
    val result = DateTypeUtils.getWeek(std = true, isEnd = false, date)
    assertEquals("2026-04-06", result) // Standard format yyyy-MM-dd
  }

  @Test def testGetWeekEnd_Thursday(): Unit = {
    // TC005: getWeekEnd - 周四返回本周日
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20260409") // 2026-04-09 is Thursday
    val result = DateTypeUtils.getWeek(std = false, isEnd = true, date)
    assertEquals("20260412", result) // Sunday is 2026-04-12
  }

  @Test def testGetWeekEnd_Sunday(): Unit = {
    // TC006: getWeekEnd - 周日返回自身
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20260412") // 2026-04-12 is Sunday
    val result = DateTypeUtils.getWeek(std = false, isEnd = true, date)
    assertEquals("20260412", result) // Should return itself
  }

  @Test def testGetWeekEnd_Monday(): Unit = {
    // TC007: getWeekEnd - 周一返回本周日
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20260406") // 2026-04-06 is Monday
    val result = DateTypeUtils.getWeek(std = false, isEnd = true, date)
    assertEquals("20260412", result) // Sunday is 2026-04-12
  }

  @Test def testCrossYearWeek_EndOfYear(): Unit = {
    // TC008: 跨年周 - 年末(2025-12-31 周三)
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20251231") // 2025-12-31 is Wednesday
    val begin = DateTypeUtils.getWeek(std = false, isEnd = false, date)
    val end = DateTypeUtils.getWeek(std = false, isEnd = true, date)
    assertEquals("20251229", begin) // Monday is 2025-12-29
    assertEquals("20260104", end) // Sunday is 2026-01-04 (cross year)
  }

  @Test def testCrossYearWeek_StartOfYear(): Unit = {
    // TC009: 跨年周 - 年初(2026-01-01 周四)
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20260101") // 2026-01-01 is Thursday
    val begin = DateTypeUtils.getWeek(std = false, isEnd = false, date)
    val end = DateTypeUtils.getWeek(std = false, isEnd = true, date)
    assertEquals("20251229", begin) // Monday is 2025-12-29 (cross year)
    assertEquals("20260104", end) // Sunday is 2026-01-04
  }

  @Test def testLeapYear_2024(): Unit = {
    // TC010: 闰年 - 2024-02-29(闰日, 周四)
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20240229") // 2024-02-29 is leap day, Thursday
    val begin = DateTypeUtils.getWeek(std = false, isEnd = false, date)
    val end = DateTypeUtils.getWeek(std = false, isEnd = true, date)
    assertEquals("20240226", begin) // Monday is 2024-02-26
    assertEquals("20240303", end) // Sunday is 2024-03-03
  }

  @Test def testLeapYear_2020(): Unit = {
    // TC011: 闰年 - 2020-02-29(闰日, 周六)
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20200229") // 2020-02-29 is leap day, Saturday
    val begin = DateTypeUtils.getWeek(std = false, isEnd = false, date)
    val end = DateTypeUtils.getWeek(std = false, isEnd = true, date)
    assertEquals("20200224", begin) // Monday is 2020-02-24
    assertEquals("20200301", end) // Sunday is 2020-03-01
  }

  @Test def testNonLeapYear_February(): Unit = {
    // TC012: 非闰年 - 2023-02-28(周二)
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20230228") // 2023-02-28 is Tuesday
    val begin = DateTypeUtils.getWeek(std = false, isEnd = false, date)
    val end = DateTypeUtils.getWeek(std = false, isEnd = true, date)
    assertEquals("20230227", begin) // Monday is 2023-02-27
    assertEquals("20230305", end) // Sunday is 2023-03-05
  }

  @Test def testEveryDayOfWeek(): Unit = {
    // TC013-TC019: 每日测试(周一到周日)
    val dateFormat = DateTypeUtils.dateFormatLocal.get()

    // Monday
    val monday = dateFormat.parse("20260406")
    assertEquals("20260406", DateTypeUtils.getWeek(std = false, isEnd = false, monday))
    assertEquals("20260412", DateTypeUtils.getWeek(std = false, isEnd = true, monday))

    // Tuesday
    val tuesday = dateFormat.parse("20260407")
    assertEquals("20260406", DateTypeUtils.getWeek(std = false, isEnd = false, tuesday))
    assertEquals("20260412", DateTypeUtils.getWeek(std = false, isEnd = true, tuesday))

    // Wednesday
    val wednesday = dateFormat.parse("20260408")
    assertEquals("20260406", DateTypeUtils.getWeek(std = false, isEnd = false, wednesday))
    assertEquals("20260412", DateTypeUtils.getWeek(std = false, isEnd = true, wednesday))

    // Thursday
    val thursday = dateFormat.parse("20260409")
    assertEquals("20260406", DateTypeUtils.getWeek(std = false, isEnd = false, thursday))
    assertEquals("20260412", DateTypeUtils.getWeek(std = false, isEnd = true, thursday))

    // Friday
    val friday = dateFormat.parse("20260410")
    assertEquals("20260406", DateTypeUtils.getWeek(std = false, isEnd = false, friday))
    assertEquals("20260412", DateTypeUtils.getWeek(std = false, isEnd = true, friday))

    // Saturday
    val saturday = dateFormat.parse("20260411")
    assertEquals("20260406", DateTypeUtils.getWeek(std = false, isEnd = false, saturday))
    assertEquals("20260412", DateTypeUtils.getWeek(std = false, isEnd = true, saturday))

    // Sunday
    val sunday = dateFormat.parse("20260412")
    assertEquals("20260406", DateTypeUtils.getWeek(std = false, isEnd = false, sunday))
    assertEquals("20260412", DateTypeUtils.getWeek(std = false, isEnd = true, sunday))
  }

  // ========== WeekType Arithmetic Tests ==========

  @Test def testWeekType_Subtract_1_Week(): Unit = {
    // TC020: WeekType - 1 means subtract 1 week (7 days), not 1 day
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20260409") // 2026-04-09 is Thursday
    val weekType = WeekType(
      new CustomWeekType("20260409", false, false)
    ) // run_week_begin = 20260406 (Monday)

    val result = weekType.calculator("-", "1") // Subtract 1 week
    val resultDate = dateFormat.parse(result)
    val expected = dateFormat.parse("20260330") // Previous Monday = 2026-03-30

    assertEquals(expected, resultDate)
  }

  @Test def testWeekType_Subtract_7_Weeks(): Unit = {
    // TC021: WeekType - 7 means subtract 7 weeks (49 days)
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20260409") // 2026-04-09 is Thursday
    val weekType = WeekType(
      new CustomWeekType("20260409", false, false)
    ) // run_week_begin = 20260406 (Monday)

    val result = weekType.calculator("-", "7") // Subtract 7 weeks
    val resultDate = dateFormat.parse(result)
    val expected = dateFormat.parse("20251124") // 7 weeks before Monday = 2025-11-24

    assertEquals(expected, resultDate)
  }

  @Test def testWeekType_Add_1_Week(): Unit = {
    // TC022: WeekType + 1 means add 1 week (7 days)
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20260409") // 2026-04-09 is Thursday
    val weekType = WeekType(
      new CustomWeekType("20260409", false, false)
    ) // run_week_begin = 20260406 (Monday)

    val result = weekType.calculator("+", "1") // Add 1 week
    val resultDate = dateFormat.parse(result)
    val expected = dateFormat.parse("20260413") // Next Monday = 2026-04-13

    assertEquals(expected, resultDate)
  }

  @Test def testWeekType_Add_2_Weeks(): Unit = {
    // TC023: WeekType + 2 means add 2 weeks (14 days)
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val date = dateFormat.parse("20260409") // 2026-04-09 is Thursday
    val weekType = WeekType(
      new CustomWeekType("20260409", false, false)
    ) // run_week_begin = 20260406 (Monday)

    val result = weekType.calculator("+", "2") // Add 2 weeks
    val resultDate = dateFormat.parse(result)
    val expected = dateFormat.parse("20260420") // 2 weeks after Monday = 2026-04-20

    assertEquals(expected, resultDate)
  }

  @Test def testWeekType_vs_DateType(): Unit = {
    // TC024: Verify WeekType (-1) != DateType (-1)
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val baseDate = "20260406" // Monday

    val weekType = WeekType(new CustomWeekType(baseDate, false, false))
    val dateType = DateType(new CustomDateType(baseDate, false))

    // WeekType - 1 = Previous Monday (7 days before)
    val weekResult = weekType.calculator("-", "1")

    // DateType - 1 = Previous day (1 day before)
    val dateResult = dateType.calculator("-", "1")

    val weekResultDate = dateFormat.parse(weekResult)
    val dateResultDate = dateFormat.parse(dateResult)
    val expectedWeek = dateFormat.parse("20260330") // Previous Monday
    val expectedDate = dateFormat.parse("20260405") // Previous day (Sunday)

    assertEquals(expectedWeek, weekResultDate)
    assertEquals(expectedDate, dateResultDate)
    // Verify they are different
    assertNotEquals(weekResult, dateResult)
  }

  // ========== CustomWeekType Tests ==========

  @Test def testCustomWeekType_ToString_WeekBegin(): Unit = {
    // TC025: CustomWeekType.toString returns Monday for week begin
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val customWeekType = new CustomWeekType("20260409", false, false) // Thursday, isEnd=false

    val result = customWeekType.toString
    val resultDate = dateFormat.parse(result)
    val expected = dateFormat.parse("20260406") // Monday

    assertEquals(expected, resultDate)
  }

  @Test def testCustomWeekType_ToString_WeekEnd(): Unit = {
    // TC026: CustomWeekType.toString returns Sunday for week end
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val customWeekType = new CustomWeekType("20260409", false, true) // Thursday, isEnd=true

    val result = customWeekType.toString
    val resultDate = dateFormat.parse(result)
    val expected = dateFormat.parse("20260412") // Sunday

    assertEquals(expected, resultDate)
  }

  @Test def testCustomWeekType_Subtract_Week(): Unit = {
    // TC027: CustomWeekType - 1 week
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val customWeekType = new CustomWeekType("20260409", false, false) // Thursday, week begin

    val result = customWeekType.-(1) // Subtract 1 week
    val resultDate = dateFormat.parse(result)
    val expected = dateFormat.parse("20260330") // Previous Monday

    assertEquals(expected, resultDate)
  }

  @Test def testCustomWeekType_Subtract_Weeks_WeekEnd(): Unit = {
    // TC028: CustomWeekType - 2 weeks (week end)
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val customWeekType = new CustomWeekType("20260409", false, true) // Thursday, week end

    val result = customWeekType.-(2) // Subtract 2 weeks
    val resultDate = dateFormat.parse(result)
    val expected = dateFormat.parse("20260329") // 2 weeks before Sunday

    assertEquals(expected, resultDate)
  }

  @Test def testCustomWeekType_Add_Week(): Unit = {
    // TC029: CustomWeekType + 1 week
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val customWeekType = new CustomWeekType("20260409", false, false) // Thursday, week begin

    val result = customWeekType.+(1) // Add 1 week
    val resultDate = dateFormat.parse(result)
    val expected = dateFormat.parse("20260413") // Next Monday

    assertEquals(expected, resultDate)
  }

  @Test def testCustomWeekType_Add_Weeks_WeekEnd(): Unit = {
    // TC030: CustomWeekType + 3 weeks (week end)
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val customWeekType = new CustomWeekType("20260409", false, true) // Thursday, week end

    val result = customWeekType.+(3) // Add 3 weeks
    val resultDate = dateFormat.parse(result)
    val expected = dateFormat.parse("20260503") // 3 weeks after Sunday

    assertEquals(expected, resultDate)
  }

  @Test def testCustomWeekType_StandardFormat(): Unit = {
    // TC031: CustomWeekType with standard format
    val customWeekType = new CustomWeekType("20260409", true, false) // std=true

    val result = customWeekType.toString
    assertEquals("2026-04-06", result) // Standard format yyyy-MM-dd
  }

  @Test def testCustomWeekType_CrossYear(): Unit = {
    // TC032: CustomWeekType with cross-year week
    val dateFormat = DateTypeUtils.dateFormatLocal.get()
    val customWeekType = new CustomWeekType("20251231", false, false) // End of year

    val result = customWeekType.toString
    val resultDate = dateFormat.parse(result)
    val expected = dateFormat.parse("20251229") // Monday in previous year

    assertEquals(expected, resultDate)
  }

  @Test def testCustomWeekType_IsEnd_Preservation(): Unit = {
    // TC033: CustomWeekType preserves isEnd during arithmetic operations
    val dateFormat = DateTypeUtils.dateFormatLocal.get()

    // Week begin - 1 should return Monday
    val weekBegin = new CustomWeekType("20260409", false, false)
    val beginResult = weekBegin.-(1)
    val beginResultDate = dateFormat.parse(beginResult)
    assertEquals("20260330", beginResult) // Previous Monday

    // Week end - 1 should return Sunday
    val weekEnd = new CustomWeekType("20260409", false, true)
    val endResult = weekEnd.-(1)
    val endResultDate = dateFormat.parse(endResult)
    assertEquals("20260329", endResult) // Previous Sunday
  }

}
