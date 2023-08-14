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

package org.apache.linkis.cs.client.utils;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.cs.client.service.DefaultSearchService;
import org.apache.linkis.cs.client.service.SearchService;
import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.object.CSFlowInfos;
import org.apache.linkis.cs.common.entity.source.CombinedNodeIDContextID;
import org.apache.linkis.cs.common.entity.source.CommonContextKey;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.utils.CSCommonUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextServiceUtils {

  private static final Logger logger = LoggerFactory.getLogger(ContextServiceUtils.class);

  /** TODO get instances */
  private static SearchService commonSearchService = DefaultSearchService.getInstance();

  public static String getContextIDStrByMap(Map<String, Object> map) {
    String contextIDStr = null;
    if (null != map) {
      Object value = map.get(CSCommonUtils.CONTEXT_ID_STR);
      if (null != value) {
        contextIDStr = value.toString();
        try {
          ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
          // commonSearchService.getContextValue()
          if (contextID instanceof CombinedNodeIDContextID) {
            contextIDStr =
                SerializeHelper.serializeContextID(
                    ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID());
          }
        } catch (ErrorException e) {
          logger.info("Failed to deserializeContextID", e);
        }
      }
    }
    return contextIDStr;
  }

  public static String getNodeNameStrByMap(Map<String, Object> map) {
    Object contextIDValue = map.get(CSCommonUtils.CONTEXT_ID_STR);
    if (null == contextIDValue) {
      return null;
    }

    String nodeName = null;
    try {
      if (null != map) {
        Object value = map.get(CSCommonUtils.NODE_NAME_STR);
        if (null != value) {
          nodeName = value.toString();
        }
      }
      if (StringUtils.isBlank(nodeName)) {
        nodeName = getNodeNameByCombinedNodeIDContextID(contextIDValue.toString());
      }
    } catch (Exception e) {
      logger.info("Failed to get nodeName", e);
    }
    map.put(CSCommonUtils.NODE_NAME_STR, nodeName);
    return nodeName;
  }

  public static String getContextIDStrByProperties(Properties properties) {
    String contextIDStr = null;
    if (null != properties) {
      Object value = properties.get(CSCommonUtils.CONTEXT_ID_STR);
      if (null != value) {
        contextIDStr = value.toString();
        try {
          ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
          // commonSearchService.getContextValue()
          if (contextID instanceof CombinedNodeIDContextID) {
            contextIDStr =
                SerializeHelper.serializeContextID(
                    ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID());
          }
        } catch (ErrorException e) {
          logger.info("Failed to deserializeContextID", e);
        }
      }
    }
    return contextIDStr;
  }

  public static String getNodeNameStrByProperties(Properties properties) {
    Object contextIDValue = properties.get(CSCommonUtils.CONTEXT_ID_STR);
    if (null == contextIDValue) {
      return null;
    }
    String nodeName = null;

    if (null != properties) {
      Object value = properties.get(CSCommonUtils.NODE_NAME_STR);
      if (null != value) {
        nodeName = value.toString();
      }
    }
    if (StringUtils.isBlank(nodeName)) {
      nodeName = getNodeNameByCombinedNodeIDContextID(contextIDValue.toString());
    }
    properties.put(CSCommonUtils.NODE_NAME_STR, nodeName);
    return nodeName;
  }

  public static String getNodeNameByCombinedNodeIDContextID(String contextIDStr) {
    String nodeName = null;
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      // commonSearchService.getContextValue()
      if (contextID instanceof CombinedNodeIDContextID) {
        logger.info("contextID{} is combinedNodeIDContextID", contextID.getContextId());
        String nodeID = ((CombinedNodeIDContextID) contextID).getNodeID();
        return getNodeNameByNodeID(
            SerializeHelper.serializeContextID(
                ((CombinedNodeIDContextID) contextID).getLinkisHaWorkFlowContextID()),
            nodeID);
      }
    } catch (Exception e) {
      logger.info("Failed to get nodeName", e);
    }
    return nodeName;
  }

  public static String getNodeNameByNodeID(String contextIDStr, String nodeID) {
    if (StringUtils.isBlank(contextIDStr) || StringUtils.isBlank(nodeID)) {
      return null;
    }
    try {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      ContextKey contextKey = new CommonContextKey();
      contextKey.setContextType(ContextType.OBJECT);
      contextKey.setContextScope(ContextScope.PUBLIC);
      contextKey.setKey(CSCommonUtils.FLOW_INFOS);
      CSFlowInfos csFlowInfos =
          commonSearchService.getContextValue(contextID, contextKey, CSFlowInfos.class);
      if (null != csFlowInfos && null != csFlowInfos.getInfos()) {
        Object idName = csFlowInfos.getInfos().get(CSCommonUtils.ID_NODE_NAME);
        if (null != idName) {
          return ((Map<String, String>) idName).get(nodeID);
        }
      }
    } catch (ErrorException e) {
      logger.info("Failed to get nodeName ", e);
    }
    return null;
  }

  public static String createCombinedNodeIDContextID(String contextIDStr, String nodeID)
      throws ErrorException {
    if (StringUtils.isNotBlank(contextIDStr) && StringUtils.isNotBlank(nodeID)) {
      ContextID contextID = SerializeHelper.deserializeContextID(contextIDStr);
      if (null != contextID) {
        return SerializeHelper.serializeContextID(new CombinedNodeIDContextID(contextID, nodeID));
      }
    }
    return null;
  }

  /* public static String[] getContextIDAndNodeName(Map<String, Object> map){
      String contextIDStr = getContextIDStrByMap(map);
      if (StringUtils.isBlank(contextIDStr)) {
          return  null;
      }

  }*/
}
