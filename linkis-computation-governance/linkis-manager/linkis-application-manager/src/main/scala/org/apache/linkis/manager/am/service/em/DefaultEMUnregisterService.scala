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
 
package org.apache.linkis.manager.am.service.em

import java.util.concurrent.TimeUnit
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.am.manager.EMNodeManager
import org.apache.linkis.manager.common.protocol.em.{EMInfoClearRequest, EMResourceClearRequest, StopEMRequest}
import org.apache.linkis.manager.label.service.NodeLabelRemoveService
import org.apache.linkis.message.annotation.Receiver
import org.apache.linkis.message.builder.ServiceMethodContext
import org.apache.linkis.protocol.label.NodeLabelRemoveRequest
import org.apache.linkis.resourcemanager.message.RMMessageService
import org.apache.linkis.rpc.utils.RPCUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DefaultEMUnregisterService extends EMUnregisterService with Logging {

  @Autowired
  private var emNodeManager: EMNodeManager = _

  @Autowired
  private var nodeLabelRemoveService: NodeLabelRemoveService = _
  @Autowired
  private var rmMessageService: RMMessageService = _

  @Receiver
  override def stopEM(stopEMRequest: StopEMRequest): Unit = {
    info(s" user ${stopEMRequest.getUser} prepare to stop em ${stopEMRequest.getEm}")
    val node = emNodeManager.getEM(stopEMRequest.getEm)
    if (null == node) return
    if (node.getOwner != stopEMRequest.getUser) {
      info(s" ${stopEMRequest.getUser}  are not owner, will not to stopEM")
    }

        //clear RM info
    val emClearRequest = new EMInfoClearRequest
    emClearRequest.setEm(node)
    emClearRequest.setUser(stopEMRequest.getUser)
    Utils.tryAndWarn(rmMessageService.dealWithStopEMRequest(stopEMRequest))
    // clear Label
    val instanceLabelRemoveRequest = new NodeLabelRemoveRequest(node.getServiceInstance, false)
    Utils.tryAndWarn(nodeLabelRemoveService.removeNodeLabel(instanceLabelRemoveRequest))
    // 此处需要先清理ECM再等待，避免ECM重启过快，导致ECM资源没清理干净
    clearEMInstanceInfo(emClearRequest)
    info(s" user ${stopEMRequest.getUser} finished to stop em ${stopEMRequest.getEm}")
  }

  implicit def stopEMRequest2EMResourceClearRequest(stopEMRequest: StopEMRequest): EMResourceClearRequest = {
    val resourceClearRequest = new EMResourceClearRequest
    resourceClearRequest.setEm(stopEMRequest.getEm)
    resourceClearRequest.setUser(stopEMRequest.getUser)
    resourceClearRequest
  }

  override def clearEMInstanceInfo(emClearRequest: EMInfoClearRequest): Unit = {
    info(s" user ${emClearRequest.getUser} prepare to clear em info ${emClearRequest.getEm.getServiceInstance}")
    emNodeManager.deleteEM(emClearRequest.getEm)
    info(s" user ${emClearRequest.getUser} Finished to clear em info ${emClearRequest.getEm.getServiceInstance}")
  }


}
