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

package org.apache.linkis.jobhistory.conf

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class JobhistoryConfigurationTest {

  @Test
  @DisplayName("constTest")
  def constTest(): Unit = {

    val jobHistorySafeTrigger = JobhistoryConfiguration.JOB_HISTORY_SAFE_TRIGGER
    val entranceSpringName = JobhistoryConfiguration.ENTRANCE_SPRING_NAME.getValue
    val entranceInstanceDelemiter = JobhistoryConfiguration.ENTRANCE_INSTANCE_DELEMITER.getValue
    val updateRetryTimes = JobhistoryConfiguration.UPDATE_RETRY_TIMES.getValue
    val updateRetryInterval = JobhistoryConfiguration.UPDATE_RETRY_INTERVAL.getValue
    val undoneJobMinimum = JobhistoryConfiguration.UNDONE_JOB_MINIMUM_ID.getValue
    val undoneJobRefreshTimeDaily = JobhistoryConfiguration.UNDONE_JOB_REFRESH_TIME_DAILY.getValue

    Assertions.assertTrue(jobHistorySafeTrigger.booleanValue())
    Assertions.assertNotNull(entranceSpringName)
    Assertions.assertNotNull(entranceInstanceDelemiter)

    Assertions.assertTrue(updateRetryTimes == 3)
    Assertions.assertTrue(updateRetryInterval == 3000)

    Assertions.assertTrue(undoneJobMinimum == 0L)
    Assertions.assertNotNull(undoneJobRefreshTimeDaily)
  }

}
