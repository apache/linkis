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

import org.apache.linkis.bml.entity.ResourceTask;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskDaoTest extends BaseDaoTest {
  @Autowired TaskDao taskDao;

  void insertResourceTask() {
    ResourceTask resourceTask = new ResourceTask();
    resourceTask.setResourceId("123");
    resourceTask.setClientIp("192.168.142");
    resourceTask.setEndTime(new Date());
    resourceTask.setId(32);
    resourceTask.setStartTime(new Date());
    resourceTask.setErrMsg("testErr");
    resourceTask.setExtraParams("testpar");
    resourceTask.setInstance("testInst");
    resourceTask.setLastUpdateTime(new Date());
    resourceTask.setOperation("testOPer");
    resourceTask.setState("1");
    resourceTask.setSubmitUser("testSumUser");
    resourceTask.setSystem("testSym");
    resourceTask.setVersion("1.2");
    taskDao.insert(resourceTask);
  }

  @Test
  void testInsert() {
    ResourceTask resourceTask = new ResourceTask();
    resourceTask.setResourceId("123");
    resourceTask.setClientIp("192.168.142");
    resourceTask.setEndTime(new Date());
    resourceTask.setId(32);
    resourceTask.setStartTime(new Date());
    resourceTask.setErrMsg("testErr");
    resourceTask.setExtraParams("testpar");
    resourceTask.setInstance("testInst");
    resourceTask.setLastUpdateTime(new Date());
    resourceTask.setOperation("testOPer");
    resourceTask.setState("1");
    resourceTask.setSubmitUser("testSumUser");
    resourceTask.setSystem("testSym");
    resourceTask.setVersion("1.2");
    taskDao.insert(resourceTask);
  }

  @Test
  void testUpdateState() {
    insertResourceTask();
    taskDao.updateState(32, "1", new Date());
  }

  @Test
  void testUpdateState2Failed() {
    insertResourceTask();
    taskDao.updateState2Failed(32, "1", new Date(), "errMsg");
  }

  @Test
  void testGetNewestVersion() {
    insertResourceTask();
    taskDao.getNewestVersion("123");
  }
}
