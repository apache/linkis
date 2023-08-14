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

import org.apache.linkis.bml.entity.BmlProject;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * When using the h2 library for testing,some methods that do not support function(on conflict) may
 * report errors, and the pg physical library will not guarantee an error pg使用h2库测试时不支持函数（on
 * conflict）部分方法会报错，pg实体库不会报错
 */
class BmlProjectDaoTest extends BaseDaoTest {

  private static final Logger logger = LoggerFactory.getLogger(BmlProjectDaoTest.class);

  @Autowired BmlProjectDao bmlProjectDao;

  void insertNewProject() {
    BmlProject bmlProject = new BmlProject();
    bmlProject.setName("testName");
    bmlProject.setSystem("testSy");
    bmlProject.setSource("test");
    bmlProject.setDescription("descTest");
    bmlProject.setCreator("creCreatorUser");
    bmlProject.setEnabled(1);
    bmlProject.setCreateTime(new Date());
    bmlProjectDao.createNewProject(bmlProject);
  }

  @Test
  void testCreateNewProject() {
    BmlProject bmlProject = new BmlProject();
    bmlProject.setName("testName");
    bmlProject.setSystem("testSy");
    bmlProject.setSource("test");
    bmlProject.setDescription("descTest");
    bmlProject.setCreator("creCreatorUser");
    bmlProject.setEnabled(1);
    bmlProject.setCreateTime(new Date());
    bmlProjectDao.createNewProject(bmlProject);
    BmlProject bmlProjects = bmlProjectDao.getBmlProject("testName");
    assertTrue(bmlProjects != null);
  }

  @Test
  void testGetBmlProject() {
    insertNewProject();
    bmlProjectDao.getBmlProject("testName");
  }

  @Test
  void testSetProjectPriv() {
    List<String> usernamesList = new ArrayList<>();
    usernamesList.add("creCreatorUser");
    usernamesList.add("creCreatorUser1");
    int priv = 2;
    bmlProjectDao.setProjectPriv(1, usernamesList, priv, "creCreatorUser", new Date());
  }

  @Test
  void testGetPrivInProject() {
    Integer privInt = bmlProjectDao.getPrivInProject("testName", "creCreatorUser");
    logger.info("privInt:" + privInt);
    assertTrue(privInt == 2);
  }

  @Test
  void testAddProjectResource() {
    bmlProjectDao.addProjectResource(1, "123");
  }

  @Test
  void testGetProjectNameByResourceId() {
    String projectName = bmlProjectDao.getProjectNameByResourceId("123");
    logger.info("projectName:" + projectName);
    assertTrue(projectName.equals("testName"));
  }

  @Test
  void testGetProjectIdByName() {
    insertNewProject();
    bmlProjectDao.getProjectIdByName("testName");
    Integer i = bmlProjectDao.getProjectIdByName("testName");
    assertTrue(i != null);
  }

  @Test
  void testAttachResourceAndProject() {
    insertNewProject();
    bmlProjectDao.attachResourceAndProject(1, "123");
  }

  @Test
  void testCheckIfExists() {
    insertNewProject();
    Integer i = bmlProjectDao.checkIfExists(1, "123");
    assertTrue(i != null);
  }

  @Test
  void testDeleteAllPriv() {
    insertNewProject();
    bmlProjectDao.deleteAllPriv(1);
  }
}
