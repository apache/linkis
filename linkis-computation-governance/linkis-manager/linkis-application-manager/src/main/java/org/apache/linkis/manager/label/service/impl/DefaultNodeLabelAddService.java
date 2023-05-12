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

import org.apache.linkis.manager.common.protocol.label.LabelReportRequest;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.service.NodeLabelAddService;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.protocol.label.NodeLabelAddRequest;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultNodeLabelAddService implements NodeLabelAddService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultNodeLabelAddService.class);

  @Autowired private NodeLabelService nodeLabelService;

  @Receiver
  @Override
  public void addNodeLabels(NodeLabelAddRequest nodeLabelAddRequest) {
    logger.info(
        String.format("Start to add labels for node %s", nodeLabelAddRequest.getServiceInstance()));
    List<Label<?>> labelList =
        LabelBuilderFactoryContext.getLabelBuilderFactory()
            .getLabels(nodeLabelAddRequest.getLabels());
    nodeLabelService.addLabelsToNode(nodeLabelAddRequest.getServiceInstance(), labelList);
    logger.info(
        String.format(
            "Finished to add labels for node %s", nodeLabelAddRequest.getServiceInstance()));
  }

  @Receiver
  @Override
  public void dealNodeLabelReport(LabelReportRequest labelReportRequest) {
    logger.info(
        String.format("Start to deal labels for node %s", labelReportRequest.getServiceInstance()));
    List<Label<?>> labelList = new ArrayList<>(labelReportRequest.getLabels());
    nodeLabelService.addLabelsToNode(labelReportRequest.getServiceInstance(), labelList);
    logger.info(
        String.format(
            "Finished to deal labels for node %s", labelReportRequest.getServiceInstance()));
  }
}
