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

package org.apache.linkis.manager.am.converter;

import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.metrics.NodeHealthyInfo;
import org.apache.linkis.manager.common.entity.metrics.NodeMetrics;
import org.apache.linkis.manager.common.entity.metrics.NodeOverLoadInfo;
import org.apache.linkis.manager.common.entity.metrics.NodeTaskInfo;
import org.apache.linkis.manager.common.entity.node.AMNode;
import org.apache.linkis.manager.service.common.metrics.MetricsConverter;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Component;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DefaultMetricsConverter implements MetricsConverter {

  private static final Logger logger = LoggerFactory.getLogger(DefaultMetricsConverter.class);

  @Override
  public NodeTaskInfo parseTaskInfo(NodeMetrics nodeMetrics) {
    String msg = nodeMetrics.getHeartBeatMsg();
    if (StringUtils.isNotBlank(msg)) {
      try {
        JsonNode jsonNode = BDPJettyServerHelper.jacksonJson().readTree(msg);
        if (jsonNode != null && jsonNode.has("taskInfo")) {
          NodeTaskInfo taskInfo =
              BDPJettyServerHelper.jacksonJson()
                  .readValue(jsonNode.get("taskInfo").asText(), NodeTaskInfo.class);
          return taskInfo;
        }
      } catch (IOException e) {
        logger.error("parse task info failed", e);
      }
    }
    return null;
  }

  @Override
  public NodeHealthyInfo parseHealthyInfo(NodeMetrics nodeMetrics) {
    String healthyInfo = nodeMetrics.getHealthy();
    if (StringUtils.isNotBlank(healthyInfo)) {
      try {
        return BDPJettyServerHelper.jacksonJson().readValue(healthyInfo, NodeHealthyInfo.class);
      } catch (IOException e) {
        logger.error("parse healthy info failed", e);
      }
    }
    return null;
  }

  @Override
  public NodeOverLoadInfo parseOverLoadInfo(NodeMetrics nodeMetrics) {
    String overLoad = nodeMetrics.getOverLoad();
    if (StringUtils.isNotBlank(overLoad)) {
      try {
        return BDPJettyServerHelper.jacksonJson().readValue(overLoad, NodeOverLoadInfo.class);
      } catch (IOException e) {
        logger.error("parse over load info failed", e);
      }
    }
    return null;
  }

  @Override
  public NodeStatus parseStatus(NodeMetrics nodeMetrics) {
    return NodeStatus.values()[nodeMetrics.getStatus()];
  }

  @Override
  public String convertTaskInfo(NodeTaskInfo nodeTaskInfo) {
    try {
      return BDPJettyServerHelper.jacksonJson().writeValueAsString(nodeTaskInfo);
    } catch (JsonProcessingException e) {
      logger.error("convert task info failed", e);
    }
    return null;
  }

  @Override
  public String convertHealthyInfo(NodeHealthyInfo nodeHealthyInfo) {
    try {
      return BDPJettyServerHelper.jacksonJson().writeValueAsString(nodeHealthyInfo);
    } catch (JsonProcessingException e) {
      logger.error("convert healthy info failed", e);
    }
    return null;
  }

  @Override
  public String convertOverLoadInfo(NodeOverLoadInfo nodeOverLoadInfo) {
    try {
      return BDPJettyServerHelper.jacksonJson().writeValueAsString(nodeOverLoadInfo);
    } catch (JsonProcessingException e) {
      logger.error("convert over load info failed", e);
    }
    return null;
  }

  @Override
  public int convertStatus(NodeStatus nodeStatus) {
    return nodeStatus.ordinal();
  }

  @Override
  public AMNode fillMetricsToNode(AMNode amNode, NodeMetrics metrics) {
    if (metrics == null) return amNode;
    amNode.setNodeStatus(parseStatus(metrics));
    amNode.setNodeTaskInfo(parseTaskInfo(metrics));
    amNode.setNodeHealthyInfo(parseHealthyInfo(metrics));
    amNode.setNodeOverLoadInfo(parseOverLoadInfo(metrics));
    amNode.setUpdateTime(metrics.getUpdateTime());
    return amNode;
  }
}
