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
import org.apache.linkis.cs.common.entity.data.LinkisJobData;
import org.apache.linkis.cs.common.entity.source.CommonContextValue;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextValue;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.exception.ErrorCode;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisJobDataServiceImpl implements LinkisJobDataService {

  private static final Logger logger = LoggerFactory.getLogger(LinkisJobDataServiceImpl.class);

  private SearchService searchService = DefaultSearchService.getInstance();

  private static LinkisJobDataService linkisJobDataService;

  private LinkisJobDataServiceImpl() {}

  public static LinkisJobDataService getInstance() {
    if (null == linkisJobDataService) {
      synchronized (LinkisJobDataServiceImpl.class) {
        if (null == linkisJobDataService) {
          linkisJobDataService = new LinkisJobDataServiceImpl();
        }
      }
    }
    return linkisJobDataService;
  }

  @Override
  public LinkisJobData getLinkisJobData(String contextIDStr, String contextKeyStr)
      throws CSErrorException {
    if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(contextKeyStr)) {
      logger.warn("contextIDStr or nodeName cannot null");
      return null;
    }
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      ContextKey contextKey = SerializeHelper.deserializeContextKey(contextKeyStr);
      LinkisJobData jobData =
          searchService.getContextValue(contextID, contextKey, LinkisJobData.class);
      if (null != jobData)
        logger.info(
            "contextID: {} and contextKeyStr: {} succeed to getLinkisJobData  {}",
            contextID.getContextId(),
            contextKeyStr,
            jobData.getJobID());
      return jobData;
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
  public void putLinkisJobData(
      String contextIDStr, String contextKeyStr, LinkisJobData linkisJobData)
      throws CSErrorException {
    ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      ContextKey contextKey = SerializeHelper.deserializeContextKey(contextKeyStr);
      ContextValue contextValue = new CommonContextValue();
      contextValue.setValue(linkisJobData);
      contextClient.update(contextID, contextKey, contextValue);
      logger.info(
          "contextID: {} and contextKeyStr: {} succeed to putLinkisJobData  {}",
          contextID.getContextId(),
          contextKeyStr,
          linkisJobData.getJobID());
    } catch (ErrorException e) {
      logger.error("Deserialize error. e ", e);
      throw new CSErrorException(
          ErrorCode.DESERIALIZE_ERROR, "Deserialize error. e : " + e.getDesc());
    }
  }
}
