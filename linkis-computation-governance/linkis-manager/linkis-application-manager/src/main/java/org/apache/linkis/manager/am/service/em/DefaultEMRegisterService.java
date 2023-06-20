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

package org.apache.linkis.manager.am.service.em;

import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.manager.EMNodeManager;
import org.apache.linkis.manager.common.constant.AMConstant;
import org.apache.linkis.manager.common.entity.node.AMEMNode;
import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.protocol.em.EMResourceRegisterRequest;
import org.apache.linkis.manager.common.protocol.em.RegisterEMRequest;
import org.apache.linkis.manager.common.protocol.em.RegisterEMResponse;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel;
import org.apache.linkis.manager.label.service.NodeLabelAddService;
import org.apache.linkis.manager.rm.message.RMMessageService;
import org.apache.linkis.protocol.label.NodeLabelAddRequest;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEMRegisterService implements EMRegisterService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultEMRegisterService.class);

  @Autowired private EMNodeManager emNodeManager;

  @Autowired private NodeLabelAddService nodeLabelAddService;

  @Autowired private RMMessageService rmMessageService;

  private EMNode registerEMRequest2EMNode(RegisterEMRequest emRegister) {
    AMEMNode emNode = new AMEMNode();
    emNode.setServiceInstance(emRegister.getServiceInstance());
    String owner =
        StringUtils.isNotBlank(emRegister.getUser())
            ? emRegister.getUser()
            : AMConfiguration.DEFAULT_NODE_OWNER.getValue();
    emNode.setOwner(owner);
    emNode.setMark(AMConstant.PROCESS_MARK);
    return emNode;
  }

  /**
   * EM注册请求的第一个处理的请求，用于插入Instance信息
   *
   * @param emRegister
   */
  @Receiver
  public RegisterEMResponse addEMNodeInstance(RegisterEMRequest emRegister, Sender sender) {
    RegisterEMResponse response;
    try {
      logger.info("Start to save em {} in persistence", emRegister.getServiceInstance());
      emNodeManager.addEMNodeInstance(registerEMRequest2EMNode(emRegister));
      logger.info("Finished to save em{" + emRegister.getServiceInstance() + "} in persistence");
      EMInstanceLabel eMInstanceLabel =
          LabelBuilderFactoryContext.getLabelBuilderFactory().createLabel(EMInstanceLabel.class);
      eMInstanceLabel.setServiceName(emRegister.getServiceInstance().getApplicationName());
      eMInstanceLabel.setInstance(emRegister.getServiceInstance().getInstance());
      if (emRegister.getLabels() == null) {
        emRegister.setLabels(new HashMap<>());
      }
      emRegister.getLabels().put(eMInstanceLabel.getLabelKey(), eMInstanceLabel.getStringValue());

      NodeLabelAddRequest instanceLabelAddRequest =
          new NodeLabelAddRequest(emRegister.getServiceInstance(), emRegister.getLabels());
      logger.info(
          "Start to publish em{" + emRegister.getServiceInstance() + "} label request to Label");
      nodeLabelAddService.addNodeLabels(instanceLabelAddRequest);
      logger.info("Finished to deal em{" + emRegister.getServiceInstance() + "} label ");
      addEMNodeMetrics(emRegister);

      rmMessageService.dealWithRegisterEMRequest(registerEMRequest2RMRequest(emRegister));
      response = new RegisterEMResponse(true, "success");
    } catch (Throwable t) {
      logger.error("Failed to register ecm " + emRegister.getServiceInstance(), t);
      response = new RegisterEMResponse(false, ExceptionUtils.getRootCauseMessage(t));
    }
    return response;
  }

  private EMResourceRegisterRequest registerEMRequest2RMRequest(
      RegisterEMRequest registerEMRequest) {
    EMResourceRegisterRequest emResourceRegisterRequest = new EMResourceRegisterRequest();
    emResourceRegisterRequest.setAlias(registerEMRequest.getAlias());
    emResourceRegisterRequest.setLabels(registerEMRequest.getLabels());
    emResourceRegisterRequest.setNodeResource(registerEMRequest.getNodeResource());
    emResourceRegisterRequest.setServiceInstance(registerEMRequest.getServiceInstance());
    emResourceRegisterRequest.setUser(registerEMRequest.getUser());
    return emResourceRegisterRequest;
  }

  private void addEMNodeMetrics(RegisterEMRequest emRegister) {
    logger.info("Start to init em{" + emRegister.getServiceInstance() + "} metrics");
    emNodeManager.initEMNodeMetrics(registerEMRequest2EMNode(emRegister));
    logger.info("Finished to init em{" + emRegister.getServiceInstance() + "} metrics");
  }
}
