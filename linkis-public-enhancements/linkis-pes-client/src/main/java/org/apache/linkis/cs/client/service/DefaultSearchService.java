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

package org.apache.linkis.cs.client.service;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.cs.client.ContextClient;
import org.apache.linkis.cs.client.builder.ContextClientFactory;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.entity.source.ContextValue;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.exception.ErrorCode;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSearchService implements SearchService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultSearchService.class);
  private static final Gson gson = new Gson();
  private static SearchService searchService = null;

  private DefaultSearchService() {}

  @Override
  public <T> T getContextValue(
      ContextID contextID, ContextKey contextKey, Class<T> contextValueType)
      throws CSErrorException {
    ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
    ContextValue contextValue = null;
    ContextType contextType = contextKey.getContextType();
    try {
      contextValue = contextClient.getContextValue(contextID, contextKey);
    } catch (Exception e) {
      logger.error("Failed to get ContextValue: " + e.getMessage());
      throw new CSErrorException(
          ErrorCode.GET_CONTEXT_VALUE_ERROR, "Failed to get ContextValue: ", e);
    }

    if (null == contextValue || null == contextValue.getValue()) {
      return null;
    } else if (contextValueType.isInstance(contextValue.getValue())) {
      return (T) contextValue.getValue();
    } else {
      throw new CSErrorException(
          ErrorCode.INVALID_CONTEXT_VALUE_TYPE, "Invalid Context Type : " + contextType);
    }
  }

  @Override
  public <T> T searchContext(
      ContextID contextId, String keyword, String nodeName, Class<T> contextValueType)
      throws CSErrorException {
    if (null == contextId
        || StringUtils.isBlank(contextId.getContextId())
        || StringUtils.isBlank(nodeName)
        || null == contextValueType) {
      logger.error("ContextID or nodeName or contextValueType cannot be null.");
      throw new CSErrorException(
          ErrorCode.INVALID_NULL_STRING,
          "ContextID or nodeName or contextValueType cannot be null.");
    }
    ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
    List<String> contains = new ArrayList<>();
    contains.add(keyword);
    List<Class> contextValueTypes = new ArrayList<>();
    contextValueTypes.add(contextValueType);
    List<ContextKeyValue> contextKeyValues = null;
    try {
      contextKeyValues =
          contextClient.search(
              contextId, null, null, contains, null, false, nodeName, 1, contextValueTypes);
    } catch (ErrorException e) {
      logger.error("Search context value error.");
      throw new CSErrorException(
          ErrorCode.GET_CONTEXT_VALUE_ERROR, "Search context value error: ", e);
    }
    if (CollectionUtils.isEmpty(contextKeyValues)
        || null == contextKeyValues.get(0).getContextValue()) {
      return null;
    }
    if (contextValueType.isInstance(contextKeyValues.get(0).getContextValue().getValue())) {
      return (T) contextKeyValues.get(0).getContextValue().getValue();
    } else {
      throw new CSErrorException(
          ErrorCode.SEARCH_CONTEXT_VALUE_ERROR,
          "Search value : "
              + gson.toJson(
                  contextKeyValues.get(0)
                      + " is not instance of class : "
                      + contextValueType.getName()));
    }
  }

  @Override
  public <T> List<T> searchUpstreamContext(
      ContextID contextID, String nodeName, int num, Class<T> contextValueType)
      throws CSErrorException {
    Map<ContextKey, T> contextMap =
        searchUpstreamContextMap(contextID, nodeName, num, contextValueType);
    if (null == contextMap || contextMap.size() < 1) {
      return null;
    }
    List<T> retValues = new ArrayList<>(contextMap.size());
    contextMap.entrySet().stream()
        .filter(kv -> null != kv && null != kv.getValue())
        .map(kv -> kv.getValue())
        .forEach(retValues::add);
    return retValues;
  }

  @Override
  public <T> Map<ContextKey, T> searchUpstreamContextMap(
      ContextID contextID, String nodeName, int num, Class<T> contextValueType)
      throws CSErrorException {
    ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
    if (null == contextID || StringUtils.isBlank(contextID.getContextId())) {
      return null;
    }
    List<Class> contextValueTypes = new ArrayList<>();
    contextValueTypes.add(contextValueType);
    List<ContextKeyValue> contextKeyValueList = null;
    try {
      contextKeyValueList =
          contextClient.search(
              contextID, null, null, null, null, true, nodeName, num, contextValueTypes);
    } catch (ErrorException e) {
      throw new CSErrorException(
          ErrorCode.GET_CONTEXT_VALUE_ERROR, "searchUpstreamContextMap error ", e);
    }
    if (CollectionUtils.isEmpty(contextKeyValueList)) {
      return null;
    }
    Map<ContextKey, T> retValues = new HashMap<>(contextKeyValueList.size());
    contextKeyValueList.stream()
        .filter(kv -> null != kv && null != kv.getContextValue())
        .forEach(kv -> retValues.put(kv.getContextKey(), (T) (kv.getContextValue().getValue())));
    return retValues;
  }

  @Override
  public <T> List<ContextKeyValue> searchUpstreamKeyValue(
      ContextID contextID, String nodeName, int num, Class<T> contextValueType)
      throws CSErrorException {
    ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
    if (null == contextID || StringUtils.isBlank(contextID.getContextId())) {
      return null;
    }
    List<Class> contextValueTypes = new ArrayList<>();
    contextValueTypes.add(contextValueType);
    List<ContextKeyValue> contextKeyValueList = null;
    try {
      contextKeyValueList =
          contextClient.search(
              contextID, null, null, null, null, true, nodeName, num, contextValueTypes);
    } catch (ErrorException e) {
      throw new CSErrorException(
          ErrorCode.GET_CONTEXT_VALUE_ERROR, "searchUpstreamKeyValue error ", e);
    }
    return contextKeyValueList;
  }

  public static SearchService getInstance() {
    if (null == searchService) {
      synchronized (DefaultSearchService.class) {
        if (null == searchService) {
          searchService = new DefaultSearchService();
        }
      }
    }
    return searchService;
  }
}
