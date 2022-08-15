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

package org.apache.linkis.scheduler.queue.fifoqueue

import org.junit.jupiter.api.Assertions.{assertFalse, assertNotNull, assertNull, assertTrue}
import org.junit.jupiter.api.Test

class FIFOConsumerManagerTest {

  @Test
  def testSetSchedulerContext: Unit = {
    val schedulerContext = new FIFOSchedulerContextImpl(100)
    val consumerManager = new FIFOConsumerManager
    consumerManager.setSchedulerContext(schedulerContext)
    assertNotNull(consumerManager.listConsumers())
  }

  @Test
  def testListConsumers: Unit = {
    val schedulerContext = new FIFOSchedulerContextImpl(100)
    val consumerManager = new FIFOConsumerManager
    assertNull(consumerManager.listConsumers()(0))
    consumerManager.setSchedulerContext(schedulerContext)
    assertNotNull(consumerManager.listConsumers()(0))
  }

  @Test
  def testShutdown: Unit = {
    val schedulerContext = new FIFOSchedulerContextImpl(100)
    val consumerManager = new FIFOConsumerManager
    consumerManager.setSchedulerContext(schedulerContext)
    assertFalse(consumerManager.listConsumers()(0).terminate)
    try {
      try consumerManager.shutdown()
    } catch {
      case e => e.printStackTrace
    }
    assertTrue(consumerManager.listConsumers()(0).terminate)
  }

}
