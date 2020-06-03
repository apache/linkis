package com.webank.wedatasphere.linkis.cs.contextcache.metric;

/**
 * @author peacewong
 * @date 2020/2/14 15:17
 */
public interface ContextCacheMetric extends Metrtic {

    int getUsedCount();

    void addCount();


    int getCachedCount();

    void setCachedCount(int count);

    long getCachedMemory();

    void setCachedMemory(long memory);
}
