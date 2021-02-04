/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
