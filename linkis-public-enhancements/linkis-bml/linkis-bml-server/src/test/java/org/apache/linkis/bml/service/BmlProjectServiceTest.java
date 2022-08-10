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

import org.apache.linkis.bml.dao.BmlProjectDao;
import org.apache.linkis.bml.entity.BmlProject;
import org.apache.linkis.bml.service.impl.BmlProjectServiceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** BmlProjectServiceImpl Tester */
@ExtendWith(MockitoExtension.class)
public class BmlProjectServiceTest {

    @InjectMocks private BmlProjectServiceImpl bmlProjectService;

    @Mock private BmlProjectDao bmlProjectDao;

    @BeforeEach
    @DisplayName("Each unit test method is executed once before execution")
    public void before() throws Exception {}

    @AfterEach
    @DisplayName("Each unit test method is executed once before execution")
    public void after() throws Exception {}

    public BmlProject addBmlProject() {
        BmlProject bmlProject = new BmlProject();
        bmlProject.setName("testName");
        bmlProject.setSystem("testSy");
        bmlProject.setSource("test");
        bmlProject.setDescription("descTest");
        bmlProject.setCreator("creCreatorUser");
        bmlProject.setEnabled(1);
        bmlProject.setCreateTime(new Date());
        bmlProject.setId(1);
        return bmlProject;
    }

    @Test
    @DisplayName("Method description: ...")
    public void testCreateBmlProject() throws Exception {
        // TODO: Test goes here...

        String projectName = "testName1";
        String creator = "creCreatorUser1";
        List<String> editUsers = new ArrayList<>();
        List<String> accessUsers = new ArrayList<>();
        BmlProject bmlProject = addBmlProject();
        Mockito.when(bmlProjectDao.getBmlProject(projectName))
                .thenReturn(null)
                .thenReturn(bmlProject);
        BmlProject bmlProject1 = new BmlProject();
        bmlProject1.setName(projectName);
        bmlProject1.setSystem("testSy");
        bmlProject1.setSource("test");
        bmlProject1.setDescription("descTest");
        bmlProject1.setCreator(creator);
        bmlProject1.setEnabled(1);
        bmlProject1.setCreateTime(new Date());
        int i = bmlProjectService.createBmlProject(projectName, creator, editUsers, accessUsers);
        assertTrue(i > 0);
    }

    @Test
    @DisplayName("Method description: ...")
    public void testCheckEditPriv() throws Exception {
        // TODO: Test goes here...
        String projectName = "testProjectName";
        String username = "testUsername";
        Mockito.when(bmlProjectDao.getPrivInProject(projectName, username)).thenReturn(6);
        boolean fa = bmlProjectService.checkEditPriv(projectName, username);
        assertTrue(fa == false);
    }

    @Test
    @DisplayName("Method description: ...")
    public void testCheckAccessPriv() throws Exception {
        // TODO: Test goes here...
        String projectName = "testProjectName";
        String username = "testUsername";
        Mockito.when(bmlProjectDao.getPrivInProject(projectName, username)).thenReturn(6);
        boolean fa = bmlProjectService.checkAccessPriv(projectName, username);
        assertTrue(fa == false);
    }

    @Test
    @DisplayName("Method description: ...")
    public void testSetProjectEditPriv() throws Exception {
        // TODO: Test goes here...
        String projectName = "testName";
        List<String> editUsers = new ArrayList<>();
        BmlProject bmlProject = addBmlProject();
        Mockito.when(bmlProjectDao.getBmlProject(projectName)).thenReturn(bmlProject);
        bmlProjectService.setProjectEditPriv(projectName, editUsers);
    }

    @Test
    @DisplayName("Method description: ...")
    public void testAddProjectEditPriv() throws Exception {
        // TODO: Test goes here...
    }

    @Test
    @DisplayName("Method description: ...")
    public void testDeleteProjectEditPriv() throws Exception {
        // TODO: Test goes here...
    }

    @Test
    @DisplayName("Method description: ...")
    public void testSetProjectAccessPriv() throws Exception {
        // TODO: Test goes here...
        String projectName = "testName";
        List<String> editUsers = new ArrayList<>();
        BmlProject bmlProject = addBmlProject();
        Mockito.when(bmlProjectDao.getBmlProject(projectName)).thenReturn(bmlProject);
        bmlProjectService.setProjectAccessPriv(projectName, editUsers);
    }

    @Test
    @DisplayName("Method description: ...")
    public void testAddProjectAccessPriv() throws Exception {
        // TODO: Test goes here...
    }

    @Test
    @DisplayName("Method description: ...")
    public void testDeleteProjectAccessPriv() throws Exception {
        // TODO: Test goes here...
    }

    @Test
    @DisplayName("Method description: ...")
    public void testGetProjectNameByResourceId() throws Exception {
        // TODO: Test goes here...
        String resourceId = "123";
        Mockito.when(bmlProjectDao.getProjectNameByResourceId(resourceId)).thenReturn("testName");
        String projectName = bmlProjectService.getProjectNameByResourceId(resourceId);
        assertNotNull(projectName);
    }

    @Test
    @DisplayName("Method description: ...")
    public void testAddProjectResource() throws Exception {
        // TODO: Test goes here...
        String resourceId = "123";
        String projectName = "testName";
        BmlProject bmlProject = addBmlProject();
        Mockito.when(bmlProjectDao.getBmlProject(projectName)).thenReturn(bmlProject);
        bmlProjectService.addProjectResource(resourceId, projectName);
    }

    @Test
    @DisplayName("Method description: ...")
    public void testAttach() throws Exception {
        // TODO: Test goes here...
        String projectName = "testName";
        String resourceId = "123";
        Mockito.when(bmlProjectDao.getProjectIdByName(projectName)).thenReturn(1);
        Mockito.when(bmlProjectDao.checkIfExists(1, resourceId)).thenReturn(0);
        bmlProjectService.attach(projectName, resourceId);
    }

    @Test
    @DisplayName("Method description: ...")
    public void testUpdateProjectUsers() throws Exception {
        // TODO: Test goes here...
        String username = "testUsername";
        String projectName = "testName";
        List<String> editUsers = new ArrayList<>();
        editUsers.add("test1");
        List<String> accessUsers = new ArrayList<>();
        accessUsers.add("test2");
        Mockito.when(bmlProjectDao.getProjectIdByName(projectName)).thenReturn(1);
        bmlProjectService.updateProjectUsers(username, projectName, editUsers, accessUsers);
    }
}
