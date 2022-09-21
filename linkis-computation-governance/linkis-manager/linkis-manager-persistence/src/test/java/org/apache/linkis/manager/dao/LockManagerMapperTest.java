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

import org.apache.linkis.manager.common.entity.persistence.PersistenceLock;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LockManagerMapperTest extends BaseDaoTest {

  @Autowired LockManagerMapper lockManagerMapper;

  @BeforeAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void beforeAll() throws Exception {
    Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
  }

  @AfterAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void afterAll() throws Exception {}

  private String jsonObj = "testJsonObj";

  @Test
  public void testLock() {
    lockManagerMapper.lock(jsonObj, 5L);
    List<PersistenceLock> locks = lockManagerMapper.getLockersByLockObject(jsonObj);
    assertEquals(locks.size(), 1);
  }

  @Test
  public void testUnlock() {
    lockManagerMapper.lock(jsonObj, 5L);
    List<PersistenceLock> locks = lockManagerMapper.getLockersByLockObject(jsonObj);
    assertEquals(locks.size(), 1);
    lockManagerMapper.unlock(locks.get(0).getId());
    List<PersistenceLock> locks1 = lockManagerMapper.getLockersByLockObject(jsonObj);
    assertEquals(locks1.size(), 0);
  }

  @Test
  public void testGetAll() {
    lockManagerMapper.lock(jsonObj, 5L);
    lockManagerMapper.lock("testJsonObj1111", 6L);
    List<PersistenceLock> locks = lockManagerMapper.getAll();
    assertEquals(2, locks.size());
  }
}
