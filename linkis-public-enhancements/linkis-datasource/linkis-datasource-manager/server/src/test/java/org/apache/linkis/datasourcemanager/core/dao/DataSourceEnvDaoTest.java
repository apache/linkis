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

package org.apache.linkis.datasourcemanager.core.dao;

import org.apache.linkis.datasourcemanager.common.domain.DataSourceEnv;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceEnvVo;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DataSourceEnvDaoTest extends BaseDaoTest {
  private static final Logger logger = LoggerFactory.getLogger(DataSourceEnvDaoTest.class);

  @Autowired DataSourceEnvDao dataSourceEnvDao;
  /**
   * User-created test data, if it is an auto-increment id, it should not be assigned CURD should be
   * based on the data created by this method insert
   *
   * @return DataSourceEnv
   */
  private DataSourceEnv insertOne() {
    DataSourceEnv dataSourceEnv = new DataSourceEnv();
    dataSourceEnv.setEnvName("testEnv");
    dataSourceEnv.setEnvDesc("testEnv desc");
    dataSourceEnv.setParameter("{}");
    dataSourceEnv.setCreateUser("test");
    dataSourceEnv.setModifyUser("test");
    dataSourceEnv.setDataSourceTypeId(1l);
    dataSourceEnv.setCreateTime(new Date());
    dataSourceEnv.setModifyTime(new Date());
    dataSourceEnvDao.insertOne(dataSourceEnv);
    return dataSourceEnv;
  }

  @AfterAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void afterAll() throws Exception {}

  @Test
  void testInsertOne() {
    DataSourceEnv dataSourceEnv = insertOne();
    assertTrue(dataSourceEnv.getId() > 0);
  }

  @Test
  void testSelectOneDetail() {
    DataSourceEnv dataSourceEnv = insertOne();
    DataSourceEnv result = dataSourceEnvDao.selectOneDetail(dataSourceEnv.getId());
    assertTrue(result.getId() > 0);
  }

  @Test
  void testListByTypeId() {
    DataSourceEnv dataSourceEnv = insertOne();
    List<DataSourceEnv> dataSourceEnvs =
        dataSourceEnvDao.listByTypeId(dataSourceEnv.getDataSourceTypeId());
    assertTrue(dataSourceEnvs.size() == 1);
  }

  @Test
  void testRemoveOne() {
    DataSourceEnv dataSourceEnv = insertOne();
    int res = dataSourceEnvDao.removeOne(dataSourceEnv.getId());
    assertTrue(res == 1);
  }

  @Test
  void testUpdateOne() {
    DataSourceEnv dataSourceEnv = insertOne();
    dataSourceEnv = dataSourceEnvDao.selectOneDetail(dataSourceEnv.getId());
    dataSourceEnv.setEnvName("modify-testEnv");
    dataSourceEnv.setEnvDesc("modify testEnv desc");
    dataSourceEnv.setParameter("{}");
    dataSourceEnv.setModifyTime(new Date());
    dataSourceEnv.setModifyUser("modify-test");
    dataSourceEnvDao.updateOne(dataSourceEnv);
    DataSourceEnv newDataSourceEnv = dataSourceEnvDao.selectOneDetail(dataSourceEnv.getId());
    assertThat(newDataSourceEnv).usingRecursiveComparison().isEqualTo(dataSourceEnv);
  }

  @Test
  void testSelectByPageVo() {
    // match
    DataSourceEnv dataSourceEnv = insertOne();
    // match
    dataSourceEnv.setEnvName("testEnv1");
    dataSourceEnvDao.insertOne(dataSourceEnv);

    // no match by env name
    dataSourceEnv.setEnvName("devEnv1");
    dataSourceEnvDao.insertOne(dataSourceEnv);

    // on match by dataSourceTypeId
    dataSourceEnv.setEnvName("testEnv2");
    dataSourceEnv.setDataSourceTypeId(2l);
    dataSourceEnvDao.insertOne(dataSourceEnv);

    DataSourceEnvVo dataSourceEnvVo = new DataSourceEnvVo();
    dataSourceEnvVo.setEnvName("testEnv");
    dataSourceEnvVo.setDataSourceTypeId(1l);

    List<DataSourceEnv> dataSourceEnvs = dataSourceEnvDao.selectByPageVo(dataSourceEnvVo);
    assertAll(
        "All",
        () -> assertTrue(dataSourceEnvs.size() == 2),
        () -> assertTrue("testEnv".equals(dataSourceEnvs.get(0).getEnvName())),
        () -> assertTrue("testEnv1".equals(dataSourceEnvs.get(1).getEnvName())));
  }
}
