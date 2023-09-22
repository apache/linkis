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

package org.apache.linkis.common.utils

import org.apache.linkis.common.variable.{CustomDateType, CustomHourType, DateType, HourType}
import org.apache.linkis.common.variable.DateTypeUtils.{getCurHour, getToday}

import java.util

import scala.collection.mutable

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VariableUtilsTest {

  private val run_date_str = "20200228"

  @Test def testReplace(): Unit = {
    val sql = """select
                |'${run_date}' as run_date,
                |'${run_today}' as run_today,
                |'${run_year_begin}' as run_year_begin,
                |'${run_year_begin_std-3}' as run_year_begin_std_sub3,
                |'${run_year_end+10}' as run_year_end_add10,
                |'${run_year_end_std}' as run_half_year_end_std,
                |'${run_half_year_begin-1}' as run_half_year_begin_sub1,
                |'${run_half_year_begin_std}' as run_half_year_begin_std,
                |'${run_half_year_end}' as run_half_year_end,
                |'${run_today_h+12}' as run_today_h_add1""".stripMargin
    val run_date = new CustomDateType(run_date_str, false)
    val dateType = DateType(run_date)
    // add 1 days
    val dateTypeRes = dateType.calculator("+", "1")
    val hourType = HourType(new CustomHourType(getCurHour(false, dateTypeRes), false))
    val hourTypeRes = hourType.calculator("+", "12")
    val resSql = s"""select
                    |'20200228' as run_date,
                    |'20200229' as run_today,
                    |'20200101' as run_year_begin,
                    |'2017-01-01' as run_year_begin_std_sub3,
                    |'20301231' as run_year_end_add10,
                    |'2020-12-31' as run_half_year_end_std,
                    |'20190701' as run_half_year_begin_sub1,
                    |'2020-01-01' as run_half_year_begin_std,
                    |'20200630' as run_half_year_end,
                    |'${hourTypeRes}' as run_today_h_add1""".stripMargin
    val varMap = new util.HashMap[String, String]()
    varMap.put("run_date", run_date_str)
    assertEquals(VariableUtils.replace(sql, "sql", varMap), resSql)
  }

  @Test
  def testGetCustomVar: Unit = {
    var scalaCode = "" +
      "-------@set globalpara=60--------\n" +
      "--@set globalpara2=66\n" +
      "select ${globalpara} as globalpara,\n" +
      "-- ${globalpara1} as globalpara1, \n" +
      "${globalpara2} as globalpara2;\n"
    var pythonCode = ""

    val nameAndValue: mutable.Map[String, String] =
      VariableUtils.getCustomVar(scalaCode, CodeAndRunTypeUtils.LANGUAGE_TYPE_SQL);
    assertEquals(nameAndValue.size, 2)
  }

}
