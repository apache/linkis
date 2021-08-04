/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.manager.label.service.impl

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLabel
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.em.EMInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineInstanceLabel
import com.webank.wedatasphere.linkis.manager.label.service.{NodeLabelRemoveService, NodeLabelService}
import com.webank.wedatasphere.linkis.manager.persistence.LabelManagerPersistence
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.protocol.label.NodeLabelRemoveRequest
import com.webank.wedatasphere.linkis.manager.label.conf.LabelCommonConfig
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
    info(s"Start to remove labels from node ${nodeLabelRemoveRequest.getServiceInstance}")
    val permanentLabelKey = LabelCommonConfig.PERMANENT_LABEL.getValue.split(",")
    nodeLabelService.removeLabelsFromNodeWithoutPermanent(nodeLabelRemoveRequest.getServiceInstance,permanentLabelKey)
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
    info(s"Finished to remove labels from node ${nodeLabelRemoveRequest.getServiceInstance}")
  }

}
