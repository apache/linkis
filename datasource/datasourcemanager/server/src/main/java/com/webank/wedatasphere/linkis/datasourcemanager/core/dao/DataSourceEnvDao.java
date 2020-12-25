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

package com.webank.wedatasphere.linkis.datasourcemanager.core.dao;

import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceEnv;
import com.webank.wedatasphere.linkis.datasourcemanager.core.vo.DataSourceEnvVo;

import java.util.List;

/**
 * Data source dao
 * @author dennyzhou-zdx
 * 2020/02/14
 */
public interface DataSourceEnvDao {

    /**
     * View details of data source environment information
     * @param dataSourceEnvId env id
     * @return
     */
    DataSourceEnv selectOneDetail(Long dataSourceEnvId);

    /**
     * Insert one
     * @param dataSourceEnv environment
     */
    void insertOne(DataSourceEnv dataSourceEnv);

    /**
     * List all by type id
     * @param dataSourceTypeId type id
     * @return
     */
    List<DataSourceEnv> listByTypeId(Long dataSourceTypeId);

    /**
     * Remove one
     * @param envId env id
     * @return
     */
    int removeOne(Long envId);

    /**
     * Update one
     * @param updatedOne
     */
    void updateOne(DataSourceEnv updatedOne);

    /**
     * Page query
     * @param dataSourceEnvVo  environment view entity
     * @return
     */
    List<DataSourceEnv> selectByPageVo(DataSourceEnvVo dataSourceEnvVo);
}
