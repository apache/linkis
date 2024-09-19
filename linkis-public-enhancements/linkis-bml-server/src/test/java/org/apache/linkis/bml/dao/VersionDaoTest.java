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

import org.apache.linkis.bml.entity.ResourceVersion;
import org.apache.linkis.bml.entity.Version;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionDaoTest extends BaseDaoTest {

  @Autowired VersionDao versionDao;

  private final String resourceId = "123";
  private final String version = "1.2";

  void insertVersion() {
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
    resourceVersion.setEnableFlag(true);
    versionDao.insertNewVersion(resourceVersion);
  }

  @Test
  void testGetVersion() {
    insertVersion();
    versionDao.getVersion(resourceId, version);
  }

  @Test
  void testGetVersions() {
    insertVersion();
    versionDao.getVersions(resourceId);
  }

  @Test
  void testGetResourcesVersions() {
    insertVersion();
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
  void testDeleteVersion() {
    insertVersion();
    versionDao.deleteVersion(resourceId, version);
  }

  @Test
  void testDeleteVersions() {
    insertVersion();
    versionDao.deleteVersions(resourceId);
  }

  @Test
  void testBathDeleteVersions() {
    insertVersion();
    List<String> resourceIdlist = new ArrayList<>();
    resourceIdlist.add(resourceId);
    resourceIdlist.add("21");
    List<String> versionlist = new ArrayList<>();
    versionlist.add(version);
    versionlist.add("2.1");
    versionDao.bathDeleteVersions(resourceIdlist, versionlist);
  }

  @Test
  void testInsertNewVersion() {
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
    resourceVersion.setEnableFlag(true);
    versionDao.insertNewVersion(resourceVersion);
  }

  @Test
  void testGetResourcePath() {
    insertVersion();
    versionDao.getResourcePath(resourceId);
  }

  @Test
  void testGetNewestVersion() {
    insertVersion();
    versionDao.getNewestVersion(resourceId);
  }

  @Test
  void testGetStartByteForResource() {
    insertVersion();
    versionDao.getStartByteForResource(resourceId, version);
  }

  @Test
  void testGetEndByte() {
    insertVersion();
    versionDao.getEndByte(resourceId, version);
  }

  @Test
  void testFindResourceVersion() {
    insertVersion();
    versionDao.findResourceVersion(resourceId, version);
  }

  @Test
  void testGetAllResourcesViaSystem() {
    insertVersion();
    versionDao.getAllResourcesViaSystem(resourceId, version);
  }

  @Test
  void testSelectResourcesViaSystemByPage() {
    insertVersion();
    versionDao.selectResourcesViaSystemByPage(resourceId, version);
  }

  @Test
  void testCheckVersion() {
    insertVersion();
    versionDao.checkVersion(resourceId, version);
  }

  @Test
  void testSelectResourceVersionEnbleFlag() {
    insertVersion();
    versionDao.selectResourceVersionEnbleFlag(resourceId, version);
  }

  @Test
  void testDeleteResource() {
    insertVersion();
    versionDao.deleteResource(resourceId);
  }

  @Test
  void testBatchDeleteResources() {
    insertVersion();
    List<String> resourceIdlist = new ArrayList<>();
    resourceIdlist.add(resourceId);
    resourceIdlist.add("21");
    List<String> versionlist = new ArrayList<>();
    versionlist.add(version);
    versionlist.add("2.1");
    versionDao.bathDeleteVersions(resourceIdlist, versionlist);
  }

  @Test
  void testGetResourceVersion() {
    versionDao.getResourceVersion(resourceId, version);
  }

  @Test
  void testSelectVersionByPage() {
    insertVersion();
    List<Version> list = versionDao.selectVersionByPage(resourceId);
    assertTrue(list.size() >= 1);
  }

  @Test
  void testGetResourceVersionsByResourceId() {
    insertVersion();
    List<ResourceVersion> list = versionDao.getResourceVersionsByResourceId(resourceId);
    assertTrue(list.size() >= 1);
  }
}
