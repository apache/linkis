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
 
package org.apache.linkis.jobhistory.cache.impl; /**
package org.apache.linkis.jobhistory.cache.impl;

import org.apache.linkis.governance.common.entity.task.RequestPersistTask;
import org.apache.linkis.jobhistory.cache.QueryCacheManager;
import org.apache.linkis.jobhistory.cache.QueryCacheService;
import org.apache.linkis.jobhistory.cache.domain.TaskResult;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.protocol.query.cache.RequestDeleteCache;
import org.apache.linkis.protocol.query.cache.RequestReadCache;
import org.apache.linkis.protocol.utils.TaskUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QueryCacheServiceImpl implements QueryCacheService {

    @Autowired
    QueryCacheManager queryCacheManager;

    public Boolean needCache(RequestPersistTask requestPersistTask) {
        Map<String, Object> runtimeMap = TaskUtils.getRuntimeMap(requestPersistTask.getParams());
        if (MapUtils.isEmpty(runtimeMap) || runtimeMap.get(TaskConstant.CACHE) == null) {
            return false;
        }
        return (Boolean) runtimeMap.get(TaskConstant.CACHE);
    }

    public void writeCache(RequestPersistTask requestPersistTask) {
        Map<String, Object> runtimeMap = TaskUtils.getRuntimeMap(requestPersistTask.getParams());
        Long cacheExpireAfter = ((Double) runtimeMap.getOrDefault(TaskConstant.CACHE_EXPIRE_AFTER, 300.0d)).longValue();
        TaskResult taskResult = new TaskResult(
                requestPersistTask.getExecutionCode(),
                requestPersistTask.getExecuteApplicationName(),
                requestPersistTask.getUmUser(),
                requestPersistTask.getResultLocation(),
                cacheExpireAfter
        );

        UserTaskResultCache userTaskResultCache = queryCacheManager.getCache(requestPersistTask.getUmUser(), requestPersistTask.getExecuteApplicationName());
        userTaskResultCache.put(taskResult);
    }

    public TaskResult readCache(RequestReadCache requestReadCache) {
        UserTaskResultCache userTaskResultCache = queryCacheManager.getCache(requestReadCache.getUser(), requestReadCache.getEngineType());
        return userTaskResultCache.get(requestReadCache.getExecutionCode(), requestReadCache.getReadCacheBefore());
    }

    public void deleteCache(RequestDeleteCache requestDeleteCache) {
        UserTaskResultCache userTaskResultCache = queryCacheManager.getCache(requestDeleteCache.getUser(), requestDeleteCache.getEngineType());
        userTaskResultCache.remove(requestDeleteCache.getExecutionCode());
    }

}

 */