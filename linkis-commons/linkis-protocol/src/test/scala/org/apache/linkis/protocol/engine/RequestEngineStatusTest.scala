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

package org.apache.linkis.protocol.engine

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class RequestEngineStatusTest {

  @Test
  @DisplayName("constTest")
  def constTest(): Unit = {

    val statusOnly = RequestEngineStatus.Status_Only
    val statusOverload = RequestEngineStatus.Status_Overload
    val statusConcurrent = RequestEngineStatus.Status_Concurrent
    val statusOverloadConcurrent = RequestEngineStatus.Status_Overload_Concurrent
    val statusBasicInfo = RequestEngineStatus.Status_BasicInfo
    val all = RequestEngineStatus.ALL

    Assertions.assertTrue(1 == statusOnly)
    Assertions.assertTrue(2 == statusOverload)
    Assertions.assertTrue(3 == statusConcurrent)
    Assertions.assertTrue(4 == statusOverloadConcurrent)
    Assertions.assertTrue(5 == statusBasicInfo)
    Assertions.assertTrue(6 == all)

  }

}
