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

package org.apache.linkis.bml.service;

import org.apache.linkis.bml.dao.DownloadDao;
import org.apache.linkis.bml.entity.DownloadModel;
import org.apache.linkis.bml.service.impl.DownloadServiceImpl;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/** DownloadServiceImpl Tester */
@ExtendWith(MockitoExtension.class)
public class DownloadServiceImplTest {

  @InjectMocks private DownloadServiceImpl downloadServiceImpl;

  @Mock private DownloadDao downloadDao;

  @Test
  public void testAddDownloadRecord() throws Exception {
    DownloadModel downloadModel = new DownloadModel();
    downloadModel.setDownloader("test");
    downloadModel.setClientIp("192.143.253");
    downloadModel.setEndTime(new Date());
    downloadModel.setId(12);
    downloadModel.setState(1);
    downloadModel.setStartTime(new Date());
    downloadModel.setVersion("1.2");
    downloadModel.setResourceId("32");
    Mockito.doNothing().when(downloadDao).insertDownloadModel(downloadModel);
    downloadServiceImpl.addDownloadRecord(downloadModel);
  }
}
