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

package org.apache.linkis.manager.am.service.em

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.manager.am.manager.EMNodeManager
import org.apache.linkis.manager.common.entity.metrics.NodeHealthyInfo
import org.apache.linkis.manager.common.entity.node.{AMEMNode, EMNode}
import org.apache.linkis.manager.common.protocol.em.GetEMInfoRequest
import org.apache.linkis.manager.common.protocol.node.NodeHealthyRequest
import org.apache.linkis.manager.label.entity.node.AliasServiceInstanceLabel
import org.apache.linkis.manager.label.service.NodeLabelService
import org.apache.linkis.manager.persistence.NodeMetricManagerPersistence
import org.apache.linkis.manager.rm.service.ResourceManager
import org.apache.linkis.manager.service.common.metrics.MetricsConverter
import org.apache.linkis.manager.service.common.pointer.NodePointerBuilder
import org.apache.linkis.rpc.message.annotation.Receiver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

@Service
class DefaultEMInfoService extends EMInfoService with Logging {

  @Autowired
  private var emNodeManager: EMNodeManager = _

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Autowired
  private var resourceManager: ResourceManager = _

  @Autowired
  private var nodeMetricManagerPersistence: NodeMetricManagerPersistence = _

  @Autowired
  private var nodePointerBuilder: NodePointerBuilder = _

  @Autowired
  private var defaultMetricsConverter: MetricsConverter = _

  @Receiver
  override def getEM(getEMInfoRequest: GetEMInfoRequest): EMNode = {
    val node = emNodeManager.getEM(getEMInfoRequest.getEm)
    if (null == node) new AMEMNode() else node
  }

  /**
   * 通过Label去拿，AliasServiceInstanceLabel 指定type为EM
   *
   * @return
   */
  override def getAllEM(): Array[EMNode] = {
    val label = new AliasServiceInstanceLabel
    label.setAlias(GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME.getValue)
    val instances = nodeLabelService.getNodesByLabel(label)
    val resourceInfo = resourceManager.getResourceInfo(instances.asScala.toSeq.toArray).resourceInfo
    val resourceInfoMap = resourceInfo.asScala.map(r => (r.getServiceInstance.toString, r)).toMap
    instances.asScala
      .map(emNodeManager.getEM)
      .filter(_ != null)
      .map { node =>
        node.setLabels(nodeLabelService.getNodeLabels(node.getServiceInstance))
        resourceInfoMap
          .get(node.getServiceInstance.toString)
          .map(_.getNodeResource)
          .foreach(node.setNodeResource)
        node
      }
      .toArray[EMNode]
  }

  override def getEM(serviceInstance: ServiceInstance): EMNode = {
    if (serviceInstance != null) {
      emNodeManager.getEM(serviceInstance)
    } else {
      null
    }
  }

  override def updateEMInfo(
      serviceInstance: ServiceInstance,
      nodeHealthyInfo: NodeHealthyInfo
  ): Unit = {
    val node = emNodeManager.getEM(serviceInstance)
    if (null != node) {
      val metrics = nodeMetricManagerPersistence.getNodeMetrics(node)
      if (null != metrics && null != nodeHealthyInfo) {
        val oldHealthyInfo = defaultMetricsConverter.parseHealthyInfo(metrics)
        if (!nodeHealthyInfo.getNodeHealthy.equals(oldHealthyInfo.getNodeHealthy)) {
          metrics.setHealthy(defaultMetricsConverter.convertHealthyInfo(nodeHealthyInfo))
          nodeMetricManagerPersistence.addOrupdateNodeMetrics(metrics)
          val nodeHealthyRequest: NodeHealthyRequest = new NodeHealthyRequest
          nodeHealthyRequest.setNodeHealthy(nodeHealthyInfo.getNodeHealthy)
          nodePointerBuilder.buildEMNodePointer(node).updateNodeHealthyRequest(nodeHealthyRequest)
          logger.info(
            s"success to update healthy metric of instance: ${serviceInstance.getInstance},${metrics.getHealthy}"
          )
        }
      }
    }
  }

}
