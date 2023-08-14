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

package org.apache.linkis.manager.rm.service;

import org.apache.linkis.manager.am.util.LinkisUtils;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLock;
import org.apache.linkis.manager.persistence.LockManagerPersistence;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ResourceLockService {

  private static final Logger logger = LoggerFactory.getLogger(ResourceLockService.class);

  final String DEFAULT_LOCKED_BY = "RM";

  @Autowired LockManagerPersistence lockManagerPersistence;

  public boolean tryLock(PersistenceLock persistenceLock, long timeout) {
    if (StringUtils.isBlank(persistenceLock.getLockObject())) {
      return true;
    }
    boolean isLocked =
        LinkisUtils.tryCatch(
            () -> {
              if (timeout > 0) {
                return lockManagerPersistence.lock(persistenceLock, timeout);
              } else {
                return lockManagerPersistence.lock(persistenceLock, Long.MAX_VALUE);
              }
            },
            (Throwable t) -> {
              logger.error("failed to lock label [" + persistenceLock.getLockObject() + "]", t);
              return false;
            });
    if (isLocked) {
      logger.info("successfully locked label" + persistenceLock.getLockObject());
    }
    return isLocked;
  }

  public void unLock(PersistenceLock persistenceLock) {
    LinkisUtils.tryCatch(
        () -> {
          lockManagerPersistence.unlock(persistenceLock);
          logger.info("unlocked " + persistenceLock.getLockObject());
          return null;
        },
        (Throwable t) -> {
          logger.error("failed to unlock label [" + persistenceLock.getLockObject() + "]", t);
          return null;
        });
  }

  public void clearTimeoutLock(long timeout) {
    Date endDate = new Date(System.currentTimeMillis() - timeout);
    List<PersistenceLock> locks = lockManagerPersistence.getTimeOutLocks(endDate);
    if (locks == null) {
      return;
    }
    for (PersistenceLock lock : locks) {
      LinkisUtils.tryAndWarn(
          () -> {
            lockManagerPersistence.unlock(lock);
            logger.warn("timeout force unlock " + lock.getLockObject());
          },
          logger);
    }
  }
}
