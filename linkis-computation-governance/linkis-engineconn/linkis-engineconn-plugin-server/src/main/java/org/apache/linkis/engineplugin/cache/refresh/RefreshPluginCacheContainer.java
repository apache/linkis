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

package org.apache.linkis.engineplugin.cache.refresh;

import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInfo;

public interface RefreshPluginCacheContainer {

  /**
   * Start container
   *
   * @param refresher
   */
  void start(PluginCacheRefresher refresher);

  /** Stop container */
  void stop();

  /**
   * Add operation to container
   *
   * @param operation operation
   */
  void addRefreshOperation(EngineConnPluginInfo cacheKey, RefreshPluginCacheOperation operation);

  /**
   * Remove operation from container
   *
   * @param cacheKey cache key
   */
  void removeRefreshOperation(EngineConnPluginInfo cacheKey);

  /**
   * Add the refreshListener
   *
   * @param refreshListener listener
   */
  void addRefreshListener(RefreshableEngineConnPluginCache.RefreshListener refreshListener);
}
