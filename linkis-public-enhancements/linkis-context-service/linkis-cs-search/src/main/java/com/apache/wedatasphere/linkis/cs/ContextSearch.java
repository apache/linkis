/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.apache.wedatasphere.linkis.cs;

import com.apache.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.apache.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.apache.wedatasphere.linkis.cs.condition.Condition;
import com.apache.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.apache.wedatasphere.linkis.cs.exception.ContextSearchFailedException;

import java.util.List;
import java.util.Map;

public interface ContextSearch {

    List<ContextKeyValue> search(ContextCacheService contextCacheService, ContextID contextID, Map<Object, Object> conditionMap) throws ContextSearchFailedException;
    List<ContextKeyValue> search(ContextCacheService contextCacheService, ContextID contextID, Condition condition) throws ContextSearchFailedException;
}
