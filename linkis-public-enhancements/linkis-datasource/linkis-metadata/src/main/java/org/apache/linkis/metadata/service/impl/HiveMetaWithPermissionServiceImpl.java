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

package org.apache.linkis.metadata.service.impl;

import org.apache.linkis.metadata.hive.config.DSEnum;
import org.apache.linkis.metadata.hive.config.DataSource;
import org.apache.linkis.metadata.hive.dao.HiveMetaDao;
import org.apache.linkis.metadata.hive.dto.MetadataQueryParam;
import org.apache.linkis.metadata.service.DataSourceService;
import org.apache.linkis.metadata.service.HiveMetaWithPermissionService;
import org.apache.linkis.metadata.util.DWSConfig;
import org.apache.linkis.metadata.utils.MdqConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class HiveMetaWithPermissionServiceImpl implements HiveMetaWithPermissionService {

  private static final Logger log =
      LoggerFactory.getLogger(HiveMetaWithPermissionServiceImpl.class);

  @Autowired private HiveMetaDao hiveMetaDao;

  @Autowired private DataSourceService dataSourceService;

  private final String adminUser = DWSConfig.HIVE_DB_ADMIN_USER.getValue();

  @Override
  public List<String> getDbsOptionalUserName(String userName) {
    if (adminUser.equals(userName)) {
      log.info("admin {} to get all dbs ", userName);
      return hiveMetaDao.getAllDbs();
    }
    Boolean flag = DWSConfig.HIVE_PERMISSION_WITH_lOGIN_USER_ENABLED.getValue();
    if (flag) {
      List<String> roles = hiveMetaDao.getRolesByUser(userName);
      return hiveMetaDao.getDbsByUserAndRoles(userName, roles);
    } else {
      log.info("user {} to get all dbs no permission control", userName);
      return hiveMetaDao.getAllDbs();
    }
  }

  @Override
  public List<Map<String, Object>> getTablesByDbNameAndOptionalUserName(
      MetadataQueryParam queryParam) {
    Boolean flag = DWSConfig.HIVE_PERMISSION_WITH_lOGIN_USER_ENABLED.getValue();
    if (null == queryParam) {
      return null;
    }
    String userName = queryParam.getUserName();
    if (adminUser.equals(userName)) {
      log.info("admin {} to get all tables ", userName);
      return hiveMetaDao.getTablesByDbName(queryParam);
    }
    if (flag) {
      List<String> roles = hiveMetaDao.getRolesByUser(queryParam.getUserName());
      queryParam.withRoles(roles);
      return hiveMetaDao.getTablesByDbNameAndUserAndRoles(queryParam);
    } else {
      log.info("user {} to getTablesByDbName no permission control", queryParam.getUserName());
      return hiveMetaDao.getTablesByDbName(queryParam);
    }
  }

  @DataSource(name = DSEnum.FIRST_DATA_SOURCE)
  @Override
  public JsonNode getColumnsByDbTableNameAndOptionalUserName(MetadataQueryParam queryParam) {
    Boolean flag = DWSConfig.HIVE_PERMISSION_WITH_lOGIN_USER_ENABLED.getValue();
    if (null == queryParam) {
      return null;
    }
    String userName = queryParam.getUserName();
    String dbName = queryParam.getDbName();
    String tableName = queryParam.getTableName();
    if (adminUser.equals(userName)) {
      log.info("admin {} to get all tables ", userName);
      return dataSourceService.queryTableMeta(queryParam);
    }
    if (flag) {
      List<String> roles = hiveMetaDao.getRolesByUser(userName);
      queryParam.withRoles(roles);
      // with permission
      Map<String, Object> tableMap =
          hiveMetaDao.getStorageDescriptionIDByDbTableNameAndUser(queryParam);
      if (null != tableMap
          && !tableMap.isEmpty()
          && tableMap.containsKey(MdqConstants.SDID_KEY())) {
        String sdid = tableMap.get(MdqConstants.SDID_KEY()).toString();
        queryParam.setSdId(sdid);
        return dataSourceService.queryTableMetaBySDID(queryParam);
      } else {
        log.error(
            "User {} has no read permission for meta of db : {}, table : {}",
            userName,
            dbName,
            tableName);
        return null;
      }
    } else {
      log.info("user {} to getTablesByDbName no permission control", userName);
      return dataSourceService.queryTableMeta(queryParam);
    }
  }
}
