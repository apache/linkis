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

package org.apache.linkis.publicservice.common.lock.dao;

import org.apache.linkis.publicservice.common.lock.entity.CommonLock;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CommonLockMapperTest extends BaseDaoTest {

  @Autowired private CommonLockMapper commonLockMapper;

  @Test
  @DisplayName("getAll")
  public void getAllTest() {
    List<CommonLock> locks = commonLockMapper.getAll();
    Assertions.assertTrue(locks.size() == 1);
  }

  public Boolean reentrantLock(CommonLock commonLock) {
    CommonLock oldLock =
        commonLockMapper.getLockByLocker(commonLock.getLockObject(), commonLock.getLocker());
    if (oldLock != null) {
      return true;
    }

    try {
      commonLockMapper.lock(commonLock, -1L);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  @Test
  @DisplayName("reentrantLockTest")
  public void reentrantLockTest() {
    String lockObject = "hadoop-warehouse4";
    CommonLock commonLock = new CommonLock();
    commonLock.setLockObject(lockObject);
    commonLock.setLocker("test");
    Boolean lock = reentrantLock(commonLock);
    Assertions.assertTrue(lock);
    lock = reentrantLock(commonLock);
    Assertions.assertTrue(lock);
    commonLock.setLocker("test1");
    lock = reentrantLock(commonLock);
    Assertions.assertFalse(lock);
  }

  @Test
  @DisplayName("unlockTest")
  public void unlockTest() {
    String lockObject = "hadoop-warehouse";
    CommonLock commonLock = new CommonLock();
    commonLock.setLockObject(lockObject);
    commonLock.setLocker("test");
    commonLockMapper.unlock(commonLock);

    List<CommonLock> locks = commonLockMapper.getAll();
    Assertions.assertTrue(locks.size() == 0);
  }

  @Test
  @DisplayName("lockTest")
  public void lockTest() {
    String lockObject = "hadoop-warehouse2";
    Long timeOut = 10000L;
    CommonLock commonLock = new CommonLock();
    commonLock.setLockObject(lockObject);

    Assertions.assertThrows(
        RuntimeException.class, () -> commonLockMapper.lock(commonLock, timeOut));

    commonLock.setLocker("test");
    commonLockMapper.lock(commonLock, timeOut);
    List<CommonLock> locks = commonLockMapper.getAll();
    Assertions.assertTrue(locks.size() == 2);
  }
}
