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

package org.apache.linkis.bml.dao;

import org.apache.linkis.bml.entity.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDaoTest extends BaseDaoTest {

  @Autowired ResourceDao resourceDao;

  void insertResource() {
    Resource resource = new Resource();
    resource.setResourceId("123");
    resource.setResourceHeader("2");
    resource.setDownloadedFileName("testFileName");
    resource.setSystem("testSystem");
    resource.setCreateTime(new Date());
    resource.setUser("testUser");
    resource.setExpireTime("2012.12.02");
    resource.setMaxVersion(3);
    resource.setUpdateTime(new Date());
    resource.setUpdator("testUpdator");
    resource.setEnableFlag(false);
    resourceDao.uploadResource(resource);
  }

  @Test
  void testGetResources() {
    insertResource();
    Map<String, Object> map = new HashMap<>();
    map.put("owner", "testowner");
    map.put("resource_id", "123");
    map.put("sys", "testsys");
    resourceDao.getResources(map);
  }

  @Test
  void testDeleteResource() {
    insertResource();
    resourceDao.deleteResource("123");
  }

  @Test
  void testBatchDeleteResources() {
    insertResource();
    List<String> list = new ArrayList<>();
    list.add("123");
    list.add("2");
    list.add("3");
    resourceDao.batchDeleteResources(list);
  }

  @Test
  void testUploadResource() {
    Resource resource = new Resource();
    resource.setResourceId("123");
    resource.setResourceHeader("2");
    resource.setDownloadedFileName("testFileName");
    resource.setSystem("testSystem");
    resource.setCreateTime(new Date());
    resource.setUser("testUser");
    resource.setExpireTime("2012.12.02");
    resource.setMaxVersion(3);
    resource.setUpdateTime(new Date());
    resource.setUpdator("testUpdator");
    resource.setEnableFlag(false);
    resourceDao.uploadResource(resource);
  }

  @Test
  void testCheckExists() {
    insertResource();
    resourceDao.checkExists("123");
  }

  @Test
  void testGetResource() {
    insertResource();
    resourceDao.getResource("123");
  }

  @Test
  void testGetUserByResourceId() {
    insertResource();
    resourceDao.getUserByResourceId("123");
  }

  @Test
  void testChangeOwner() {
    String oldOwner = "oldtest";
    String newOwner = "newtest";
    resourceDao.changeOwner("123", oldOwner, newOwner);
  }
}
