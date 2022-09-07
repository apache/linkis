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

class LinkisJobRetryExceptionTest {

  @Test
  def testLinkisJobRetryException: Unit = {
    val des = "testLinkisJobRetryExceptionTest"
    val localIp = "127.0.0.1";
    val linkisJobRetryException = new LinkisJobRetryException(des);
    linkisJobRetryException.setIp(localIp)
    assertEquals(des, linkisJobRetryException.getDesc)
    assertEquals(25000, linkisJobRetryException.getErrCode)
    assertEquals(localIp, linkisJobRetryException.getIp)
  }

}
