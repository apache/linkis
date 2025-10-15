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

import org.apache.linkis.metadata.hive.dto.MetadataQueryParam;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface HiveMetaDao {

  String getLocationByDbAndTable(MetadataQueryParam queryParam);

  String getLocationByDbAndTableSlave(MetadataQueryParam queryParam);

  Map<String, Object> getStorageInfo(MetadataQueryParam queryParam);

  Map<String, Object> getStorageInfoSlave(MetadataQueryParam queryParam);

  /**
   * get user's roles by username
   *
   * @param userName user's username
   * @return the role name list
   */
  List<String> getRolesByUser(String userName);

  List<String> getRolesByUserSlave(String userName);

  /**
   * get dbs by user's username and user's roles
   *
   * @param userName user's username
   * @param roles user's roles
   * @return the db name list
   */
  List<String> getDbsByUserAndRoles(
      @Param("userName") String userName, @Param("roles") List<String> roles);

  List<String> getDbsByUserAndRolesSlave(
      @Param("userName") String userName, @Param("roles") List<String> roles);

  /**
   * get all list of DBS NAME
   *
   * @return the db name list
   */
  List<String> getAllDbs();

  List<String> getAllDbsSlave();

  List<Map<String, Object>> getTablesByDbNameAndUserAndRolesFromDbPrvs(
      MetadataQueryParam queryParam);

  List<Map<String, Object>> getTablesByDbNameAndUserAndRolesFromDbPrvsSlave(
      MetadataQueryParam queryParam);

  List<Map<String, Object>> getTablesByDbNameAndUserAndRolesFromTblPrvs(
      MetadataQueryParam queryParam);

  List<Map<String, Object>> getTablesByDbNameAndUserAndRolesFromTblPrvsSlave(
      MetadataQueryParam queryParam);

  List<Map<String, Object>> getTablesByDbName(MetadataQueryParam queryParam);

  List<Map<String, Object>> getTablesByDbNameSlave(MetadataQueryParam queryParam);

  Map<String, Object> getTableInfoByTableNameAndDbName(
      @Param("tableName") String tableName, @Param("dbName") String dbName);

  Map<String, Object> getTableInfoByTableNameAndDbNameSlave(
      @Param("tableName") String tableName, @Param("dbName") String dbName);

  /**
   * get the table partition's size
   *
   * @param queryParam the database search properties
   * @return the size
   */
  Long getPartitionSize(MetadataQueryParam queryParam);

  Long getPartitionSizeSlave(MetadataQueryParam queryParam);

  List<String> getPartitions(MetadataQueryParam queryParam);

  List<String> getPartitionsSlave(MetadataQueryParam queryParam);

  List<Map<String, Object>> getColumns(MetadataQueryParam queryParam);

  List<Map<String, Object>> getColumnsSlave(MetadataQueryParam queryParam);

  Map<String, Object> getStorageDescriptionIDByDbTableNameAndUser(MetadataQueryParam queryParam);

  Map<String, Object> getStorageDescriptionIDByDbTableNameAndUserSlave(
      MetadataQueryParam queryParam);

  Map<String, Object> getStorageDescriptionIDByDbTableNameAndUserFromDB(
      MetadataQueryParam queryParam);

  Map<String, Object> getStorageDescriptionIDByDbTableNameAndUserFromTBL(
      MetadataQueryParam queryParam);

  Map<String, Object> getStorageDescriptionIDByDbTableNameAndUserSlaveFromDB(
      MetadataQueryParam queryParam);

  Map<String, Object> getStorageDescriptionIDByDbTableNameAndUserSlaveFromTBL(
      MetadataQueryParam queryParam);

  List<Map<String, Object>> getColumnsByStorageDescriptionID(MetadataQueryParam queryParam);

  List<Map<String, Object>> getColumnsByStorageDescriptionIDSlave(MetadataQueryParam queryParam);

  List<Map<String, Object>> getPartitionKeys(MetadataQueryParam queryParam);

  List<Map<String, Object>> getPartitionKeysSlave(MetadataQueryParam queryParam);

  String getTableComment(@Param("DbName") String DbName, @Param("tableName") String tableName);

  String getTableCommentSlave(@Param("DbName") String DbName, @Param("tableName") String tableName);

  List<String> getCanWriteDbsByUser(@Param("userName") String userName);

  List<String> getCanWriteDbsByUserSlave(@Param("userName") String userName);
}
