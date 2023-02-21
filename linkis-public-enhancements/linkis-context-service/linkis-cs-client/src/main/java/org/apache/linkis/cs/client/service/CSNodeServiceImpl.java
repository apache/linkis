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

import org.apache.linkis.cs.client.ContextClient;
import org.apache.linkis.cs.client.builder.ContextClientFactory;
import org.apache.linkis.cs.client.utils.SerializeHelper;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.utils.CSCommonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSNodeServiceImpl implements CSNodeService {

  private static final Logger logger = LoggerFactory.getLogger(CSNodeServiceImpl.class);

  private SearchService searchService = DefaultSearchService.getInstance();

  private static CSNodeService csNodeService;

  private CSNodeServiceImpl() {}

  public static CSNodeService getInstance() {
    if (null == csNodeService) {
      synchronized (CSNodeServiceImpl.class) {
        if (null == csNodeService) {
          csNodeService = new CSNodeServiceImpl();
        }
      }
    }
    return csNodeService;
  }

  @Override
  public void initNodeCSInfo(String contextIDStr, String ndeName) {

    try {
      ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      contextClient.removeAllValueByKeyPrefixAndContextType(
          contextID, ContextType.METADATA, CSCommonUtils.NODE_PREFIX + ndeName + ".");
      logger.info("contextIDStr: {} and  nodeName: {} init cs info", contextIDStr, ndeName);
    } catch (Exception e) {
      logger.error("Failed to init node cs Info", e);
    }
  }
}
