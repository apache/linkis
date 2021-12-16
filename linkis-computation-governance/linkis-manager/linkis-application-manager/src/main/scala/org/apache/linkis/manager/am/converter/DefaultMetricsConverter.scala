/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.manager.am.converter

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.metrics.{NodeHealthyInfo, NodeMetrics, NodeOverLoadInfo, NodeTaskInfo}
import org.apache.linkis.manager.common.entity.node.AMNode
import org.apache.linkis.manager.service.common.metrics.MetricsConverter
import org.apache.linkis.server.BDPJettyServerHelper
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component


@Component
class DefaultMetricsConverter extends MetricsConverter with Logging {

  override def parseTaskInfo(nodeMetrics: NodeMetrics): NodeTaskInfo = {
    val msg = nodeMetrics.getHeartBeatMsg
    if (StringUtils.isNotBlank(msg)) {
      val jsonNode = BDPJettyServerHelper.jacksonJson.readTree(msg)
      if (jsonNode != null && jsonNode.has("taskInfo")) {
        val taskInfo = BDPJettyServerHelper.jacksonJson.readValue(jsonNode.get("taskInfo").asText(), classOf[NodeTaskInfo])
        return taskInfo
      }
    }
    null
  }

  override def parseHealthyInfo(nodeMetrics: NodeMetrics): NodeHealthyInfo = {
    val healthyInfo = nodeMetrics.getHealthy
    if (StringUtils.isNotBlank(healthyInfo)) {
      BDPJettyServerHelper.jacksonJson.readValue(healthyInfo, classOf[NodeHealthyInfo])
    } else {
      null
    }
  }

  override def parseOverLoadInfo(nodeMetrics: NodeMetrics): NodeOverLoadInfo = {
    val overLoad = nodeMetrics.getOverLoad
    if (StringUtils.isNotBlank(overLoad)) {
      BDPJettyServerHelper.jacksonJson.readValue(overLoad, classOf[NodeOverLoadInfo])
    } else {
      null
    }
  }

  override def parseStatus(nodeMetrics: NodeMetrics): NodeStatus = {
    NodeStatus.values()(nodeMetrics.getStatus)
  }


  override def convertTaskInfo(nodeTaskInfo: NodeTaskInfo): String = {
    BDPJettyServerHelper.jacksonJson.writeValueAsString(nodeTaskInfo)
  }

  override def convertHealthyInfo(nodeHealthyInfo: NodeHealthyInfo): String = {
    BDPJettyServerHelper.jacksonJson.writeValueAsString(nodeHealthyInfo)
  }

  override def convertOverLoadInfo(nodeOverLoadInfo: NodeOverLoadInfo): String = {
    BDPJettyServerHelper.jacksonJson.writeValueAsString(nodeOverLoadInfo)
  }

  override def convertStatus(nodeStatus: NodeStatus): Int = {
    nodeStatus.ordinal()
  }

  override def fillMetricsToNode(amNode: AMNode, metrics: NodeMetrics): AMNode = {
    if(metrics == null) return amNode
    amNode.setNodeStatus(parseStatus(metrics))
    amNode.setNodeTaskInfo(parseTaskInfo(metrics))
    amNode.setNodeHealthyInfo(parseHealthyInfo(metrics))
    amNode.setNodeOverLoadInfo(parseOverLoadInfo(metrics))
    amNode.setUpdateTime(metrics.getUpdateTime)
    amNode
  }
}
