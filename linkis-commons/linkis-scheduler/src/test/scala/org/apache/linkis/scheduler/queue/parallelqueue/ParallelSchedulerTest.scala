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

package org.apache.linkis.scheduler.queue.parallelqueue

import org.apache.linkis.scheduler.SchedulerContext
import org.apache.linkis.scheduler.queue.UserJob
import org.apache.linkis.scheduler.queue.fifoqueue.{
  FIFOGroupFactory,
  FIFOScheduler,
  FIFOSchedulerContextImpl
}

import org.junit.jupiter.api.{BeforeEach, Test}
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull}

class ParallelSchedulerTest {
  var schedulerContext: SchedulerContext = _
  var parallelScheduler: ParallelScheduler = _

  @BeforeEach
  def prepareScheduler: Unit = {
    val schedulerContext = new ParallelSchedulerContextImpl(100)
    parallelScheduler = new ParallelScheduler(schedulerContext)
  }

  @Test
  def testParallelSchedulerInit: Unit = {
    parallelScheduler.init()
    assertEquals("ParallelScheduler", parallelScheduler.getName)
  }

}
