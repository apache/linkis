package com.webank.wedatasphere.linkis.cs.execution;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.ContextCache;
import com.webank.wedatasphere.linkis.cs.execution.fetcher.ContextCacheFetcher;
import com.webank.wedatasphere.linkis.cs.execution.matcher.ContextSearchMatcher;
import com.webank.wedatasphere.linkis.cs.execution.ruler.ContextSearchRuler;
import com.webank.wedatasphere.linkis.cs.optimize.ConditionOptimizer;
import com.webank.wedatasphere.linkis.cs.optimize.OptimizedCondition;
import com.webank.wedatasphere.linkis.cs.optimize.cost.ConditionCostCalculator;
import com.webank.wedatasphere.linkis.cs.optimize.impl.CostBasedConditionOptimizer;

import java.util.List;

public abstract class AbstractConditionExecution implements ConditionExecution{

    protected ContextSearchMatcher contextSearchMatcher;
    protected ContextSearchRuler contextSearchRuler;
    protected ContextCacheFetcher contextCacheFetcher;
    protected ContextCacheService contextCacheService;
    protected Condition condition;
    protected ContextID contextID;

    public AbstractConditionExecution(Condition condition, ContextCacheService contextCacheService, ContextID contextID) {
        this.condition = condition;
        this.contextCacheService = contextCacheService;
        this.contextID = contextID;
    }

    @Override
    public List<ContextKeyValue> execute() {
        if(needOptimization()){
            OptimizedCondition optimizedCondition = getConditionOptimizer().optimize(condition);
        }
        ContextCacheFetcher fastFetcher = getFastFetcher();
        if(fastFetcher != null){
            return getContextSearchRuler().rule(fastFetcher.fetch(contextID));
        } else {
            return getContextCacheFetcher().fetch(contextID);
        }
    }

    abstract protected boolean needOptimization();

    abstract protected ContextCacheFetcher getFastFetcher();

    @Override
    public ContextSearchMatcher getContextSearchMatcher() {
        return this.contextSearchMatcher;
    }

    @Override
    public ContextSearchRuler getContextSearchRuler() {
        return this.contextSearchRuler;
    }

    @Override
    public ContextCacheFetcher getContextCacheFetcher() {
        return this.contextCacheFetcher;
    }

    public ContextCacheService getContextCacheService() {
        return contextCacheService;
    }

    public void setContextCacheService(ContextCacheService contextCacheService) {
        this.contextCacheService = contextCacheService;
    }

    public Condition getCondition() {
        return condition;
    }

    public ConditionOptimizer getConditionOptimizer() {
        return new CostBasedConditionOptimizer(getConditionCostCalculator());
    }

    public ConditionCostCalculator getConditionCostCalculator(){
        return new ConditionCostCalculator();
    }
}
