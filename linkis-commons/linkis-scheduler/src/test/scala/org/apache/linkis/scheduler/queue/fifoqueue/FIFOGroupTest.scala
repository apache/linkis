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

import org.apache.linkis.scheduler.queue.GroupStatus

import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull}
import org.junit.jupiter.api.Test

class FIFOGroupTest {

  @Test
  def testFIFOGroup: Unit = {
    val schedulerContext = new FIFOSchedulerContextImpl(100)
    val group = schedulerContext.getOrCreateGroupFactory
      .getOrCreateGroup(null)
      .asInstanceOf[FIFOGroup]
    group.setMaxAskInterval(10)
    group.setMinAskInterval(8)
    group.setStatus(GroupStatus.USING)
    group.setMaxRunningJobs(10)
    group.setMaxAskExecutorTimes(100L)
    assertEquals(10, group.getMaxAskInterval)
    assertEquals(8, group.getMinAskInterval)
    assertEquals(GroupStatus.USING, group.getStatus)
    assertEquals(10, group.getMaxRunningJobs)
    assertEquals(100L, group.getMaxAskExecutorTimes)
  }

}
