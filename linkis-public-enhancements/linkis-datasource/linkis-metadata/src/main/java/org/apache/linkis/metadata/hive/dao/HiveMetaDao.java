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

  /**
   * get user's roles by username
   *
   * @param userName user's username
   * @return the role name list
   */
  List<String> getRolesByUser(String userName);

  /**
   * get dbs by user's username and user's roles
   *
   * @param userName user's username
   * @param roles user's roles
   * @return the db name list
   */
  List<String> getDbsByUserAndRoles(
      @Param("userName") String userName, @Param("roles") List<String> roles);

  /**
   * get all list of DBS NAME
   *
   * @return the db name list
   */
  List<String> getAllDbs();

  List<Map<String, Object>> getTablesByDbNameAndUserAndRoles(MetadataQueryParam queryParam);

  List<Map<String, Object>> getTablesByDbName(MetadataQueryParam queryParam);

  /**
   * get the table partition's size
   *
   * @param queryParam the database search properties
   * @return the size
   */
  Long getPartitionSize(MetadataQueryParam queryParam);

  List<String> getPartitions(MetadataQueryParam queryParam);

  List<Map<String, Object>> getColumns(MetadataQueryParam queryParam);

  Map<String, Object> getStorageDescriptionIDByDbTableNameAndUser(MetadataQueryParam queryParam);

  List<Map<String, Object>> getColumnsByStorageDescriptionID(MetadataQueryParam queryParam);

  List<Map<String, Object>> getPartitionKeys(MetadataQueryParam queryParam);

  String getTableComment(@Param("DbName") String DbName, @Param("tableName") String tableName);
}
