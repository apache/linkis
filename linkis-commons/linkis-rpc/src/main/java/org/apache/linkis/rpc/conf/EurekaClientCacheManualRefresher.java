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

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@ConditionalOnProperty(name = "discovery", havingValue = "eureka")
public class EurekaClientCacheManualRefresher implements CacheManualRefresher {
  private static final Logger logger =
      LoggerFactory.getLogger(EurekaClientCacheManualRefresher.class);
  private final AtomicBoolean isRefreshing = new AtomicBoolean(false);
  private final ExecutorService refreshExecutor = Executors.newSingleThreadExecutor();
  private final String cacheRefreshTaskField = "cacheRefreshTask";
  private Object cacheRefreshTask;

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

              String discoveryClientClassName = "com.netflix.discovery.DiscoveryClient";
              if (null == cacheRefreshTask) {
                Class<?> discoveryClientClass = Class.forName(discoveryClientClassName);
                Field field =
                    ReflectionUtils.findField(discoveryClientClass, cacheRefreshTaskField);
                if (null != field) {
                  ReflectionUtils.makeAccessible(field);
                  Object discoveryClient = beanFactory.getBean(discoveryClientClass);
                  cacheRefreshTask = ReflectionUtils.getField(field, discoveryClient);
                }
              }

              if (null == cacheRefreshTask) {
                logger.error(
                    "Field ({}) not found in class '{}'",
                    cacheRefreshTaskField,
                    discoveryClientClassName);
                return;
              }

              lastRefreshMillis = System.currentTimeMillis();
              Class<?> timedSupervisorTaskClass =
                  Class.forName("com.netflix.discovery.TimedSupervisorTask");
              Method method = timedSupervisorTaskClass.getDeclaredMethod("run");
              method.setAccessible(true);
              method.invoke(cacheRefreshTask);
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
