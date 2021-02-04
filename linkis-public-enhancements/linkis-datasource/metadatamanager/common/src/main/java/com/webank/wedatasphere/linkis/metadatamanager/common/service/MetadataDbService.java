/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.metadatamanager.common.service;

import com.webank.wedatasphere.linkis.metadatamanager.common.domain.MetaColumnInfo;
import com.webank.wedatasphere.linkis.metadatamanager.common.domain.MetaPartitionInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by jackyxxie on 2020/2/10.
 */
public interface MetadataDbService extends BaseMetadataService {

    /**
     * Get all databases
     * @param params connect params
     * @return
     */
    List<String> getDatabases(String operator, Map<String, Object> params);

    /**
     * Get all tables from database specified
     * @param params params
     * @param database database name
     * @return
     */
    List<String> getTables(String operator, Map<String, Object> params, String database);

    /**
     * Get table properties from database specified
     * @param params params
     * @param database database name
     * @return
     */
    Map<String, String> getTableProps(String operator, Map<String, Object> params, String database, String table);
    /**
     * Get all partitions from table specified
     * @param params params
     * @param database
     * @param table
     * @return
     */
    MetaPartitionInfo getPartitions(String operator, Map<String, Object> params, String database, String table);

    /**
     * Get all field information from table specified
     * @param params
     * @param database
     * @param table
     * @return
     */
    List<MetaColumnInfo> getColumns(String operator, Map<String, Object> params, String database, String table);

}
