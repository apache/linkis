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
import org.apache.linkis.cs.client.utils.SerializeHelper;
import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.metadata.CSTable;
import org.apache.linkis.cs.common.entity.source.CombinedNodeIDContextID;
import org.apache.linkis.cs.common.entity.source.CommonContextKey;
import org.apache.linkis.cs.common.entity.source.CommonContextValue;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.entity.source.ContextValue;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.exception.ErrorCode;
import org.apache.linkis.cs.common.utils.CSCommonUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSTableService implements TableService {

  private static final Logger logger = LoggerFactory.getLogger(CSTableService.class);

  private SearchService searchService = DefaultSearchService.getInstance();

  private static CSTableService csTableService;

  private CSTableService() {}

  public static CSTableService getInstance() {
    if (null == csTableService) {
      synchronized (CSTableService.class) {
        if (null == csTableService) {
          csTableService = new CSTableService();
        }
      }
    }
    return csTableService;
  }

  @Override
  public CSTable getCSTable(ContextID contextID, ContextKey contextKey) throws CSErrorException {
    if (null == contextID || null == contextKey) {
      logger.warn("contextIDStr or nodeName cannot null");
      return null;
    }
    if (contextID instanceof CombinedNodeIDContextID) {
      contextID = ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID();
    }
    CSTable csTable = searchService.getContextValue(contextID, contextKey, CSTable.class);
    if (null != csTable)
      logger.info(
          "contextID: {} and contextKey: {} succeed to get table tableName {}",
          contextID.getContextId(),
          contextKey.getKey(),
          csTable.getName());
    return csTable;
  }

  @Override
  public List<CSTable> getUpstreamTables(String contextIDStr, String nodeName)
      throws CSErrorException {
    List<CSTable> rsList = new ArrayList<>();
    if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeName)) {
      logger.warn("contextIDStr or nodeName cannot null");
      return rsList;
    }
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      if (null != contextID) {
        if (contextID instanceof CombinedNodeIDContextID) {
          contextID = ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID();
        }
        rsList =
            searchService.searchUpstreamContext(
                contextID, nodeName, Integer.MAX_VALUE, CSTable.class);
      }
      if (null != rsList)
        logger.info(
            "contextID: {} and nodeName: {} succeed to get tables size {}",
            contextID.getContextId(),
            nodeName,
            rsList.size());
      return rsList;
    } catch (ErrorException e) {
      logger.error("Deserialize contextID error. contextIDStr : " + contextIDStr);
      throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "getUpstreamTables error ", e);
    }
  }

  @Override
  public CSTable getUpstreamSuitableTable(String contextIDStr, String nodeName, String keyword)
      throws CSErrorException {
    CSTable csTable = null;
    if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeName)) {
      logger.warn("contextIDStr or nodeName cannot null");
      return csTable;
    }
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      if (null != contextID) {
        if (contextID instanceof CombinedNodeIDContextID) {
          contextID = ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID();
        }
        csTable = searchService.searchContext(contextID, keyword, nodeName, CSTable.class);
        if (null != csTable)
          logger.info(
              "contextID: {} , nodeName: {}, keyword {} succeed to getUpstreamSuitableTable tableName {}",
              contextID.getContextId(),
              nodeName,
              keyword,
              csTable.getName());
      }
    } catch (ErrorException e) {
      throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "getUpstreamSuitableTable error ", e);
    }
    return csTable;
  }

  @Override
  public List<ContextKeyValue> searchUpstreamTableKeyValue(String contextIDStr, String nodeName)
      throws CSErrorException {
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      if (contextID instanceof CombinedNodeIDContextID) {
        contextID = ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID();
      }
      return searchService.searchUpstreamKeyValue(
          contextID, nodeName, Integer.MAX_VALUE, CSTable.class);
    } catch (ErrorException e) {
      throw new CSErrorException(
          ErrorCode.DESERIALIZE_ERROR, "Failed to searchUpstreamTableKeyValue ", e);
    }
  }

  @Override
  public void putCSTable(String contextIDStr, String contextKeyStr, CSTable csTable)
      throws CSErrorException {
    ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      ContextKey contextKey = SerializeHelper.deserializeContextKey(contextKeyStr);
      ContextValue contextValue = new CommonContextValue();
      // todo check keywords
      contextValue.setKeywords("");
      contextValue.setValue(csTable);
      if (contextID instanceof CombinedNodeIDContextID) {
        contextID = ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID();
      }
      contextClient.update(contextID, contextKey, contextValue);
      logger.info(
          "contextID: {} , contextKeyStr: {} succeed to putCSTable tableName {}",
          contextID.getContextId(),
          contextKeyStr,
          csTable.getName());
    } catch (ErrorException e) {
      throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "putCSTable error ", e);
    }
  }

  @Override
  public CSTable getCSTable(String contextIDStr, String contextKeyStr) throws CSErrorException {
    if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(contextKeyStr)) {
      return null;
    }
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      ContextKey contextKey = SerializeHelper.deserializeContextKey(contextKeyStr);
      if (contextID instanceof CombinedNodeIDContextID) {
        contextID = ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID();
      }
      return getCSTable(contextID, contextKey);
    } catch (ErrorException e) {
      throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "getCSTable error ", e);
    }
  }

  @Override
  public void registerCSTable(String contextIDStr, String nodeName, String alias, CSTable csTable)
      throws CSErrorException {

    if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeName)) {
      return;
    }
    String tableName = "";
    if (StringUtils.isNotBlank(alias)) {
      tableName = CSCommonUtils.CS_TMP_TABLE_PREFIX + nodeName + "_" + alias;
    } else {
      for (int i = 1; i < 10; i++) {
        String tmpTable = CSCommonUtils.CS_TMP_TABLE_PREFIX + nodeName + "_rs" + i;
        try {
          ContextKey contextKey = new CommonContextKey();
          contextKey.setContextScope(ContextScope.PUBLIC);
          contextKey.setContextType(ContextType.METADATA);
          contextKey.setKey(CSCommonUtils.getTableKey(nodeName, tmpTable));
          CSTable oldCsTable =
              getCSTable(contextIDStr, SerializeHelper.serializeContextKey(contextKey));
          if (null == oldCsTable) {
            tableName = tmpTable;
            break;
          }
        } catch (Exception e) {
          tableName = tmpTable;
          logger.warn("Failed to build tmp tableName", e);
          break;
        }
      }
    }
    try {
      csTable.setName(tableName);
      ContextKey contextKey = new CommonContextKey();
      contextKey.setContextScope(ContextScope.PUBLIC);
      contextKey.setContextType(ContextType.METADATA);
      contextKey.setKey(CSCommonUtils.getTableKey(nodeName, tableName));
      putCSTable(contextIDStr, SerializeHelper.serializeContextKey(contextKey), csTable);
    } catch (ErrorException e) {
      throw new CSErrorException(
          ErrorCode.DESERIALIZE_ERROR, "Failed to register cs tmp table ", e);
    }
  }
}
