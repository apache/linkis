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
