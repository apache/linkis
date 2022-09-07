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

import org.apache.linkis.common.variable.DateTypeUtils.{getCurHour, getMonthDay}

import java.util.Calendar

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VariableTypeTest {

  private val run_date_str = "20200228"

  @Test def testDateType(): Unit = {
    val run_date = new CustomDateType(run_date_str, false)
    val dateType = DateType(run_date)
    val dateTypeRes = dateType.calculator("+", "10")
    assertEquals(dateTypeRes, "20200309")
  }

  @Test def testMonthType(): Unit = {
    val monthType = MonthType(new CustomMonthType(run_date_str, false))
    val monthTypeRes = monthType.calculator("+", "3")
    assertEquals(monthTypeRes, "20200501")
    val monthEndType = MonthType(new CustomMonthType(run_date_str, false, true))
    val monthEndTypeRes = monthEndType.calculator("+", "3")
    assertEquals(monthEndTypeRes, "20200531")
  }

  @Test def testMonType(): Unit = {
    val run_date = new CustomDateType(run_date_str, false)
    val run_mon = new CustomMonType(getMonthDay(false, run_date.getDate), false)
    val run_mon_end = new CustomMonType(getMonthDay(false, run_date.getDate), false, true)
    val monType = MonType(run_mon)
    val monTypeRes = monType.calculator("+", "1")
    assertEquals(monTypeRes, "202003")
    val monTypeEnd = MonType(run_mon_end)
    val monTypeEndRes = monTypeEnd.calculator("+", "1")
    assertEquals(monTypeEndRes, "202012")
  }

  @Test def testQuarterType(): Unit = {
    val quarterType = QuarterType(new CustomQuarterType(run_date_str, false))
    val quarterTypeRes = quarterType.calculator("+", "1")
    assertEquals(quarterTypeRes, "20200401")
    val quarterTypeEnd = QuarterType(new CustomQuarterType(run_date_str, false, true))
    val quarterTypeEndRes = quarterTypeEnd.calculator("+", "1")
    assertEquals(quarterTypeEndRes, "20200630")
  }

  @Test def testHalfYearType(): Unit = {
    val halfYearType = HalfYearType(new CustomHalfYearType(run_date_str, false))
    val halfYearTypeRes = halfYearType.calculator("+", "1")
    assertEquals(halfYearTypeRes, "20200701")
    val halfYearTypeEnd = HalfYearType(new CustomHalfYearType(run_date_str, false, true))
    val halfYearTypeEndRes = halfYearTypeEnd.calculator("+", "1")
    assertEquals(halfYearTypeEndRes, "20201231")
  }

  @Test def testYearType(): Unit = {
    val yearType = YearType(new CustomYearType(run_date_str, false))
    val yearTypeRes = yearType.calculator("+", "1")
    assertEquals(yearTypeRes, "20210101")
    val yearTypeEnd = YearType(new CustomYearType(run_date_str, false, true))
    val yearTypeEndRes = yearTypeEnd.calculator("+", "1")
    assertEquals(yearTypeEndRes, "20211231")
  }

  @Test def testHourType(): Unit = {
    val hourType = HourType(new CustomHourType(getCurHour(false, run_date_str), false))
    // add 24 h
    val hourTypeRes = hourType.calculator("+", "24")
    val run_date = new CustomDateType(run_date_str, false)
    val dateType = DateType(run_date)
    // add 1 days
    val dateTypeRes = dateType.calculator("+", "1")
    val cal: Calendar = Calendar.getInstance()
    val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
    val hourOfDayStd = if (hourOfDay < 10) "0" + hourOfDay else "" + hourOfDay
    val hour = dateTypeRes + hourOfDayStd
    assertEquals(hourTypeRes, hour)
  }

}
