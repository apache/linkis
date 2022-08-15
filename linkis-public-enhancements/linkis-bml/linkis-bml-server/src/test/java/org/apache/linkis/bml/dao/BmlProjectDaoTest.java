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

import org.apache.linkis.bml.entity.BmlProject;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

class BmlProjectDaoTest extends BaseDaoTest {

  @Autowired BmlProjectDao bmlProjectDao;

  @Test
  void createNewProject() {
    BmlProject bmlProject = new BmlProject();
    bmlProject.setName("testName");
    bmlProject.setSystem("testSy");
    bmlProject.setSource("test");
    bmlProject.setDescription("descTest");
    bmlProject.setCreator("creCreatorUser");
    bmlProject.setEnabled(1);
    bmlProject.setCreateTime(new Date());
    bmlProject.setId(1);
    bmlProjectDao.createNewProject(bmlProject);
  }

  @Test
  void getBmlProject() {
    createNewProject();
    bmlProjectDao.getBmlProject("testName");
  }

  @Test
  void setProjectPriv() {
    List<String> usernamesList = new ArrayList<>();
    usernamesList.add("creCreatorUser");
    usernamesList.add("creCreatorUser1");
    int priv = 2;
    bmlProjectDao.setProjectPriv(1, usernamesList, priv, "creCreatorUser", new Date());
  }

  @Test
  void getPrivInProject() {
    setProjectPriv();
    createNewProject();
    bmlProjectDao.getPrivInProject("testName", "creCreatorUser");
  }

  @Test
  void addProjectResource() {
    bmlProjectDao.addProjectResource(1, "123");
  }

  @Test
  void getProjectNameByResourceId() {
    setProjectPriv();
    bmlProjectDao.getProjectNameByResourceId("123");
  }

  @Test
  void getProjectIdByName() {
    createNewProject();
    bmlProjectDao.getProjectIdByName("testName");
  }

  @Test
  void attachResourceAndProject() {
    createNewProject();
    bmlProjectDao.attachResourceAndProject(1, "123");
  }

  @Test
  void checkIfExists() {
    setProjectPriv();
    bmlProjectDao.checkIfExists(1, "123");
  }

  @Test
  void deleteAllPriv() {
    setProjectPriv();
    bmlProjectDao.deleteAllPriv(1);
  }
}
