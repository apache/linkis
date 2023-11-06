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

import org.apache.linkis.basedatamanager.server.dao.DatasourceAccessMapper;
import org.apache.linkis.basedatamanager.server.domain.DatasourceAccessEntity;
import org.apache.linkis.basedatamanager.server.service.impl.DatasourceAccessServiceImpl;

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
class DatasourceAccessServiceTest {
  private Logger logger = LoggerFactory.getLogger(DatasourceAccessServiceTest.class);

  @InjectMocks private DatasourceAccessServiceImpl datasourceAccessService;

  @Mock private DatasourceAccessMapper datasourceAccessMapper;

  @Test
  void getListByPageTest() {
    ArrayList<DatasourceAccessEntity> t = new ArrayList<>();
    DatasourceAccessEntity datasourceAccessEntity = new DatasourceAccessEntity();
    datasourceAccessEntity.setId(0L);
    datasourceAccessEntity.setTableId(0L);
    datasourceAccessEntity.setVisitor("test");
    datasourceAccessEntity.setFields("test");
    datasourceAccessEntity.setApplicationId(0);
    datasourceAccessEntity.setAccessTime(new Date());

    t.add(datasourceAccessEntity);
    Mockito.when(datasourceAccessMapper.getListByPage("")).thenReturn(t);
    PageInfo listByPage = datasourceAccessService.getListByPage("", 1, 1);
    Assertions.assertTrue(listByPage.getSize() > 0);
    logger.info(listByPage.toString());
  }
}
