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
 
package org.apache.linkis.datasourcemanager.core.service;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceEnv;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceEnvVo;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceVo;

import java.util.List;

public interface DataSourceInfoService {

    /**
     * Save data source information
     * @param dataSource data source
     */
    void saveDataSourceInfo(DataSource dataSource) throws ErrorException;

    /**
     * Add parameters of data source environment
     * @param dataSourceEnvId data source environment
     * @param dataSource data source
     */
    void addEnvParamsToDataSource(Long dataSourceEnvId, DataSource dataSource);

    /**
     * Get data source
     * @param dataSourceId id
     * @param createSystem system name
     * @return data source entity
     */
    DataSource getDataSourceInfo(Long dataSourceId, String createSystem);

    /**
     * Get data source brief information
     * @param dataSourceId data source id
     * @param createSystem system
     * @return
     */
    DataSource getDataSourceInfoBrief(Long dataSourceId, String createSystem);
    /**
     * Remove data source
     * @param dataSourceId id
     * @param createSystem system name
     * @return
     */
    Long removeDataSourceInfo(Long dataSourceId, String createSystem);

    /**
     * Update data source
     * @param updatedOne updated data source
     * @param storedOne stored data source
     */
    void updateDataSourceInfo(DataSource updatedOne, DataSource storedOne) throws ErrorException;

    /**
     * Page query of data source
     * @param dataSourceVo data source view entity
     * @return
     */
    List<DataSource> queryDataSourceInfoPage(DataSourceVo dataSourceVo);

    /**
     * Save data source environment
     * @param dataSourceEnv data source environment
     */
    void saveDataSourceEnv(DataSourceEnv dataSourceEnv) throws ErrorException;

    /**
     * List data source environments
     * @param dataSourceTypeId type id
     * @return
     */
    List<DataSourceEnv> listDataSourceEnvByType(Long dataSourceTypeId);

    /**
     * Get data source environment
     * @param envId environment id
     * @return
     */
    DataSourceEnv getDataSourceEnv(Long envId);

    /**
     * Remove data source environment
     * @param envId environment id
     * @return
     */
    Long removeDataSourceEnv(Long envId);

    /**
     * Update data source environment
     * @param updatedOne
     * @param storedOne
     */
    void updateDataSourceEnv(DataSourceEnv updatedOne, DataSourceEnv storedOne) throws ErrorException;

    /**
     * Page query of data source environment
     * @param dataSourceEnvVo
     * @return
     */
    List<DataSourceEnv> queryDataSourceEnvPage(DataSourceEnvVo dataSourceEnvVo);
}
