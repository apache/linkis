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

package org.apache.linkis.engineplugin.cache;

import org.apache.linkis.engineplugin.cache.config.EngineConnPluginCacheConfig;
import org.apache.linkis.engineplugin.cache.refresh.DefaultRefreshPluginCacheContainer;
import org.apache.linkis.engineplugin.cache.refresh.PluginCacheRefresher;
import org.apache.linkis.engineplugin.cache.refresh.RefreshPluginCacheContainer;
import org.apache.linkis.engineplugin.cache.refresh.RefreshPluginCacheOperation;
import org.apache.linkis.engineplugin.cache.refresh.RefreshableEngineConnPluginCache;
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginNotFoundException;
import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInfo;
import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInstance;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuavaEngineConnPluginCache implements RefreshableEngineConnPluginCache {

  private static final Logger LOG = LoggerFactory.getLogger(GuavaEngineConnPluginCache.class);

  private Cache<String, EngineConnPluginInstance> pluginCache;

  private PluginCacheRefresher refresher;
  /** Refresh container */
  private RefreshPluginCacheContainer refreshContainer;

  public GuavaEngineConnPluginCache() {
    this(null);
  }

  public GuavaEngineConnPluginCache(PluginCacheRefresher refresher) {
    this.refreshContainer = new DefaultRefreshPluginCacheContainer(this);
    this.refresher = refresher;
    this.pluginCache =
        constructCache(
            EngineConnPluginCacheConfig.PLUGIN_CACHE_SIZE.getValue(),
            EngineConnPluginCacheConfig.PLUGIN_CACHE_EXPIRE_TIME_SECONDS.getValue(),
            removalNotification -> {
              String cacheKey = removalNotification.getKey();
              LOG.info(
                  "Remove cache of engine conn plugin:[ "
                      + cacheKey
                      + " ], cause: "
                      + removalNotification.getCause());
              if (!RemovalCause.REPLACED.equals(removalNotification.getCause())) {
                // Ignore replace condition
                this.refreshContainer.removeRefreshOperation(removalNotification.getValue().info());
              }
            });
    if (null != refresher) {
      this.refreshContainer.start(refresher);
    }
  }

  private Cache<String, EngineConnPluginInstance> constructCache(
      int cacheSize,
      long expireTimeInSeconds,
      RemovalListener<String, EngineConnPluginInstance> removeListener) {
    CacheBuilder<String, EngineConnPluginInstance> cacheBuilder =
        CacheBuilder.newBuilder().maximumSize(cacheSize).removalListener(removeListener);
    if (expireTimeInSeconds > 0) {
      cacheBuilder.expireAfterAccess(expireTimeInSeconds, TimeUnit.SECONDS);
    }
    return cacheBuilder.build();
  }

  @Override
  public void addRefreshListener(RefreshListener refreshListener) {
    refreshContainer.addRefreshListener(refreshListener);
  }

  @Override
  public synchronized void setRefresher(PluginCacheRefresher refresher) {
    if (null != refresher) {
      if (null != this.refresher) {
        refreshContainer.stop();
      }
      this.refreshContainer.start(refresher);
      this.refresher = refresher;
    }
  }

  @Override
  public void refresh(EngineConnPluginInfo pluginInfo, EngineConnPluginInstance pluginInstance)
      throws Exception {
    // The same as the put method
    put(pluginInfo, pluginInstance);
  }

  @Override
  public void put(EngineConnPluginInfo pluginInfo, EngineConnPluginInstance pluginInstance)
      throws Exception {
    this.pluginCache.put(pluginInfo.toString(), pluginInstance);
    // Be care of that: put method doesn't add refresh operation
  }

  @Override
  public EngineConnPluginInstance get(
      EngineConnPluginInfo pluginInfo, EngineConnPluginCache.PluginGetter caller) throws Exception {
    return this.pluginCache.get(
        pluginInfo.toString(),
        () -> {
          EngineConnPluginInstance instance = caller.call(pluginInfo);
          if (null != refresher) {
            refreshContainer.addRefreshOperation(
                pluginInfo,
                new RefreshPluginCacheOperation(
                    info -> {
                      try {
                        // Use the getter method of plugin
                        return caller.call(info);
                      } catch (EngineConnPluginNotFoundException ne) {
                        LOG.trace(
                            "Not need to refresh the cache of plugin: ["
                                + info.toString()
                                + "], because the resource is not found");
                        return null;
                      } catch (Exception e) {
                        LOG.error(
                            "Refresh cache of plugin: ["
                                + info.toString()
                                + "] failed, message: ["
                                + e.getMessage()
                                + "]",
                            e);
                        return null;
                      }
                    }));
          }
          return instance;
        });
  }

  @Override
  public EngineConnPluginInstance remove(EngineConnPluginInfo pluginInfo) throws Exception {
    final EngineConnPluginInstance instance = pluginCache.getIfPresent(pluginInfo.toString());
    pluginCache.invalidate(pluginInfo.toString());
    return instance;
  }
}
