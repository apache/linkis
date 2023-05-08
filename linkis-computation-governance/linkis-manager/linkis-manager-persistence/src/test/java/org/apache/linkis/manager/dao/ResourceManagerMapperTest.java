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

package org.apache.linkis.manager.dao;

import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceManagerMapperTest extends BaseDaoTest {

  @Autowired ResourceManagerMapper resourceManagerMapper;

  @Test
  PersistenceResource registerResource() {
    PersistenceResource persistenceResource = new PersistenceResource();
    //    persistenceResource.setId(1);
    persistenceResource.setMaxResource("testmax");
    persistenceResource.setMinResource("mintest");
    persistenceResource.setLeftResource("left");
    persistenceResource.setUsedResource("user");
    persistenceResource.setTicketId("1");
    persistenceResource.setResourceType("testtype");
    resourceManagerMapper.registerResource(persistenceResource);
    PersistenceResource persistenceResources =
        resourceManagerMapper.getResourceById(persistenceResource.getId());
    assertThat(persistenceResources.getId())
        .usingRecursiveComparison()
        .isEqualTo(persistenceResource.getId());
    return persistenceResource;
  }

  @Test
  void nodeResourceUpdate() {
    PersistenceResource insert = registerResource();
    PersistenceResource persistenceResource = new PersistenceResource();
    persistenceResource.setId(insert.getId());
    persistenceResource.setMaxResource("testmaxss");
    persistenceResource.setMinResource("mintestss");
    persistenceResource.setLeftResource("left");
    persistenceResource.setUsedResource("user");
    persistenceResource.setResourceType("testtype");
    persistenceResource.setUpdateTime(new Date());
    resourceManagerMapper.nodeResourceUpdate("1", persistenceResource);
    PersistenceResource persistenceResources =
        resourceManagerMapper.getResourceById(insert.getId());
    assertTrue(persistenceResources.getMaxResource().equals(persistenceResource.getMaxResource()));
  }

  @Test
  void nodeResourceUpdateByResourceId() {
    PersistenceResource insert = registerResource();
    PersistenceResource persistenceResource = new PersistenceResource();
    persistenceResource.setId(insert.getId());
    persistenceResource.setMaxResource("testmaxss");
    persistenceResource.setMinResource("mintestss");
    persistenceResource.setLeftResource("left");
    persistenceResource.setUsedResource("user");
    resourceManagerMapper.nodeResourceUpdateByResourceId(insert.getId(), persistenceResource);
    assertTrue(persistenceResource.getMaxResource() == persistenceResource.getMaxResource());
  }

  @Test
  void getNodeResourceUpdateResourceId() {
    Integer i = resourceManagerMapper.getNodeResourceUpdateResourceId("instance1");
    assertTrue(i >= 1);
  }

  @Test
  void deleteResourceAndLabelId() {
    resourceManagerMapper.deleteResourceAndLabelId("instance1");
  }

  @Test
  void deleteResourceByInstance() {
    registerResource();
    resourceManagerMapper.deleteResourceByInstance("instance1");
    List<PersistenceResource> list =
        resourceManagerMapper.getResourceByServiceInstance("instance1");
    assertTrue(list.size() == 0);
  }

  @Test
  void deleteResourceByTicketId() {
    registerResource();
    resourceManagerMapper.deleteResourceByTicketId("1");
    PersistenceResource persistenceResource = resourceManagerMapper.getNodeResourceByTicketId("1");
    assertTrue(persistenceResource == null);
  }

  @Test
  void getResourceByInstanceAndResourceType() {
    registerResource();
    List<PersistenceResource> list =
        resourceManagerMapper.getResourceByInstanceAndResourceType("instance1", "testtype");
    int i = list.size();
    assertTrue(i == 1);
  }

  @Test
  void getResourceByServiceInstance() {
    registerResource();
    List<PersistenceResource> list =
        resourceManagerMapper.getResourceByServiceInstance("instance1");
    int i = list.size();
    assertTrue(i == 1);
  }

  @Test
  void getNodeResourceByTicketId() {
    PersistenceResource persistenceResource = resourceManagerMapper.getNodeResourceByTicketId("1");
    assertTrue(persistenceResource != null);
  }

  @Test
  void getResourceByUserName() {
    registerResource();
    List<PersistenceResource> list = resourceManagerMapper.getResourceByUserName("testname");
    int i = list.size();
    assertTrue(i == 1);
  }

  @Test
  void getLabelsByTicketId() {
    registerResource();
    List<PersistenceLabel> list = resourceManagerMapper.getLabelsByTicketId("1");
    int i = list.size();
    assertTrue(i == 1);
  }

  @Test
  void deleteResourceById() {
    PersistenceResource insert = registerResource();
    List<Integer> list = new ArrayList<>();
    list.add(insert.getId());
    resourceManagerMapper.deleteResourceById(list);
    PersistenceResource persistenceResource = resourceManagerMapper.getResourceById(insert.getId());
    assertTrue(persistenceResource == null);
  }

  @Test
  void deleteResourceRelByResourceId() {
    PersistenceResource insert = registerResource();
    List<Integer> list = new ArrayList<>();
    list.add(insert.getId());
    resourceManagerMapper.deleteResourceRelByResourceId(list);
  }

  @Test
  void getResourceById() {
    PersistenceResource insert = registerResource();
    PersistenceResource persistenceResource = resourceManagerMapper.getResourceById(insert.getId());
    assertTrue(persistenceResource != null);
  }
}
