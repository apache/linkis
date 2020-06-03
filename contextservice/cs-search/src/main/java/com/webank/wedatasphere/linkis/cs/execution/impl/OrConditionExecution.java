package com.webank.wedatasphere.linkis.cs.execution.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.condition.impl.AndCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.OrCondition;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.ContextCache;
import com.webank.wedatasphere.linkis.cs.execution.fetcher.IterateContextCacheFetcher;
import com.webank.wedatasphere.linkis.cs.execution.matcher.AndLogicContextSearchMatcher;
import com.webank.wedatasphere.linkis.cs.execution.matcher.OrLogicContextSearchMatcher;
import com.webank.wedatasphere.linkis.cs.execution.ruler.CommonListContextSearchRuler;

public class OrConditionExecution extends BinaryLogicConditionExecution {

    public OrConditionExecution(OrCondition condition, ContextCacheService contextCacheService, ContextID contextID) {
        super(condition, contextCacheService, contextID);
        this.contextSearchMatcher = new OrLogicContextSearchMatcher(condition);
        this.contextSearchRuler = new CommonListContextSearchRuler(contextSearchMatcher);
        this.contextCacheFetcher = new IterateContextCacheFetcher(contextCacheService, contextSearchRuler);
    }
}
