/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.common.utils

import com.sun.management.OperatingSystemMXBean
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

import java.lang.management.ManagementFactory

class OverloadUtilsTest {
  @Test private[utils] def testGetOSBean():Unit = {
    assertTrue(OverloadUtils.getOSBean.isInstanceOf[OperatingSystemMXBean])
  }

  @Test private[utils] def testGetProcessMaxMemory():Unit = {
    val a = OverloadUtils.getProcessMaxMemory
    val b = ManagementFactory.getMemoryMXBean.getHeapMemoryUsage.getMax
    assertEquals(a, b)
  }

  @Test private[utils] def testGetProcessUsedMemory():Unit = {
    this.synchronized {
      assertEquals(OverloadUtils.getProcessUsedMemory,
        ManagementFactory.getMemoryMXBean.getHeapMemoryUsage.getUsed)
    }
  }

  @Test private[utils] def testGetSystemCPUUsed():Unit = {
    this.synchronized {
      assertEquals(OverloadUtils.getSystemCPUUsed,
        ManagementFactory.getOperatingSystemMXBean.getSystemLoadAverage.toFloat)
    }
  }

  @Test private[utils] def testGetSystemFreeMemory():Unit = {
    this.synchronized {
      assertEquals(OverloadUtils.getSystemFreeMemory,
        OverloadUtils.getOSBean.getFreePhysicalMemorySize)
    }
  }
}
