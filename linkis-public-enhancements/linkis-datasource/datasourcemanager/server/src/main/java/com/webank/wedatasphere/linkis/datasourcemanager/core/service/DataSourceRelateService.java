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

package com.webank.wedatasphere.linkis.datasourcemanager.core.service;

import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceType;

import java.util.List;

/**
 * @author davidhua
 * 2020/02/14
 */
public interface DataSourceRelateService {
    /**
     * Get key definitions by data source type and scope
     * @param dataSourceTypeId data source type id
     * @param scope scope
     * @return
     */
    List<DataSourceParamKeyDefinition> getKeyDefinitionsByType(Long dataSourceTypeId,
                                                               DataSourceParamKeyDefinition.Scope scope);

    /**
     * Get key definitions by data source type and scope
     * @param dataSourceTypeId data source type id
     * @return
     */
    List<DataSourceParamKeyDefinition> getKeyDefinitionsByType(Long dataSourceTypeId);

    /**
     * Get all data source types
     * @return
     */
    List<DataSourceType> getAllDataSourceTypes();

    /**
     * Get data source type
     * @param typeId
     * @return
     */
    DataSourceType getDataSourceType(Long typeId);
}
