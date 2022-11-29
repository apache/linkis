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

package org.apache.linkis.jobhistory.util

import java.util.Date

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class QueryUtilsTest {

  private val username = "hadoop"

  @Test
  @DisplayName("isJobHistoryAdminTest")
  def isJobHistoryAdminTest(): Unit = {
    val defaultName = "hadoop"
    val otherName = "hadoops"
    val defaultVal = QueryUtils.isJobHistoryAdmin(defaultName)
    val otherVal = QueryUtils.isJobHistoryAdmin(otherName)

    Assertions.assertTrue(defaultVal)
    Assertions.assertFalse(otherVal)
  }

  @Test
  @DisplayName("getJobHistoryAdminTest")
  def getJobHistoryAdminTest(): Unit = {
    val admins = QueryUtils.getJobHistoryAdmin()
    Assertions.assertTrue(admins.exists(username.equalsIgnoreCase))
  }

  @Test
  @DisplayName("dateToStringTest")
  def dateToStringTest(): Unit = {
    val dateStr = QueryUtils.dateToString(new Date)
    Assertions.assertNotNull(dateStr)
  }

  @Test
  @DisplayName("checkNameValidTest")
  def checkNameValidTest(): Unit = {
    val name = "hadoops"
    val bool = QueryUtils.checkNameValid(name)
    Assertions.assertTrue(bool)
  }

}
