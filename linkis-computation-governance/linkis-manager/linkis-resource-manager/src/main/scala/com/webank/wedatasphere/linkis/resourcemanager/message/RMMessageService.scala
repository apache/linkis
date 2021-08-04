/*
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
 */

package com.webank.wedatasphere.linkis.resourcemanager.message

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.protocol.em.{EMInfoClearRequest, RegisterEMRequest, StopEMRequest}
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineInfoClearRequest
import com.webank.wedatasphere.linkis.manager.common.protocol.node.NodeHeartbeatMsg
import com.webank.wedatasphere.linkis.manager.common.protocol.resource.ResourceUsedProtocol
import com.webank.wedatasphere.linkis.manager.label.service.NodeLabelService
import com.webank.wedatasphere.linkis.manager.service.common.label.ManagerLabelService
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext
import com.webank.wedatasphere.linkis.resourcemanager.service.ResourceManager
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

  @Receiver
  def dealWithEMInfoClearRequest(eMInfoClearRequest: EMInfoClearRequest, smc: ServiceMethodContext): Unit = {
    resourceManager.unregister(eMInfoClearRequest.getEm.getServiceInstance)
  }

  @Receiver
  def dealWithEngineInfoClearRequest(engineInfoClearRequest: EngineInfoClearRequest, smc: ServiceMethodContext): Unit = {
    resourceManager.resourceReleased(engineInfoClearRequest.getEngineNode.getLabels)
  }

  @Receiver
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
    resourceManager.resourceUsed(labels, resourceUsedProtocol.engineResource)
  }

  @Receiver
  def dealWithRegisterEMRequest(registerEMRequest: RegisterEMRequest, smc: ServiceMethodContext): Unit = {
    resourceManager.register(registerEMRequest.getServiceInstance, registerEMRequest.getNodeResource)
  }


  def dealWithStopEMRequest(stopEMRequest: StopEMRequest, smc: ServiceMethodContext): Unit = {
    resourceManager.unregister(stopEMRequest.getEm)
  }

}
