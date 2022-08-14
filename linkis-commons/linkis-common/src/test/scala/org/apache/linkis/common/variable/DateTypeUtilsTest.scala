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

}
