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

package org.apache.linkis.datasourcemanager.core.dao;

import org.apache.linkis.datasourcemanager.common.domain.DatasourceVersion;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataSourceVersionDao {

  /**
   * get latest version from datasource id, if null return 0;
   *
   * @param dataSourceId
   * @return
   */
  Long getLatestVersion(Long dataSourceId);

  /**
   * insert a version of the datasource
   *
   * @param datasourceVersion
   */
  void insertOne(DatasourceVersion datasourceVersion);

  /**
   * get a version of datasource
   *
   * @param dataSourceId
   * @param version
   * @return
   */
  String selectOneVersion(@Param("dataSourceId") Long dataSourceId, @Param("version") Long version);

  /**
   * get version list from datasource id
   *
   * @param dataSourceId
   * @return
   */
  List<DatasourceVersion> getVersionsFromDatasourceId(Long dataSourceId);

  /**
   * remove all versions form datasourceId
   *
   * @param dataSourceId
   * @return
   */
  int removeFromDataSourceId(Long dataSourceId);

  void updateByDatasourceVersion(DatasourceVersion datasourceVersion);
}
