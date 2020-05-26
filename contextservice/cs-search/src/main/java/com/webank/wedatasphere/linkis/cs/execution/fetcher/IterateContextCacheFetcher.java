package com.webank.wedatasphere.linkis.cs.execution.fetcher;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.ContextCache;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.csid.ContextIDValue;
import com.webank.wedatasphere.linkis.cs.execution.ruler.ContextSearchRuler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class IterateContextCacheFetcher extends AbstractContextCacheFetcher{

    private static final Logger logger = LoggerFactory.getLogger(IterateContextCacheFetcher.class);

    ContextSearchRuler contextSearchRuler;

    public IterateContextCacheFetcher(ContextCacheService contextCacheService, ContextSearchRuler contextSearchRuler) {
        super(contextCacheService);
        this.contextSearchRuler = contextSearchRuler;
    }

    private IterateContextCacheFetcher(ContextCacheService contextCacheService) {
        super(contextCacheService);
    }

    @Override
    public List<ContextKeyValue> fetch(ContextID contextID) {
        return contextSearchRuler.rule(contextCacheService.getAll(contextID));
    }


}
