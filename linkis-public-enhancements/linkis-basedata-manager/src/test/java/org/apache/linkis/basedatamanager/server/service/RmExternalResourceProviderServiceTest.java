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

package org.apache.linkis.basedatamanager.server.service;

import org.apache.linkis.basedatamanager.server.dao.RmExternalResourceProviderMapper;
import org.apache.linkis.basedatamanager.server.domain.RmExternalResourceProviderEntity;
import org.apache.linkis.basedatamanager.server.service.impl.RmExternalResourceProviderServiceImpl;

import org.springframework.util.Assert;

import java.util.ArrayList;

import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class RmExternalResourceProviderServiceTest {
  private Logger logger = LoggerFactory.getLogger(UdfTreeServiceTest.class);
  @InjectMocks private RmExternalResourceProviderServiceImpl rmExternalResourceProviderService;
  @Mock private RmExternalResourceProviderMapper rmExternalResourceProviderMapper;

  @Test
  public void getListByPageTest() {
    ArrayList<RmExternalResourceProviderEntity> t = new ArrayList<>();
    RmExternalResourceProviderEntity rmExternalResourceProviderEntity =
        new RmExternalResourceProviderEntity();
    rmExternalResourceProviderEntity.setId(0);
    rmExternalResourceProviderEntity.setResourceType("test");
    rmExternalResourceProviderEntity.setName("test");
    rmExternalResourceProviderEntity.setLabels("test");
    rmExternalResourceProviderEntity.setConfig("test");
    t.add(rmExternalResourceProviderEntity);
    Mockito.when(rmExternalResourceProviderMapper.getListByPage("")).thenReturn(t);
    PageInfo listByPage = rmExternalResourceProviderService.getListByPage("", 1, 10);
    Assert.isTrue(listByPage.getSize() > 0);
    logger.info(listByPage.toString());
  }
}
