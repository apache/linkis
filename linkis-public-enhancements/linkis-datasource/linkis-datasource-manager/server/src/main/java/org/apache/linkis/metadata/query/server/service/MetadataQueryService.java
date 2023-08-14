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

package org.apache.linkis.metadata.query.server.service;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.metadata.query.common.domain.GenerateSqlInfo;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;
import org.apache.linkis.metadata.query.common.domain.MetaPartitionInfo;

import java.util.List;
import java.util.Map;

public interface MetadataQueryService {

  /**
   * Get connection
   *
   * @param params connect params
   * @return
   */
  void getConnection(String dataSourceType, String operator, Map<String, Object> params)
      throws Exception;

  /**
   * @param dataSourceId data source id
   * @param system system
   * @return
   */
  @Deprecated
  List<String> getDatabasesByDsId(String dataSourceId, String system, String userName)
      throws ErrorException;

  /**
   * @param dataSourceId data source id
   * @param system system
   * @param database database
   * @return
   */
  @Deprecated
  List<String> getTablesByDsId(String dataSourceId, String database, String system, String userName)
      throws ErrorException;

  /**
   * @param dataSourceId data source id
   * @param database database
   * @param table table
   * @param system system
   * @return
   */
  @Deprecated
  Map<String, String> getTablePropsByDsId(
      String dataSourceId, String database, String table, String system, String userName)
      throws ErrorException;
  /**
   * @param dataSourceId data source i
   * @param database database
   * @param table table
   * @param system system
   * @return
   */
  @Deprecated
  MetaPartitionInfo getPartitionsByDsId(
      String dataSourceId,
      String database,
      String table,
      String system,
      Boolean traverse,
      String userName)
      throws ErrorException;

  /**
   * @param dataSourceId data source id
   * @param database database
   * @param table table
   * @param partition partition
   * @param system system
   * @param userName userName
   * @return
   * @throws ErrorException
   */
  @Deprecated
  Map<String, String> getPartitionPropsByDsId(
      String dataSourceId,
      String database,
      String table,
      String partition,
      String system,
      String userName)
      throws ErrorException;

  /**
   * @param dataSourceId data source id
   * @param database database
   * @param table table
   * @param system system
   * @return
   */
  @Deprecated
  List<MetaColumnInfo> getColumnsByDsId(
      String dataSourceId, String database, String table, String system, String userName)
      throws ErrorException;

  /**
   * Get connection information
   *
   * @param dataSourceName data source name
   * @param queryParams query params
   * @param system system
   * @param userName user
   * @return
   */
  Map<String, String> getConnectionInfoByDsName(
      String dataSourceName, Map<String, String> queryParams, String system, String userName)
      throws ErrorException;

  /**
   * @param dataSourceName data source name
   * @param system system
   * @return
   */
  List<String> getDatabasesByDsName(String dataSourceName, String system, String userName)
      throws ErrorException;

  /**
   * @param dataSourceName
   * @param system
   * @param userName
   * @param envId
   * @return
   * @throws ErrorException
   */
  List<String> getDatabasesByDsNameAndEnvId(
      String dataSourceName, String system, String userName, String envId) throws ErrorException;

  /**
   * @param dataSourceName data source name
   * @param system system
   * @param database database
   * @return
   */
  List<String> getTablesByDsName(
      String dataSourceName, String database, String system, String userName) throws ErrorException;

  /**
   * @param dataSourceName
   * @param database
   * @param system
   * @param userName
   * @param envId
   * @return
   * @throws ErrorException
   */
  List<String> getTablesByDsNameAndEnvId(
      String dataSourceName, String database, String system, String userName, String envId)
      throws ErrorException;

  /**
   * @param dataSourceName data source name
   * @param database database
   * @param table table
   * @param system system
   * @return
   */
  Map<String, String> getTablePropsByDsName(
      String dataSourceName, String database, String table, String system, String userName)
      throws ErrorException;
  /**
   * @param dataSourceName data source name
   * @param database database
   * @param table table
   * @param system system
   * @return
   */
  MetaPartitionInfo getPartitionsByDsName(
      String dataSourceName,
      String database,
      String table,
      String system,
      Boolean traverse,
      String userName)
      throws ErrorException;

  /**
   * @param dataSourceName data source name
   * @param database database
   * @param table table
   * @param partition partition
   * @param system system
   * @param userName userName
   * @return
   * @throws ErrorException
   */
  Map<String, String> getPartitionPropsByDsName(
      String dataSourceName,
      String database,
      String table,
      String partition,
      String system,
      String userName)
      throws ErrorException;

  /**
   * @param dataSourceName data source id
   * @param database database
   * @param table table
   * @param system system
   * @return
   */
  List<MetaColumnInfo> getColumnsByDsName(
      String dataSourceName, String database, String table, String system, String userName)
      throws ErrorException;

  /**
   * @param dataSourceName
   * @param database
   * @param table
   * @param system
   * @param userName
   * @param envId
   * @return
   * @throws ErrorException
   */
  List<MetaColumnInfo> getColumnsByDsNameAndEnvId(
      String dataSourceName,
      String database,
      String table,
      String system,
      String userName,
      String envId)
      throws ErrorException;

  /**
   * @param dataSourceName
   * @param database
   * @param table
   * @param system
   * @param userName
   * @param envId
   * @return
   * @throws ErrorException
   */
  GenerateSqlInfo getSparkSqlByDsNameAndEnvId(
      String dataSourceName,
      String database,
      String table,
      String system,
      String userName,
      String envId)
      throws ErrorException;

  /**
   * @param dataSourceName
   * @param database
   * @param table
   * @param system
   * @param userName
   * @param envId
   * @return
   * @throws ErrorException
   */
  GenerateSqlInfo getJdbcSqlByDsNameAndEnvId(
      String dataSourceName,
      String database,
      String table,
      String system,
      String userName,
      String envId)
      throws ErrorException;
}
