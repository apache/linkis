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

package org.apache.linkis.manager.am.service.impl;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.governance.common.protocol.task.ResponseEngineConnPid;
import org.apache.linkis.manager.am.manager.DefaultEngineNodeManager;
import org.apache.linkis.manager.am.service.EngineConnPidCallbackService;
import org.apache.linkis.manager.am.service.engine.AbstractEngineService;
import org.apache.linkis.manager.common.constant.AMConstant;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEngineConnPidCallbackService extends AbstractEngineService
    implements EngineConnPidCallbackService {
  private static final Logger logger =
      LoggerFactory.getLogger(DefaultEngineConnPidCallbackService.class);

  @Autowired private DefaultEngineNodeManager defaultEngineNodeManager;

  @Autowired private NodeLabelService nodeLabelService;

  @Receiver
  @Override
  public void dealPid(ResponseEngineConnPid protocol) {
    // set pid
    logger.info(
        "DefaultEngineConnPidCallbackService dealPid serviceInstance: [{}] pid: [{}]"
            + " ticketId: [{}]",
        protocol.serviceInstance(),
        protocol.pid(),
        protocol.ticketId());

    EngineNode engineNode =
        defaultEngineNodeManager.getEngineNodeInfoByTicketId(protocol.ticketId());
    if (engineNode == null) {
      logger.error(
          "DefaultEngineConnPidCallbackService dealPid failed, engineNode is null, serviceInstance:{}",
          protocol.serviceInstance());
      return;
    }

    engineNode.setIdentifier(protocol.pid());
    ServiceInstance oldServiceInstance = engineNode.getServiceInstance();
    if (engineNode.getMark().equals(AMConstant.CLUSTER_PROCESS_MARK)) {
      ServiceInstance serviceInstance = protocol.serviceInstance();
      engineNode.setServiceInstance(serviceInstance);
      getEngineNodeManager().updateEngineNode(oldServiceInstance, engineNode);
      nodeLabelService.labelsFromInstanceToNewInstance(oldServiceInstance, serviceInstance);
    }
    defaultEngineNodeManager.updateEngine(engineNode);
  }
}
