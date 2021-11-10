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
 
package org.apache.linkis.metadatamanager.common.cache;

import com.google.common.cache.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ConnCacheManager implements CacheManager {
    private ConcurrentHashMap<String, Cache> cacheStore = new ConcurrentHashMap<>();
    private ConnCacheManager(){

    }

    public static CacheManager custom(){
        return new ConnCacheManager();
    }

    @Override
    public <V> Cache<String, V> buildCache(String cacheId, RemovalListener<String, V> removalListener) {
        Cache<String, V> vCache = CacheBuilder.newBuilder()
                .maximumSize(CacheConfiguration.CACHE_MAX_SIZE.getValue())
                .expireAfterWrite(CacheConfiguration.CACHE_EXPIRE_TIME.getValue(), TimeUnit.SECONDS)
                .removalListener(removalListener)
                .build();
        cacheStore.putIfAbsent(cacheId, vCache);
        return vCache;
    }

    @Override
    public <V> LoadingCache<String, V> buildCache(String cacheId, CacheLoader<String, V> loader,
                                                  RemovalListener<String, V> removalListener) {
        LoadingCache<String, V> vCache = CacheBuilder.newBuilder()
                .maximumSize(CacheConfiguration.CACHE_MAX_SIZE.getValue())
                .expireAfterWrite(CacheConfiguration.CACHE_EXPIRE_TIME.getValue(), TimeUnit.SECONDS)
                .removalListener(removalListener)
                .build(loader);
        cacheStore.putIfAbsent(cacheId, vCache);
        return vCache;
    }
}
