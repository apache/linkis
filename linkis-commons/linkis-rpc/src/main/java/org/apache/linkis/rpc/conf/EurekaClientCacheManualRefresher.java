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

package org.apache.linkis.rpc.conf;

import org.apache.commons.lang.exception.ExceptionUtils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.TimedSupervisorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class EurekaClientCacheManualRefresher {
  private static final Logger logger =
      LoggerFactory.getLogger(EurekaClientCacheManualRefresher.class);
  private final AtomicBoolean isRefreshing = new AtomicBoolean(false);
  private final ExecutorService refreshExecutor = Executors.newSingleThreadExecutor();
  private final String cacheRefreshTaskField = "cacheRefreshTask";
  private TimedSupervisorTask cacheRefreshTask;

  private long lastRefreshMillis = 0;
  private final Duration refreshIntervalDuration = Duration.ofSeconds(3);

  @Autowired private BeanFactory beanFactory;

  public void refreshOnExceptions(Exception e, List<Class<? extends Exception>> clazzs) {
    if (null == clazzs || clazzs.size() == 0) {
      throw new IllegalArgumentException();
    }

    if (clazzs.stream()
        .anyMatch(
            clazz -> clazz.isInstance(e) || clazz.isInstance(ExceptionUtils.getRootCause(e)))) {
      refresh();
    }
  }

  public void refresh() {
    if (isRefreshing.compareAndSet(false, true)) {
      refreshExecutor.execute(
          () -> {
            try {
              if (System.currentTimeMillis()
                  <= lastRefreshMillis + refreshIntervalDuration.toMillis()) {
                logger.warn(
                    "Not manually refresh eureka client cache as refresh interval was not exceeded:{}",
                    refreshIntervalDuration.getSeconds());
                return;
              }

              if (null == cacheRefreshTask) {
                Field field =
                    ReflectionUtils.findField(DiscoveryClient.class, cacheRefreshTaskField);
                if (null != field) {
                  ReflectionUtils.makeAccessible(field);
                  DiscoveryClient discoveryClient = beanFactory.getBean(DiscoveryClient.class);
                  cacheRefreshTask =
                      (TimedSupervisorTask) ReflectionUtils.getField(field, discoveryClient);
                }
              }

              if (null == cacheRefreshTask) {
                logger.error(
                    "Field ({}) not found in class '{}'",
                    cacheRefreshTaskField,
                    DiscoveryClient.class.getSimpleName());
                return;
              }

              lastRefreshMillis = System.currentTimeMillis();
              cacheRefreshTask.run();
              logger.info(
                  "Manually refresh eureka client cache completed(DiscoveryClient.cacheRefreshTask#run())");
            } catch (Exception e) {
              logger.error("An exception occurred when manually refresh eureka client cache", e);
            } finally {
              isRefreshing.set(false);
            }
          });
    } else {
      logger.warn(
          "Not manually refresh eureka client cache as another thread is refreshing it already");
    }
  }
}
