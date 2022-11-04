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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LockManagerMapperTest extends BaseDaoTest {

  @Autowired LockManagerMapper lockManagerMapper;

  @Test
  void lock() {
    PersistenceLock persistenceLock = new PersistenceLock();
    persistenceLock.setTimeOut(1L);
    persistenceLock.setLockObject("testjson");
    persistenceLock.setCreator("hadoop");
    lockManagerMapper.lock(persistenceLock);
    List<PersistenceLock> list = lockManagerMapper.getLockersByLockObject("testjson");
    assertTrue(list.size() >= 1);
  }

  @Test
  void unlock() {
    lockManagerMapper.unlock(1);
  }

  @Test
  void getLockersByLockObject() {
    lock();
    List<PersistenceLock> list = lockManagerMapper.getLockersByLockObject("testjson");
    assertTrue(list.size() >= 1);
  }

  @Test
  void getAll() {
    lock();
    List<PersistenceLock> list = lockManagerMapper.getAll();
    assertTrue(list.size() >= 1);
  }
}
