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

package org.apache.linkis.manager.label.service.impl

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel
import org.apache.linkis.manager.label.errorcode.LabelCommonErrorCodeSummary._
import org.apache.linkis.manager.label.exception.LabelRuntimeException
import org.apache.linkis.manager.label.service.{NodeLabelRemoveService, NodeLabelService}
import org.apache.linkis.manager.persistence.LabelManagerPersistence
import org.apache.linkis.protocol.label.NodeLabelRemoveRequest
import org.apache.linkis.rpc.message.annotation.Receiver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DefaultNodeLabelRemoveService extends NodeLabelRemoveService with Logging {

  @Autowired
  private var nodeLabelService: NodeLabelService = _

  @Autowired
  private var labelPersistence: LabelManagerPersistence = _

  private val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  @Receiver
  override def removeNodeLabel(nodeLabelRemoveRequest: NodeLabelRemoveRequest): Unit = {
    logger.info(s"Start to remove labels from node ${nodeLabelRemoveRequest.getServiceInstance}")
    if (nodeLabelRemoveRequest.getServiceInstance == null) {
      throw new LabelRuntimeException(
        CHECK_LABEL_REMOVE_REQUEST.getErrorCode,
        CHECK_LABEL_REMOVE_REQUEST.getErrorDesc
      )
    }
    nodeLabelService.removeLabelsFromNode(
      nodeLabelRemoveRequest.getServiceInstance,
      nodeLabelRemoveRequest.isEngine
    )
    val persistenceLabel = if (nodeLabelRemoveRequest.isEngine) {
      val engineLabel = labelFactory.createLabel(classOf[EngineInstanceLabel])
      engineLabel.setInstance(nodeLabelRemoveRequest.getServiceInstance.getInstance)
      engineLabel.setServiceName(nodeLabelRemoveRequest.getServiceInstance.getApplicationName)
      labelFactory.convertLabel(engineLabel, classOf[PersistenceLabel])
    } else {
      val eMInstanceLabel = labelFactory.createLabel(classOf[EMInstanceLabel])
      eMInstanceLabel.setServiceName(nodeLabelRemoveRequest.getServiceInstance.getApplicationName)
      eMInstanceLabel.setInstance(nodeLabelRemoveRequest.getServiceInstance.getInstance)
      labelFactory.convertLabel(eMInstanceLabel, classOf[PersistenceLabel])
    }

    labelPersistence.removeLabel(persistenceLabel)
    logger.info(s"Finished to remove labels from node ${nodeLabelRemoveRequest.getServiceInstance}")
  }

}
