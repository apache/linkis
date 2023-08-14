/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.metadata.query.common.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.*;

public class ConnCacheManager implements CacheManager {
  private ConcurrentHashMap<String, Cache> cacheStore = new ConcurrentHashMap<>();
  private static CacheManager manager;

  private ConnCacheManager() {}

  public static CacheManager custom() {
    if (null == manager) {
      synchronized (ConnCacheManager.class) {
        if (null == manager) {
          manager = new ConnCacheManager();
        }
      }
    }
    return manager;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> Cache<String, V> buildCache(
      String cacheId, RemovalListener<String, V> removalListener) {
    return cacheStore.computeIfAbsent(
        cacheId,
        id ->
            CacheBuilder.newBuilder()
                .maximumSize(CacheConfiguration.CACHE_MAX_SIZE.getValue())
                .expireAfterWrite(CacheConfiguration.CACHE_EXPIRE_TIME.getValue(), TimeUnit.SECONDS)
                .removalListener(removalListener)
                .build());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> LoadingCache<String, V> buildCache(
      String cacheId, CacheLoader<String, V> loader, RemovalListener<String, V> removalListener) {
    return (LoadingCache<String, V>)
        cacheStore.computeIfAbsent(
            cacheId,
            id ->
                CacheBuilder.newBuilder()
                    .maximumSize(CacheConfiguration.CACHE_MAX_SIZE.getValue())
                    .expireAfterWrite(
                        CacheConfiguration.CACHE_EXPIRE_TIME.getValue(), TimeUnit.SECONDS)
                    .removalListener(removalListener)
                    .build(loader));
  }
}
