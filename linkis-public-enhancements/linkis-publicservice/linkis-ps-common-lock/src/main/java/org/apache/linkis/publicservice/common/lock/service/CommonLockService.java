package org.apache.linkis.publicservice.common.lock.service;

import org.apache.linkis.publicservice.common.lock.entity.CommonLock;

import java.util.List;

public interface CommonLockService {
    Boolean lock(CommonLock commonLock, Long timeOut);

    void unlock(CommonLock commonLock);

    List<CommonLock> getAll();
}
