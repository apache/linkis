/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.metadata.hive.dao;

import org.apache.linkis.metadata.hive.dto.DatabaseQueryParam;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface HiveMetaDao {

    String getLocationByDbAndTable(DatabaseQueryParam queryParam);

    /**
     * get user's rose by username
     * @param userName user's username
     * @return the rose name list
     */
    List<String> getRosesByUser(String userName);

    /**
     * get dbs by user's username and user's roles
     * @param userName user's username
     * @param roles user's roles
     * @return the db name list
     */
    List<String> getDbsByUserAndRoles(@Param("userName") String userName, @Param("roles") List<String> roles);

    /**
     * get all list of DBS NAME
     *
     * @return  the db name list
     */
    List<String> getAllDbs();

    List<Map<String, Object>> getTablesByDbNameAndUserAndRoles(DatabaseQueryParam queryParam);

    List<Map<String, Object>> getTablesByDbName(DatabaseQueryParam queryParam);

    /**
     * get the table partition's size
     * @param queryParam the database search properties
     * @return the size
     */
    Long getPartitionSize(DatabaseQueryParam queryParam);

    List<String> getPartitions(DatabaseQueryParam queryParam);

    List<Map<String, Object>> getColumns(DatabaseQueryParam queryParam);

    Map<String, Object> getStorageDescriptionIDByDbTableNameAndUser(DatabaseQueryParam queryParam);

    List<Map<String, Object>> getColumnsByStorageDescriptionID(DatabaseQueryParam queryParam);

    List<Map<String, Object>> getPartitionKeys(DatabaseQueryParam queryParam);

    String getTableComment(@Param("DbName") String DbName, @Param("tableName") String tableName);
}
