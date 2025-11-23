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

package org.apache.linkis.publicservice.common.lock.service.impl;

import org.apache.linkis.publicservice.common.lock.dao.CommonLockMapper;
import org.apache.linkis.publicservice.common.lock.entity.CommonLock;
import org.apache.linkis.publicservice.common.lock.service.CommonLockService;

import org.springframework.dao.DataAccessException;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCommonLockService implements CommonLockService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultCommonLockService.class);

  private CommonLockMapper commonLockMapper;

  public CommonLockMapper getLockManagerMapper() {
    return commonLockMapper;
  }

  public void setLockManagerMapper(CommonLockMapper commonLockMapper) {
    this.commonLockMapper = commonLockMapper;
  }

  @Override
  public Boolean lock(CommonLock commonLock, Long timeOut) {
    long startTime = System.currentTimeMillis();
    Boolean isLocked = tryLock(commonLock, timeOut);
    while (!isLocked && System.currentTimeMillis() - startTime < timeOut) {
      try {
        Thread.sleep(1000); // TODO
        isLocked = tryLock(commonLock, timeOut);
      } catch (InterruptedException e) {
        logger.warn("lock waiting interrupted", e);
      }
    }
    return isLocked;
  }

  @Override
  public Boolean reentrantLock(CommonLock commonLock, Long timeOut) {
    CommonLock oldLock =
        commonLockMapper.getLockByLocker(commonLock.getLockObject(), commonLock.getLocker());
    if (oldLock != null) {
      return true;
    }
    long startTime = System.currentTimeMillis();
    Boolean isLocked = tryLock(commonLock, timeOut);
    while (!isLocked && System.currentTimeMillis() - startTime < timeOut) {
      try {
        Thread.sleep(1000);
        isLocked = tryLock(commonLock, timeOut);
      } catch (InterruptedException e) {
        logger.warn("lock waiting interrupted", e);
      }
    }
    return isLocked;
  }

  private boolean tryLock(CommonLock commonLock, Long timeOut) {
    try {
      commonLockMapper.lock(commonLock, timeOut);
      return true;
    } catch (DataAccessException e) {
      logger.warn("Failed to obtain lock:" + commonLock.getLockObject());
      return false;
    }
  }

  @Override
  public void unlock(CommonLock commonLock) {
    commonLockMapper.unlock(commonLock);
  }

  @Override
  public List<CommonLock> getAll() {
    return commonLockMapper.getAll();
  }
}
