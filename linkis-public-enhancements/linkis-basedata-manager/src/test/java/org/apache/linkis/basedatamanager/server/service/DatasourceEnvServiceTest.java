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

import org.apache.linkis.basedatamanager.server.dao.DatasourceEnvMapper;
import org.apache.linkis.basedatamanager.server.domain.DatasourceEnvEntity;
import org.apache.linkis.basedatamanager.server.service.impl.DatasourceEnvServiceImpl;

import java.util.ArrayList;
import java.util.Date;

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
class DatasourceEnvServiceTest {
  private Logger logger = LoggerFactory.getLogger(DatasourceEnvServiceTest.class);

  @InjectMocks private DatasourceEnvServiceImpl datasourceEnvService;

  @Mock private DatasourceEnvMapper datasourceEnvMapper;

  @Test
  void getListByPageTest() {
    ArrayList<DatasourceEnvEntity> t = new ArrayList<>();
    DatasourceEnvEntity datasourceEnvEntity = new DatasourceEnvEntity();
    datasourceEnvEntity.setId(0);
    datasourceEnvEntity.setEnvName("test");
    datasourceEnvEntity.setEnvDesc("test");
    datasourceEnvEntity.setDatasourceTypeId(0);
    datasourceEnvEntity.setParameter("test");
    datasourceEnvEntity.setCreateTime(new Date());
    datasourceEnvEntity.setCreateUser("test");
    datasourceEnvEntity.setModifyTime(new Date());
    datasourceEnvEntity.setModifyUser("test");
    t.add(datasourceEnvEntity);
    Mockito.when(datasourceEnvMapper.getListByPage("")).thenReturn(t);
    PageInfo listByPage = datasourceEnvService.getListByPage("", 1, 1);
    Assertions.assertTrue(listByPage.getSize() > 0);
    logger.info(listByPage.toString());
  }
}
