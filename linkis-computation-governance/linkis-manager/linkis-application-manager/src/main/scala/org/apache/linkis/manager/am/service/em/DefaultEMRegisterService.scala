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

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.am.manager.EMNodeManager
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.entity.node.{AMEMNode, EMNode}
import org.apache.linkis.manager.common.protocol.em.{
  EMResourceRegisterRequest,
  RegisterEMRequest,
  RegisterEMResponse
}
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel
import org.apache.linkis.manager.label.service.NodeLabelAddService
import org.apache.linkis.manager.rm.message.RMMessageService
import org.apache.linkis.protocol.label.NodeLabelAddRequest
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util

@Service
class DefaultEMRegisterService extends EMRegisterService with Logging {

  @Autowired
  private var emNodeManager: EMNodeManager = _

  @Autowired
  private var nodeLabelAddService: NodeLabelAddService = _

  @Autowired
  private var rmMessageService: RMMessageService = _

  def registerEMRequest2EMNode(emRegister: RegisterEMRequest): EMNode = {
    val emNode = new AMEMNode()
    emNode.setServiceInstance(emRegister.getServiceInstance)
    val owner =
      if (StringUtils.isNotBlank(emRegister.getUser)) emRegister.getUser
      else AMConfiguration.DEFAULT_NODE_OWNER.getValue
    emNode.setOwner(owner)
    emNode.setMark(AMConstant.PROCESS_MARK)
    emNode
  }

  /**
   * EM注册请求的第一个处理的请求，用于插入Instance信息
   *
   * @param emRegister
   */
  @Receiver
  override def addEMNodeInstance(
      emRegister: RegisterEMRequest,
      sender: Sender
  ): RegisterEMResponse = Utils.tryCatch {
    logger.info(s"Start to save em{${emRegister.getServiceInstance}}  in persistence")
    emNodeManager.addEMNodeInstance(registerEMRequest2EMNode(emRegister))
    logger.info(s"Finished to save em{${emRegister.getServiceInstance}}  in persistence")
    val eMInstanceLabel =
      LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(classOf[EMInstanceLabel])
    eMInstanceLabel.setServiceName(emRegister.getServiceInstance.getApplicationName)
    eMInstanceLabel.setInstance(emRegister.getServiceInstance.getInstance)
    if (null == emRegister.getLabels) {
      emRegister.setLabels(new util.HashMap[String, Object]())
    }
    emRegister.getLabels.put(eMInstanceLabel.getLabelKey, eMInstanceLabel.getStringValue)
    val instanceLabelAddRequest =
      new NodeLabelAddRequest(emRegister.getServiceInstance, emRegister.getLabels)
    logger.info(s"Start to publish em{${emRegister.getServiceInstance}} label request to Label ")
    nodeLabelAddService.addNodeLabels(instanceLabelAddRequest)
    logger.info(s"Finished to deal em{${emRegister.getServiceInstance}} label ")
    addEMNodeMetrics(emRegister)
    rmMessageService.dealWithRegisterEMRequest(emRegister)
    RegisterEMResponse(isSuccess = true)
  } { t =>
    logger.error(s"Failed to register ecm ${emRegister.getServiceInstance}", t)
    RegisterEMResponse(isSuccess = false, ExceptionUtils.getRootCauseMessage(t))
  }

  implicit def registerEMRequest2RMRequest(
      registerEMRequest: RegisterEMRequest
  ): EMResourceRegisterRequest = {
    val emResourceRegisterRequest = new EMResourceRegisterRequest
    emResourceRegisterRequest.setAlias(registerEMRequest.getAlias)
    emResourceRegisterRequest.setLabels(registerEMRequest.getLabels)
    emResourceRegisterRequest.setNodeResource(registerEMRequest.getNodeResource)
    emResourceRegisterRequest.setServiceInstance(registerEMRequest.getServiceInstance)
    emResourceRegisterRequest.setUser(registerEMRequest.getUser)
    emResourceRegisterRequest
  }

  def addEMNodeMetrics(emRegister: RegisterEMRequest): Unit = {
    logger.info(s"Start to init em{${emRegister.getServiceInstance}}  metrics")
    emNodeManager.initEMNodeMetrics(registerEMRequest2EMNode(emRegister))
    logger.info(s"Finished to init em{${emRegister.getServiceInstance}}  metrics")
  }

}
