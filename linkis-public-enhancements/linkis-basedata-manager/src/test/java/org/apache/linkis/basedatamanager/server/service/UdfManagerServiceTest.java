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

import org.apache.linkis.basedatamanager.server.dao.UdfManagerMapper;
import org.apache.linkis.basedatamanager.server.domain.UdfManagerEntity;
import org.apache.linkis.basedatamanager.server.service.impl.UdfManagerServiceImpl;

import java.util.ArrayList;
import java.util.List;

import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class UdfManagerServiceTest {
  private Logger logger = LoggerFactory.getLogger(UdfManagerServiceTest.class);
  @InjectMocks private UdfManagerServiceImpl udfManagerService;
  @Mock private UdfManagerMapper udfManagerMapper;

  @Test
  public void getListByPage() {
    List<UdfManagerEntity> udfManagerEntities = new ArrayList<>();
    UdfManagerEntity udfManagerEntity = new UdfManagerEntity();
    udfManagerEntity.setId(3L);
    udfManagerEntity.setUserName("userName");
    udfManagerEntities.add(udfManagerEntity);
    Mockito.when(udfManagerMapper.getListByPage(Mockito.any())).thenReturn(udfManagerEntities);

    PageInfo<UdfManagerEntity> listByPage = udfManagerService.getListByPage("", 1, 10);
    Assertions.assertTrue(listByPage.getSize() > 0);
    Assertions.assertEquals(listByPage.getSize(), 1);
    Assertions.assertEquals(listByPage.getList().get(0).getUserName(), "userName");
    logger.info(listByPage.toString());
  }
}
