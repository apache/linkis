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

package org.apache.linkis.common.utils

import org.junit.jupiter.api.Test

class HardwareUtilsTest {

  @Test private[utils] def testGetAvailableMemory() = {
    val availableMemory = HardwareUtils.getAvailableMemory()
    assert(availableMemory > 1000L)
  }

  @Test private[utils] def testGetMaxMemory() = {
    val maxMemory = HardwareUtils.getMaxMemory()
    assert(maxMemory > 1000L)
  }

  @Test private[utils] def testGetMaxLogicalCore() = {
    val maxCore = HardwareUtils.getMaxLogicalCore()
    assert(maxCore >= 1)
  }

  @Test private[utils] def testGetTotalAndAvailableMemory() = {
    val (maxMemory, availableMemory) = HardwareUtils.getTotalAndAvailableMemory()
    assert(maxMemory >= availableMemory)
  }

  @Test private[utils] def testMemoryUsage() = {
    val usage = HardwareUtils.memoryUsage()
    assert(usage >= 0.000)
  }

  @Test private[utils] def testloadAverageUsage() = {
    val usage = HardwareUtils.loadAverageUsage()
    assert(usage >= 0.000)
  }

}
