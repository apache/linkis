/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.cs.execution;

import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.condition.Condition;
import org.apache.linkis.cs.contextcache.ContextCacheService;
import org.apache.linkis.cs.execution.fetcher.ContextCacheFetcher;
import org.apache.linkis.cs.execution.matcher.ContextSearchMatcher;
import org.apache.linkis.cs.execution.ruler.ContextSearchRuler;
import org.apache.linkis.cs.optimize.ConditionOptimizer;
import org.apache.linkis.cs.optimize.OptimizedCondition;
import org.apache.linkis.cs.optimize.cost.ConditionCostCalculator;
import org.apache.linkis.cs.optimize.impl.CostBasedConditionOptimizer;

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
