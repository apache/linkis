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
 
package org.apache.linkis.resourcemanager.message

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.em.{EMInfoClearRequest, EMResourceRegisterRequest, RegisterEMRequest, StopEMRequest}
import org.apache.linkis.manager.common.protocol.engine.EngineInfoClearRequest
import org.apache.linkis.manager.common.protocol.node.NodeHeartbeatMsg
import org.apache.linkis.manager.common.protocol.resource.ResourceUsedProtocol
import org.apache.linkis.manager.label.service.NodeLabelService
import org.apache.linkis.manager.service.common.label.ManagerLabelService
import org.apache.linkis.message.annotation.Receiver
import org.apache.linkis.message.builder.ServiceMethodContext
import org.apache.linkis.resourcemanager.domain.RMLabelContainer
import org.apache.linkis.resourcemanager.service.ResourceManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RMMessageService extends Logging {


  @Autowired
  private var resourceManager: ResourceManager = _

  @Autowired
  private var managerLabelService: ManagerLabelService = _


  @Autowired
  private var nodeLabelService: NodeLabelService = _


  def dealWithEMInfoClearRequest(eMInfoClearRequest: EMInfoClearRequest, smc: ServiceMethodContext): Unit = {
    resourceManager.unregister(eMInfoClearRequest.getEm.getServiceInstance)
  }

  @Receiver
  def dealWithEngineInfoClearRequest(engineInfoClearRequest: EngineInfoClearRequest, smc: ServiceMethodContext): Unit = {
    resourceManager.resourceReleased(engineInfoClearRequest.getEngineNode.getLabels)
  }


  def dealWithNodeHeartbeatMsg(nodeHeartbeatMsg: NodeHeartbeatMsg, smc: ServiceMethodContext): Unit = {
    debug(s"Start to deal with nodeHeartbeatMsg resource info $nodeHeartbeatMsg")
    /*val labels = nodeLabelService.getNodeLabels(nodeHeartbeatMsg.getServiceInstance)
    if (managerLabelService.isEngine(labels) && !nodeHeartbeatMsg.getStatus.equals(NodeStatus.ShuttingDown)) {
      resourceManager.resourceReport(labels, nodeHeartbeatMsg.getNodeResource)
    }*/
    debug(s"Finished to deal with nodeHeartbeatMsg resource info $nodeHeartbeatMsg")
    /* info(s"Start to deal with resourceUsedProtocol $nodeHeartbeatMsg")
     val labels = nodeLabelService.getNodeLabels(nodeHeartbeatMsg.getServiceInstance)
     resourceManager.resourceUsed(labels, nodeHeartbeatMsg.getNodeResource)
     if(managerLabelService.isEM(nodeHeartbeatMsg.getServiceInstance)){
       //resourceManager.register(nodeHeartbeatMsg.getServiceInstance, nodeHeartbeatMsg.getNodeResource)
     } else if(managerLabelService.isEngine(nodeHeartbeatMsg.getServiceInstance)){
       info(s"Start to deal with resourceUsedProtocol $nodeHeartbeatMsg")


     } else {
       info(s"${nodeHeartbeatMsg.getServiceInstance} is neither EM nor Engine")
     }*/
  }


  @Receiver
  def dealWithResourceUsedProtocol(resourceUsedProtocol: ResourceUsedProtocol): Unit = {
    info(s"Start to deal with resourceUsedProtocol $resourceUsedProtocol")
    val labels = nodeLabelService.getNodeLabels(resourceUsedProtocol.serviceInstance)
    Utils.tryCatch(resourceManager.resourceUsed(labels, resourceUsedProtocol.engineResource)) {
      case exception: Exception => {
        val nodeLabels = new RMLabelContainer(labels)
        warn(s"usedResource failed, request from:${nodeLabels.getCombinedUserCreatorEngineTypeLabel.getStringValue}, request engine: ${nodeLabels.getEngineInstanceLabel}, " +
          s"reason:${exception.getMessage}")
        throw exception
      }
    }
  }


  def dealWithRegisterEMRequest(registerEMRequest: EMResourceRegisterRequest): Unit = {
    resourceManager.register(registerEMRequest.getServiceInstance, registerEMRequest.getNodeResource)
  }


  def dealWithStopEMRequest(stopEMRequest: StopEMRequest): Unit = {
    resourceManager.unregister(stopEMRequest.getEm)
  }

}
