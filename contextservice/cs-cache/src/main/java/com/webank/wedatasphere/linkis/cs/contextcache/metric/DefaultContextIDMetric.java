package com.webank.wedatasphere.linkis.cs.contextcache.metric;

/**
 * @author peacewong
 * @date 2020/2/15 16:19
 */
public class DefaultContextIDMetric implements ContextIDMetric {

    private int usedCount;

    private long memory;

    private long cachedTime = System.currentTimeMillis();

    private long accessTime = System.currentTimeMillis();

    @Override
    public int getUsedCount() {
        return this.usedCount;
    }

    @Override
    public void addCount() {
        this.usedCount++;
    }

    @Override
    public long getMemory() {
        return this.memory;
    }

    @Override
    public void setMemory(long memory) {
        if (memory < 0) {
            memory = 0;
        }
        this.memory = memory;
    }

    @Override
    public long getCachedTime() {
        return this.cachedTime;
    }

    @Override
    public long getLastAccessTime() {
        return this.accessTime;
    }

    @Override
    public void setLastAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }
}
