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

import org.apache.linkis.basedatamanager.server.dao.GatewayAuthTokenMapper;
import org.apache.linkis.basedatamanager.server.domain.GatewayAuthTokenEntity;
import org.apache.linkis.basedatamanager.server.service.impl.GatewayAuthTokenServiceImpl;

import java.util.ArrayList;
import java.util.Date;

import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GatewayAuthTokenServiceTest {
  private Logger logger = LoggerFactory.getLogger(GatewayAuthTokenServiceTest.class);
  @InjectMocks private GatewayAuthTokenServiceImpl gatewayAuthTokenService;
  @Mock private GatewayAuthTokenMapper gatewayAuthTokenMapper;

  @Test
  public void getListByPageTest() {
    ArrayList<GatewayAuthTokenEntity> t = new ArrayList<>();
    GatewayAuthTokenEntity gatewayAuthTokenEntity = new GatewayAuthTokenEntity();
    gatewayAuthTokenEntity.setId(0);
    gatewayAuthTokenEntity.setTokenName("test");
    gatewayAuthTokenEntity.setLegalUsers("test");
    gatewayAuthTokenEntity.setLegalHosts("test");
    gatewayAuthTokenEntity.setBusinessOwner("test");
    gatewayAuthTokenEntity.setCreateTime(new Date());
    gatewayAuthTokenEntity.setUpdateTime(new Date());
    gatewayAuthTokenEntity.setElapseDay(0L);
    gatewayAuthTokenEntity.setUpdateBy("test");
    t.add(gatewayAuthTokenEntity);
    Mockito.when(gatewayAuthTokenMapper.getListByPage("")).thenReturn(t);
    PageInfo listByPage = gatewayAuthTokenService.getListByPage("", 1, 10);
    logger.info(listByPage.toString());
  }
}
