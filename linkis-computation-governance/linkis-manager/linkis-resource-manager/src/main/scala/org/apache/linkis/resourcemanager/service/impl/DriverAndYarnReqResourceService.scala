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
 
package org.apache.linkis.resourcemanager.service.impl

import org.apache.linkis.manager.common.entity.resource.ResourceType.DriverAndYarn
import org.apache.linkis.manager.common.entity.resource.{DriverAndYarnResource, NodeResource, Resource, ResourceSerializer, ResourceType, YarnResource}
import org.apache.linkis.resourcemanager.domain.RMLabelContainer
import org.apache.linkis.resourcemanager.exception.{RMErrorCode, RMWarnException}
import org.apache.linkis.resourcemanager.external.service.ExternalResourceService
import org.apache.linkis.resourcemanager.external.yarn.YarnResourceIdentifier
import org.apache.linkis.resourcemanager.service.{LabelResourceService, RequestResourceService}
import org.json4s.DefaultFormats

class DriverAndYarnReqResourceService(labelResourceService: LabelResourceService, externalResourceService: ExternalResourceService) extends RequestResourceService(labelResourceService) {

  implicit val formats = DefaultFormats + ResourceSerializer

  override val resourceType: ResourceType = DriverAndYarn

  override def canRequest(labelContainer: RMLabelContainer, resource: NodeResource): Boolean = {
    if (!super.canRequest(labelContainer, resource)) {
      return false
    }
    val requestedDriverAndYarnResource = resource.getMaxResource.asInstanceOf[DriverAndYarnResource]
    val requestedYarnResource = requestedDriverAndYarnResource.yarnResource
    val yarnIdentifier = new YarnResourceIdentifier(requestedYarnResource.queueName)
    val providedYarnResource = externalResourceService.getResource(ResourceType.Yarn, labelContainer, yarnIdentifier)
    val (maxCapacity, usedCapacity) = (providedYarnResource.getMaxResource, providedYarnResource.getUsedResource)
    logger.debug(s"This queue: ${requestedYarnResource.queueName} used resource:$usedCapacity and max resource: $maxCapacity")
    val queueLeftResource = maxCapacity  - usedCapacity
    logger.info(s"queue: ${requestedYarnResource.queueName} left $queueLeftResource, this request requires: $requestedYarnResource")
    if (queueLeftResource < requestedYarnResource) {
      logger.info(s"user: ${labelContainer.getUserCreatorLabel.getUser} request queue resource $requestedYarnResource > left resource $queueLeftResource")
      val notEnoughMessage = generateQueueNotEnoughMessage(requestedYarnResource, queueLeftResource)
      throw new RMWarnException(notEnoughMessage._1, notEnoughMessage._2)
    } else true
  }

  def generateQueueNotEnoughMessage(requestResource: Resource, availableResource: Resource) : (Int, String) = {
    requestResource match {
      case yarn: YarnResource =>
        val yarnAvailable = availableResource.asInstanceOf[YarnResource]
        if(yarn.queueCores > yarnAvailable.queueCores) {
          (RMErrorCode.CLUSTER_QUEUE_CPU_INSUFFICIENT.getCode, RMErrorCode.CLUSTER_QUEUE_CPU_INSUFFICIENT.getMessage)
        } else if (yarn.queueMemory > yarnAvailable.queueMemory) {
          (RMErrorCode.CLUSTER_QUEUE_MEMORY_INSUFFICIENT.getCode, RMErrorCode.CLUSTER_QUEUE_MEMORY_INSUFFICIENT.getMessage)
        } else {
          (RMErrorCode.CLUSTER_QUEUE_INSTANCES_INSUFFICIENT.getCode, RMErrorCode.CLUSTER_QUEUE_INSTANCES_INSUFFICIENT.getMessage)
        }
      case _ =>
        (RMErrorCode.CLUSTER_QUEUE_MEMORY_INSUFFICIENT.getCode, RMErrorCode.CLUSTER_QUEUE_MEMORY_INSUFFICIENT.getMessage)
    }
  }

}
