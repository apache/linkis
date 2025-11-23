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

package org.apache.linkis.server.conf

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class ServerConfigurationTest {

  @Test
  @DisplayName("constTest")
  def constTest(): Unit = {

    val bdpServerExcludePackagesStr = ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES.getValue
    val bdpServerExcludeClassesStr = ServerConfiguration.BDP_SERVER_EXCLUDE_CLASSES.getValue
    val bdpServerExcludeAnnotation = ServerConfiguration.BDP_SERVER_EXCLUDE_ANNOTATION.getValue
    val bdpServerSpringApplicationStr =
      ServerConfiguration.BDP_SERVER_SPRING_APPLICATION_LISTENERS.getValue
    val bdpserverversion = ServerConfiguration.BDP_SERVER_VERSION
    val bdpTestUser = ServerConfiguration.BDP_TEST_USER.getValue
    val bdpServerHome = ServerConfiguration.BDP_SERVER_HOME.getValue
    val bdpServerDistinctMode = ServerConfiguration.BDP_SERVER_DISTINCT_MODE.getValue
    val bdpServerSocketMode = ServerConfiguration.BDP_SERVER_SOCKET_MODE.getValue
    val bdpServerIdentString = ServerConfiguration.BDP_SERVER_IDENT_STRING.getValue
    val bdpServerPort = ServerConfiguration.BDP_SERVER_PORT.getValue

    Assertions.assertEquals("", bdpServerExcludePackagesStr)
    Assertions.assertEquals("", bdpServerExcludeClassesStr)
    Assertions.assertEquals("", bdpServerExcludeAnnotation)
    Assertions.assertEquals("", bdpServerSpringApplicationStr)
    Assertions.assertEquals("v1", bdpserverversion)
    Assertions.assertEquals("hadoop", bdpTestUser)
    Assertions.assertEquals("", bdpServerHome)
    Assertions.assertTrue(bdpServerDistinctMode)
    Assertions.assertFalse(bdpServerSocketMode)
    Assertions.assertEquals("true", bdpServerIdentString)
    Assertions.assertTrue(20303 == bdpServerPort.intValue())

  }

}
