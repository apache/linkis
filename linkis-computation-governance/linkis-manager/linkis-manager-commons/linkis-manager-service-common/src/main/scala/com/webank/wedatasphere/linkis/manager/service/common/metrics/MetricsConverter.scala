/*
 *
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
