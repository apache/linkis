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

class ArrayUtilsTest {

  @Test private[utils] def testCopyArray() = {
    val array = ArrayUtils.newArray[scala.Int](2, Array.emptyIntArray.getClass)
    array(0) = 123
    array(1) = 456
    val newArray = ArrayUtils.copyArray(array)
    assertEquals("Array(123, 456)", newArray.mkString("Array(", ", ", ")"))
    val newArray1 = ArrayUtils.copyArray(array, 1)
    assertEquals(1, newArray1.length)
    assertEquals("Array(123)", newArray1.mkString("Array(", ", ", ")"))
    val newArray2 = ArrayUtils.copyArray(array, 10)
    assertEquals(10, newArray2.length)
    assertEquals(123, newArray2(0))
    assertEquals(456, newArray2(1))
    assertEquals(0, newArray2(2))
  }

  @Test private[utils] def testCopyArrayWithClass() = {
    val array = ArrayUtils.newArray[scala.Int](3, Array.emptyIntArray.getClass)
    val newArray = ArrayUtils.copyArrayWithClass(array, array(0).getClass)
    assertEquals(newArray.getClass, Array.emptyIntArray.getClass)
    assertTrue(newArray.isInstanceOf[Array[scala.Int]])
  }

  @Test private[utils] def testNewArray() = {
    val array = ArrayUtils.newArray[scala.Int](0, Array.emptyIntArray.getClass)
    assertEquals(array.getClass, Array.emptyIntArray.getClass)
    assertTrue(array.isInstanceOf[Array[scala.Int]])
  }

  @Test private[utils] def testCopyScalaArray() = {
    val array = ArrayUtils.newArray[scala.Int](2, Array.emptyIntArray.getClass)
    array(0) = 123
    array(1) = 456
    val newArray = ArrayUtils.copyScalaArray(array)
    assertEquals("Array(123, 456)", newArray.mkString("Array(", ", ", ")"))
    val newArray1 = ArrayUtils.copyScalaArray(array, 1)
    assertEquals(1, newArray1.length)
    assertEquals("Array(123)", newArray1.mkString("Array(", ", ", ")"))
    val newArray2 = ArrayUtils.copyScalaArray(array, 10)
    assertEquals(10, newArray2.length)
    assertEquals(123, newArray2(0))
    assertEquals(456, newArray2(1))
    assertEquals(0, newArray2(2))
  }

}
