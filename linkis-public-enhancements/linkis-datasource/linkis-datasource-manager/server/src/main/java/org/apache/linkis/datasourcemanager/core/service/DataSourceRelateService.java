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

package org.apache.linkis.datasourcemanager.core.service;

import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceType;

import java.util.List;

public interface DataSourceRelateService {
  /**
   * Get key definitions by data source type and scope
   *
   * @param dataSourceTypeId data source type id
   * @param scope scope
   * @return
   */
  List<DataSourceParamKeyDefinition> getKeyDefinitionsByType(
      Long dataSourceTypeId, DataSourceParamKeyDefinition.Scope scope);

  /**
   * Get key definitions by data source type and scope
   *
   * @param dataSourceTypeId data source type id
   * @return
   */
  List<DataSourceParamKeyDefinition> getKeyDefinitionsByType(Long dataSourceTypeId);

  /**
   * Get key definitions by data source type and languageType
   *
   * @param dataSourceTypeId data source type id
   * @param languageType language type zh or en
   * @return
   */
  List<DataSourceParamKeyDefinition> getKeyDefinitionsByType(
      Long dataSourceTypeId, String languageType);

  /**
   * Get all data source types
   *
   * @return
   */
  List<DataSourceType> getAllDataSourceTypes(String languageType);

  /**
   * Get data source type
   *
   * @param typeId
   * @return
   */
  DataSourceType getDataSourceType(Long typeId);
}
