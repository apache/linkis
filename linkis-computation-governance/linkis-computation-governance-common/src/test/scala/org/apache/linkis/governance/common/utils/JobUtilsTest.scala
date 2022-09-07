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

package org.apache.linkis.governance.common.utils

import org.apache.linkis.governance.common.constant.job.JobRequestConstants

import java.util

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class JobUtilsTest {

  @Test
  @DisplayName("testGetJobIdFromMap")
  def testGetJobIdFromMap(): Unit = {
    val map: util.Map[String, Object] = new util.HashMap[String, Object]()
    Assertions.assertNull(JobUtils.getJobIdFromMap(map))
    map.put(JobRequestConstants.JOB_ID, "100")
    Assertions.assertNotNull(JobUtils.getJobIdFromMap(map))
  }

  @Test
  @DisplayName("getJobIdFromStringMap")
  def testGetJobIdFromStringMap(): Unit = {
    val map: util.Map[String, String] = new util.HashMap[String, String]()
    Assertions.assertNull(JobUtils.getJobIdFromStringMap(map))
    map.put(JobRequestConstants.JOB_ID, "100")
    Assertions.assertNotNull(JobUtils.getJobIdFromStringMap(map))
  }

}
