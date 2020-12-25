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
import com.webank.wedatasphere.linkis.cs.condition.impl.NotCondition;
import com.webank.wedatasphere.linkis.cs.condition.impl.OrCondition;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.ContextCache;
import com.webank.wedatasphere.linkis.cs.execution.fetcher.IterateContextCacheFetcher;
import com.webank.wedatasphere.linkis.cs.execution.matcher.NotLogicContextSearchMatcher;
import com.webank.wedatasphere.linkis.cs.execution.matcher.OrLogicContextSearchMatcher;
import com.webank.wedatasphere.linkis.cs.execution.ruler.CommonListContextSearchRuler;

public class NotConditionExecution extends UnaryLogicConditionExecution {

    public NotConditionExecution(NotCondition condition, ContextCacheService contextCacheService, ContextID contextID) {
        super(condition, contextCacheService, contextID);
        this.contextSearchMatcher = new NotLogicContextSearchMatcher(condition);
        this.contextSearchRuler = new CommonListContextSearchRuler(contextSearchMatcher);
        this.contextCacheFetcher = new IterateContextCacheFetcher(contextCacheService, contextSearchRuler);
    }
}
