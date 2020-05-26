package com.webank.wedatasphere.linkis.cs.execution;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.ContextCache;
import com.webank.wedatasphere.linkis.cs.execution.fetcher.ContextCacheFetcher;
import com.webank.wedatasphere.linkis.cs.execution.matcher.ContextSearchMatcher;
import com.webank.wedatasphere.linkis.cs.execution.ruler.ContextSearchRuler;

import java.util.List;

public interface ConditionExecution {

    ContextSearchMatcher getContextSearchMatcher();
    ContextSearchRuler getContextSearchRuler();
    ContextCacheFetcher getContextCacheFetcher();
    List<ContextKeyValue> execute();
    void setContextCacheService(ContextCacheService contextCacheService);
    ContextCacheService getContextCacheService();
    Condition getCondition();

}
