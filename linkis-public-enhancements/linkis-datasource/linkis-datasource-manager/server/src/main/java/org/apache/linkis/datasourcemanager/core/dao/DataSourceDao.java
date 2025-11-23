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

import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceVo;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/** Data source dao */
public interface DataSourceDao {

  /**
   * Insert
   *
   * @param dataSource data source
   */
  void insertOne(DataSource dataSource);

  /**
   * View detail
   *
   * @param dataSourceId data source id
   * @return data source entity
   */
  DataSource selectOneDetail(@Param("dataSourceId") Long dataSourceId);

  /**
   * View detail by name
   *
   * @param dataSourceName data source name
   * @return data source entity
   */
  DataSource selectOneDetailByName(@Param("dataSourceName") String dataSourceName);
  /**
   * View normal
   *
   * @param dataSourceId data source id
   * @return data source entity
   */
  DataSource selectOne(@Param("dataSourceId") Long dataSourceId);

  /**
   * Select one by username
   *
   * @param dataSourceName data source name
   * @return data source entity
   */
  DataSource selectOneByName(@Param("dataSourceName") String dataSourceName);
  /**
   * Delete One
   *
   * @param dataSourceId data source id
   * @return affect row
   */
  int removeOne(@Param("dataSourceId") Long dataSourceId);

  /**
   * Expire One
   *
   * @param dataSourceId data source id
   * @return affect row
   */
  int expireOne(@Param("dataSourceId") Long dataSourceId);

  /**
   * Update one
   *
   * @param updatedOne updated one
   */
  void updateOne(DataSource updatedOne);

  /**
   * Page of query
   *
   * @param dataSourceVo data source view entity
   * @return query list
   */
  List<DataSource> selectByPageVo(DataSourceVo dataSourceVo);

  /**
   * Find by id list
   *
   * @param ids
   * @return
   */
  List<DataSource> selectByIds(@Param("ids") List ids);

  /**
   * update published version id
   *
   * @param dataSourceId
   * @param versionId
   */
  int setPublishedVersionId(
      @Param("dataSourceId") Long dataSourceId, @Param("versionId") Long versionId);

  /**
   * update version id
   *
   * @param datasourceId
   * @param versionId
   */
  void updateVersionId(
      @Param("dataSourceId") Long datasourceId, @Param("versionId") long versionId);

  List<DataSource> selectDatasourcesByType(
      @Param("datasourceTypeName") String datasourceTypeName,
      @Param("datasourceUser") String datasourceUser);
}
