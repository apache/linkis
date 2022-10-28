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

package org.apache.linkis.manager.rm.service.impl

import org.apache.linkis.manager.common.entity.resource.{CommonNodeResource, Resource, ResourceType}
import org.apache.linkis.manager.label.builder.CombinedLabelBuilder
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.manager.persistence.{
  LabelManagerPersistence,
  NodeManagerPersistence,
  ResourceManagerPersistence
}
import org.apache.linkis.manager.rm.restful.vo.UserCreatorEngineType
import org.apache.linkis.manager.rm.service.LabelResourceService
import org.apache.linkis.manager.rm.utils.UserConfiguration
import org.apache.linkis.server.BDPJettyServerHelper

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils

import java.util

import scala.collection.JavaConverters._

@Component
class UserResourceService {

  @Autowired
  var labelResourceService: LabelResourceService = _

  @Autowired
  var resourceManagerPersistence: ResourceManagerPersistence = _

  @Autowired
  var nodeManagerPersistence: NodeManagerPersistence = _

  @Autowired
  var labelManagerPersistence: LabelManagerPersistence = _

  val gson = BDPJettyServerHelper.gson

  val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  private val combinedLabelBuilder: CombinedLabelBuilder = new CombinedLabelBuilder

  @Transactional
  def resetUserResource(resourceId: Integer): Unit = {
    val resource = resourceManagerPersistence.getNodeResourceById(resourceId)
    val resourceLabel = labelManagerPersistence.getLabelByResource(resource)
    if (resource != null && !CollectionUtils.isEmpty(resourceLabel)) {
      val userCreatorEngineType =
        gson.fromJson(resourceLabel.get(0).getStringValue, classOf[UserCreatorEngineType])
      val labelResource = new CommonNodeResource
      val resourceType = ResourceType.valueOf(resource.getResourceType)
      labelResource.setResourceType(resourceType)
      labelResource.setUsedResource(Resource.initResource(resourceType))
      labelResource.setLockedResource(Resource.initResource(resourceType))
      val userCreatorLabel = labelFactory.createLabel(classOf[UserCreatorLabel])
      userCreatorLabel.setUser(userCreatorEngineType.getUser)
      userCreatorLabel.setCreator(userCreatorEngineType.getCreator)
      val engineTypeLabel = labelFactory.createLabel(classOf[EngineTypeLabel])
      engineTypeLabel.setEngineType(userCreatorEngineType.getEngineType)
      engineTypeLabel.setVersion(userCreatorEngineType.getVersion)
      val configuredResource =
        UserConfiguration.getUserConfiguredResource(resourceType, userCreatorLabel, engineTypeLabel)
      labelResource.setMaxResource(configuredResource)
      labelResource.setMinResource(Resource.initResource(labelResource.getResourceType))
      labelResource.setLeftResource(
        labelResource.getMaxResource - labelResource.getUsedResource - labelResource.getLockedResource
      )
      val idList = new util.ArrayList[Integer]()
      idList.add(resourceId)
      resourceManagerPersistence.deleteResourceById(idList)
      resourceManagerPersistence.deleteResourceRelByResourceId(idList)
      val labelList = new util.ArrayList[Label[_]]()
      labelList.add(userCreatorLabel)
      labelList.add(engineTypeLabel)
      val combinedLabel = combinedLabelBuilder.build("", labelList)
      labelResourceService.setLabelResource(
        resourceLabel.get(0),
        labelResource,
        combinedLabel.getStringValue
      )
    }
  }

  @Transactional
  def resetAllUserResource(combinedLabelKey: String): Unit = {
    val userLabels = labelManagerPersistence.getLabelByPattern("%", combinedLabelKey, 0, 0)
    val resourceIdList = userLabels.asScala.map(_.getResourceId)
    resourceManagerPersistence.deleteResourceById(resourceIdList.asJava)
    resourceManagerPersistence.deleteResourceRelByResourceId(resourceIdList.asJava)
  }

}
