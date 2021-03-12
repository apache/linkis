package com.webank.wedatasphere.linkis.jobhistory.cache;

import com.webank.wedatasphere.linkis.jobhistory.cache.impl.UserTaskResultCache;

public interface QueryCacheManager {

    UserTaskResultCache getCache(String user, String engineType);

    void cleanAll();

    void refreshAll();
}
