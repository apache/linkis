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
 
package org.apache.linkis.metadatamanager.server.service;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.metadatamanager.common.domain.MetaColumnInfo;
import org.apache.linkis.metadatamanager.common.domain.MetaPartitionInfo;

import java.util.List;
import java.util.Map;

public interface MetadataAppService {

    /**
     * @param dataSourceId data source id
     * @param system system
     * @return
     */
    List<String> getDatabasesByDsId(String dataSourceId, String system) throws ErrorException;

    /**
     * @param dataSourceId data source id
     * @param system system
     * @param database database
     * @return
     */
    List<String> getTablesByDsId(String dataSourceId, String database, String system) throws ErrorException;

    /**
     * @param dataSourceId data source id
     * @param database database
     * @param table table
     * @param system system
     * @return
     */
    Map<String, String> getTablePropsByDsId(String dataSourceId, String database, String table, String system) throws ErrorException;
    /**
     * @param dataSourceId data source i
     * @param database database
     * @param table table
     * @param system system
     * @return
     */
    MetaPartitionInfo getPartitionsByDsId(String dataSourceId, String database, String table, String system) throws ErrorException;

    /**
     * @param dataSourceId data source id
     * @param database database
     * @param table table
     * @param system system
     * @return
     */
    List<MetaColumnInfo> getColumns(String dataSourceId, String database, String table, String system) throws ErrorException;
}
