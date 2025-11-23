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

package org.apache.linkis.bml.service;

import org.apache.linkis.bml.dao.VersionDao;
import org.apache.linkis.bml.entity.ResourceVersion;
import org.apache.linkis.bml.entity.Version;
import org.apache.linkis.bml.service.impl.VersionServiceImpl;

import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** VersionServiceImpl Tester */
@ExtendWith(MockitoExtension.class)
public class VersionServiceImplTest {

  @InjectMocks private VersionServiceImpl versionServiceImpl;

  @Mock private VersionDao versionDao;

  ResourceVersion buildResourceVersion() {
    ResourceVersion resourceVersion = new ResourceVersion();
    resourceVersion.setResourceId("123");
    resourceVersion.setUser("binbin");
    resourceVersion.setSystem("testSys");
    resourceVersion.setFileMd5("binbinmd5");
    resourceVersion.setVersion("0.231");
    resourceVersion.setSize(25);
    resourceVersion.setStartByte(12);
    resourceVersion.setEndByte(36);
    resourceVersion.setResource("testreso");
    resourceVersion.setDescription("testDesc");
    resourceVersion.setStartTime(new Date());
    resourceVersion.setEndTime(new Date());
    resourceVersion.setClientIp("132.145.36");
    resourceVersion.setUpdator("testUp");
    resourceVersion.setEnableFlag(false);
    return resourceVersion;
  }

  @Test
  public void testGetVersion() throws Exception {
    String resourceId = "123";
    String version = "0.231";
    Version versionInfo = new Version();
    versionInfo.setResource(resourceId);
    versionInfo.setVersion(version);
    Mockito.when(versionDao.getVersion(resourceId, version)).thenReturn(versionInfo);
    Version versionInfo1 = versionServiceImpl.getVersion(resourceId, version);
    assertTrue(versionInfo1.equals(versionInfo));
  }

  @Test
  public void testGetResourcesVersions() throws Exception {
    Map paramMap = new HashMap();
    List<ResourceVersion> list = new ArrayList<>();
    list.add(buildResourceVersion());
    Mockito.when(versionDao.getResourcesVersions(paramMap)).thenReturn(list);
    List<ResourceVersion> list1 = versionServiceImpl.getResourcesVersions(paramMap);
    assertTrue(list1.equals(list));
  }

  @Test
  public void testDeleteResourceVersion() throws Exception {
    String resourceId = "123";
    String version = "0.231";
    versionServiceImpl.deleteResourceVersion(resourceId, version);
  }

  @Test
  public void testDeleteResourceVersions() throws Exception {}

  @Test
  public void testDeleteResourcesVersions() throws Exception {}

  @Test
  public void testUpdateVersion() throws Exception {
    /* String resourceId="123";
    String user="testUser";
    MultipartFile file=Mockito.mock(MultipartFile.class);
    InputStream inputStream = Mockito.mock(InputStream.class);
    Map<String, Object> params=new HashMap<>();
    params.put("newVersion","c/path");
    StringBuilder stringBuilder = new StringBuilder();
    Mockito.when(versionDao.getResourcePath(resourceId)).thenReturn("hdfs:///data/linkis/linkis/20220609/b4fd8f59-9492-4a0f-a074-9ac573a69b60");
    ResourceHelper resourceHelper=Mockito.mock(ResourceHelper.class);
    Mockito.when(resourceHelper.upload("/etc",user,inputStream,stringBuilder,false)).thenReturn(1l);
    String version=versionServiceImpl.updateVersion(resourceId,user,file,params);
    assertTrue(params.get("newVersion").equals(version));*/
  }

  @Test
  public void testGetNewestVersion() throws Exception {
    String resourceId = "123";
    Mockito.when(versionDao.getNewestVersion(resourceId)).thenReturn("test");
    String startbyte = versionServiceImpl.getNewestVersion(resourceId);
    assertTrue(startbyte != null);
  }

  @Test
  public void testDownloadResource() throws Exception {
    /*String user="testuser";
    String resourceId="123";
    String version="0.231";
    OutputStream outputStream=Mockito.mock(OutputStream.class);
    Map<String, Object> properties=new HashMap<>();
    ResourceVersion resourceVersion = buildResourceVersion();
    Mockito.when(versionDao.findResourceVersion(resourceId,version)).thenReturn(resourceVersion);
    boolean whether=versionServiceImpl.downloadResource(user,resourceId,version,outputStream,properties);
    assertTrue(whether == true);*/
  }

  @Test
  public void testGetVersions() throws Exception {}

  @Test
  public void testSelectVersionByPage() throws Exception {}

  @Test
  public void testGetAllResourcesViaSystem() throws Exception {
    String system = "testSystem";
    String user = "userTest";
    List<ResourceVersion> list = new ArrayList<>();
    list.add(buildResourceVersion());
    Mockito.when(versionDao.getAllResourcesViaSystem(system, user)).thenReturn(list);
    List<ResourceVersion> list1 = versionServiceImpl.getAllResourcesViaSystem(system, user);
    assertTrue(list1.equals(list));
  }

  @Test
  public void testSelectResourcesViaSystemByPage() throws Exception {
    int currentPage = 1;
    int pageSize = 3;
    String system = "testSystem";
    String user = "userTest";
    List<ResourceVersion> list = new ArrayList<>();
    list.add(buildResourceVersion());
    Mockito.when(versionDao.selectResourcesViaSystemByPage(system, user)).thenReturn(list);
    List<ResourceVersion> list1 =
        versionServiceImpl.selectResourcesViaSystemByPage(currentPage, pageSize, system, user);
    assertTrue(list1.equals(list));
  }

  @Test
  public void testCheckVersion() throws Exception {
    String resourceId = "123";
    String version = "0.231";
    Mockito.when(versionDao.checkVersion(resourceId, version)).thenReturn(1);
    boolean whether = versionServiceImpl.checkVersion(resourceId, version);
    assertTrue(whether);
  }

  @Test
  public void testCanAccess() throws Exception {
    String resourceId = "123";
    String version = "0.231";
    Mockito.when(versionDao.selectResourceVersionEnbleFlag(resourceId, version)).thenReturn(1);
    boolean whether = versionServiceImpl.canAccess(resourceId, version);
    assertTrue(whether);
  }

  @Test
  @DisplayName("Method description: ...")
  public void testGenerateNewVersion() throws Exception {
    // TODO: Test goes here...
    /*
    try {
       Method method = VersionServiceImpl.getClass().getMethod("generateNewVersion", String.class);
       method.setAccessible(true);
       method.invoke(<Object>, <Parameters>);
    } catch(NoSuchMethodException e) {
    } catch(IllegalAccessException e) {
    } catch(InvocationTargetException e) {
    }
    */
  }
}
