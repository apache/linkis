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
import org.apache.linkis.manager.common.entity.label.LabelKeyValue
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel
import org.apache.linkis.manager.common.entity.resource.NodeResource
import org.apache.linkis.manager.common.utils.ResourceUtils
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel
import org.apache.linkis.manager.label.LabelManagerUtils
import org.apache.linkis.manager.label.service.ResourceLabelService
import org.apache.linkis.manager.persistence.ResourceLabelPersistence
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._



@Component
class DefaultResourceLabelService extends ResourceLabelService with Logging {

  @Autowired
  private var resourceLabelPersistence: ResourceLabelPersistence = _

  private val labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory




  /**
    * 通过传入的Labels 查找所有和Resource相关的Label
    * 包含CombineLabel，需要CombineLabel的所有Label Key 出现过
    * 默认返回的是PersistenceLabel，方便加快性能
    *
    * @param labels
    * @return
    */
  override def getResourceLabels(labels: util.List[Label[_]]): util.List[Label[_]] = {
    val labelKeyValueList = labels.flatMap { label =>
      val persistenceLabel = LabelManagerUtils.convertPersistenceLabel(label)
      persistenceLabel.getValue.map { keyValue =>
        new LabelKeyValue(keyValue._1, keyValue._2)
      }
    }.filter(null != _).toList
    val resourceLabels = resourceLabelPersistence.getResourceLabels(labelKeyValueList)
    resourceLabels.map{ label =>
      val realyLabel:Label[_] = labelBuilderFactory.createLabel(label.getLabelKey, label.getValue)
      realyLabel
    }
  }




  /**
    * 设置某个Label的资源数值，如果不存在add，存在对应的Label update
    * lABEL 不存在需要插入Label先
    *
    * @param label
    * @param resource
    */
  override def setResourceToLabel(label: Label[_], resource: NodeResource, source: String): Unit = {
    val persistResource = ResourceUtils.toPersistenceResource(resource)
    persistResource.setUpdator(source)
    resourceLabelPersistence.setResourceToLabel(LabelManagerUtils.convertPersistenceLabel(label), persistResource)
  }

  /**
    * 通过Label 返回对应的Resource
    *
    * @param label
    * @return
    */
  override def getResourceByLabel(label: Label[_]): NodeResource = {
    if (null == label) {
      return null
    }
    val persistenceResource = label match {
      case p: PersistenceLabel => resourceLabelPersistence.getResourceByLabel(p)
      case _ =>  resourceLabelPersistence.getResourceByLabel(LabelManagerUtils.convertPersistenceLabel(label))
    }
    if(persistenceResource.isEmpty){
      null
    } else {
      // TODO: 判断取哪个resource
      ResourceUtils.fromPersistenceResource(persistenceResource.get(0))
    }
  }

  /**
    * 清理Label的资源信息和记录
    * 1. 清理Label对应的Resource信息
    * 2. 清理包含改Label的CombinedLabel的Resource信息
    *
    * @param label
    */
  override def removeResourceByLabel(label: Label[_]): Unit = {
    resourceLabelPersistence.removeResourceByLabel(LabelManagerUtils.convertPersistenceLabel(label))
  }

  override def removeResourceByLabels(labels: util.List[Label[_]]): Unit = {
    resourceLabelPersistence.removeResourceByLabels(labels.map(LabelManagerUtils.convertPersistenceLabel))
  }

  override def setEngineConnResourceToLabel(label: Label[_], nodeResource: NodeResource, source: String): Unit = {
    label match {
      case label: EngineInstanceLabel =>
        val resource = ResourceUtils.toPersistenceResource(nodeResource)
        resource.setTicketId(label.getInstance())
        val resourceLabel = LabelManagerUtils.convertPersistenceLabel(label)
        resource.setUpdator(source)
        resourceLabelPersistence.setResourceToLabel(resourceLabel, resource)
      case _ =>
    }
  }
}
