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

import org.apache.linkis.datasourcemanager.common.domain.DatasourceVersion;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
class DataSourceVersionDaoTest extends BaseDaoTest {
  private static final Logger logger = LoggerFactory.getLogger(DataSourceVersionDaoTest.class);

  @Autowired DataSourceVersionDao dataSourceVersionDao;

  @AfterAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void afterAll() throws Exception {}

  /**
   * User-created test data, if it is an auto-increment id, it should not be assigned CURD should be
   * based on the data created by this method insert
   *
   * @return DataSource
   */
  private DatasourceVersion insertOne() {
    DatasourceVersion datasourceVersion = new DatasourceVersion();
    datasourceVersion.setVersionId(1l);
    datasourceVersion.setDatasourceId(1l);
    datasourceVersion.setParameter("{}");
    datasourceVersion.setParameter("test");
    datasourceVersion.setCreateTime(new Date());
    datasourceVersion.setCreateUser("test");
    dataSourceVersionDao.insertOne(datasourceVersion);

    datasourceVersion.setVersionId(2l);
    datasourceVersion.setParameter("a:b");
    dataSourceVersionDao.insertOne(datasourceVersion);
    return datasourceVersion;
  }

  @Test
  void testInsertOne() {
    insertOne();
    assertTrue(dataSourceVersionDao.getLatestVersion(1l) == 2l);
  }

  @Test
  void testGetLatestVersion() {
    insertOne();
    assertTrue(dataSourceVersionDao.getLatestVersion(1l) == 2l);
  }

  @Disabled
  @Test
  void testSelectOneVersion() {
    /*insertOne();
    String param = dataSourceVersionDao.selectOneVersion(1l, 2l);
    assertTrue("a:b".equals(param));*/
  }

  @Test
  void testGetVersionsFromDatasourceId() {
    insertOne();
    assertTrue(dataSourceVersionDao.getVersionsFromDatasourceId(1l).size() == 2);
  }

  @Test
  void testRemoveFromDataSourceId() {
    insertOne();
    int res = dataSourceVersionDao.removeFromDataSourceId(1l);
    assertTrue(res == 2);
  }
}
