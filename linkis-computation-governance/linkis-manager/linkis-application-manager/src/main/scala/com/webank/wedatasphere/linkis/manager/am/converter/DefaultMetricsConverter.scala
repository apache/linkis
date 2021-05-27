package com.webank.wedatasphere.linkis.manager.am.converter

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.{NodeHealthyInfo, NodeMetrics, NodeOverLoadInfo, NodeTaskInfo}
import com.webank.wedatasphere.linkis.manager.common.entity.node.AMNode
import com.webank.wedatasphere.linkis.manager.service.common.metrics.MetricsConverter
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
  * @author peacewong
  * @date 2020/7/9 15:31
  */
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
    amNode
  }
}
