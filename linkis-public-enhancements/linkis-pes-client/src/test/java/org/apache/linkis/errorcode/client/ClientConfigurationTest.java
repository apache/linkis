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

package org.apache.linkis.errorcode.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ClientConfigurationTest {

  @Test
  @DisplayName("commonConstTest")
  public void commonConstTest() {

    String linkisGatewayUrl = ClientConfiguration.LINKIS_GATEWAY_URL.getValue();
    String errorCodeUrlPrefix = ClientConfiguration.ERRORCODE_URL_PREFIX.getValue();
    String errorCodeGetUrl = ClientConfiguration.ERRORCODE_GET_URL.getValue();
    Long defaultConnectTimeOut = ClientConfiguration.DEFAULT_CONNECT_TIME_OUT.getValue();
    Long defaultReadTimeOut = ClientConfiguration.DEFAULT_READ_TIME_OUT.getValue();
    String authTokenValue = ClientConfiguration.AUTH_TOKEN_VALUE.getValue();
    Long futureTimeOut = ClientConfiguration.FUTURE_TIME_OUT.getValue();

    Assertions.assertNotNull(linkisGatewayUrl);
    Assertions.assertNotNull(errorCodeUrlPrefix);
    Assertions.assertNotNull(errorCodeGetUrl);
    Assertions.assertTrue(defaultConnectTimeOut.longValue() == 600000L);
    Assertions.assertTrue(defaultReadTimeOut == 600000L);
    Assertions.assertNotNull(authTokenValue);
    Assertions.assertTrue(futureTimeOut.longValue() == 2000L);
  }
}
