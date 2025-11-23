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

package org.apache.linkis.scheduler

import org.apache.linkis.scheduler.queue.UserJob
import org.apache.linkis.scheduler.queue.fifoqueue.{FIFOScheduler, FIFOSchedulerContextImpl}

import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull, assertTrue}
import org.junit.jupiter.api.Test

class AbstractSchedulerTest {

  @Test
  def testSubmit: Unit = {
    val userJob = UserJob()
    val schedulerContext = new FIFOSchedulerContextImpl(100)
    val fIFOScheduler = new FIFOScheduler(schedulerContext)
    fIFOScheduler.submit(userJob)
    assertNotNull(
      fIFOScheduler.getSchedulerContext.getOrCreateConsumerManager
        .getOrCreateConsumer("FIFO-Group")
    )
    assertNotNull(fIFOScheduler.get(userJob))
  }

  @Test
  def testShutdown: Unit = {
    val schedulerContext = new FIFOSchedulerContextImpl(100)
    val fIFOScheduler = new FIFOScheduler(schedulerContext)
    fIFOScheduler.shutdown
    assertTrue(
      fIFOScheduler.getSchedulerContext.getOrCreateConsumerManager.getOrCreateExecutorService.isShutdown
    )
  }

}
