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

package org.apache.linkis.bml.dao;

import org.apache.linkis.bml.entity.ResourceVersion;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionDaoTest extends BaseDaoTest {

  @Autowired VersionDao versionDao;

  private final String resourceId = "123";
  private final String version = "1.2";

  @Test
  void getVersion() {
    insertNewVersion();
    versionDao.getVersion(resourceId, version);
  }

  @Test
  void getVersions() {
    insertNewVersion();
    versionDao.getVersions(resourceId);
  }

  @Test
  void getResourcesVersions() {
    insertNewVersion();
    Map<String, Object> map = new HashMap<>();
    map.put("system", "testSys");
    map.put("user", "binbin");
    List<String> list = new ArrayList<>();
    list.add("123");
    list.add("321");
    map.put("resourceIds", list);
    versionDao.getResourcesVersions(map);
  }

  @Test
  void deleteVersion() {
    insertNewVersion();
    versionDao.deleteVersion(resourceId, version);
  }

  @Test
  void deleteVersions() {
    insertNewVersion();
    versionDao.deleteVersions(resourceId);
  }

  @Test
  void bathDeleteVersions() {
    insertNewVersion();
    List<String> resourceIdlist = new ArrayList<>();
    resourceIdlist.add(resourceId);
    resourceIdlist.add("21");
    List<String> versionlist = new ArrayList<>();
    versionlist.add(version);
    versionlist.add("2.1");
    versionDao.bathDeleteVersions(resourceIdlist, versionlist);
  }

  @Test
  void insertNewVersion() {
    ResourceVersion resourceVersion = new ResourceVersion();
    resourceVersion.setResourceId(resourceId);
    resourceVersion.setUser("binbin");
    resourceVersion.setSystem("testSys");
    resourceVersion.setFileMd5("binbinmd5");
    resourceVersion.setVersion(version);
    resourceVersion.setSize(25);
    resourceVersion.setStartByte(35);
    resourceVersion.setEndByte(36);
    resourceVersion.setResource("testreso");
    resourceVersion.setDescription("testDesc");
    resourceVersion.setStartTime(new Date());
    resourceVersion.setEndTime(new Date());
    resourceVersion.setClientIp("132.145.36");
    resourceVersion.setUpdator("testUp");
    resourceVersion.setEnableFlag(false);
    versionDao.insertNewVersion(resourceVersion);
  }

  @Test
  void getResourcePath() {
    insertNewVersion();
    versionDao.getResourcePath(resourceId);
  }

  @Test
  void getNewestVersion() {
    insertNewVersion();
    versionDao.getNewestVersion(resourceId);
  }

  @Test
  void getStartByteForResource() {
    insertNewVersion();
    versionDao.getStartByteForResource(resourceId, version);
  }

  @Test
  void getEndByte() {
    insertNewVersion();
    versionDao.getEndByte(resourceId, version);
  }

  @Test
  void findResourceVersion() {
    insertNewVersion();
    versionDao.findResourceVersion(resourceId, version);
  }

  @Test
  void getAllResourcesViaSystem() {
    insertNewVersion();
    versionDao.getAllResourcesViaSystem(resourceId, version);
  }

  @Test
  void selectResourcesViaSystemByPage() {
    insertNewVersion();
    versionDao.selectResourcesViaSystemByPage(resourceId, version);
  }

  @Test
  void checkVersion() {
    insertNewVersion();
    versionDao.checkVersion(resourceId, version);
  }

  @Test
  void selectResourceVersionEnbleFlag() {
    insertNewVersion();
    versionDao.selectResourceVersionEnbleFlag(resourceId, version);
  }

  @Test
  void deleteResource() {
    insertNewVersion();
    versionDao.deleteResource(resourceId);
  }

  @Test
  void batchDeleteResources() {
    insertNewVersion();
    List<String> resourceIdlist = new ArrayList<>();
    resourceIdlist.add(resourceId);
    resourceIdlist.add("21");
    List<String> versionlist = new ArrayList<>();
    versionlist.add(version);
    versionlist.add("2.1");
    versionDao.bathDeleteVersions(resourceIdlist, versionlist);
  }

  @Test
  void getResourceVersion() {
    versionDao.getResourceVersion(resourceId, version);
  }

  @Test
  void selectVersionByPage() {
    insertNewVersion();
    versionDao.selectVersionByPage(resourceId);
  }

  @Test
  void getResourceVersionsByResourceId() {
    insertNewVersion();
    versionDao.getResourceVersionsByResourceId(resourceId);
  }
}
