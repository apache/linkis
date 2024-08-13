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
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.contextcache.cache.cskey.ContextValueMapSet;
import org.apache.linkis.cs.contextcache.index.ContextInvertedIndexSetImpl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextValueMapSetImpl implements ContextValueMapSet {

  private static final Logger logger = LoggerFactory.getLogger(ContextInvertedIndexSetImpl.class);

  Map<String, Map<String, ContextKeyValue>> contextValueMapSet = new ConcurrentHashMap<>();

  @Override
  public Map<String, ContextKeyValue> getContextValueMap(ContextType contextType) {
    if (contextType == null) {
      contextType = ContextType.METADATA;
    }
    String csType = contextType.name();
    if (!contextValueMapSet.containsKey(csType)) {
      synchronized (csType.intern()) {
        if (!contextValueMapSet.containsKey(csType)) {
          logger.info("For ContextType({}) init ContextValueMap", csType);
          contextValueMapSet.put(csType, new HashMap<String, ContextKeyValue>(16));
        }
      }
    }
    return contextValueMapSet.get(csType);
  }

  @Override
  public ContextKeyValue put(ContextKeyValue contextKeyValue) {
    if (contextKeyValue.getContextKey() != null
        && contextKeyValue.getContextValue() != null
        && StringUtils.isNotBlank(contextKeyValue.getContextKey().getKey())) {
      return getContextValueMap(contextKeyValue.getContextKey().getContextType())
          .put(contextKeyValue.getContextKey().getKey(), contextKeyValue);
    }
    return null;
  }

  @Override
  public ContextKeyValue getByContextKey(ContextKey contextKey, ContextType contextType) {
    if (contextKey != null && StringUtils.isNotBlank(contextKey.getKey())) {
      return getContextValueMap(contextType).get(contextKey.getKey());
    }
    return null;
  }

  @Override
  public ContextKeyValue getByContextKey(String contextKey, ContextType contextType) {
    if (StringUtils.isNotBlank(contextKey)) {
      return getContextValueMap(contextType).get(contextKey);
    }
    return null;
  }

  @Override
  public List<ContextKeyValue> getByContextKeys(List<String> contextKeys, ContextType contextType) {
    if (CollectionUtils.isEmpty(contextKeys)) {
      return null;
    }
    List<ContextKeyValue> contextKeyValueList = new ArrayList<>();
    for (String contextKey : contextKeys) {
      ContextKeyValue contextKeyValue = getByContextKey(contextKey, contextType);
      if (null != contextKeyValue) {
        contextKeyValueList.add(contextKeyValue);
      }
    }
    return contextKeyValueList;
  }

  @Override
  public List<ContextKeyValue> getAllValuesByType(ContextType contextType) {

    Collection<ContextKeyValue> values = getContextValueMap(contextType).values();

    if (null != values) {
      return new ArrayList<>(values);
    }
    return null;
  }

  @Override
  public List<ContextKeyValue> getAllLikes(String regex, ContextType contextType) {
    if (StringUtils.isBlank(regex)) {
      return null;
    }
    Map<String, ContextKeyValue> contextValueMap = getContextValueMap(contextType);
    List<ContextKeyValue> contextKeyValueList = new ArrayList<>();
    Iterator<String> iterator = contextValueMap.keySet().iterator();
    while (iterator.hasNext()) {
      String key = iterator.next();
      if (key.matches(regex)) {
        contextKeyValueList.add(contextValueMap.get(key));
      }
    }
    return contextKeyValueList;
  }

  @Override
  public List<ContextKeyValue> getAll() {
    List<ContextKeyValue> contextKeyValueList = new ArrayList<>();

    for (Map<String, ContextKeyValue> contextKeyValueMap : contextValueMapSet.values()) {
      contextKeyValueList.addAll(contextKeyValueMap.values());
    }
    return contextKeyValueList;
  }

  @Override
  public ContextKeyValue remove(String contextKey, ContextType contextType) {
    if (StringUtils.isNotBlank(contextKey)) {
      return getContextValueMap(contextType).remove(contextKey);
    }
    return null;
  }

  @Override
  public Map<String, ContextKeyValue> removeAll(ContextType contextType) {
    return contextValueMapSet.remove(contextType);
  }

  @Override
  public List<ContextKey> findByKeyPrefix(String preFix) {
    if (StringUtils.isBlank(preFix)) {
      return null;
    }
    List<ContextKey> contextKeyValueList = new ArrayList<>();
    for (Map<String, ContextKeyValue> contextKeyValueMap : contextValueMapSet.values()) {
      Iterator<String> iterator = contextKeyValueMap.keySet().iterator();
      while (iterator.hasNext()) {
        String key = iterator.next();
        if (key.startsWith(preFix)) {
          contextKeyValueList.add(contextKeyValueMap.get(key).getContextKey());
        }
      }
    }
    return contextKeyValueList;
  }

  @Override
  public List<ContextKey> findByKey(String keyStr) {
    if (StringUtils.isBlank(keyStr)) {
      return null;
    }
    List<ContextKey> contextKeyValueList = new ArrayList<>();
    for (Map<String, ContextKeyValue> contextKeyValueMap : contextValueMapSet.values()) {
      Iterator<String> iterator = contextKeyValueMap.keySet().iterator();
      while (iterator.hasNext()) {
        String key = iterator.next();
        if (key.equals(keyStr)) {
          contextKeyValueList.add(contextKeyValueMap.get(key).getContextKey());
        }
      }
    }
    return contextKeyValueList;
  }

  @Override
  public List<ContextKey> findByKeyPrefix(String preFix, ContextType csType) {
    if (StringUtils.isBlank(preFix)) {
      return null;
    }
    Map<String, ContextKeyValue> contextValueMap = getContextValueMap(csType);
    List<ContextKey> contextKeyValueList = new ArrayList<>();
    Iterator<String> iterator = contextValueMap.keySet().iterator();
    while (iterator.hasNext()) {
      String key = iterator.next();
      if (key.startsWith(preFix)) {
        contextKeyValueList.add(contextValueMap.get(key).getContextKey());
      }
    }
    return contextKeyValueList;
  }

  @Override
  public List<ContextKey> findByKey(String keyStr, ContextType csType) {
    if (StringUtils.isBlank(keyStr)) {
      return null;
    }
    Map<String, ContextKeyValue> contextValueMap = getContextValueMap(csType);
    List<ContextKey> contextKeyValueList = new ArrayList<>();
    Iterator<String> iterator = contextValueMap.keySet().iterator();
    while (iterator.hasNext()) {
      String key = iterator.next();
      if (key.equals(keyStr)) {
        contextKeyValueList.add(contextValueMap.get(key).getContextKey());
      }
    }
    return contextKeyValueList;
  }
}
