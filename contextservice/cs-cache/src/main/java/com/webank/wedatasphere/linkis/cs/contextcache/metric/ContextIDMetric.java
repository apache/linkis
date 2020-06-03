package com.webank.wedatasphere.linkis.cs.contextcache.metric;

/**
 * @author peacewong
 * @date 2020/2/12 17:01
 */
public interface ContextIDMetric extends Metrtic{

    int getUsedCount();

    void addCount();

    long getMemory();

    void setMemory(long memory);

    long getCachedTime();

    long getLastAccessTime();

    void setLastAccessTime(long accessTime);
}
