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
 
package org.apache.linkis.cs.contextcache.cache;

import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.contextcache.cache.csid.ContextIDValue;
import org.apache.linkis.cs.contextcache.metric.ContextCacheMetric;

import java.util.List;
import java.util.Map;

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
