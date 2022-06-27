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

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface HiveMetaDao {

    String getLocationByDbAndTable(Map<String, String> map);

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

    List<Map<String, Object>> getTablesByDbNameAndUserAndRoles(Map<String, Object> params);

    List<Map<String, Object>> getTablesByDbName(Map<String, Object> map);

    Long getPartitionSize(Map<String, String> map);

    List<String> getPartitions(Map<String, String> map);

    List<Map<String, Object>> getColumns(Map<String, Object> map);

    Map<String, Object> getStorageDescriptionIDByDbTableNameAndUser(Map<String, Object> map);

    List<Map<String, Object>> getColumnsByStorageDescriptionID(Map<String, Object> map);

    List<Map<String, Object>> getPartitionKeys(Map<String, Object> map);

    String getTableComment(@Param("DbName") String DbName, @Param("tableName") String tableName);
}
