package com.webank.wedatasphere.linkis.cs.execution.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.condition.BinaryLogicCondition;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.impl.ContextTypeCondition;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.ContextCache;
import com.webank.wedatasphere.linkis.cs.execution.AbstractConditionExecution;
import com.webank.wedatasphere.linkis.cs.execution.fetcher.ContextCacheFetcher;
import com.webank.wedatasphere.linkis.cs.execution.fetcher.ContextTypeContextSearchFetcher;

public abstract class BinaryLogicConditionExecution extends AbstractConditionExecution {

    ContextCacheFetcher fastFetcher;

    public BinaryLogicConditionExecution(BinaryLogicCondition condition, ContextCacheService contextCacheService, ContextID contextID) {
        super(condition, contextCacheService, contextID);
        ContextTypeCondition contextTypeCondition = findFastCondition(condition.getLeft(), condition);
        if(contextTypeCondition != null){
            fastFetcher = new ContextTypeContextSearchFetcher(contextCacheService, contextTypeCondition.getContextType());
        }
    }

    protected ContextTypeCondition findFastCondition(Condition condition, BinaryLogicCondition parent){
        if(condition instanceof BinaryLogicCondition){
            BinaryLogicCondition binaryLogicCondition = (BinaryLogicCondition) condition;
            return findFastCondition(binaryLogicCondition.getLeft(), binaryLogicCondition);
        } else if(condition instanceof ContextTypeCondition){
            parent.setLeft(null);
            return (ContextTypeCondition) condition;
        } else {
            return null;
        }
    }

    @Override
    protected ContextCacheFetcher getFastFetcher() {
        return fastFetcher;
    }

    @Override
    protected boolean needOptimization() {
        return true;
    }
}
