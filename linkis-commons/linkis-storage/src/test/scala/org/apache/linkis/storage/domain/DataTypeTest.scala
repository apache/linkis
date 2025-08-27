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

package org.apache.linkis.storage.domain

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class DataTypeTest {

  @Test
  @DisplayName("constTest")
  def constTest(): Unit = {

    val nullvalue = Dolphin.NULL
    val lowcasenullvalue = DataType.LOWCASE_NULL_VALUE

    Assertions.assertEquals("NULL", nullvalue)
    Assertions.assertEquals("null", lowcasenullvalue)

  }

  @Test
  @DisplayName("isNullTest")
  def isNullTest(): Unit = {

    val bool = DataType.isNull(null)
    Assertions.assertTrue(bool)

  }

  @Test
  @DisplayName("isNumberNullTest")
  def isNumberNullTest(): Unit = {

    val bool = DataType.isNumberNull("123")
    Assertions.assertFalse(bool)

  }

  @Test
  @DisplayName("valueToStringTest")
  def valueToStringTest(): Unit = {

    val str = DataType.valueToString("123")
    Assertions.assertNotNull(str)

  }

  @Test
  @DisplayName("toValueTest")
  def toValueTest(): Unit = {
    val dateType = DataType.toDataType("double")
    val str = DataType.toValue(dateType, "NaN")
    Assertions.assertNotNull(str)
  }

  @Test
  @DisplayName("decimalTest")
  def decimalTest(): Unit = {
    val dateType = DataType.toDataType("decimal(10, 8)")
    Assertions.assertTrue(dateType.typeName.equals("decimal"))
  }

}
