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

import org.apache.linkis.manager.am.manager.EMNodeManager;
import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.protocol.em.EMInfoClearRequest;
import org.apache.linkis.manager.common.protocol.em.StopEMRequest;
import org.apache.linkis.manager.label.service.NodeLabelRemoveService;
import org.apache.linkis.manager.rm.message.RMMessageService;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEMUnregisterService implements EMUnregisterService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultEMUnregisterService.class);

  @Autowired private EMNodeManager emNodeManager;

  @Autowired private NodeLabelRemoveService nodeLabelRemoveService;

  @Autowired private RMMessageService rmMessageService;

  @Override
  @Receiver
  public void stopEM(StopEMRequest stopEMRequest, Sender sender) {
    logger.info(
        " user " + stopEMRequest.getUser() + " prepare to stop em " + stopEMRequest.getEm());
    EMNode node = emNodeManager.getEM(stopEMRequest.getEm());
    if (null == node) return;
    if (!node.getOwner().equals(stopEMRequest.getUser())) {
      logger.info(stopEMRequest.getUser() + " are not owner, will not to stopEM");
    }

    // clear RM info
    EMInfoClearRequest emClearRequest = new EMInfoClearRequest();
    emClearRequest.setEm(node);
    emClearRequest.setUser(stopEMRequest.getUser());
    clearEMInstanceInfo(emClearRequest);
    logger.info(
        " user " + stopEMRequest.getUser() + " finished to stop em " + stopEMRequest.getEm());
  }

  @Override
  public void clearEMInstanceInfo(EMInfoClearRequest emClearRequest) {
    logger.info(
        " user "
            + emClearRequest.getUser()
            + " prepare to clear em info "
            + emClearRequest.getEm().getServiceInstance());
    emNodeManager.deleteEM(emClearRequest.getEm());
    logger.info(
        " user "
            + emClearRequest.getUser()
            + " Finished to clear em info "
            + emClearRequest.getEm().getServiceInstance());
  }
}
