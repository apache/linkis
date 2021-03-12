package com.webank.wedatasphere.linkis.jobhistory.cache;

import com.webank.wedatasphere.linkis.governance.common.entity.task.RequestPersistTask;
import com.webank.wedatasphere.linkis.jobhistory.cache.domain.TaskResult;
import com.webank.wedatasphere.linkis.protocol.query.cache.RequestDeleteCache;
import com.webank.wedatasphere.linkis.protocol.query.cache.RequestReadCache;

public interface QueryCacheService {

    Boolean needCache(RequestPersistTask requestPersistTask);

    void writeCache(RequestPersistTask requestPersistTask);

    TaskResult readCache(RequestReadCache requestReadCache);

    void deleteCache(RequestDeleteCache requestDeleteCache);
}
