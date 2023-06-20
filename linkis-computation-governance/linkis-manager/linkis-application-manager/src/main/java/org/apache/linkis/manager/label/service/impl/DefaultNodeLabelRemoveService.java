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

package org.apache.linkis.manager.label.service.impl;

import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel;
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel;
import org.apache.linkis.manager.label.errorcode.LabelCommonErrorCodeSummary;
import org.apache.linkis.manager.label.exception.LabelRuntimeException;
import org.apache.linkis.manager.label.service.NodeLabelRemoveService;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.persistence.LabelManagerPersistence;
import org.apache.linkis.protocol.label.NodeLabelRemoveRequest;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultNodeLabelRemoveService implements NodeLabelRemoveService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultNodeLabelRemoveService.class);

  @Autowired private NodeLabelService nodeLabelService;

  @Autowired private LabelManagerPersistence labelPersistence;

  private final LabelBuilderFactory labelFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  @Receiver
  @Override
  public void removeNodeLabel(NodeLabelRemoveRequest nodeLabelRemoveRequest) {
    logger.info(
        String.format(
            "Start to remove labels from node %s", nodeLabelRemoveRequest.getServiceInstance()));
    if (nodeLabelRemoveRequest.getServiceInstance() == null) {
      throw new LabelRuntimeException(
          LabelCommonErrorCodeSummary.CHECK_LABEL_REMOVE_REQUEST.getErrorCode(),
          LabelCommonErrorCodeSummary.CHECK_LABEL_REMOVE_REQUEST.getErrorDesc());
    }
    nodeLabelService.removeLabelsFromNode(
        nodeLabelRemoveRequest.getServiceInstance(), nodeLabelRemoveRequest.isEngine());
    PersistenceLabel persistenceLabel;
    if (nodeLabelRemoveRequest.isEngine()) {
      EngineInstanceLabel engineLabel = labelFactory.createLabel(EngineInstanceLabel.class);
      engineLabel.setInstance(nodeLabelRemoveRequest.getServiceInstance().getInstance());
      engineLabel.setServiceName(nodeLabelRemoveRequest.getServiceInstance().getApplicationName());
      persistenceLabel = labelFactory.convertLabel(engineLabel, PersistenceLabel.class);
    } else {
      EMInstanceLabel eMInstanceLabel = labelFactory.createLabel(EMInstanceLabel.class);
      eMInstanceLabel.setServiceName(
          nodeLabelRemoveRequest.getServiceInstance().getApplicationName());
      eMInstanceLabel.setInstance(nodeLabelRemoveRequest.getServiceInstance().getInstance());
      persistenceLabel = labelFactory.convertLabel(eMInstanceLabel, PersistenceLabel.class);
    }
    labelPersistence.removeLabel(persistenceLabel);
    logger.info(
        String.format(
            "Finished to remove labels from node %s", nodeLabelRemoveRequest.getServiceInstance()));
  }
}
