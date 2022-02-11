package org.apache.linkis.publicservice.common.lock.service.impl;

import org.apache.linkis.publicservice.common.lock.dao.CommonLockMapper;
import org.apache.linkis.publicservice.common.lock.entity.CommonLock;
import org.apache.linkis.publicservice.common.lock.service.CommonLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import java.util.List;

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
                Thread.sleep(1000);// TODO
                isLocked = tryLock(commonLock, timeOut);
            } catch (InterruptedException e) {
                logger.warn("lock waiting interrupted", e);
            }
        }
        return isLocked;
    }

    private boolean tryLock(CommonLock commonLock, Long timeOut) {
        try {
            commonLockMapper.lock(commonLock.getLockObject(), timeOut);
            return true;
        } catch (DataAccessException e) {
            logger.warn("Failed to obtain lock:" + commonLock.getLockObject());
            return false;
        }
    }

    @Override
    public void unlock(CommonLock commonLock) {
        commonLockMapper.unlock(commonLock.getLockObject());
    }

    @Override
    public List<CommonLock> getAll() {
        return commonLockMapper.getAll();
    }


}
