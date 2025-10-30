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

package org.apache.linkis.metadata.hive.dao;

import org.apache.linkis.metadata.dao.BaseDaoTest;
import org.apache.linkis.metadata.hive.dto.MetadataQueryParam;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HiveMetaDaoTest extends BaseDaoTest {

  @Autowired private HiveMetaDao hiveMetaDao;

  @Test
  @DisplayName("getLocationByDbAndTableTest")
  public void getLocationByDbAndTableTest() {

    MetadataQueryParam queryParam = new MetadataQueryParam();
    queryParam.setDbName("default");
    queryParam.setTableName("employee");
    String location = hiveMetaDao.getLocationByDbAndTable(queryParam);
    Assertions.assertNotNull(location);
  }

  @Test
  @DisplayName("getAllDbsTest")
  public void getAllDbsTest() {

    List<String> dbs = hiveMetaDao.getAllDbs();

    Assertions.assertTrue(dbs.size() > 0);
  }

  @Test
  @DisplayName("getRolesByUserTest")
  public void getRolesByUserTest() {

    List<String> roles = hiveMetaDao.getRolesByUser("admin");
    Assertions.assertTrue(roles.size() == 0);
  }

  @Test
  @DisplayName("getDbsByUserAndRolesTest")
  public void getDbsByUserAndRolesTest() {

    List<String> dbs = hiveMetaDao.getDbsByUserAndRoles("admin", new ArrayList<>());
    Assertions.assertTrue(dbs.size() == 0);
  }

  @Test
  @DisplayName("getTablesByDbNameAndUserAndRolesTest")
  public void getTablesByDbNameAndUserAndRolesTest() {
    MetadataQueryParam queryParam = new MetadataQueryParam();
    queryParam.setDbName("default");
    queryParam.setTableName("employee");
    queryParam.setUserName("admin");
    List<Map<String, Object>> tables =
        hiveMetaDao.getTablesByDbNameAndUserAndRolesFromDbPrvs(queryParam);
    Assertions.assertTrue(tables.size() == 0);
  }

  @Test
  @DisplayName("getTablesByDbNameTest")
  public void getTablesByDbNameTest() {

    MetadataQueryParam queryParam = new MetadataQueryParam();
    queryParam.setDbName("default");
    List<Map<String, Object>> tables = hiveMetaDao.getTablesByDbName(queryParam);
    Assertions.assertTrue(tables.size() == 1);
  }

  @Test
  @DisplayName("getPartitionSizeTest")
  public void getPartitionSizeTest() {

    MetadataQueryParam queryParam = new MetadataQueryParam();
    queryParam.setDbName("default");
    queryParam.setTableName("employee");
    queryParam.setPartitionName("ds=202202");

    Long size = hiveMetaDao.getPartitionSize(queryParam);
    Assertions.assertTrue(size.longValue() >= 0);
  }

  @Test
  @DisplayName("getPartitionsTest")
  public void getPartitionsTest() {

    MetadataQueryParam queryParam = new MetadataQueryParam();
    queryParam.setDbName("default");
    queryParam.setTableName("employee");

    List<String> partitions = hiveMetaDao.getPartitions(queryParam);
    Assertions.assertTrue(partitions.size() >= 0);
  }

  @Test
  @DisplayName("getColumnsTest")
  public void getColumnsTest() {

    MetadataQueryParam queryParam = new MetadataQueryParam();
    queryParam.setDbName("default");
    queryParam.setTableName("employee");

    List<Map<String, Object>> columns = hiveMetaDao.getColumns(queryParam);
    Assertions.assertTrue(columns.size() >= 0);
  }

  @Test
  @DisplayName("getColumnsByStorageDescriptionIDTest")
  public void getColumnsByStorageDescriptionIDTest() {

    MetadataQueryParam queryParam = new MetadataQueryParam();
    queryParam.setSdId("1");
    List<Map<String, Object>> columns = hiveMetaDao.getColumnsByStorageDescriptionID(queryParam);
    Assertions.assertTrue(columns.size() >= 0);
  }

  @Test
  @DisplayName("getPartitionKeysTest")
  public void getPartitionKeysTest() {
    MetadataQueryParam queryParam = new MetadataQueryParam();
    queryParam.setDbName("default");
    queryParam.setTableName("employee");
    List<Map<String, Object>> partitionKeys = hiveMetaDao.getPartitionKeys(queryParam);
    Assertions.assertTrue(partitionKeys.size() > 0);
  }
}
