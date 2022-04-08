/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.governance.common.paser

import org.junit.jupiter.api.{DisplayName, Test}
import org.junit.jupiter.api.Assertions.assertTrue

import java.util.regex.Pattern

class SQLCodeParserTest {

  @Test
  @DisplayName("testParseSql")
  def testParseSql(): Unit = {
    val parser = new SQLCodeParser
    val sqlString: String =
      """
        |-- select device_id, client_id, stats_uv_day,
        |-- stats_uv_week,
        |-- stats_uv_month
        |-- from kbase.test1 limit 10;
        |-- select device_id,
        |-- stats_uv
        |-- from kbase.test1 limit 10;
        |select *
        |-- 这是一条注释
        |from test.test_table3
        |limit 10;
        |select * from test.leo_test
        |-- 这是注释;
        |/** 这也是注释 **/
        |limit 10
        |""".stripMargin
    // sqlString = "select * from test.leo_test"
    val p = Pattern.compile("(?ms)('(?:''|[^'])*')|--.*?$|/\\*.*?\\*/")
    // val p = Pattern.compile("(?ms)('(?:''|[^'])*')|--.*?$|//.*?$|/\\*.*?\\*/|#.*?$|")
    // val result = p.matcher(sqlString).replaceAll("$1")
    val strings = parser.parse(sqlString)
    assertTrue(strings.length == 2)
  }
}
