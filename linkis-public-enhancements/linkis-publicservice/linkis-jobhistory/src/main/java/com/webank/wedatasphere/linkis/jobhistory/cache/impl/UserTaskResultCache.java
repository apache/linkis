package com.webank.wedatasphere.linkis.jobhistory.cache.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.webank.wedatasphere.linkis.jobhistory.cache.domain.TaskResult;
import com.webank.wedatasphere.linkis.jobhistory.cache.utils.MD5Util;
import com.webank.wedatasphere.linkis.jobhistory.util.QueryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UserTaskResultCache {

    private static Logger logger = LoggerFactory.getLogger(UserTaskResultCache.class);
    Cache<String, TaskResult> resultCache;
    Long lastCleaned;

    public UserTaskResultCache() {
        resultCache = CacheBuilder.newBuilder()
                .expireAfterWrite((Long) QueryConfig.CACHE_MAX_EXPIRE_HOUR().getValue(), TimeUnit.DAYS)
                .maximumSize((Long) QueryConfig.CACHE_MAX_SIZE().getValue())
                .build();
        lastCleaned = System.currentTimeMillis();
    }

    public void refresh() {
        resultCache.invalidateAll();
        logger.info("Cache refreshed.");
    }

    public void clean() {
        resultCache.cleanUp();
        logger.info("Cache cleaned up.");
        for (Map.Entry<String, TaskResult> taskResultEntry : resultCache.asMap().entrySet()) {
            TaskResult taskResult = taskResultEntry.getValue();
            if (taskResult != null && expired(taskResult)) {
                resultCache.invalidate(taskResultEntry.getKey());
            }
        }
        logger.info("Finished checking expired cache records.");
        lastCleaned = System.currentTimeMillis();
    }

    public void put(TaskResult taskResult) {
        String md5 = getMD5(taskResult.getExecutionCode());
        TaskResult existingCacheOjb = resultCache.getIfPresent(md5);
        if (existingCacheOjb != null && taskResult.getExpireAt() < existingCacheOjb.getExpireAt()) {
            taskResult.setExpireAt(existingCacheOjb.getExpireAt());
        }
        resultCache.put(md5, taskResult);
    }

    public TaskResult get(String executionCode, Long readCacheBefore) {
        String md5 = getMD5(executionCode);
        TaskResult taskResult = resultCache.getIfPresent(md5);
        if (taskResult == null) {
            return null;
        }
        if (expired(taskResult)) {
            resultCache.invalidate(md5);
            return null;
        }
        if (taskResult.getCreatedAt() < System.currentTimeMillis() - readCacheBefore * 1000) {
            return null;
        }
        return taskResult;
    }

    public void remove(String executionCode) {
        String md5 = getMD5(executionCode);
        resultCache.invalidate(md5);
    }


    private boolean expired(TaskResult taskResult) {
        return taskResult.getExpireAt() <= System.currentTimeMillis();
    }

    private String getMD5(String executionCode) {
        return MD5Util.getMD5(executionCode, true, 32);
    }

    public Long getLastCleaned() {
        return lastCleaned;
    }
}
