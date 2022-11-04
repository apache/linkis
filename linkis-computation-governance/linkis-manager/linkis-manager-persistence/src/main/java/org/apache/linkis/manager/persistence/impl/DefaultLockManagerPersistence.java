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

package org.apache.linkis.manager.persistence.impl;

import org.apache.linkis.manager.common.entity.persistence.PersistenceLock;
import org.apache.linkis.manager.dao.LockManagerMapper;
import org.apache.linkis.manager.persistence.LockManagerPersistence;
import org.apache.linkis.manager.util.PersistenceManagerConf;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLockManagerPersistence implements LockManagerPersistence {

  private static final Logger logger = LoggerFactory.getLogger(DefaultLockManagerPersistence.class);

  private LockManagerMapper lockManagerMapper;

  public LockManagerMapper getLockManagerMapper() {
    return lockManagerMapper;
  }

  public void setLockManagerMapper(LockManagerMapper lockManagerMapper) {
    this.lockManagerMapper = lockManagerMapper;
  }

  @Override
  public Boolean lock(PersistenceLock persistenceLock, Long timeOut) {
    try {
      return tryQueueLock(persistenceLock, timeOut);
    } catch (Exception e) {
      logger.error("Failed to get queue lock", e);
      if (persistenceLock.getId() > 0) {
        unlock(persistenceLock);
      }
    }
    return false;
  }

  private boolean tryQueueLock(PersistenceLock persistenceLock, Long timeOut) {
    long startTime = System.currentTimeMillis();
    if (StringUtils.isBlank(persistenceLock.getLockObject())) {
      return true;
    }
    persistenceLock.setTimeOut(timeOut);
    String syncLocker = persistenceLock.getLockObject().intern();
    synchronized (syncLocker) {
      // insert lock The order is determined by the id auto-incrementing number
      do {
        lockManagerMapper.lock(persistenceLock);
      } while (persistenceLock.getId() < 0);
    }
    boolean isLocked = isAcquireLock(persistenceLock);
    while (!isLocked && System.currentTimeMillis() - startTime < timeOut) {
      try {
        if (PersistenceManagerConf.Distributed_lock_request_sync_enabled) {
          synchronized (syncLocker) {
            syncLocker.wait(PersistenceManagerConf.Distributed_lock_request_interval);
            isLocked = isAcquireLock(persistenceLock);
            if (isLocked) {
              syncLocker.notifyAll();
            }
          }
        } else {
          Thread.sleep(PersistenceManagerConf.Distributed_lock_request_interval);
          isLocked = isAcquireLock(persistenceLock);
        }
      } catch (Exception e) {
        logger.info("lock waiting failed", e);
      }
    }
    if (!isLocked) {
      logger.error(
          "Failed to get lock by time out {} s", (System.currentTimeMillis() - startTime) / 1000);
      unlock(persistenceLock);
    }
    return isLocked;
  }

  private boolean isAcquireLock(PersistenceLock persistenceLock) {
    Integer minimumOrder =
        lockManagerMapper.getMinimumOrder(persistenceLock.getLockObject(), persistenceLock.getId());
    if (null == minimumOrder || minimumOrder >= persistenceLock.getId()) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void unlock(PersistenceLock persistenceLock) {
    if (persistenceLock.getId() > 0) {
      lockManagerMapper.unlock(persistenceLock.getId());
    } else {
      logger.error("Unlock{} id cannot be null", persistenceLock.getLockObject());
    }
  }

  @Override
  public List<PersistenceLock> getAll() {
    return lockManagerMapper.getAll();
  }

  @Override
  public List<PersistenceLock> getTimeOutLocks(Date endDate) {
    return lockManagerMapper.getTimeOutLocks(endDate);
  }
}
