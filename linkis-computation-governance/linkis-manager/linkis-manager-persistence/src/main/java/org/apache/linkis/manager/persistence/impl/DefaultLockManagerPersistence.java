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
 
package org.apache.linkis.manager.persistence.impl;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLock;
import org.apache.linkis.manager.dao.LockManagerMapper;
import org.apache.linkis.manager.persistence.LockManagerPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import java.util.List;

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
        long startTime = System.currentTimeMillis();
        Boolean isLocked = tryLock(persistenceLock, timeOut);
        while (!isLocked && System.currentTimeMillis() - startTime < timeOut){
            try {
                Thread.sleep(1000);// TODO
                isLocked = tryLock(persistenceLock, timeOut);
            } catch (InterruptedException e) {
                logger.warn("lock waiting interrupted", e);
            }
        }
        return isLocked;
    }

    private boolean tryLock(PersistenceLock persistenceLock, Long timeOut){
        try {
            lockManagerMapper.lock(persistenceLock.getLockObject(),timeOut);
            return true;
        } catch (DataAccessException e){
          logger.warn("Failed to obtain lock:" + persistenceLock.getLockObject());
          return false;
        }
    }

    @Override
    public void unlock(PersistenceLock persistenceLock) {
        lockManagerMapper.unlock(persistenceLock.getLockObject());
    }

    @Override
    public List<PersistenceLock> getAll() {
        return lockManagerMapper.getAll();
    }


}
