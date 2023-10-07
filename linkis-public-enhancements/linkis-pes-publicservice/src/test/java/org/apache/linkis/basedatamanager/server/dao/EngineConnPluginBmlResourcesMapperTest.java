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

import org.apache.linkis.basedatamanager.server.domain.EngineConnPluginBmlResources;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineConnPluginBmlResourcesMapperTest extends BaseDaoTest {

  @Autowired EngineConnPluginBmlResourcesMapper engineConnPluginBmlResourcesMapper;

  EngineConnPluginBmlResources insert() {
    EngineConnPluginBmlResources engineConnPluginBmlResources = new EngineConnPluginBmlResources();
    engineConnPluginBmlResources.setBmlResourceId("dmlResourceId");
    engineConnPluginBmlResources.setEngineConnType("engineConnType");
    engineConnPluginBmlResources.setBmlResourceVersion("bmlResourceVersion");
    engineConnPluginBmlResources.setCreateTime(new Date());
    engineConnPluginBmlResources.setFileName("fileName");
    engineConnPluginBmlResources.setFileSize(1L);
    engineConnPluginBmlResources.setLastModified(1L);
    engineConnPluginBmlResources.setLastUpdateTime(new Date());
    engineConnPluginBmlResources.setVersion("version");
    engineConnPluginBmlResourcesMapper.insert(engineConnPluginBmlResources);
    return engineConnPluginBmlResources;
  }

  @Test
  void getListByPage() {
    insert();
    List<String> list = engineConnPluginBmlResourcesMapper.getEngineTypeList();
    assertTrue(list.size() > 0);
  }
}
