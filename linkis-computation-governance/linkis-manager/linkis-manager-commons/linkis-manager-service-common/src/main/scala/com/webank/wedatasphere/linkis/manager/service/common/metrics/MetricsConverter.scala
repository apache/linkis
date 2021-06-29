package com.webank.wedatasphere.linkis.manager.service.common.metrics

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.{NodeHealthy, NodeStatus}
import com.webank.wedatasphere.linkis.manager.common.entity.metrics._
import com.webank.wedatasphere.linkis.manager.common.entity.node.AMNode


trait MetricsConverter {

  def parseTaskInfo(nodeMetrics: NodeMetrics): NodeTaskInfo

  def parseHealthyInfo(nodeMetrics: NodeMetrics): NodeHealthyInfo

  def parseOverLoadInfo(nodeMetrics: NodeMetrics): NodeOverLoadInfo

  def parseStatus(nodeMetrics: NodeMetrics): NodeStatus

  def convertTaskInfo(nodeTaskInfo: NodeTaskInfo): String

  def convertHealthyInfo(nodeHealthyInfo: NodeHealthyInfo): String

  def convertOverLoadInfo(nodeOverLoadInfo: NodeOverLoadInfo): String

  def convertStatus(nodeStatus: NodeStatus): Int

  def fillMetricsToNode(amNode: AMNode, metrics: NodeMetrics): AMNode

  def getInitMetric(serviceInstance: ServiceInstance): NodeMetrics = {
    val nodeMetrics: AMNodeMetrics = new AMNodeMetrics

    nodeMetrics.setStatus(NodeStatus.Starting.ordinal)

    val nodeHealthyInfo: NodeHealthyInfo = new NodeHealthyInfo
    nodeHealthyInfo.setNodeHealthy(NodeHealthy.Healthy)
    nodeMetrics.setHealthy(convertHealthyInfo(nodeHealthyInfo))
    nodeMetrics.setServiceInstance(serviceInstance)
    nodeMetrics
  }
}
