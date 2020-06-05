package com.webank.wedatasphere.linkis.cs.contextcache.metric;

/**
 * @author peacewong
 * @date 2020/2/16 19:15
 */
public class DefaultContextCacheMetric implements ContextCacheMetric {

    private int usedCount;

    private int cachedCount;

    private long memory;


    @Override
    public int getUsedCount() {
        return this.usedCount;
    }

    @Override
    public void addCount() {
        this.usedCount++ ;
    }

    @Override
    public int getCachedCount() {
        return this.cachedCount;
    }

    @Override
    public void setCachedCount(int count) {
        if (count < 0) {
            count =0;
        }
        this.cachedCount = count;
    }

    @Override
    public long getCachedMemory() {
        return this.memory;
    }

    @Override
    public void setCachedMemory(long memory) {
        if (memory < 0){
            memory = 0;
        }
        this.memory = memory;
    }
}
