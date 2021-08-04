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

package com.webank.wedatasphere.linkis.manager.am.service.em

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf
import com.webank.wedatasphere.linkis.manager.am.exception.{AMErrorCode, AMErrorException}
import com.webank.wedatasphere.linkis.manager.am.manager.EMNodeManager
import com.webank.wedatasphere.linkis.manager.am.vo.EMNodeVo
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeHealthy
import com.webank.wedatasphere.linkis.manager.common.entity.node.{AMEMNode, EMNode}
import com.webank.wedatasphere.linkis.manager.common.protocol.em.GetEMInfoRequest
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.node.AliasServiceInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.service.NodeLabelService
import com.webank.wedatasphere.linkis.manager.persistence.NodeMetricManagerPersistence
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.resourcemanager.service.ResourceManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConversions._


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
    val resourceInfo = resourceManager.getResourceInfo(instances.toSeq.toArray).resourceInfo
    val resourceInfoMap = resourceInfo.map(r => (r.getServiceInstance.toString, r)).toMap
    instances.map(emNodeManager.getEM).filter(_!= null).map { node =>
      node.setLabels(nodeLabelService.getNodeLabels(node.getServiceInstance))
      resourceInfoMap.get(node.getServiceInstance.toString).map(_.getNodeResource).foreach(node.setNodeResource)
      node
    }.toArray[EMNode]
  }

  override def getEM(serviceInstance: ServiceInstance): EMNode = {
    if(serviceInstance != null){
      emNodeManager.getEM(serviceInstance)
    }else null
  }

  /**
   * 目前仅支持用户进行EM健康状态的修改
   *
   * @param serviceInstance
   * @param healthyStatus
   */
  override def updateEMInfo(serviceInstance: ServiceInstance, healthyStatus: String): Unit={
    val node = emNodeManager.getEM(serviceInstance)
    if (null != node) {
      val metrics = nodeMetricManagerPersistence.getNodeMetrics(node)
      if(healthyStatus != null && !healthyStatus.equals(metrics.getHealthy)){
        metrics.setHealthy(healthyStatus)
        nodeMetricManagerPersistence.addOrupdateNodeMetrics(metrics)
        info(s"success to update healthy metric of instance: ${serviceInstance.getInstance},${metrics.getHealthy} => ${healthyStatus} !")
      }
    }
  }
}
