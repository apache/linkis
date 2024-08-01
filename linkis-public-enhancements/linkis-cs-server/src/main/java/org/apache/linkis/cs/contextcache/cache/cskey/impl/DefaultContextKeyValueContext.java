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

package org.apache.linkis.cs.contextcache.cache.cskey.impl;

import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.exception.CSWarnException;
import org.apache.linkis.cs.contextcache.cache.cskey.ContextKeyValueContext;
import org.apache.linkis.cs.contextcache.cache.cskey.ContextValueMapSet;
import org.apache.linkis.cs.contextcache.index.ContextInvertedIndexSet;
import org.apache.linkis.cs.contextcache.index.ContextInvertedIndexSetImpl;
import org.apache.linkis.cs.contextcache.parser.ContextKeyValueParser;
import org.apache.linkis.cs.listener.ListenerBus.ContextAsyncListenerBus;
import org.apache.linkis.cs.listener.event.enumeration.OperateType;
import org.apache.linkis.cs.listener.event.impl.DefaultContextKeyEvent;
import org.apache.linkis.cs.listener.manager.imp.DefaultContextListenerManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Scope("prototype")
public class DefaultContextKeyValueContext implements ContextKeyValueContext {

  private static final Logger logger = LoggerFactory.getLogger(DefaultContextKeyValueContext.class);

  ContextInvertedIndexSet contextInvertedIndexSet = new ContextInvertedIndexSetImpl();

  ContextValueMapSet contextValueMapSet = new ContextValueMapSetImpl();

  ContextAsyncListenerBus listenerBus =
      DefaultContextListenerManager.getInstance().getContextAsyncListenerBus();

  private ContextID contextID;

  @Autowired ContextKeyValueParser contextKeyValueParser;

  @Override
  public ContextID getContextID() {
    return this.contextID;
  }

  @Override
  public void setContextID(ContextID contextID) throws CSWarnException {
    if (this.contextID == null) {
      this.contextID = contextID;
    } else {
      logger.error(
          "Do not set contextID repeatedly.The current context is {}",
          this.contextID.getContextId());
      throw new CSWarnException(97001, "Do not set contextID repeatedly");
    }
  }

  @Override
  public ContextInvertedIndexSet getContextInvertedIndexSet() {
    return this.contextInvertedIndexSet;
  }

  @Override
  public ContextValueMapSet getContextValueMapSet() {
    return this.contextValueMapSet;
  }

  @Override
  public ContextKeyValueParser getContextKeyValueParser() {
    return this.contextKeyValueParser;
  }

  @Override
  public ContextKeyValue put(ContextKeyValue contextKeyValue) {
    ContextKey contextKey = contextKeyValue.getContextKey();
    if (contextKey == null || StringUtils.isBlank(contextKey.getKey())) {
      return null;
    }
    ContextKeyValue oldValue = getContextValueMapSet().put(contextKeyValue);
    Set<String> keyWords = getContextKeyValueParser().parse(contextKeyValue);

    getContextInvertedIndexSet()
        .addKeywords(keyWords, contextKey.getKey(), contextKey.getContextType());
    DefaultContextKeyEvent defaultContextKeyEvent = new DefaultContextKeyEvent();
    defaultContextKeyEvent.setContextID(contextID);
    defaultContextKeyEvent.setContextKeyValue(contextKeyValue);
    defaultContextKeyEvent.setOldValue(oldValue);
    if (null != oldValue) {
      defaultContextKeyEvent.setOperateType(OperateType.UPDATE);
    } else {
      defaultContextKeyEvent.setOperateType(OperateType.ADD);
    }
    listenerBus.post(defaultContextKeyEvent);
    return oldValue;
  }

  @Override
  public ContextKeyValue remove(ContextKey contextKey) {
    if (contextKey == null || StringUtils.isBlank(contextKey.getKey())) {
      return null;
    }
    ContextKeyValue contextKeyValue =
        getContextValueMapSet().remove(contextKey.getKey(), contextKey.getContextType());
    if (null == contextKeyValue) {
      return null;
    }
    Set<String> keyWords = getContextKeyValueParser().parse(contextKeyValue);
    Iterator<String> iterator = keyWords.iterator();
    ContextInvertedIndexSet contextInvertedIndexSet = getContextInvertedIndexSet();
    while (iterator.hasNext()) {
      contextInvertedIndexSet.remove(
          iterator.next(), contextKey.getKey(), contextKey.getContextType());
    }
    logger.info("Succeed to remove contextKey of {}", contextKey.getKey());
    DefaultContextKeyEvent defaultContextKeyEvent = new DefaultContextKeyEvent();
    defaultContextKeyEvent.setContextID(contextID);
    defaultContextKeyEvent.setContextKeyValue(contextKeyValue);
    defaultContextKeyEvent.setOperateType(OperateType.DELETE);
    listenerBus.post(defaultContextKeyEvent);
    return contextKeyValue;
  }

  @Override
  public ContextKeyValue getContextKeyValue(ContextKey contextKey, ContextType contextType) {
    return getContextValueMapSet().getByContextKey(contextKey, contextType);
  }

  @Override
  public List<ContextKeyValue> getValues(String keyword, ContextType contextType) {
    List<String> contextKeys = getContextInvertedIndexSet().getContextKeys(keyword, contextType);
    return getValues(contextKeys, contextType);
  }

  @Override
  public List<ContextKeyValue> getValues(List<String> contextKeys, ContextType contextType) {
    return getContextValueMapSet().getByContextKeys(contextKeys, contextType);
  }

  @Override
  public List<ContextKeyValue> getAllValues(ContextType contextType) {
    return getContextValueMapSet().getAllValuesByType(contextType);
  }

  @Override
  public List<ContextKeyValue> getAllLikes(String regex, ContextType contextType) {
    return getContextValueMapSet().getAllLikes(regex, contextType);
  }

  @Override
  public List<ContextKeyValue> getAll() {

    return contextValueMapSet.getAll();
  }

  @Override
  public Map<String, ContextKeyValue> removeAll(ContextType contextType) {

    DefaultContextKeyEvent defaultContextKeyEvent = new DefaultContextKeyEvent();
    defaultContextKeyEvent.setContextID(contextID);
    defaultContextKeyEvent.setOperateType(OperateType.REMOVEALL);
    listenerBus.post(defaultContextKeyEvent);
    getContextInvertedIndexSet().removeAll(contextType);
    logger.warn(
        "ContextID({}) removeAll contextKey of  contextType({})",
        contextID.getContextId(),
        contextType.name());
    return getContextValueMapSet().removeAll(contextType);
  }

  @Override
  public Boolean putAll(List<ContextKeyValue> contextKeyValueList) {
    for (ContextKeyValue contextKeyValue : contextKeyValueList) {
      put(contextKeyValue);
    }
    return true;
  }

  @Override
  public void removeByKeyPrefix(String preFix) {
    List<ContextKey> removeKeys = getContextValueMapSet().findByKeyPrefix(preFix);
    if (CollectionUtils.isNotEmpty(removeKeys)) {
      for (ContextKey key : removeKeys) {
        remove(key);
      }
      logger.warn("Remove keyValue by key preFix: " + preFix);
    }
  }

  @Override
  public void removeByKeyPrefix(String preFix, ContextType csType) {
    List<ContextKey> removeKeys = getContextValueMapSet().findByKeyPrefix(preFix, csType);
    if (CollectionUtils.isNotEmpty(removeKeys)) {
      for (ContextKey key : removeKeys) {
        remove(key);
      }
      logger.warn("Remove keyValue by key preFix{} and csType{} ", preFix, csType);
    }
  }

  @Override
  public void removeByKey(String keyStr, ContextType csType) {
    List<ContextKey> removeKeys = getContextValueMapSet().findByKey(keyStr, csType);
    if (CollectionUtils.isNotEmpty(removeKeys)) {
      for (ContextKey key : removeKeys) {
        remove(key);
      }
      logger.warn("Remove keyValue by keyStr {} and csType{} ", keyStr, csType);
    }
  }
}
