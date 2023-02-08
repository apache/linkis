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

package org.apache.linkis.manager.rm.message

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.common.protocol.em.{EMResourceRegisterRequest, StopEMRequest}
import org.apache.linkis.manager.common.protocol.resource.ResourceUsedProtocol
import org.apache.linkis.manager.label.service.NodeLabelService
import org.apache.linkis.manager.rm.domain.RMLabelContainer
import org.apache.linkis.manager.rm.service.ResourceManager
import org.apache.linkis.rpc.message.annotation.Receiver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RMMessageService extends Logging {

  @Autowired
  private var resourceManager: ResourceManager = _

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Receiver
  def dealWithResourceUsedProtocol(resourceUsedProtocol: ResourceUsedProtocol): Unit = {
    logger.info(s"Start to deal with resourceUsedProtocol $resourceUsedProtocol")
    val labels = nodeLabelService.getNodeLabels(resourceUsedProtocol.serviceInstance)
    Utils.tryCatch(resourceManager.resourceUsed(labels, resourceUsedProtocol.engineResource)) {
      case exception: Exception =>
        val nodeLabels = new RMLabelContainer(labels)
        val value: String = Option(nodeLabels.getCombinedUserCreatorEngineTypeLabel)
          .map(_.getStringValue)
          .getOrElse("")
        logger.warn(
          s"usedResource failed, request from:$value, request engine: ${nodeLabels.getEngineInstanceLabel}, ",
          exception
        )
    }
  }

  def dealWithRegisterEMRequest(registerEMRequest: EMResourceRegisterRequest): Unit = {
    resourceManager.register(
      registerEMRequest.getServiceInstance,
      registerEMRequest.getNodeResource
    )
  }

  def dealWithStopEMRequest(stopEMRequest: StopEMRequest): Unit = {
    resourceManager.unregister(stopEMRequest.getEm)
  }

}
