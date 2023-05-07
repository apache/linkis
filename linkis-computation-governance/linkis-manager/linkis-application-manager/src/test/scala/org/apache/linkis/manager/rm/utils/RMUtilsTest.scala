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

package org.apache.linkis.manager.rm.utils

import org.junit.jupiter.api.Test

class RMUtilsTest {

  @Test def getResourceInfoMsg(): Unit = {
    val resourceType: String = "Memory"
    val unitType: String = "bytes"
    val requestResource: Any = "644245094400" // 600G
    val availableResource: Any = "-2147483648" // -2G
    val maxResource: Any = "20454781747200" // 19050G
    val result = RMUtils.getResourceInfoMsg(
      resourceType,
      unitType,
      requestResource,
      availableResource,
      maxResource
    )
    assert(
      " user Memory, requestResource : 600GB > availableResource : -2GB,  maxResource : 19050GB."
        .equals(result)
    )
  }

}
