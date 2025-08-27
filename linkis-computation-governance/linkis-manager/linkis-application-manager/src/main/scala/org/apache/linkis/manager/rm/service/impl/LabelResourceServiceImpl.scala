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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource
import org.apache.linkis.manager.common.entity.resource.NodeResource
import org.apache.linkis.manager.common.utils.ResourceUtils
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.service.ResourceLabelService
import org.apache.linkis.manager.persistence.{LabelManagerPersistence, ResourceManagerPersistence}
import org.apache.linkis.manager.rm.domain.RMLabelContainer
import org.apache.linkis.manager.rm.service.LabelResourceService

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.CannotGetJdbcConnectionException
import org.springframework.retry.annotation.{Backoff, Retryable}
import org.springframework.stereotype.Component

import java.util

import scala.collection.JavaConverters._

@Component
class LabelResourceServiceImpl extends LabelResourceService with Logging {

  @Autowired
  var resourceManagerPersistence: ResourceManagerPersistence = _

  @Autowired
  var labelManagerPersistence: LabelManagerPersistence = _

  @Autowired
  var resourceLabelService: ResourceLabelService = _

  private val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  override def getLabelResource(label: Label[_]): NodeResource =
    resourceLabelService.getResourceByLabel(label)

  @Retryable(
    value = Array(classOf[CannotGetJdbcConnectionException]),
    maxAttempts = 5,
    backoff = new Backoff(delay = 10000)
  )
  override def setLabelResource(
      label: Label[_],
      nodeResource: NodeResource,
      source: String
  ): Unit = {
    resourceLabelService.setResourceToLabel(label, nodeResource, source)
  }

  override def getResourcesByUser(user: String): Array[NodeResource] = {
    resourceManagerPersistence
      .getResourceByUser(user)
      .asScala
      .map(ResourceUtils.fromPersistenceResource)
      .toArray
  }

  override def enrichLabels(labels: util.List[Label[_]]): RMLabelContainer = {
    new RMLabelContainer(labels)
  }

  override def removeResourceByLabel(label: Label[_]): Unit = {
    resourceLabelService.removeResourceByLabel(label)
  }

  override def setEngineConnLabelResource(
      label: Label[_],
      nodeResource: NodeResource,
      source: String
  ): Unit = resourceLabelService.setEngineConnResourceToLabel(label, nodeResource, source)

  override def getLabelsByResource(resource: PersistenceResource): Array[Label[_]] = {
    labelManagerPersistence
      .getLabelByResource(resource)
      .asScala
      .map { label =>
        labelFactory.createLabel(label.getLabelKey, label.getValue)
      }
      .toArray
  }

  override def getPersistenceResource(label: Label[_]): PersistenceResource = {
    resourceLabelService.getPersistenceResourceByLabel(label)
  }

}
