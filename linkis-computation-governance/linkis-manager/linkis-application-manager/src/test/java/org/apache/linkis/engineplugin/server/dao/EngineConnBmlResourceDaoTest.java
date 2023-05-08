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

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineConnBmlResourceDaoTest extends BaseDaoTest {

  @Autowired EngineConnBmlResourceDao engineConnBmlResourceDao;

  @Test
  void save() {
    EngineConnBmlResource engineConnBmlResource = new EngineConnBmlResource();
    engineConnBmlResource.setEngineConnType("engineConnType");
    engineConnBmlResource.setVersion("version");
    engineConnBmlResource.setFileName("filename");
    engineConnBmlResource.setFileSize(1L);
    engineConnBmlResource.setLastModified(1L);
    engineConnBmlResource.setBmlResourceId("1");
    engineConnBmlResource.setBmlResourceVersion("bmlResourceVersion");
    engineConnBmlResource.setCreateTime(new Date());
    engineConnBmlResource.setLastUpdateTime(new Date());
    engineConnBmlResourceDao.save(engineConnBmlResource);
    EnginePluginBMLVo enginePluginBMLVo = new EnginePluginBMLVo("engineConnType", "version");
    List<EngineConnBmlResource> list = engineConnBmlResourceDao.selectByPageVo(enginePluginBMLVo);
    assertTrue(list.size() > 0);
  }

  @Test
  void getTypeList() {
    save();
    List<String> list = engineConnBmlResourceDao.getTypeList();
    assertTrue(list.size() > 0);
  }

  @Test
  void getTypeVersionList() {
    save();
    List<String> list = engineConnBmlResourceDao.getTypeVersionList("engineConnType");
    assertTrue(list.size() > 0);
  }

  @Test
  void selectByPageVo() {
    save();
    EnginePluginBMLVo enginePluginBMLVo = new EnginePluginBMLVo("engineConnType", "version");
    List<EngineConnBmlResource> list = engineConnBmlResourceDao.selectByPageVo(enginePluginBMLVo);
    assertTrue(list.size() > 0);
  }

  @Test
  void getAllEngineConnBmlResource() {
    save();
    List<EngineConnBmlResource> list =
        engineConnBmlResourceDao.getAllEngineConnBmlResource("engineConnType", "version");
    assertTrue(list.size() > 0);
  }

  @Test
  void update() {
    save();
    EngineConnBmlResource engineConnBmlResource = new EngineConnBmlResource();
    engineConnBmlResource.setEngineConnType("engineConnType");
    engineConnBmlResource.setVersion("version");
    engineConnBmlResource.setFileName("filename");
    engineConnBmlResource.setFileSize(2L);
    engineConnBmlResource.setLastModified(2L);
    engineConnBmlResource.setBmlResourceId("1");
    engineConnBmlResource.setBmlResourceVersion("bmlResourceVersion2");
    engineConnBmlResource.setLastUpdateTime(new Date());
    engineConnBmlResourceDao.update(engineConnBmlResource);
  }

  @Test
  void delete() {
    save();
    EngineConnBmlResource engineConnBmlResource = new EngineConnBmlResource();
    engineConnBmlResource.setEngineConnType("engineConnType");
    engineConnBmlResource.setVersion("version");
    engineConnBmlResource.setFileName("filename");
    engineConnBmlResourceDao.delete(engineConnBmlResource);
    List<EngineConnBmlResource> list =
        engineConnBmlResourceDao.getAllEngineConnBmlResource("engineConnType", "version");
    assertTrue(list.size() == 0);
  }
}
