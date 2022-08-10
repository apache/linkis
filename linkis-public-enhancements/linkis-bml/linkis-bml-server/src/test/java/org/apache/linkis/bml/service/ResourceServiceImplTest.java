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

import org.apache.linkis.bml.dao.ResourceDao;
import org.apache.linkis.bml.dao.VersionDao;
import org.apache.linkis.bml.entity.Resource;
import org.apache.linkis.bml.entity.ResourceVersion;
import org.apache.linkis.bml.service.impl.ResourceServiceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** ResourceServiceImpl Tester */
@ExtendWith(MockitoExtension.class)
public class ResourceServiceImplTest {

    @InjectMocks private ResourceServiceImpl resourceServiceImpl;

    @Mock private ResourceDao resourceDao;

    @Mock private VersionDao versionDao;

    @BeforeEach
    @DisplayName("Each unit test method is executed once before execution")
    public void before() throws Exception {}

    @AfterEach
    @DisplayName("Each unit test method is executed once before execution")
    public void after() throws Exception {}

    Resource addResource() {
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
        resource.setEnableFlag(true);
        return resource;
    }

    @Test
    public void testGetResources() throws Exception {
        // TODO: Test goes here...
        Map paramMap = new HashMap();
        paramMap.put("resource_id", "1");
        paramMap.put("owner", "owner");
        paramMap.put("sys", "sys");
        List<Resource> list = new ArrayList<>();
        list.add(addResource());
        Mockito.when(resourceDao.getResources(paramMap)).thenReturn(list);
        List<Resource> resourceList = resourceServiceImpl.getResources(paramMap);
        assertTrue(resourceList.size() > 0);
    }

    @Test
    public void testDeleteResource() throws Exception {
        // TODO: Test goes here...
        String resourceId = "123";
        resourceServiceImpl.deleteResource(resourceId);
    }

    @Test
    public void testBatchDeleteResources() throws Exception {
        // TODO: Test goes here...
        List<String> list = new ArrayList<>();
        list.add("123");
        list.add("234");
        resourceServiceImpl.batchDeleteResources(list);
    }

    @Test
    public void testUpload() throws Exception {
        // TODO: Test goes here...
        /*List< MultipartFile > files =new ArrayList<>();
        String user="testuser";
        Map<String, Object> properties =new HashMap<>();
        properties.put("resourceId","123");
        ResourceHelper resourceHelper = ResourceHelperFactory.getResourceHelper();
        Mockito.when(ResourceHelperFactory.getResourceHelper()).thenReturn(resourceHelper);
        Mockito.when(resourceHelper.generatePath(user,"123",properties)).thenReturn("testpath");*/

    }

    @Test
    public void testCheckResourceId() throws Exception {
        // TODO: Test goes here...
        String resourceId = "123";
        Mockito.when(resourceDao.checkExists(resourceId)).thenReturn(1);
        boolean boo = resourceServiceImpl.checkResourceId(resourceId);
        assertTrue(boo == true);
    }

    @Test
    public void testGetResource() throws Exception {
        // TODO: Test goes here...
        String resourceId = "123";
        Resource resource = addResource();
        Mockito.when(resourceDao.getResource(resourceId)).thenReturn(resource);
        Resource rs = resourceServiceImpl.getResource(resourceId);
        assertTrue(rs != null);
    }

    @Test
    public void testCheckAuthority() throws Exception {
        // TODO: Test goes here...
        String resourceId = "123";
        String user = "testuser";
        Mockito.when(resourceDao.getUserByResourceId(resourceId)).thenReturn("testuser");
        boolean boo = resourceServiceImpl.checkAuthority(user, resourceId);
        assertTrue(boo == true);
    }

    @Test
    public void testCheckExpire() throws Exception {
        // TODO: Test goes here...
        String resourceId = "123";
        String version = "321";
        Resource resource = addResource();
        ResourceVersion resourceVersion = new ResourceVersion();
        resourceVersion.setEnableFlag(true);
        Mockito.when(resourceDao.getResource(resourceId)).thenReturn(resource);
        Mockito.when(versionDao.getResourceVersion(resourceId, version))
                .thenReturn(resourceVersion);
        boolean boo = resourceServiceImpl.checkExpire(resourceId, version);
        assertTrue(boo == true);
    }

    @Test
    public void testCleanExpiredResources() throws Exception {
        // TODO: Test goes here...
    }

    @Test
    public void testChangeOwnerByResourceId() throws Exception {
        // TODO: Test goes here...
        String resourceId = "123";
        String oldOwner = "oldOwnertest";
        String newOwner = "newOwnertest";
        resourceServiceImpl.changeOwnerByResourceId(resourceId, oldOwner, newOwner);
    }

    @Test
    public void testCopyResourceToOtherUser() throws Exception {
        // TODO: Test goes here...
        String resourceId = "123";
        String otherUser = "otherUser";
        resourceServiceImpl.copyResourceToOtherUser(resourceId, otherUser);
    }

}
