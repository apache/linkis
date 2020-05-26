package com.webank.wedatasphere.linkis.cs.execution.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.condition.UnaryLogicCondition;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.ContextCache;
import com.webank.wedatasphere.linkis.cs.execution.AbstractConditionExecution;
import com.webank.wedatasphere.linkis.cs.execution.fetcher.ContextCacheFetcher;

public abstract class UnaryLogicConditionExecution extends AbstractConditionExecution {

    public UnaryLogicConditionExecution(UnaryLogicCondition condition, ContextCacheService contextCacheService, ContextID contextID) {
        super(condition, contextCacheService, contextID);
    }

    @Override
    protected ContextCacheFetcher getFastFetcher() {
        return null;
    }

    @Override
    protected boolean needOptimization() {
        return true;
    }
}
