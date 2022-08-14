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

package org.apache.linkis.common.collection

import org.assertj.core.api.Assertions.{assertThat, assertThatThrownBy}
import org.assertj.core.api.ThrowableAssert.ThrowingCallable
import org.junit.jupiter.api.{BeforeEach, Test}

class LoopArrayTest {

  private var loopArray: LoopArray[Int] = _
  private val capability = 4
  private val total = 10
  private val realSize = 8

  @BeforeEach private[collection] def setUp(): Unit = {
    loopArray = LoopArray(capability)
    for (i <- 0 to total) {
      loopArray.add(i)
    }
  }

  @Test private[collection] def add(): Unit = {
    val removed = loopArray.add(realSize)
    assertThat(removed).isEqualTo(realSize - 1)
    assertThat(loopArray.max).isEqualTo(total + 1)
    assertThat(loopArray.min).isEqualTo(realSize + 1)
  }

  @Test private[collection] def clear(): Unit = {
    loopArray.clear()
    assertThat(loopArray.max).isEqualTo(0)
    assertThat(loopArray.min).isEqualTo(0)
  }

  @Test private[collection] def get(): Unit = {
    assertThatThrownBy(new ThrowingCallable {
      override def call(): Unit = {
        loopArray.get(realSize - 1)
      }
    }).isInstanceOf(classOf[IllegalArgumentException])
    assertThat(loopArray.get(total)).isEqualTo(total)
    assertThatThrownBy(new ThrowingCallable {
      override def call(): Unit = {
        loopArray.get(total + 1)
      }
    }).isInstanceOf(classOf[IllegalArgumentException])
  }

}
