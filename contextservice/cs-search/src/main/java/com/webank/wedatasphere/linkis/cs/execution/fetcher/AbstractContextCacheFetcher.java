package com.webank.wedatasphere.linkis.cs.execution.fetcher;

import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.ContextCache;

public abstract class AbstractContextCacheFetcher implements ContextCacheFetcher{

    ContextCacheService contextCacheService;

    public AbstractContextCacheFetcher(ContextCacheService contextCacheService) {
        this.contextCacheService = contextCacheService;
    }
}
