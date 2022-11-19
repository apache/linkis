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

package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.DatasourceTypeEntity;
import org.apache.linkis.basedatamanager.server.domain.DatasourceTypeKeyEntity;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @description Database operations Mapper for table [linkis_ps_dm_datasource_type_key]
 * @createDate 2022-11-19 10:53:47 @Entity generator.domain.LinkisPsDmDatasourceTypeKey
 */
public interface DatasourceTypeKeyMapper extends BaseMapper<DatasourceTypeKeyEntity> {
  List<DatasourceTypeEntity> getListByPage(String searchName);
}
