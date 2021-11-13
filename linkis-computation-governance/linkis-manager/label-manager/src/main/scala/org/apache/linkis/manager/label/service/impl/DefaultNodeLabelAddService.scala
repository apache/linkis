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
 
package org.apache.linkis.manager.label.service.impl

import java.util

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.protocol.label.LabelReportRequest
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.service.{NodeLabelAddService, NodeLabelService}
import org.apache.linkis.message.annotation.Receiver
import org.apache.linkis.protocol.label.NodeLabelAddRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class DefaultNodeLabelAddService extends NodeLabelAddService with Logging {

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Receiver
  override def addNodeLabels(nodeLabelAddRequest: NodeLabelAddRequest): Unit = {
    info(s"Start to add labels for node ${nodeLabelAddRequest.getServiceInstance}")
    val labelList: util.List[Label[_]] = LabelBuilderFactoryContext.getLabelBuilderFactory.getLabels(nodeLabelAddRequest.getLabels)
    nodeLabelService.addLabelsToNode(nodeLabelAddRequest.getServiceInstance, labelList)
    info(s"Finished to add labels for node ${nodeLabelAddRequest.getServiceInstance}")
  }

  @Receiver
  override def dealNodeLabelReport(labelReportRequest: LabelReportRequest): Unit = {
    info(s"Start to deal labels for node ${labelReportRequest.serviceInstance}")
    val labelList = labelReportRequest.labels
    nodeLabelService.addLabelsToNode(labelReportRequest.serviceInstance, labelList)
    info(s"Finished to deal labels for node ${labelReportRequest.serviceInstance}")
  }
}
