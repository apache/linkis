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

package org.apache.linkis.hadoop.common.conf

import org.apache.linkis.hadoop.common.utils.HDFSUtils

import org.junit.jupiter.api.{Assertions, Test}

class HDFSUtilsTest {

  @Test
  def testDefaultKerberosConfiguration: Unit = {
    // isKerberosEnabled
    Assertions.assertFalse(HDFSUtils.isKerberosEnabled(null))
    Assertions.assertTrue(HDFSUtils.isKerberosEnabled("cluster2"))

    // getKerberosUser
    Assertions.assertEquals("user-test", HDFSUtils.getKerberosUser("user-test", null))
    Assertions.assertEquals("user-test", HDFSUtils.getKerberosUser("user-test", "cluster3"))
    Assertions.assertEquals(
      "user-test/127.0.0.3",
      HDFSUtils.getKerberosUser("user-test", "cluster2")
    )

    // isKeytabProxyUserEnabled
    Assertions.assertFalse(HDFSUtils.isKeytabProxyUserEnabled(null))
    Assertions.assertTrue(HDFSUtils.isKeytabProxyUserEnabled("cluster2"))

    // getKeytabSuperUser
    Assertions.assertEquals("hadoop2", HDFSUtils.getKeytabSuperUser("cluster2"))

    // isKeytabProxyUserEnabled
    Assertions.assertEquals("/appcom/keytab/", HDFSUtils.getKeytabPath(null))
    Assertions.assertEquals(
      "/appcom/config/external-conf/keytab/cluster2",
      HDFSUtils.getKeytabPath("cluster2")
    )
  }

}
