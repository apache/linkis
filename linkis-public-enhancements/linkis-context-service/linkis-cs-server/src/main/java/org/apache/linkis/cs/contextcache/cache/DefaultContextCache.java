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

package org.apache.linkis.cs.contextcache.cache;

import org.apache.linkis.common.listener.Event;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.contextcache.cache.csid.ContextIDValue;
import org.apache.linkis.cs.contextcache.cache.csid.ContextIDValueGenerator;
import org.apache.linkis.cs.contextcache.conf.ContextCacheConf;
import org.apache.linkis.cs.contextcache.metric.ContextCacheMetric;
import org.apache.linkis.cs.contextcache.metric.ContextIDMetric;
import org.apache.linkis.cs.contextcache.metric.DefaultContextCacheMetric;
import org.apache.linkis.cs.listener.CSIDListener;
import org.apache.linkis.cs.listener.ListenerBus.ContextAsyncListenerBus;
import org.apache.linkis.cs.listener.event.ContextIDEvent;
import org.apache.linkis.cs.listener.event.impl.DefaultContextIDEvent;
import org.apache.linkis.cs.listener.manager.imp.DefaultContextListenerManager;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.cs.listener.event.enumeration.OperateType.*;

@Component
public class DefaultContextCache implements ContextCache, CSIDListener {

  private static final Logger logger = LoggerFactory.getLogger(DefaultContextCache.class);

  private ContextAsyncListenerBus listenerBus =
      DefaultContextListenerManager.getInstance().getContextAsyncListenerBus();

  @Autowired private RemovalListener<String, ContextIDValue> contextIDRemoveListener;

  @Autowired private ContextIDValueGenerator contextIDValueGenerator;

  private Cache<String, ContextIDValue> cache = null;

  private ContextCacheMetric contextCacheMetric = new DefaultContextCacheMetric();

  @PostConstruct
  private void init() {
    listenerBus.addListener(this);
    this.cache =
        CacheBuilder.newBuilder()
            .maximumSize(ContextCacheConf.MAX_CACHE_SIZE)
            .expireAfterWrite(Duration.ofMillis(ContextCacheConf.MAX_CACHE_READ_EXPIRE_MILLS))
            .removalListener(contextIDRemoveListener)
            .recordStats()
            .build();
  }

  @Override
  public ContextIDValue getContextIDValue(ContextID contextID) throws CSErrorException {
    if (null == contextID || StringUtils.isBlank(contextID.getContextId())) {
      return null;
    }
    try {
      ContextIDValue contextIDValue = cache.getIfPresent(contextID.getContextId());
      if (contextIDValue == null) {
        contextIDValue = contextIDValueGenerator.createContextIDValue(contextID);
        put(contextIDValue);
        DefaultContextIDEvent defaultContextIDEvent = new DefaultContextIDEvent();
        defaultContextIDEvent.setContextID(contextID);
        defaultContextIDEvent.setOperateType(ADD);
        listenerBus.post(defaultContextIDEvent);
      }
      DefaultContextIDEvent defaultContextIDEvent = new DefaultContextIDEvent();
      defaultContextIDEvent.setContextID(contextID);
      defaultContextIDEvent.setOperateType(ACCESS);
      listenerBus.post(defaultContextIDEvent);
      return contextIDValue;
    } catch (Exception e) {
      String errorMsg =
          String.format("Failed to get contextIDValue of ContextID(%s)", contextID.getContextId());
      logger.error(errorMsg);
      throw new CSErrorException(97001, errorMsg, e);
    }
  }

  @Override
  public void remove(ContextID contextID) {
    if (null != contextID && StringUtils.isNotBlank(contextID.getContextId())) {
      logger.info("From cache to remove contextID:{}", contextID.getContextId());
      cache.invalidate(contextID.getContextId());
    }
  }

  @Override
  public void put(ContextIDValue contextIDValue) throws CSErrorException {

    if (contextIDValue != null && StringUtils.isNotBlank(contextIDValue.getContextID())) {
      logger.info("update contextID:{}", contextIDValue.getContextID());
      cache.put(contextIDValue.getContextID(), contextIDValue);
    }
  }

  @Override
  public Map<String, ContextIDValue> getAllPresent(List<ContextID> contextIDList) {
    List<String> contextIDKeys =
        contextIDList.stream()
            .map(
                contextID -> {
                  if (StringUtils.isBlank(contextID.getContextId())) {
                    return null;
                  }
                  return contextID.getContextId();
                })
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    return cache.getAllPresent(contextIDKeys);
  }

  @Override
  public void refreshAll() throws CSErrorException {
    // TODO
  }

  @Override
  public void putAll(List<ContextIDValue> contextIDValueList) throws CSErrorException {
    for (ContextIDValue contextIDValue : contextIDValueList) {
      put(contextIDValue);
    }
  }

  @Override
  public ContextIDValue loadContextIDValue(ContextID contextID) {
    return null;
  }

  @Override
  public ContextCacheMetric getContextCacheMetric() {
    return this.contextCacheMetric;
  }

  @Override
  public void onCSIDAccess(ContextIDEvent contextIDEvent) {
    ContextID contextID = contextIDEvent.getContextID();
    try {
      ContextIDValue contextIDValue = getContextIDValue(contextID);
      ContextIDMetric contextIDMetric = contextIDValue.getContextIDMetric();

      contextIDMetric.setLastAccessTime(System.currentTimeMillis());
      contextIDMetric.addCount();
      getContextCacheMetric().addCount();
    } catch (CSErrorException e) {
      logger.error("Failed to deal CSIDAccess event csid is {}", contextID.getContextId());
    }
  }

  @Override
  public void onCSIDADD(ContextIDEvent contextIDEvent) {
    logger.info("deal contextID ADD event of {}", contextIDEvent.getContextID());
    getContextCacheMetric().addCount();
    getContextCacheMetric().setCachedCount(getContextCacheMetric().getCachedCount() + 1);
    logger.info("Now, The cachedCount is ({})", getContextCacheMetric().getCachedCount());
  }

  @Override
  public void onCSIDRemoved(ContextIDEvent contextIDEvent) {
    logger.info("deal contextID remove event of {}", contextIDEvent.getContextID());
    getContextCacheMetric().setCachedCount(getContextCacheMetric().getCachedCount() - 1);
    logger.info("Now, The cachedCount is ({})", getContextCacheMetric().getCachedCount());
  }

  @Override
  public void onEventError(Event event, Throwable t) {
    logger.error("Failed to deal event", t);
  }

  @Override
  public void onEvent(Event event) {
    DefaultContextIDEvent defaultContextIDEvent = null;
    if (event != null && event instanceof DefaultContextIDEvent) {
      defaultContextIDEvent = (DefaultContextIDEvent) event;
    }
    if (null == defaultContextIDEvent) {
      return;
    }
    if (ADD.equals(defaultContextIDEvent.getOperateType())) {
      onCSIDADD(defaultContextIDEvent);
    } else if (DELETE.equals(defaultContextIDEvent.getOperateType())) {
      onCSIDRemoved(defaultContextIDEvent);
    } else {
      onCSIDAccess(defaultContextIDEvent);
    }
  }
}
