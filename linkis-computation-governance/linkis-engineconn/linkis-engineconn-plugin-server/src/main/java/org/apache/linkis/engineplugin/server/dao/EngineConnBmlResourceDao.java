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

package org.apache.linkis.engineplugin.server.dao;

import org.apache.linkis.engineplugin.server.entity.EngineConnBmlResource;
import org.apache.linkis.engineplugin.vo.EnginePluginBMLVo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EngineConnBmlResourceDao {

  List<String> getTypeList();

  List<String> getTypeVersionList(@Param("type") String type);

  List<EngineConnBmlResource> selectByPageVo(EnginePluginBMLVo enginePluginBMLVo);

  List<EngineConnBmlResource> getAllEngineConnBmlResource(
      @Param("engineConnType") String engineConnType, @Param("version") String version);

  void save(@Param("engineConnBmlResource") EngineConnBmlResource engineConnBmlResource);

  void update(@Param("engineConnBmlResource") EngineConnBmlResource engineConnBmlResource);

  void delete(@Param("engineConnBmlResource") EngineConnBmlResource engineConnBmlResource);
}
