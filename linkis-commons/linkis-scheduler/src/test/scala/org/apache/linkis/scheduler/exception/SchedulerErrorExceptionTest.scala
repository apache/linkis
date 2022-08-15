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

package org.apache.linkis.scheduler.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SchedulerErrorExceptionTest {

  @Test
  def testSchedulerErrorException: Unit = {
    val des = "testSchedulerErrorExceptionTest"
    val localIp = "127.0.0.1";
    val errorCode = 25000
    val schedulerErrorException = new SchedulerErrorException(errorCode, des);
    schedulerErrorException.setIp(localIp)
    assertEquals(des, schedulerErrorException.getDesc)
    assertEquals(25000, schedulerErrorException.getErrCode)
    assertEquals(localIp, schedulerErrorException.getIp)
  }

}
