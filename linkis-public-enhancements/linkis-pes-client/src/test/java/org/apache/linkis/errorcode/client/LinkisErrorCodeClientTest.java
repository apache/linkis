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

import org.apache.linkis.errorcode.client.action.ErrorCodeGetAllAction;
import org.apache.linkis.errorcode.common.LinkisErrorCode;
import org.apache.linkis.httpclient.dws.DWSHttpClient;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class LinkisErrorCodeClientTest {

  @Test
  @DisplayName("getErrorCodesFromServerTest")
  public void getErrorCodesFromServerTest() {

    // Simulated pile driving
    DWSHttpClient dwsHttpClient = Mockito.mock(DWSHttpClient.class);
    Mockito.when(dwsHttpClient.execute(Mockito.any(ErrorCodeGetAllAction.class))).thenReturn(null);
    LinkisErrorCodeClient linkisErrorCodeClient = new LinkisErrorCodeClient(dwsHttpClient);
    List<LinkisErrorCode> codes = linkisErrorCodeClient.getErrorCodesFromServer();
    Assertions.assertTrue(codes.size() == 0);
  }
}
