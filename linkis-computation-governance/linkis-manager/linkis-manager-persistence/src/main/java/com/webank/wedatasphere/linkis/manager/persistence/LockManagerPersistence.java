package com.webank.wedatasphere.linkis.manager.persistence;

import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLock;

/**
 * @Author: chaogefeng
 * @Date: 2020/7/8
 */
public interface LockManagerPersistence {
    Boolean lock(PersistenceLock persistenceLock,Long timeOut);
    void unlock(PersistenceLock persistenceLock);
}
