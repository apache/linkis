/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.resourcemanager.service.impl

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.entity.persistence.{PersistenceLabel, PersistenceResource}
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.manager.common.utils.ResourceUtils
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import com.webank.wedatasphere.linkis.manager.label.entity.{CombinedLabel, Label}
import com.webank.wedatasphere.linkis.manager.label.service.ResourceLabelService
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtils
import com.webank.wedatasphere.linkis.manager.persistence.{LabelManagerPersistence, ResourceManagerPersistence}
import com.webank.wedatasphere.linkis.resourcemanager.domain.RMLabelContainer
import com.webank.wedatasphere.linkis.resourcemanager.service.LabelResourceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._


@Component
class LabelResourceServiceImpl extends LabelResourceService with Logging {

  @Autowired
  var resourceManagerPersistence: ResourceManagerPersistence = _

  @Autowired
  var labelManagerPersistence: LabelManagerPersistence = _

  @Autowired
  var resourceLabelService: ResourceLabelService = _

  private val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  override def getLabelResource(label: Label[_]): NodeResource = {
    resourceLabelService.getResourceByLabel(label)
  }

  override def setLabelResource(label: Label[_], nodeResource: NodeResource): Unit = {
    resourceLabelService.setResourceToLabel(label, nodeResource)
  }

  override def getResourcesByUser(user: String): Array[NodeResource] = {
    resourceManagerPersistence.getResourceByUser(user).map(ResourceUtils.fromPersistenceResource).toArray
  }

  override def enrichLabels(labelContainer: RMLabelContainer): RMLabelContainer = {
     new RMLabelContainer(labelContainer.getLabels)
  }

  override def removeResourceByLabel(label: Label[_]): Unit = {
    resourceLabelService.removeResourceByLabel(label)
  }

  /**
   * 方法同 setLabelResource 只适用于启动引擎申请资源后设置engineConn资源
   *
   * @param label
   * @param nodeResource
   */
  override def setEngineConnLabelResource(label: Label[_], nodeResource: NodeResource): Unit = resourceLabelService.setEngineConnResourceToLabel(label, nodeResource)

  override def getLabelsByResource(resource: PersistenceResource): Array[Label[_]] = {
    labelManagerPersistence.getLabelByResource(resource).map{ label =>
      labelFactory.createLabel(label.getLabelKey, label.getValue)
    }.toArray
  }
}
