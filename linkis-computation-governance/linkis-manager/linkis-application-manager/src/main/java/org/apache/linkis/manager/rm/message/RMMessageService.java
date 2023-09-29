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

package org.apache.linkis.manager.rm.message;

import org.apache.linkis.manager.common.protocol.em.EMResourceRegisterRequest;
import org.apache.linkis.manager.common.protocol.em.StopEMRequest;
import org.apache.linkis.manager.common.protocol.resource.ResourceUsedProtocol;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.rm.domain.RMLabelContainer;
import org.apache.linkis.manager.rm.service.ResourceManager;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RMMessageService {
  private static final Logger logger = LoggerFactory.getLogger(RMMessageService.class);

  @Autowired private ResourceManager resourceManager;

  @Autowired private NodeLabelService nodeLabelService;

  @Receiver
  public void dealWithResourceUsedProtocol(ResourceUsedProtocol resourceUsedProtocol) {
    logger.info(String.format("Start to deal with resourceUsedProtocol %s", resourceUsedProtocol));
    List<Label<?>> labels =
        nodeLabelService.getNodeLabels(resourceUsedProtocol.getServiceInstance());
    try {
      resourceManager.resourceUsed(labels, resourceUsedProtocol.getEngineResource());
    } catch (Exception e) {
      RMLabelContainer nodeLabels = new RMLabelContainer(labels);
      String value =
          Optional.of(nodeLabels.getCombinedUserCreatorEngineTypeLabel())
              .map(Object::toString)
              .orElse("");
      logger.warn(
          String.format(
              "usedResource failed, request from:%s, request engine: %s, ",
              value, nodeLabels.getEngineInstanceLabel()),
          e);
    }
  }

  public void dealWithRegisterEMRequest(EMResourceRegisterRequest registerEMRequest) {
    resourceManager.register(
        registerEMRequest.getServiceInstance(), registerEMRequest.getNodeResource());
  }

  public void dealWithStopEMRequest(StopEMRequest stopEMRequest) {
    resourceManager.unregister(stopEMRequest.getEm());
  }
}
