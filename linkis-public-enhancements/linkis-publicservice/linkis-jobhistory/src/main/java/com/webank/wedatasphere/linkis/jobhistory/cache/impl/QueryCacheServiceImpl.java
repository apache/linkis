package com.webank.wedatasphere.linkis.jobhistory.cache.impl; /**
package com.webank.wedatasphere.linkis.jobhistory.cache.impl;

import com.webank.wedatasphere.linkis.governance.common.entity.task.RequestPersistTask;
import com.webank.wedatasphere.linkis.jobhistory.cache.QueryCacheManager;
import com.webank.wedatasphere.linkis.jobhistory.cache.QueryCacheService;
import com.webank.wedatasphere.linkis.jobhistory.cache.domain.TaskResult;
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant;
import com.webank.wedatasphere.linkis.protocol.query.cache.RequestDeleteCache;
import com.webank.wedatasphere.linkis.protocol.query.cache.RequestReadCache;
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils;
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