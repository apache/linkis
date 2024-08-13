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

package org.apache.linkis.cs.execution.ruler;

import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.object.CSFlowInfos;
import org.apache.linkis.cs.common.entity.source.CommonContextKey;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.utils.CSCommonUtils;
import org.apache.linkis.cs.condition.impl.NearestCondition;
import org.apache.linkis.cs.contextcache.ContextCacheService;
import org.apache.linkis.cs.execution.matcher.ContextSearchMatcher;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NearestContextSearchRuler extends AbstractContextSearchRuler {

  private static Logger logger = LoggerFactory.getLogger(NearestContextSearchRuler.class);

  static int LARGE_INT = 100000;
  static int SMALL_INT = -100000;
  static String DOT = ".";

  ContextCacheService contextCacheService;
  ContextID contextID;
  NearestCondition nearestCondition;
  CommonListContextSearchRuler commonListContextSearchRuler;

  public NearestContextSearchRuler(
      ContextSearchMatcher matcher,
      ContextCacheService contextCacheService,
      ContextID contextID,
      NearestCondition condition) {
    super(matcher);
    this.contextCacheService = contextCacheService;
    this.contextID = contextID;
    this.nearestCondition = condition;
    this.commonListContextSearchRuler = new CommonListContextSearchRuler(matcher);
  }

  @Override
  public List<ContextKeyValue> rule(List<ContextKeyValue> contextKeyValues) {
    contextKeyValues = commonListContextSearchRuler.rule(contextKeyValues);
    List<ContextKeyValue> filtered = Lists.newArrayList();
    ContextKey flowInfosKey = new CommonContextKey();
    flowInfosKey.setContextType(ContextType.OBJECT);
    flowInfosKey.setKey(CSCommonUtils.FLOW_INFOS);
    ContextKeyValue flowInfos = contextCacheService.get(contextID, flowInfosKey);

    if (null != flowInfos && flowInfos.getContextValue().getValue() instanceof CSFlowInfos) {
      CSFlowInfos csFlowInfos = (CSFlowInfos) flowInfos.getContextValue().getValue();
      logger.info(
          "Calculate nearest nodes based on flow info: \n"
              + BDPJettyServerHelper.gson().toJson(csFlowInfos));
      List<Map<String, String>> edges =
          (List<Map<String, String>>) csFlowInfos.getInfos().get("edges");
      Map<String, String> idNodeName =
          (Map<String, String>) csFlowInfos.getInfos().get(CSCommonUtils.ID_NODE_NAME);
      String[] fullNodeName = StringUtils.split(nearestCondition.getCurrentNode(), DOT);
      String currentNodeName = fullNodeName[fullNodeName.length - 1];
      List<ContextKeyValue> notUpstream = Lists.newArrayList();

      contextKeyValues.sort(
          new Comparator<ContextKeyValue>() {
            @Override
            public int compare(ContextKeyValue o1, ContextKeyValue o2) {
              return distance(o1) - distance(o2);
            }

            private int distance(ContextKeyValue contextKeyValue) {
              String nodeNameToCalc =
                  StringUtils.substringBetween(
                      contextKeyValue.getContextKey().getKey(), CSCommonUtils.NODE_PREFIX, DOT);
              if (StringUtils.isBlank(nodeNameToCalc)) {
                return LARGE_INT;
              }
              if (nodeNameToCalc.equals(currentNodeName)) {
                return 0;
              }
              int upDistance = upstreamDistance(nodeNameToCalc);
              if (nearestCondition.getUpstreamOnly()) {
                if (upDistance >= LARGE_INT) {
                  notUpstream.add(contextKeyValue);
                }
                return upDistance;
              } else {
                int downDistance = downstreamDistance(nodeNameToCalc);
                return Integer.min(upDistance, downDistance);
              }
            }

            private int upstreamDistance(String nodeNameToCalc) {
              int minDistance = LARGE_INT;
              for (Map<String, String> edge : edges) {
                if (getNodeName(edge.get("source")).equals(nodeNameToCalc)) {
                  if (getNodeName(edge.get("target")).equals(currentNodeName)) {
                    return 1;
                  } else {
                    minDistance =
                        Integer.min(
                            minDistance, upstreamDistance(getNodeName(edge.get("target"))) + 1);
                  }
                }
              }
              return minDistance;
            }

            private int downstreamDistance(String nodeNameToCalc) {
              int minDistance = LARGE_INT;
              for (Map<String, String> edge : edges) {
                if (getNodeName(edge.get("target")).equals(nodeNameToCalc)) {
                  if (getNodeName(edge.get("source")).equals(currentNodeName)) {
                    return 1;
                  } else {
                    minDistance = downstreamDistance(getNodeName(edge.get("source"))) + 1;
                  }
                }
              }
              return minDistance;
            }

            private String getNodeName(String nodeId) {
              if (StringUtils.isBlank(nodeId) || null == idNodeName) {
                return "";
              }
              return idNodeName.get(nodeId);
            }
          });
      if (nearestCondition.getUpstreamOnly()) {
        contextKeyValues.removeAll(notUpstream);
      }
      int endIndex =
          nearestCondition.getNumber() < contextKeyValues.size()
              ? nearestCondition.getNumber()
              : contextKeyValues.size();
      filtered = contextKeyValues.subList(0, endIndex);
      logger.info(
          "Finished to NearestContextSearchRuler nearest nodes based on flow info: \n, filtered size {}"
              + BDPJettyServerHelper.gson().toJson(csFlowInfos),
          filtered.size());
    }
    return filtered;
  }
}
