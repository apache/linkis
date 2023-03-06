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

import org.apache.linkis.basedatamanager.server.dao.DatasourceTypeMapper;
import org.apache.linkis.basedatamanager.server.domain.DatasourceTypeEntity;
import org.apache.linkis.basedatamanager.server.service.impl.DatasourceTypeServiceImpl;

import java.util.ArrayList;

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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DatasourceTypeServiceTest {

  private Logger logger = LoggerFactory.getLogger(DatasourceTypeServiceTest.class);
  @InjectMocks private DatasourceTypeServiceImpl datasourceTypeService;

  @Mock private DatasourceTypeMapper datasourceTypeMapper;

  @Test
  void getListByPageTest() {
    ArrayList<DatasourceTypeEntity> t = new ArrayList<>();
    DatasourceTypeEntity datasourceTypeEntity = new DatasourceTypeEntity();
    datasourceTypeEntity.setId(0);
    datasourceTypeEntity.setName("test");
    datasourceTypeEntity.setDescription("test");
    datasourceTypeEntity.setOption("test");
    datasourceTypeEntity.setClassifier("test");
    datasourceTypeEntity.setIcon("test");
    datasourceTypeEntity.setLayers(0);
    t.add(datasourceTypeEntity);
    Mockito.when(datasourceTypeMapper.getListByPage("")).thenReturn(t);
    PageInfo listByPage = datasourceTypeService.getListByPage("", 1, 10);
    Assertions.assertTrue(listByPage.getSize() > 0);
    logger.info(listByPage.toString());
  }
}
