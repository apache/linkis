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

package org.apache.linkis.scheduler.conf

import org.apache.linkis.common.conf.{CommonVars, TimeType}

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchedulerConfigurationTest {

  @Test
  def testSchedulerConfiguration: Unit = {

    val fifoConsumerAutoClearEnabled =
      CommonVars("wds.linkis.fifo.consumer.auto.clear.enabled", true)
    assertEquals(
      SchedulerConfiguration.FIFO_CONSUMER_AUTO_CLEAR_ENABLED,
      fifoConsumerAutoClearEnabled
    )
    val fifoConsumerMaxIdleTime =
      CommonVars("wds.linkis.fifo.consumer.max.idle.time", new TimeType("10m")).getValue.toLong
    assertEquals(SchedulerConfiguration.FIFO_CONSUMER_MAX_IDLE_TIME, fifoConsumerMaxIdleTime)
    val fifoConsumerIdleScanInitTime =
      CommonVars("wds.linkis.fifo.consumer.idle.scan.init.time", new TimeType("1s")).getValue.toLong
    assertEquals(
      SchedulerConfiguration.FIFO_CONSUMER_IDLE_SCAN_INIT_TIME.getValue.toLong,
      fifoConsumerIdleScanInitTime
    )
  }

}
