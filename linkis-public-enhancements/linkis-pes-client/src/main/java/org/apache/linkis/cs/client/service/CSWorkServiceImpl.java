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
import org.apache.linkis.cs.common.entity.enumeration.WorkType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.exception.ErrorCode;
import org.apache.linkis.cs.common.utils.CSCommonUtils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSWorkServiceImpl implements CSWorkService {

  private static final Logger logger = LoggerFactory.getLogger(CSWorkServiceImpl.class);

  private CSWorkServiceImpl() {}

  private static CSWorkService csWorkService = null;

  public static CSWorkService getInstance() {
    if (null == csWorkService) {
      synchronized (CSWorkServiceImpl.class) {
        if (null == csWorkService) {
          csWorkService = new CSWorkServiceImpl();
        }
      }
    }
    return csWorkService;
  }

  @Override
  public void initContextServiceInfo(String contextIDStr, WorkType workType)
      throws CSErrorException {
    List<WorkType> typeList = new ArrayList<>();
    typeList.add(workType);
    initContextServiceInfo(contextIDStr, typeList);
  }

  @Override
  public void initContextServiceInfo(String contextIDStr, List<WorkType> workTypes)
      throws CSErrorException {
    try {
      ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      for (WorkType type : workTypes) {
        contextClient.removeAllValueByKeyPrefix(contextID, getWorkTypePrefix(type));
      }
      logger.info("contextID: {} initContextServiceInfo", contextID.getContextId());
    } catch (Exception e) {
      logger.error(
          "InitContextInfo error. contextIDStr : {}, workTypes : {}" + contextIDStr,
          CSCommonUtils.gson.toJson(workTypes));
      throw new CSErrorException(
          ErrorCode.DESERIALIZE_ERROR,
          "InitContextInfo error. contextIDStr : "
              + contextIDStr
              + ", workTypes : "
              + CSCommonUtils.gson.toJson(workTypes),
          e);
    }
  }

  private String getWorkTypePrefix(WorkType workType) {
    String prefix = null;
    switch (workType) {
      case WORKSPACE:
        prefix = CSCommonUtils.WORKSPACE_PREFIX;
        break;
      case PROJECT:
        prefix = CSCommonUtils.PROJECT_PREFIX;
        break;
      case FLOW:
        prefix = CSCommonUtils.FLOW_PREFIX;
        break;
      case NODE:
        prefix = CSCommonUtils.NODE_PREFIX;
        break;
      default:
        logger.error("Invalid workType : {}", workType);
    }
    return prefix;
  }
}
