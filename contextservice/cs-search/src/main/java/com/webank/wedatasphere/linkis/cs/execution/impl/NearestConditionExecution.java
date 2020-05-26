package com.webank.wedatasphere.linkis.cs.execution.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.condition.impl.NearestCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.NotCondition;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.execution.fetcher.IterateContextCacheFetcher;
import com.webank.wedatasphere.linkis.cs.execution.matcher.NearestLogicContextSearchMatcher;
import com.webank.wedatasphere.linkis.cs.execution.matcher.NotLogicContextSearchMatcher;
import com.webank.wedatasphere.linkis.cs.execution.matcher.SkipContextSearchMather;
import com.webank.wedatasphere.linkis.cs.execution.ruler.CommonListContextSearchRuler;
import com.webank.wedatasphere.linkis.cs.execution.ruler.NearestContextSearchRuler;

public class NearestConditionExecution extends UnaryLogicConditionExecution {

    public NearestConditionExecution(NearestCondition condition, ContextCacheService contextCacheService, ContextID contextID) {
        super(condition, contextCacheService, contextID);
        this.contextSearchMatcher = new NearestLogicContextSearchMatcher(condition);
        this.contextSearchRuler = new NearestContextSearchRuler(contextSearchMatcher, contextCacheService, contextID, condition);
        this.contextCacheFetcher = new IterateContextCacheFetcher(contextCacheService, contextSearchRuler);
    }
}
