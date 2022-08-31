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

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

@Test
class CodeAndRunTypeUtilsTest {

  @Test
  def testGetCodeTypeAndRunTypeRelationMap(): Unit = {
    val codeTypeAndRunTypeRelationMap = CodeAndRunTypeUtils.getCodeTypeAndRunTypeRelationMap
    assertTrue(codeTypeAndRunTypeRelationMap.nonEmpty)
    assertTrue(codeTypeAndRunTypeRelationMap.keySet.contains("sql"))
    assertEquals(3, codeTypeAndRunTypeRelationMap("python").size)
  }

  @Test
  def testGetRunTypeAndCodeTypeRelationMap(): Unit = {
    val runTypeAndCodeTypeRelationMap = CodeAndRunTypeUtils.getRunTypeAndCodeTypeRelationMap
    assertTrue(runTypeAndCodeTypeRelationMap.nonEmpty)
    assertTrue(CodeAndRunTypeUtils.RUN_TYPE_SHELL.equals(runTypeAndCodeTypeRelationMap("sh")))
    assertTrue(CodeAndRunTypeUtils.RUN_TYPE_SQL.equals(runTypeAndCodeTypeRelationMap("psql")))
  }

  @Test
  def testGetRunTypeByCodeType(): Unit = {
    val codeType = "psql"
    val runType = CodeAndRunTypeUtils.getRunTypeByCodeType(codeType)
    assertTrue(CodeAndRunTypeUtils.RUN_TYPE_SQL.equals(runType))
  }

  @Test
  def testGetSuffixBelongToRunTypeOrNot(): Unit = {
    val shell =
      CodeAndRunTypeUtils.getSuffixBelongToRunTypeOrNot("sh", CodeAndRunTypeUtils.RUN_TYPE_SHELL)
    assertTrue(shell)
    val sql =
      CodeAndRunTypeUtils.getSuffixBelongToRunTypeOrNot("jdbc", CodeAndRunTypeUtils.RUN_TYPE_SQL)
    assertTrue(sql)
    val hql =
      CodeAndRunTypeUtils.getSuffixBelongToRunTypeOrNot("hql", CodeAndRunTypeUtils.RUN_TYPE_SQL)
    assertTrue(hql)
    val python =
      CodeAndRunTypeUtils.getSuffixBelongToRunTypeOrNot("py", CodeAndRunTypeUtils.RUN_TYPE_PYTHON)
    assertTrue(python)
    val scala =
      CodeAndRunTypeUtils.getSuffixBelongToRunTypeOrNot("java", CodeAndRunTypeUtils.RUN_TYPE_SCALA)
    assertTrue(!scala)
  }

}
