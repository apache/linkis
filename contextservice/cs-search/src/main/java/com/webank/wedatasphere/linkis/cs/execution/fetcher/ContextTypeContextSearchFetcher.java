package com.webank.wedatasphere.linkis.cs.execution.fetcher;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ContextTypeContextSearchFetcher extends AbstractContextCacheFetcher{

    private static final Logger logger = LoggerFactory.getLogger(ContextTypeContextSearchFetcher.class);

    ContextType contextType;

    public ContextTypeContextSearchFetcher(ContextCacheService contextCacheService, ContextType contextType) {
        super(contextCacheService);
        this.contextType = contextType;
    }

    private ContextTypeContextSearchFetcher(ContextCacheService contextCacheService) {
        super(contextCacheService);
    }

    @Override
    public List<ContextKeyValue> fetch(ContextID contextID) {
        return contextCacheService.getAllByType(contextID, contextType);
    }
}
