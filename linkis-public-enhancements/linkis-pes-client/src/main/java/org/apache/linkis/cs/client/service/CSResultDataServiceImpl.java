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
import org.apache.linkis.cs.common.entity.data.CSResultData;
import org.apache.linkis.cs.common.entity.source.CommonContextValue;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextValue;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.exception.ErrorCode;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSResultDataServiceImpl implements CSResultDataService {

  private static final Logger logger = LoggerFactory.getLogger(CSResultDataServiceImpl.class);

  private SearchService searchService = DefaultSearchService.getInstance();

  private static CSResultDataService csResultDataService;

  private CSResultDataServiceImpl() {}

  public static CSResultDataService getInstance() {
    if (null == csResultDataService) {
      synchronized (CSResultDataServiceImpl.class) {
        if (null == csResultDataService) {
          csResultDataService = new CSResultDataServiceImpl();
        }
      }
    }
    return csResultDataService;
  }

  @Override
  public CSResultData getCSResultData(String contextIDStr, String contextKeyStr)
      throws CSErrorException {
    if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(contextKeyStr)) {
      logger.warn("contextIDStr or nodeName cannot null");
      return null;
    }
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      ContextKey contextKey = SerializeHelper.deserializeContextKey(contextKeyStr);
      return searchService.getContextValue(contextID, contextKey, CSResultData.class);
    } catch (ErrorException e) {
      logger.error(
          "Deserialize failed, invalid contextId : "
              + contextIDStr
              + ", or contextKey : "
              + contextKeyStr
              + ", e : "
              + e.getMessage());
      logger.error("exception ", e);
      throw new CSErrorException(
          ErrorCode.DESERIALIZE_ERROR,
          "Deserialize failed, invalid contextId : "
              + contextIDStr
              + ", or contextKey : "
              + contextKeyStr
              + ", e : "
              + e.getMessage());
    }
  }

  @Override
  public void putCSResultData(String contextIDStr, String contextKeyStr, CSResultData csResultData)
      throws CSErrorException {
    ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      ContextKey contextKey = SerializeHelper.deserializeContextKey(contextKeyStr);
      ContextValue contextValue = new CommonContextValue();
      contextValue.setValue(csResultData);
      contextClient.update(contextID, contextKey, contextValue);
      logger.info(
          "succeed to update cs result data,contextIDStr: {}, contextKeyStr: {}",
          contextKey,
          contextKeyStr);
    } catch (ErrorException e) {
      logger.error("Deserialize error. e ", e);
      throw new CSErrorException(
          ErrorCode.DESERIALIZE_ERROR, "Deserialize error. e : " + e.getDesc());
    }
  }

  @Override
  public List<CSResultData> getUpstreamCSResultData(String contextIDStr, String nodeName)
      throws CSErrorException {
    List<CSResultData> rsList = new ArrayList<>();
    if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeName)) {
      logger.warn("contextIDStr or nodeName cannot null");
      return rsList;
    }
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      if (null != contextID) {
        rsList =
            searchService.searchUpstreamContext(
                contextID, nodeName, Integer.MAX_VALUE, CSResultData.class);
      }
      return rsList;
    } catch (ErrorException e) {
      logger.error("Deserialize contextID error. contextIDStr : " + contextIDStr, e);
      throw new CSErrorException(
          ErrorCode.DESERIALIZE_ERROR,
          "Deserialize contextID error. contextIDStr : " + contextIDStr + "e : " + e.getDesc());
    }
  }
}
