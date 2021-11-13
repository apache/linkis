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
 
package org.apache.linkis.datasourcemanager.core.dao;


import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Data source dao
 */
public interface DataSourceDao {

    /**
     * Insert
     * @param dataSource data source
     */
    void  insertOne(DataSource dataSource);

    /**
     * View detail
     * @param dataSourceId data source id
     * @param createSystem system name
     * @return data source entity
     */
    DataSource selectOneDetail(@Param("dataSourceId") Long dataSourceId,
                               @Param("createSystem") String createSystem);

    /**
     * View normal
     * @param dataSourceId data source id
     * @param createSystem system name
     * @return data source entity
     */
    DataSource selectOne(@Param("dataSourceId") Long dataSourceId,
                         @Param("createSystem") String createSystem);
    /**
     * Delete One
     * @param dataSourceId data source id
     * @param createSystem create system
     * @return affect row
     */
    int removeOne(@Param("dataSourceId")Long dataSourceId,
                  @Param("createSystem")String createSystem);


    /**
     * Update one
     * @param updatedOne updated one
     */
    void updateOne(DataSource updatedOne);

    /**
     * Page of query
     * @param dataSourceVo data source view entity
     * @return query list
     */
    List<DataSource> selectByPageVo(DataSourceVo dataSourceVo);
}
