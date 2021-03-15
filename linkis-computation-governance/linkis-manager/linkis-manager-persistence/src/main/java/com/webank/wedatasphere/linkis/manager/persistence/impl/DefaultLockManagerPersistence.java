/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.manager.persistence.impl;

import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLock;
import com.webank.wedatasphere.linkis.manager.dao.LockManagerMapper;
import com.webank.wedatasphere.linkis.manager.persistence.LockManagerPersistence;

public class DefaultLockManagerPersistence implements LockManagerPersistence {

    private LockManagerMapper lockManagerMapper;

    public LockManagerMapper getLockManagerMapper() {
        return lockManagerMapper;
    }

    public void setLockManagerMapper(LockManagerMapper lockManagerMapper) {
        this.lockManagerMapper = lockManagerMapper;
    }

    @Override
    public void lock(PersistenceLock persistenceLock, Long timeOut) {
        lockManagerMapper.lock(persistenceLock.getLockObject(),timeOut);
    }

    @Override
    public void unlock(PersistenceLock persistenceLock) {
        lockManagerMapper.unlock(persistenceLock.getLockObject());
    }


}
