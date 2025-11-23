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

package org.apache.linkis.engineplugin.loader.loaders;

import org.apache.linkis.engineplugin.cache.EngineConnPluginCache;
import org.apache.linkis.engineplugin.cache.GuavaEngineConnPluginCache;
import org.apache.linkis.engineplugin.cache.refresh.PluginCacheRefresher;
import org.apache.linkis.engineplugin.cache.refresh.RefreshableEngineConnPluginCache;
import org.apache.linkis.engineplugin.loader.EngineConnPluginLoaderConf;
import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInfo;
import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInstance;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CacheablesEngineConnPluginLoader implements EngineConnPluginsLoader {

  protected EngineConnPluginCache pluginCache;

  private static final Logger LOG = LoggerFactory.getLogger(CacheablesEngineConnPluginLoader.class);

  public CacheablesEngineConnPluginLoader() {
    // Init cache
    RefreshableEngineConnPluginCache refreshablePluginCache = new GuavaEngineConnPluginCache();
    refreshablePluginCache.addRefreshListener(
        enginePluginInfo ->
            LOG.trace(
                "Refresh engine conn plugin: [name: "
                    + enginePluginInfo.typeLabel().getEngineType()
                    + ", version: "
                    + enginePluginInfo.typeLabel().getVersion()
                    + ", resource_id: "
                    + enginePluginInfo.resourceId()
                    + ", resource_version: "
                    + enginePluginInfo.resourceVersion()
                    + ", resource_update_time: "
                    + enginePluginInfo.resourceUpdateTime()
                    + "]"));
    refreshablePluginCache.setRefresher(
        new PluginCacheRefresher() {
          @Override
          public long interval() {
            return Long.parseLong(
                EngineConnPluginLoaderConf.ENGINE_PLUGIN_LOADER_CACHE_REFRESH_INTERVAL()
                    .getValue());
          }

          @Override
          public TimeUnit timeUnit() {
            return TimeUnit.SECONDS;
          }
        });
    this.pluginCache = refreshablePluginCache;
  }

  @Override
  public EngineConnPluginInstance getEngineConnPlugin(EngineTypeLabel engineTypeLabel)
      throws Exception {
    // Construct plugin info
    EngineConnPluginInfo pluginInfo =
        new EngineConnPluginInfo(engineTypeLabel, -1L, null, null, null);
    return pluginCache.get(pluginInfo, this::loadEngineConnPluginInternal);
  }

  @Override
  public EngineConnPluginInstance loadEngineConnPlugin(EngineTypeLabel engineTypeLabel)
      throws Exception {
    // Construct plugin info
    EngineConnPluginInfo pluginInfo =
        new EngineConnPluginInfo(engineTypeLabel, -1L, null, null, null);
    EngineConnPluginInstance pluginInstance = loadEngineConnPluginInternal(pluginInfo);
    // Update the cache
    pluginCache.put(pluginInstance.info(), pluginInstance);
    return pluginInstance;
  }

  /**
   * Internal method
   *
   * @param enginePluginInfo plugin info
   * @return plugin
   */
  protected abstract EngineConnPluginInstance loadEngineConnPluginInternal(
      EngineConnPluginInfo enginePluginInfo) throws Exception;
}
