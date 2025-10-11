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

import org.apache.linkis.metadata.conf.MdqConfiguration;
import org.apache.linkis.metadata.hive.config.DSEnum;
import org.apache.linkis.metadata.hive.config.DataSource;
import org.apache.linkis.metadata.hive.dao.HiveMetaDao;
import org.apache.linkis.metadata.hive.dto.MetadataQueryParam;
import org.apache.linkis.metadata.service.DataSourceService;
import org.apache.linkis.metadata.service.HiveMetaWithPermissionService;
import org.apache.linkis.metadata.util.DWSConfig;
import org.apache.linkis.metadata.utils.MdqConstants;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
  public List<String> getDbsOptionalUserName(String userName, String permission) {
    if (StringUtils.isNotBlank(permission) && permission.equals("write")) {
      if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
        return hiveMetaDao.getCanWriteDbsByUser(userName);
      } else {
        return hiveMetaDao.getCanWriteDbsByUserSlave(userName);
      }
    } else {
      if (adminUser.equals(userName)) {
        log.info("admin {} to get all dbs ", userName);
        if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
          return hiveMetaDao.getAllDbs();
        } else {
          return hiveMetaDao.getAllDbsSlave();
        }
      }
      Boolean flag = DWSConfig.HIVE_PERMISSION_WITH_lOGIN_USER_ENABLED.getValue();
      if (flag) {
        List<String> roles;
        List<String> dbsByUserAndRoles;
        if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
          roles = hiveMetaDao.getRolesByUser(userName);
          dbsByUserAndRoles = hiveMetaDao.getDbsByUserAndRoles(userName, roles);
        } else {
          roles = hiveMetaDao.getRolesByUserSlave(userName);
          dbsByUserAndRoles = hiveMetaDao.getDbsByUserAndRolesSlave(userName, roles);
        }
        return dbsByUserAndRoles;
      } else {
        log.info("user {} to get all dbs no permission control", userName);
        if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
          return hiveMetaDao.getAllDbs();
        } else {
          return hiveMetaDao.getAllDbsSlave();
        }
      }
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
    String dbName = queryParam.getDbName();
    if (adminUser.equals(userName)) {
      String tableName = queryParam.getTableName();
      // if tableName is not empty；query by tablename
      if (StringUtils.isNotEmpty(tableName) && StringUtils.isNotEmpty(dbName)) {
        log.info("admin {} to get table with tableName:{} ", userName, tableName);
        Map<String, Object> queryRes;
        if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
          queryRes = hiveMetaDao.getTableInfoByTableNameAndDbName(tableName, dbName);
        } else {
          queryRes = hiveMetaDao.getTableInfoByTableNameAndDbNameSlave(tableName, dbName);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        if (queryRes != null) {
          result.add(queryRes);
        }
        return result;
      }

      log.info("admin {} to get all tables ", userName);
      if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
        return hiveMetaDao.getTablesByDbName(queryParam);
      } else {
        return hiveMetaDao.getTablesByDbNameSlave(queryParam);
      }
    }
    if (flag) {
      List<String> roles;
      List<Map<String, Object>> hiveTables;
      if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
        roles = hiveMetaDao.getRolesByUser(userName);
        queryParam.withRoles(roles);
        hiveTables = hiveMetaDao.getTablesByDbNameAndUserAndRolesFromDbPrvs(queryParam);
        hiveTables.addAll(hiveMetaDao.getTablesByDbNameAndUserAndRolesFromTblPrvs(queryParam));
      } else {
        roles = hiveMetaDao.getRolesByUserSlave(userName);
        queryParam.withRoles(roles);
        hiveTables = hiveMetaDao.getTablesByDbNameAndUserAndRolesFromDbPrvsSlave(queryParam);
        hiveTables.addAll(hiveMetaDao.getTablesByDbNameAndUserAndRolesFromTblPrvsSlave(queryParam));
      }
      return hiveTables.stream()
          .distinct()
          .sorted(Comparator.comparing(hiveTable -> (String) hiveTable.get("NAME")))
          .collect(Collectors.toList());
    } else {
      log.info("user {} to getTablesByDbName no permission control", queryParam.getUserName());
      if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
        return hiveMetaDao.getTablesByDbName(queryParam);
      } else {
        return hiveMetaDao.getTablesByDbNameSlave(queryParam);
      }
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
      List<String> roles;
      Map<String, Object> tableMap = new HashMap<>();
      if (!MdqConfiguration.HIVE_METADATA_SALVE_SWITCH()) {
        roles = hiveMetaDao.getRolesByUser(userName);
        queryParam.withRoles(roles);
        if (MdqConfiguration.HIVE_METADATA_SLOW_SQL_SWITCH()) {
          Map<String, Object> dbMap =
              hiveMetaDao.getStorageDescriptionIDByDbTableNameAndUserFromDB(queryParam);
          if (MapUtils.isNotEmpty(dbMap)) {
            tableMap.putAll(dbMap);
          }
          Map<String, Object> tblMap =
              hiveMetaDao.getStorageDescriptionIDByDbTableNameAndUserFromTBL(queryParam);
          if (MapUtils.isNotEmpty(tblMap)) {
            tableMap.putAll(tblMap);
          }
        } else {
          tableMap = hiveMetaDao.getStorageDescriptionIDByDbTableNameAndUser(queryParam);
        }
      } else {
        roles = hiveMetaDao.getRolesByUserSlave(userName);
        queryParam.withRoles(roles);
        if (MdqConfiguration.HIVE_METADATA_SLOW_SQL_SWITCH()) {
          Map<String, Object> dbMap =
              hiveMetaDao.getStorageDescriptionIDByDbTableNameAndUserSlaveFromDB(queryParam);
          if (MapUtils.isNotEmpty(dbMap)) {
            tableMap.putAll(dbMap);
          }
          Map<String, Object> tblMap =
              hiveMetaDao.getStorageDescriptionIDByDbTableNameAndUserSlaveFromTBL(queryParam);
          if (MapUtils.isNotEmpty(tblMap)) {
            tableMap.putAll(tblMap);
          }
        } else {
          tableMap = hiveMetaDao.getStorageDescriptionIDByDbTableNameAndUserSlave(queryParam);
        }
      }
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
