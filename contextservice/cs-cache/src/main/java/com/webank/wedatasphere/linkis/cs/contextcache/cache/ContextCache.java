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
package com.webank.wedatasphere.linkis.cs.contextcache.cache;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.csid.ContextIDValue;
import com.webank.wedatasphere.linkis.cs.contextcache.metric.ContextCacheMetric;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author peacewong
 * @date 2020/2/12 16:45
 */
public interface ContextCache {

    ContextIDValue getContextIDValue(ContextID contextID)  throws CSErrorException;

    void remove(ContextID contextID);

    void put(ContextIDValue contextIDValue) throws CSErrorException;

    Map<String, ContextIDValue> getAllPresent(List<ContextID> contextIDList);

    void refreshAll() throws CSErrorException;

    void putAll(List<ContextIDValue> contextIDValueList) throws CSErrorException;

    ContextIDValue loadContextIDValue(ContextID contextID);

    ContextCacheMetric getContextCacheMetric();
}
