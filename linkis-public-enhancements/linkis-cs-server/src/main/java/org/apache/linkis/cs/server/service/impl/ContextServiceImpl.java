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

package org.apache.linkis.cs.server.service.impl;

import org.apache.linkis.cs.ContextSearch;
import org.apache.linkis.cs.DefaultContextSearch;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.entity.source.ContextValue;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.contextcache.ContextCacheService;
import org.apache.linkis.cs.errorcode.LinkisCsServerErrorCodeSummary;
import org.apache.linkis.cs.exception.ContextSearchFailedException;
import org.apache.linkis.cs.highavailable.ha.ContextHAChecker;
import org.apache.linkis.cs.persistence.ContextPersistenceManager;
import org.apache.linkis.cs.persistence.entity.PersistenceContextID;
import org.apache.linkis.cs.persistence.entity.PersistenceContextKeyValue;
import org.apache.linkis.cs.persistence.persistence.ContextIDPersistence;
import org.apache.linkis.cs.persistence.persistence.ContextMapPersistence;
import org.apache.linkis.cs.server.enumeration.ServiceType;
import org.apache.linkis.cs.server.parser.KeywordParser;
import org.apache.linkis.cs.server.service.ContextService;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ContextServiceImpl extends ContextService {

  // 对接search

  private ContextSearch contextSearch = new DefaultContextSearch();

  @Autowired private ContextCacheService contextCacheService;

  @Autowired private ContextPersistenceManager persistenceManager;

  @Autowired private KeywordParser keywordParser;

  @Autowired private ContextHAChecker contextHAChecker;

  private ObjectMapper jackson = BDPJettyServerHelper.jacksonJson();

  private ContextMapPersistence getPersistence() throws CSErrorException {
    return persistenceManager.getContextMapPersistence();
  }

  private ContextIDPersistence getIDPersistence() throws CSErrorException {
    return persistenceManager.getContextIDPersistence();
  }

  @Override
  public String getName() {
    return ServiceType.CONTEXT.name();
  }

  @Override
  public ContextValue getContextValue(ContextID contextID, ContextKey contextKey) {
    ContextKeyValue keyValue = contextCacheService.get(contextID, contextKey);
    if (keyValue == null) {
      logger.info(
          "getContextValue is null contextId: {}, key: {}",
          contextID.getContextId(),
          contextKey.getKey());
      return null;
    }
    logger.info(
        "getContextValue,csId:{},key:{},csType:{},csScope:{}",
        contextID.getContextId(),
        contextKey.getKey(),
        contextKey.getContextType(),
        contextKey.getContextScope());
    return keyValue.getContextValue();
  }

  @Override
  public List<ContextKeyValue> searchContextValue(
      ContextID contextID, Map<Object, Object> conditionMap) throws ContextSearchFailedException {
    logger.info("searchContextValue,csId:{}", contextID.getContextId());
    return contextSearch.search(contextCacheService, contextID, conditionMap);
  }

  @Override
  public void setValueByKey(ContextID contextID, ContextKey contextKey, ContextValue contextValue)
      throws CSErrorException, JsonProcessingException {
    Object value = contextValue.getValue();
    Set<String> keys = keywordParser.parse(value);
    keys.add(contextKey.getKey());
    contextValue.setKeywords(jackson.writeValueAsString(keys));
    ContextKeyValue keyValue = contextCacheService.get(contextID, contextKey);
    if (keyValue == null) {
      if (contextKey.getContextScope() == null || contextKey.getContextType() == null) {
        throw new CSErrorException(
            LinkisCsServerErrorCodeSummary.PARAMS_CANNOT_EMPTY.getErrorCode(),
            "try to create context ,type or scope cannot be empty");
      }
      logger.info(
          "Create cs key value setValueByKey, csId:{}},key:{}",
          contextID.getContextId(),
          contextKey.getKey());
      keyValue = new PersistenceContextKeyValue();
      keyValue.setContextKey(contextKey);
      keyValue.setContextValue(contextValue);
      getPersistence().create(contextID, keyValue);
    } else {
      if (contextKey.getContextScope() == null) {
        contextKey.setContextScope(keyValue.getContextKey().getContextScope());
      }
      if (contextKey.getContextType() == null) {
        contextKey.setContextType(keyValue.getContextKey().getContextType());
      }
      keyValue.setContextKey(contextKey);
      keyValue.setContextValue(contextValue);
      getPersistence().update(contextID, keyValue);
    }
    contextCacheService.put(contextID, keyValue);
    logger.info(
        "setValueByKey, csId:{},key: {},keywords:{}",
        contextID.getContextId(),
        contextKey.getKey(),
        contextValue.getKeywords());
  }

  @Override
  public void setValue(ContextID contextID, ContextKeyValue contextKeyValue)
      throws CSErrorException, ClassNotFoundException, JsonProcessingException {
    // parse keywords
    Object value = contextKeyValue.getContextValue().getValue();
    Set<String> keys = keywordParser.parse(value);
    keys.add(contextKeyValue.getContextKey().getKey());
    contextKeyValue.getContextValue().setKeywords(jackson.writeValueAsString(keys));
    ContextKeyValue keyValue = contextCacheService.get(contextID, contextKeyValue.getContextKey());
    if (keyValue == null) {
      logger.warn(
          "cache can not find contextId:{},key:{},now try to load from MySQL",
          contextID.getContextId(),
          contextKeyValue.getContextKey().getKey());
      keyValue = getPersistence().get(contextID, contextKeyValue.getContextKey());
      if (keyValue != null) {
        logger.warn("MySQL find the key,now reset the cache and get it");
        contextCacheService.rest(contextID, contextKeyValue.getContextKey());
        keyValue = contextCacheService.get(contextID, contextKeyValue.getContextKey());
      }
    }
    if (keyValue == null) {
      if (contextKeyValue.getContextKey().getContextScope() == null
          || contextKeyValue.getContextKey().getContextType() == null) {
        throw new CSErrorException(97000, "try to create context ,type or scope cannot be empty");
      }
      getPersistence().create(contextID, contextKeyValue);
    } else {
      // For update, if the scope and type are empty, use the value in the database, because the
      // update cache needs to be used
      if (contextKeyValue.getContextKey().getContextScope() == null) {
        contextKeyValue.getContextKey().setContextScope(keyValue.getContextKey().getContextScope());
      }
      if (contextKeyValue.getContextKey().getContextType() == null) {
        contextKeyValue.getContextKey().setContextType(keyValue.getContextKey().getContextType());
      }
      getPersistence().update(contextID, contextKeyValue);
    }
    // refresh cache
    contextCacheService.put(contextID, contextKeyValue);
    logger.info(
        "From db and cache  to setValue, csId:{},key:{}",
        contextID.getContextId(),
        contextKeyValue.getContextKey().getKey());
  }

  @Override
  public void resetValue(ContextID contextID, ContextKey contextKey) throws CSErrorException {
    // 1.reset db
    getPersistence().reset(contextID, contextKey);
    // 2.reset cache
    contextCacheService.rest(contextID, contextKey);
    logger.info(
        "From db and cache  resetValue, csId:{},key:{}",
        contextID.getContextId(),
        contextKey.getKey());
  }

  @Override
  public void removeValue(ContextID contextID, ContextKey contextKey) throws CSErrorException {
    ContextKeyValue contextKeyValue = contextCacheService.get(contextID, contextKey);
    if (contextKeyValue == null) {
      return;
    }

    if (contextKey.getContextScope() == null) {
      contextKey.setContextScope(contextKeyValue.getContextKey().getContextScope());
    }
    if (contextKey.getContextType() == null) {
      contextKey.setContextType(contextKeyValue.getContextKey().getContextType());
    }
    // 1.remove db
    getPersistence().remove(contextID, contextKey);
    // 2.remove cache
    contextCacheService.remove(contextID, contextKey);
    logger.info(
        "From db and cache removeValue, csId:{},key:{}",
        contextID.getContextId(),
        contextKey.getKey());
  }

  @Override
  public void removeAllValue(ContextID contextID) throws CSErrorException {
    getPersistence().removeAll(contextID);
    contextCacheService.removeAll(contextID);
    logger.info("From db and cache removeAllValue, csId:{}", contextID.getContextId());
  }

  @Override
  public void removeAllValueByKeyPrefixAndContextType(
      ContextID contextID, ContextType contextType, String keyPrefix) throws CSErrorException {
    contextCacheService.removeByKeyPrefix(contextID, keyPrefix, contextType);
    getPersistence().removeByKeyPrefix(contextID, contextType, keyPrefix);
    logger.info(
        "From db and cache  removeAllValueByKeyPrefixAndContextType, csId:{},csType:{},keyPrefix:{}",
        contextID.getContextId(),
        contextType,
        keyPrefix);
  }

  @Override
  public void removeValueByKeyAndContextType(
      ContextID contextID, ContextType contextType, String keyStr) throws CSErrorException {
    contextCacheService.removeByKey(contextID, keyStr, contextType);
    getPersistence().removeByKey(contextID, contextType, keyStr);
    logger.info(
        "From db and cache  removeAllValueByKeyAndContextType, csId:{},csType:{},keyStr:{}",
        contextID.getContextId(),
        contextType,
        keyStr);
  }

  @Override
  public void removeAllValueByKeyPrefix(ContextID contextID, String keyPrefix)
      throws CSErrorException {
    contextCacheService.removeByKeyPrefix(contextID, keyPrefix);
    getPersistence().removeByKeyPrefix(contextID, keyPrefix);
    logger.info(
        "From db and cache  removeAllValueByKeyPrefix, csId:{},keyPrefix:{}",
        contextID.getContextId(),
        keyPrefix);
  }

  @Override
  public int clearAllContextByID(List<String> idList) throws CSErrorException {
    int num = 0;
    for (String haid : idList) {
      try {
        ContextID contextID = contextHAChecker.parseHAIDFromKey(haid);
        String csid = contextID.getContextId();
        contextID.setContextId(haid);
        getPersistence().removeAll(contextID);
        getIDPersistence().deleteContextID(csid);
        num++;
      } catch (Exception e) {
        logger.warn("clear all for haid : {}", haid, e);
      }
    }
    return num;
  }

  @Override
  public int clearAllContextByTime(
      Date createTimeStart,
      Date createTimeEnd,
      Date updateTimeStart,
      Date updateTimeEnd,
      Date accessTimeStart,
      Date accessTimeEnd)
      throws CSErrorException {
    int num = 0;
    List<PersistenceContextID> idList =
        getIDPersistence()
            .searchCSIDByTime(
                createTimeStart,
                createTimeEnd,
                updateTimeStart,
                updateTimeEnd,
                accessTimeStart,
                accessTimeEnd);
    for (PersistenceContextID id : idList) {
      try {
        String csid = id.getContextId();
        logger.info("will clear context for csid : {}", csid);
        id.setContextId(contextHAChecker.convertHAIDToHAKey(id));
        getPersistence().removeAll(id);
        getIDPersistence().deleteContextID(csid);
        num++;
      } catch (Exception e) {
        logger.error("Clear context of id {} failed, {}", id.getContextId(), e.getMessage());
      }
    }
    return num;
  }
}
