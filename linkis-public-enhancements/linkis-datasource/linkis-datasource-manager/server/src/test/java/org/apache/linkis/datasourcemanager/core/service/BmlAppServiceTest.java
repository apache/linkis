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

package org.apache.linkis.datasourcemanager.core.service;

import org.apache.linkis.bml.client.BmlClient;
import org.apache.linkis.bml.protocol.BmlDeleteResponse;
import org.apache.linkis.bml.protocol.BmlUpdateResponse;
import org.apache.linkis.bml.protocol.BmlUploadResponse;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.datasourcemanager.core.service.impl.BmlAppServiceImpl;

import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class BmlAppServiceTest {
  private static final Logger logger = LoggerFactory.getLogger(BmlAppServiceTest.class);
  @InjectMocks BmlAppServiceImpl bmlAppService;

  @Mock BmlClient client;

  @Test
  void testClientUploadResource() throws ErrorException {
    InputStream inputStream = Mockito.mock(FileInputStream.class);
    BmlUploadResponse bmlUploadResponse = new BmlUploadResponse(true, "10001", "1");
    Mockito.when(client.uploadResource("test", "/test/a.txt", inputStream))
        .thenReturn(bmlUploadResponse);
    String res = bmlAppService.clientUploadResource("test", "/test/a.txt", inputStream);
    assertTrue(res.equals("10001"));
  }

  @Test
  void testClientRemoveResource() {
    String user = "test";
    String resourceId = "10001";
    BmlDeleteResponse bmlDeleteResponse = new BmlDeleteResponse(true);
    Mockito.when(client.deleteResource(user, resourceId)).thenReturn(bmlDeleteResponse);
    try {
      bmlAppService.clientRemoveResource(user, resourceId);
    } catch (ErrorException e) {
      logger.error("bmlAppService clientRemoveResource error:" + e.getMessage());
    }
  }

  @Test
  void testClientUpdateResource() throws ErrorException {
    String userName = "test";
    String resourceId = "10001";
    String version = "2";
    InputStream inputStream = Mockito.mock(FileInputStream.class);
    BmlUpdateResponse bmlUpdateResponse = new BmlUpdateResponse(true, resourceId, version);
    Mockito.when(client.updateResource(userName, resourceId, "filename", inputStream))
        .thenReturn(bmlUpdateResponse);
    String res = bmlAppService.clientUpdateResource(userName, resourceId, inputStream);
    assertTrue(version.equals(res));
  }
}
