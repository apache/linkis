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

import org.apache.linkis.manager.am.vo.CanCreateECRes
import org.apache.linkis.manager.common.constant.RMConstant
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.common.entity.resource.ResourceType.DriverAndYarn
import org.apache.linkis.manager.common.exception.RMWarnException
import org.apache.linkis.manager.common.protocol.engine.EngineCreateRequest
import org.apache.linkis.manager.rm.domain.RMLabelContainer
import org.apache.linkis.manager.rm.exception.RMErrorCode
import org.apache.linkis.manager.rm.external.service.ExternalResourceService
import org.apache.linkis.manager.rm.external.yarn.YarnResourceIdentifier
import org.apache.linkis.manager.rm.service.{LabelResourceService, RequestResourceService}
import org.apache.linkis.manager.rm.utils.AcrossClusterRulesJudgeUtils.{
  originClusterResourceCheck,
  targetClusterResourceCheck
}
import org.apache.linkis.manager.rm.utils.RMUtils

class DriverAndYarnReqResourceService(
    labelResourceService: LabelResourceService,
    externalResourceService: ExternalResourceService
) extends RequestResourceService(labelResourceService) {

  override val resourceType: ResourceType = DriverAndYarn

  override def canRequestResource(
      labelContainer: RMLabelContainer,
      resource: NodeResource,
      engineCreateRequest: EngineCreateRequest
  ): CanCreateECRes = {
    val canCreateECRes = super.canRequestResource(labelContainer, resource, engineCreateRequest)
    if (!canCreateECRes.isCanCreateEC) {
      return canCreateECRes
    }
    val requestedDriverAndYarnResource =
      resource.getMaxResource.asInstanceOf[DriverAndYarnResource]
    val requestedYarnResource = requestedDriverAndYarnResource.getYarnResource
    val yarnIdentifier = new YarnResourceIdentifier(requestedYarnResource.getQueueName)
    val providedYarnResource =
      externalResourceService.getResource(ResourceType.Yarn, labelContainer, yarnIdentifier)
    val (maxCapacity, usedCapacity) =
      (providedYarnResource.getMaxResource, providedYarnResource.getUsedResource)
    logger.debug(
      s"This queue: ${requestedYarnResource.getQueueName} used resource:$usedCapacity and max resource: $maxCapacity"
    )
    val queueLeftResource = maxCapacity.minus(usedCapacity)
    logger.info(
      s"queue: ${requestedYarnResource.getQueueName} left $queueLeftResource, this request requires: $requestedYarnResource"
    )
    if (!queueLeftResource.notLess(requestedYarnResource)) {
      logger.info(
        s"user: ${labelContainer.getUserCreatorLabel.getUser} request queue resource $requestedYarnResource > left resource $queueLeftResource"
      )

      val notEnoughMessage =
        generateQueueNotEnoughMessage(requestedYarnResource, queueLeftResource, maxCapacity)
      canCreateECRes.setCanCreateEC(false);
      canCreateECRes.setReason(notEnoughMessage._2)
    }
    canCreateECRes.setYarnResource(RMUtils.serializeResource(queueLeftResource))
    canCreateECRes
  }

  override def canRequest(
      labelContainer: RMLabelContainer,
      resource: NodeResource,
      engineCreateRequest: EngineCreateRequest
  ): Boolean = {
    if (!super.canRequest(labelContainer, resource, engineCreateRequest)) {
      return false
    }
    val requestedDriverAndYarnResource =
      resource.getMaxResource.asInstanceOf[DriverAndYarnResource]
    val requestedYarnResource = requestedDriverAndYarnResource.getYarnResource
    val yarnIdentifier = new YarnResourceIdentifier(requestedYarnResource.getQueueName)
    val providedYarnResource =
      externalResourceService.getResource(ResourceType.Yarn, labelContainer, yarnIdentifier)
    val (maxCapacity, usedCapacity) =
      (providedYarnResource.getMaxResource, providedYarnResource.getUsedResource)
    logger.debug(
      s"This queue: ${requestedYarnResource.getQueueName} used resource:$usedCapacity and max resource: $maxCapacity"
    )
    val queueLeftResource = maxCapacity.minus(usedCapacity)
    logger.info(
      s"queue: ${requestedYarnResource.getQueueName} left $queueLeftResource, this request requires: $requestedYarnResource"
    )
    if (engineCreateRequest.getProperties != null) {
      // judge if is cross cluster task and origin cluster priority first
      originClusterResourceCheck(engineCreateRequest, maxCapacity, usedCapacity)
    }
    if (!queueLeftResource.notLess(requestedYarnResource)) {
      logger.info(
        s"user: ${labelContainer.getUserCreatorLabel.getUser} request queue resource $requestedYarnResource > left resource $queueLeftResource"
      )
      val notEnoughMessage =
        generateQueueNotEnoughMessage(requestedYarnResource, queueLeftResource, maxCapacity)
      throw new RMWarnException(notEnoughMessage._1, notEnoughMessage._2)
    }
    if (engineCreateRequest.getProperties != null) {
      // judge if is cross cluster task and target cluster priority first
      targetClusterResourceCheck(
        labelContainer,
        engineCreateRequest,
        maxCapacity,
        usedCapacity,
        externalResourceService
      )
    }
    true
  }

  def generateQueueNotEnoughMessage(
      requestResource: Resource,
      availableResource: Resource,
      maxResource: Resource
  ): (Int, String) = {
    requestResource match {
      case yarn: YarnResource =>
        val yarnAvailable = availableResource.asInstanceOf[YarnResource]
        val maxYarn = maxResource.asInstanceOf[YarnResource]
        if (yarn.getQueueCores > yarnAvailable.getQueueCores) {
          (
            RMErrorCode.CLUSTER_QUEUE_CPU_INSUFFICIENT.getErrorCode,
            RMErrorCode.CLUSTER_QUEUE_CPU_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.CPU,
                RMConstant.CPU_UNIT,
                yarn.getQueueCores,
                yarnAvailable.getQueueCores,
                maxYarn.getQueueCores,
                yarn.getQueueName
              )
          )
        } else if (yarn.getQueueMemory > yarnAvailable.getQueueMemory) {
          (
            RMErrorCode.CLUSTER_QUEUE_MEMORY_INSUFFICIENT.getErrorCode,
            RMErrorCode.CLUSTER_QUEUE_MEMORY_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.MEMORY,
                RMConstant.MEMORY_UNIT_BYTE,
                yarn.getQueueMemory,
                yarnAvailable.getQueueMemory,
                maxYarn.getQueueMemory,
                yarn.getQueueName
              )
          )
        } else {
          (
            RMErrorCode.CLUSTER_QUEUE_INSTANCES_INSUFFICIENT.getErrorCode,
            RMErrorCode.CLUSTER_QUEUE_INSTANCES_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.APP_INSTANCE,
                RMConstant.INSTANCE_UNIT,
                yarn.getQueueInstances,
                yarnAvailable.getQueueInstances,
                maxYarn.getQueueInstances,
                yarn.getQueueName
              )
          )
        }
      case _ =>
        (
          RMErrorCode.CLUSTER_QUEUE_MEMORY_INSUFFICIENT.getErrorCode,
          RMErrorCode.CLUSTER_QUEUE_MEMORY_INSUFFICIENT.getErrorDesc + " Unusual insufficient queue memory."
        )
    }
  }

}
