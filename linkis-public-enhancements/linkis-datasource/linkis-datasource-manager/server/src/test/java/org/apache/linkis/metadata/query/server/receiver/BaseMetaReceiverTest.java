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

package org.apache.linkis.metadata.query.server.receiver;

import org.apache.linkis.metadata.query.common.protocol.MetadataConnect;
import org.apache.linkis.metadata.query.common.protocol.MetadataResponse;
import org.apache.linkis.metadata.query.server.WebApplicationServer;
import org.apache.linkis.metadata.query.server.service.impl.MetadataQueryServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
@SpringBootTest(classes = {WebApplicationServer.class})
public class BaseMetaReceiverTest {

  @Autowired private BaseMetaReceiver baseMetaReceiver;

  @MockBean(name = "metadataQueryServiceImpl")
  MetadataQueryServiceImpl metadataQueryServiceImpl;

  @Test
  @DisplayName("dealMetadataConnectRequestTest")
  public void dealMetadataConnectRequestTest() throws Exception {

    MetadataConnect metadataConnect = new MetadataConnect("mysql", "query", new HashMap<>(), "1");
    Mockito.doNothing()
        .when(metadataQueryServiceImpl)
        .getConnection(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
    MetadataResponse response = baseMetaReceiver.dealMetadataConnectRequest(metadataConnect);

    Assertions.assertTrue(response.status());
  }
}
