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

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.exception.WarnException;
import org.apache.linkis.datasourcemanager.core.formdata.FormStreamContent;
import org.apache.linkis.datasourcemanager.core.service.impl.MetadataOperateServiceImpl;
import org.apache.linkis.metadata.query.common.protocol.MetadataConnect;
import org.apache.linkis.metadata.query.common.protocol.MetadataResponse;
import org.apache.linkis.rpc.BaseRPCSender;
import org.apache.linkis.rpc.Sender;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MetadataOperateServiceTest {
  private static final Logger logger = LoggerFactory.getLogger(MetadataOperateServiceTest.class);

  @InjectMocks MetadataOperateServiceImpl metadataOperateService;

  @Mock BmlAppService bmlAppService;

  @Test
  void doRemoteConnect() throws ErrorException {
    String mdRemoteServiceName = "linkis-ps-metadata-manager";
    String dataSourceType = "mysql";
    String operator = "test";
    Map<String, Object> connectParams = new HashMap<>();
    FormStreamContent formStreamContent = new FormStreamContent();
    String fileName = "/tmp/a.txt";
    InputStream inputStream = Mockito.mock(FileInputStream.class);
    formStreamContent.setFileName(fileName);
    formStreamContent.setStream(inputStream);
    connectParams.put("formStreamContent", formStreamContent);
    Mockito.when(bmlAppService.clientUploadResource(operator, fileName, inputStream))
        .thenReturn("10001");
    Mockito.mockStatic(Sender.class);
    BaseRPCSender baseRPCSender = Mockito.mock(BaseRPCSender.class);
    Mockito.when(Sender.getSender("linkis-ps-metadata-manager")).thenReturn(baseRPCSender);

    MetadataConnect metadataConnect =
        new MetadataConnect(dataSourceType, operator, connectParams, "");
    MetadataResponse ok = new MetadataResponse(true, "success");
    MetadataResponse fail = new MetadataResponse(false, "fail");

    Mockito.when(baseRPCSender.ask(metadataConnect)).thenReturn(ok, fail);
    metadataOperateService.doRemoteConnect(
        mdRemoteServiceName, dataSourceType, operator, connectParams);
    Mockito.verify(baseRPCSender, Mockito.times(1)).ask(metadataConnect);

    assertThrows(
        WarnException.class,
        () ->
            metadataOperateService.doRemoteConnect(
                mdRemoteServiceName, dataSourceType, operator, connectParams));
    Mockito.verify(baseRPCSender, Mockito.times(2)).ask(metadataConnect);

    assertTrue(connectParams.get("formStreamContent").equals("10001"));
  }
}
