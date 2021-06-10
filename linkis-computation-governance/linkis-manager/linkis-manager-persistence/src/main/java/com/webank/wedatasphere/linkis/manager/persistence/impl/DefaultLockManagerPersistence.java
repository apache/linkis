package com.webank.wedatasphere.linkis.manager.persistence.impl;

import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLock;
import com.webank.wedatasphere.linkis.manager.dao.LockManagerMapper;
import com.webank.wedatasphere.linkis.manager.persistence.LockManagerPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;


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


}
