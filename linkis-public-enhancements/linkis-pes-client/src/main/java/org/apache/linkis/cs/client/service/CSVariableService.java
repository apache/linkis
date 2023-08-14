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
import org.apache.linkis.cs.common.entity.object.LinkisVariable;
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

public class CSVariableService implements VariableService {

  private static final Logger logger = LoggerFactory.getLogger(CSVariableService.class);

  private SearchService searchService = DefaultSearchService.getInstance();

  private static CSVariableService csVariableService;

  private CSVariableService() {}

  @Override
  public List<LinkisVariable> getUpstreamVariables(String contextIDStr, String nodeName)
      throws CSErrorException {
    List<LinkisVariable> rsList = new ArrayList<>();
    if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeName)) {
      logger.warn("contextIDStr or nodeName cannot null");
      return rsList;
    }
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      if (null != contextID) {
        rsList =
            searchService.searchUpstreamContext(
                contextID, nodeName, Integer.MAX_VALUE, LinkisVariable.class);
        if (null != rsList)
          logger.info(
              "contextID: {} and nodeName: {} succeed to getUpstreamVariables size {}",
              contextID.getContextId(),
              nodeName,
              rsList.size());
      }
      return rsList;
    } catch (Throwable e) {
      logger.error("Failed to get variable : " + contextIDStr, e);
      // throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "Failed to get variable : " +
      // contextIDStr + "e : " + e.getMessage());
    }
    return rsList;
  }

  @Override
  public void putVariable(String contextIDStr, String contextKeyStr, LinkisVariable linkisVariable)
      throws CSErrorException {
    ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      ContextKey contextKey = SerializeHelper.deserializeContextKey(contextKeyStr);
      ContextValue contextValue = new CommonContextValue();
      contextValue.setValue(linkisVariable);
      contextClient.update(contextID, contextKey, contextValue);
      logger.info(
          "contextID: {} and contextKeyStr: {} succeed to putVariable {}",
          contextID.getContextId(),
          contextKeyStr,
          linkisVariable.getValue());
    } catch (ErrorException e) {
      logger.error("Deserialize error. e ");
      throw new CSErrorException(ErrorCode.DESERIALIZE_ERROR, "Deserialize error. e : ", e);
    }
  }

  public static CSVariableService getInstance() {
    if (null == csVariableService) {
      synchronized (CSVariableService.class) {
        if (null == csVariableService) {
          csVariableService = new CSVariableService();
        }
      }
    }
    return csVariableService;
  }
}
