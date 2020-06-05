package com.webank.wedatasphere.linkis.cs.execution.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextTypeCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextValueTypeCondition;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.execution.AbstractConditionExecution;
import com.webank.wedatasphere.linkis.cs.execution.fetcher.ContextCacheFetcher;
import com.webank.wedatasphere.linkis.cs.execution.fetcher.ContextTypeContextSearchFetcher;
import com.webank.wedatasphere.linkis.cs.execution.fetcher.IterateContextCacheFetcher;
import com.webank.wedatasphere.linkis.cs.execution.matcher.ContextTypeContextSearchMatcher;
import com.webank.wedatasphere.linkis.cs.execution.matcher.ContextValueTypeContextSearchMatcher;
import com.webank.wedatasphere.linkis.cs.execution.ruler.CommonListContextSearchRuler;

public class ContextValueTypeConditionExecution extends AbstractConditionExecution {

    public ContextValueTypeConditionExecution(ContextValueTypeCondition condition, ContextCacheService contextCacheService, ContextID contextID) {
        super(condition, contextCacheService, contextID);
        this.contextSearchMatcher = new ContextValueTypeContextSearchMatcher(condition);
        this.contextSearchRuler = new CommonListContextSearchRuler(contextSearchMatcher);
        this.contextCacheFetcher = new IterateContextCacheFetcher(contextCacheService, contextSearchRuler);
    }

    @Override
    protected boolean needOptimization() {
        return false;
    }

    @Override
    protected ContextCacheFetcher getFastFetcher() {
        return null;
    }
}
