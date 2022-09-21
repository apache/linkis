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

import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceVo;

import org.apache.commons.collections.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSourceDaoTest extends BaseDaoTest {
  private static final Logger logger = LoggerFactory.getLogger(DataSourceDaoTest.class);

  @Autowired DataSourceDao dataSourceDao;
  /**
   * User-created test data, if it is an auto-increment id, it should not be assigned CURD should be
   * based on the data created by this method insert
   *
   * @return DataSource
   */
  private DataSource insertOne() {
    DataSource dataSource = new DataSource();
    dataSource.setDataSourceName("unitTest");
    dataSource.setDataSourceTypeId(1l);
    dataSource.setDataSourceDesc("unit test by h2 database");
    dataSource.setCreateIdentify("unitTest");
    dataSource.setCreateSystem("local");
    dataSource.setCreateUser("test");
    dataSource.setParameter("{}");
    dataSource.setModifyUser("test");
    dataSource.setCreateTime(new Date());
    dataSource.setModifyTime(new Date());
    dataSourceDao.insertOne(dataSource);
    return dataSource;
  }

  @BeforeAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void beforeAll() throws Exception {
    // Start the console of h2 to facilitate viewing of h2 data
    Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
  }

  @AfterAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void afterAll() throws Exception {}

  @Test
  void testInsertOne() {
    DataSource dataSource = insertOne();
    assertTrue(dataSource.getId() > 0);
  }

  @Test
  void testSelectOneDetail() {
    DataSource dataSource = insertOne();
    DataSource result = dataSourceDao.selectOneDetail(dataSource.getId());
    assertEquals("mysql", result.getDataSourceType().getName());
  }

  @Test
  void testSelectOneDetailByName() {
    DataSource dataSource = insertOne();
    DataSource result = dataSourceDao.selectOneDetailByName(dataSource.getDataSourceName());
    assertEquals("mysql", result.getDataSourceType().getName());
  }

  @Test
  void testSelectOne() {
    DataSource dataSource = insertOne();
    DataSource result = dataSourceDao.selectOne(dataSource.getId());
    assertTrue(result.getId() > 0);
  }

  @Test
  void testSelectOneByName() {
    DataSource dataSource = insertOne();
    DataSource result = dataSourceDao.selectOneByName(dataSource.getDataSourceName());
    assertTrue(result.getId() > 0);
  }

  @Test
  void testRemoveOne() {
    DataSource dataSource = insertOne();
    int res = dataSourceDao.removeOne(dataSource.getId());
    assertTrue(res == 1);
  }

  @Test
  void testExpireOne() {
    DataSource dataSource = insertOne();
    int res = dataSourceDao.expireOne(dataSource.getId());
    assertTrue(res == 1);
  }

  @Test
  void testUpdateOne() {
    DataSource dataSource = insertOne();
    dataSource = dataSourceDao.selectOne(dataSource.getId());
    dataSource.setDataSourceName("modify " + "unitTest");
    dataSource.setDataSourceTypeId(1l);
    dataSource.setDataSourceDesc("modify " + "unit test by h2 database");
    dataSource.setCreateIdentify("modify " + "unitTest");
    dataSource.setCreateSystem("modify " + "local");
    dataSource.setParameter("{}");
    dataSource.setModifyUser("modify " + "test");
    dataSource.setModifyTime(new Date());
    dataSourceDao.updateOne(dataSource);
    DataSource newDataSource = dataSourceDao.selectOne(dataSource.getId());
    assertThat(newDataSource).usingRecursiveComparison().isEqualTo(dataSource);
  }

  @Test
  void testSelectByPageVo() {
    Date originDate = new Date();
    // match
    DataSource dataSource = insertOne();
    // match
    dataSource.setDataSourceName("unitTest1");
    dataSource.setCreateTime(new Date());
    dataSourceDao.insertOne(dataSource);

    // match
    dataSource.setDataSourceName("unitTest2");
    dataSource.setCreateTime(originDate);
    dataSourceDao.insertOne(dataSource);

    // no match by datasource name
    dataSource.setDataSourceName("tmpTest");
    dataSourceDao.insertOne(dataSource);

    // on match by dataSourceTypeId
    dataSource.setDataSourceName("unitTest3");
    dataSource.setDataSourceTypeId(2l);
    dataSourceDao.insertOne(dataSource);

    // no match by createUser
    dataSource.setDataSourceName("unitTest4");
    dataSource.setDataSourceTypeId(1l);
    dataSource.setCreateUser("other");
    dataSourceDao.insertOne(dataSource);

    DataSourceVo dataSourceVo = new DataSourceVo();
    dataSourceVo.setDataSourceName("unitTest");
    dataSourceVo.setDataSourceTypeId(1l);
    dataSourceVo.setPermissionUser("test");

    List<DataSource> dataSources = dataSourceDao.selectByPageVo(dataSourceVo);
    assertAll(
        "All",
        () -> assertTrue(dataSources.size() == 3),
        () -> assertTrue("unitTest".equals(dataSources.get(1).getDataSourceName())),
        () -> assertTrue("unitTest1".equals(dataSources.get(0).getDataSourceName())),
        () -> assertTrue("unitTest2".equals(dataSources.get(2).getDataSourceName())));
  }

  @Test
  void testSetPublishedVersionId() {
    DataSource dataSource = insertOne();
    dataSourceDao.setPublishedVersionId(dataSource.getId(), 10l);
    assertTrue(
        dataSourceDao.selectByPageVo(new DataSourceVo()).get(0).getPublishedVersionId() == 10l);
  }

  @Test
  void testUpdateVersionId() {
    DataSource dataSource = insertOne();
    dataSourceDao.updateVersionId(dataSource.getId(), 10l);
    assertTrue(dataSourceDao.selectByPageVo(new DataSourceVo()).get(0).getVersionId() == 10l);
  }

  @Test
  void testSelectByIds() {
    DataSource dataSource = insertOne();
    dataSource.setDataSourceName("unitTest");
    dataSource.setDataSourceTypeId(1l);
    dataSource.setCreateUser("test");
    List<DataSource> list = dataSourceDao.selectByIds(Arrays.asList(dataSource.getId()));
    assertTrue(CollectionUtils.isNotEmpty(list));
  }
}
