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

import org.apache.linkis.engineplugin.cache.config.EngineConnPluginCacheConfig;
import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInfo;
import org.apache.linkis.manager.engineplugin.common.loader.entity.EngineConnPluginInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRefreshPluginCacheContainer implements RefreshPluginCacheContainer {

  private static final Logger LOG =
      LoggerFactory.getLogger(DefaultRefreshPluginCacheContainer.class);

  private static final String REFRESH_SCHEDULE_THREAD = "Refresh-plugin-cache-scheduler-";

  private static final String REFRESH_WORKER_THREAD = "Refresh-plugin-cache-worker-";

  private ConcurrentHashMap<String, RefreshPluginCacheOperation> pluginRefreshOps =
      new ConcurrentHashMap<>();

  /** Refresh listeners */
  private List<RefreshableEngineConnPluginCache.RefreshListener> refreshListeners =
      new ArrayList<>();

  /** Delay queue for refreshing cache */
  private DelayQueue<RefreshPluginCacheOperation> refreshDelayQueue = new DelayQueue<>();

  private PluginCacheRefresher refresher;

  private volatile boolean isRunning = false;

  private ExecutorService scheduleService;

  private ExecutorService workService;

  private RefreshableEngineConnPluginCache pluginCache;

  public DefaultRefreshPluginCacheContainer(RefreshableEngineConnPluginCache pluginCache) {
    this.pluginCache = pluginCache;
  }

  @Override
  public synchronized void start(PluginCacheRefresher refresher) {
    if (!isRunning) {
      LOG.info("Starting container of refreshing plugin cache...");
      checkRefresher(refresher);
      this.refresher = refresher;
      startExecutors();
      this.isRunning = true;
      // Start consumer thread
      startConsumer();
    } else {
      LOG.trace("This container has been started");
    }
  }

  @Override
  public synchronized void stop() {
    LOG.info("Stopping container of refreshing plugin cache...");
    stopExecutors();
    isRunning = false;
    LOG.info("Success to stop container of refreshing plugin cache");
  }

  @Override
  public void addRefreshOperation(
      EngineConnPluginInfo cacheKey, RefreshPluginCacheOperation operation) {
    if (null != refresher) {
      operation.setTimeUnit(refresher.timeUnit());
      operation.setDuration(refresher.interval());
      operation.setPluginInfo(cacheKey);
      operation.nextTime();
      this.pluginRefreshOps.computeIfAbsent(
          cacheKey.toString(),
          (key) -> {
            this.refreshDelayQueue.put(operation);
            return operation;
          });
    }
  }

  @Override
  public void removeRefreshOperation(EngineConnPluginInfo cacheKey) {
    RefreshPluginCacheOperation operation = this.pluginRefreshOps.remove(cacheKey.toString());
    if (null != operation) {
      LOG.trace("Remove refresh-cache operation in queue for plugin:[ " + cacheKey + " ]");
      this.refreshDelayQueue.remove(operation);
    }
  }

  @Override
  public void addRefreshListener(RefreshableEngineConnPluginCache.RefreshListener refreshListener) {
    this.refreshListeners.add(refreshListener);
  }

  /** Start executors */
  private void startExecutors() {
    LOG.info("Start executors: [ schedule, worker ] in container");
    this.scheduleService =
        Executors.newSingleThreadExecutor(new RefreshThreadFactory(REFRESH_SCHEDULE_THREAD));
    // OOM ?
    this.workService =
        Executors.newFixedThreadPool(
            EngineConnPluginCacheConfig.PLUGIN_CACHE_REFRESH_WORKERS.getValue(),
            new RefreshThreadFactory(REFRESH_WORKER_THREAD));
  }

  /** Stop executors */
  private void stopExecutors() {
    LOG.info("Stop executors: [ schedule, worker ] in container");
    if (null != this.scheduleService) {
      this.scheduleService.shutdownNow();
      this.scheduleService = null;
    }
    if (null != this.workService) {
      this.workService.shutdownNow();
      this.workService = null;
    }
  }

  /** Invoke refresh listener */
  private void onRefresh(EngineConnPluginInfo pluginInfo) {
    refreshListeners.forEach(refreshListener -> refreshListener.onRefresh(pluginInfo));
  }

  private void checkRefresher(PluginCacheRefresher refresher) {
    long interval = refresher.interval();
    if (interval <= 0) {
      throw new IllegalArgumentException(
          "interval value [" + interval + "]of refresher cannot be <= 0 ");
    }
  }

  private void startConsumer() {
    this.scheduleService.submit(
        () -> {
          while (isRunning) {
            try {
              RefreshPluginCacheOperation operation = refreshDelayQueue.take();
              String cacheKey = operation.cacheStringKey();
              // Avoid concurrent operation
              if (operation == pluginRefreshOps.get(cacheKey)) {
                try {
                  try {
                    // Invoke Refresh operation
                    Future<EngineConnPluginInstance> future =
                        workService.submit(
                            () -> operation.getOperation().apply(operation.pluginInfo()));
                    EngineConnPluginInstance newPluginInstance = future.get(12, TimeUnit.HOURS);
                    if (null != newPluginInstance) {
                      // Refresh the cache;
                      this.pluginCache.refresh(newPluginInstance.info(), newPluginInstance);
                      // Also update plugin info into refresh operation
                      operation.setPluginInfo(newPluginInstance.info());
                      onRefresh(newPluginInstance.info());
                    }
                  } catch (Exception e) {
                    LOG.info("Unable to refresh plugin: [ " + pluginCache.toString() + " ]", e);
                    // Ignore
                  }
                } finally {
                  // Update the trigger time
                  operation.nextTime();
                  operation.setDuration(refresher.interval());
                  operation.setTimeUnit(refresher.timeUnit());
                  // Re-into the delay queue
                  this.refreshDelayQueue.add(operation);
                }
              }
            } catch (InterruptedException e) {
              LOG.info(
                  "Error in consuming delay queue of refresh-plugin-cache operation, message:["
                      + e.getMessage()
                      + "]",
                  e);
              // Just wait a seconds
              try {
                Thread.sleep(1000);
              } catch (InterruptedException ex) {
                // Ignore
              }
            }
          }
        });
  }

  static class RefreshThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    RefreshThreadFactory(String namePrefix) {
      SecurityManager s = System.getSecurityManager();
      group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
      this.namePrefix = namePrefix + poolNumber.getAndIncrement() + "-thread-";
    }

    public Thread newThread(Runnable r) {
      Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
      if (t.isDaemon()) t.setDaemon(false);
      if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
      return t;
    }
  }
}
