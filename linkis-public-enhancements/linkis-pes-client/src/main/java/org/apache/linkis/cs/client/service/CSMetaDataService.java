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
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.cs.client.utils.SerializeHelper;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.exception.ErrorCode;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSMetaDataService implements MetaDataService {

  private static final Logger logger = LoggerFactory.getLogger(CSMetaDataService.class);

  private static CSMetaDataService csMetaDataService;

  private CSMetaDataService() {}

  public static CSMetaDataService getInstance() {
    if (null == csMetaDataService) {
      synchronized (CSMetaDataService.class) {
        if (null == csMetaDataService) {
          csMetaDataService = new CSMetaDataService();
        }
      }
    }
    return csMetaDataService;
  }

  @Override
  public Map<ContextKey, MetaData> getAllUpstreamMetaData(String contextIDStr, String nodeName)
      throws CSErrorException {
    if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeName)) {
      logger.warn("contextIDStr or nodeName cannot null");
      return null;
    }
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      return DefaultSearchService.getInstance()
          .searchUpstreamContextMap(contextID, nodeName, Integer.MAX_VALUE, MetaData.class);
    } catch (ErrorException e) {
      logger.error("Deserialize contextid error. contextID : " + contextIDStr + ", e ", e);
      throw new CSErrorException(
          ErrorCode.DESERIALIZE_ERROR,
          "Deserialize contextid error. contextID : " + contextIDStr + ", e " + e.getDesc());
    }
  }
}
