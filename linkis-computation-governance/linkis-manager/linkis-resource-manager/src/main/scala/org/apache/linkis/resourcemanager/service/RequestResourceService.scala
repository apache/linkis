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
 
package org.apache.linkis.resourcemanager.service

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel
import org.apache.linkis.resourcemanager.domain.RMLabelContainer
import org.apache.linkis.resourcemanager.exception.{RMErrorCode, RMWarnException}
import org.apache.linkis.resourcemanager.utils.{RMUtils, UserConfiguration}

abstract class RequestResourceService(labelResourceService: LabelResourceService) extends Logging{

  val resourceType: ResourceType = ResourceType.Default

  val enableRequest = RMUtils.RM_REQUEST_ENABLE.getValue

  def canRequest(labelContainer: RMLabelContainer, resource: NodeResource): Boolean = {

    labelContainer.getCurrentLabel match {
      case emInstanceLabel: EMInstanceLabel =>
        return checkEMResource(labelContainer.getUserCreatorLabel.getUser, emInstanceLabel, resource)
      case _ =>
    }

    var labelResource = labelResourceService.getLabelResource(labelContainer.getCurrentLabel)
    val requestResource = resource.getMinResource
    // for configuration resource
    if (labelContainer.getCombinedUserCreatorEngineTypeLabel.equals(labelContainer.getCurrentLabel)) {
      if (labelResource == null) {
        labelResource = new CommonNodeResource
        labelResource.setResourceType(resource.getResourceType)
        labelResource.setUsedResource(Resource.initResource(resource.getResourceType))
        labelResource.setLockedResource(Resource.initResource(resource.getResourceType))
        logger.info(s"ResourceInit: ${labelContainer.getCurrentLabel.getStringValue} ")
      }
      val configuredResource = UserConfiguration.getUserConfiguredResource(resource.getResourceType, labelContainer.getUserCreatorLabel, labelContainer.getEngineTypeLabel)
      logger.debug(s"Get configured resource ${configuredResource} for [${labelContainer.getUserCreatorLabel}] and [${labelContainer.getEngineTypeLabel}] ")
      labelResource.setMaxResource(configuredResource)
      labelResource.setMinResource(Resource.initResource(labelResource.getResourceType))
      labelResource.setLeftResource(labelResource.getMaxResource - labelResource.getUsedResource - labelResource.getLockedResource)
      labelResourceService.setLabelResource(labelContainer.getCurrentLabel, labelResource, labelContainer.getCombinedUserCreatorEngineTypeLabel.getStringValue)
      logger.debug(s"${labelContainer.getCurrentLabel} to request [${requestResource}]  \t labelResource: Max: ${labelResource.getMaxResource}  \t " +
        s"use:  ${labelResource.getUsedResource}  \t locked: ${labelResource.getLockedResource}")
    }
    logger.debug(s"Label [${labelContainer.getCurrentLabel}] has resource + [${labelResource }]")
    if (labelResource != null) {
      val labelAvailableResource = labelResource.getLeftResource
      if(labelAvailableResource < requestResource && enableRequest) {
        logger.info(s"Failed check: ${labelContainer.getUserCreatorLabel.getUser} want to use label [${labelContainer.getCurrentLabel}] resource[${requestResource}] > label available resource[${labelAvailableResource}]")
        // TODO sendAlert(moduleInstance, user, creator, requestResource, moduleAvailableResource.resource, moduleLeftResource)
        val notEnoughMessage = generateNotEnoughMessage(requestResource, labelAvailableResource)
        throw new RMWarnException(notEnoughMessage._1, notEnoughMessage._2)
      }
      logger.debug(s"Passed check: ${labelContainer.getUserCreatorLabel.getUser} want to use label [${labelContainer.getCurrentLabel}] resource[${requestResource}] <= label available resource[${labelAvailableResource}]")
      true
    } else {
      logger.warn(s"No resource available found for label ${labelContainer.getCurrentLabel}")
      throw new RMWarnException(11201, s"Resource label ${labelContainer.getCurrentLabel} has no resource, please check resource in db.")
    }
  }

  private def checkEMResource(user: String, emInstanceLabel: EMInstanceLabel, resource: NodeResource): Boolean = {
    val labelResource = labelResourceService.getLabelResource(emInstanceLabel)
    val requestResource = resource.getMinResource
    logger.debug(s"emInstanceLabel resource info ${labelResource }")
    if (labelResource != null) {
      val labelAvailableResource = labelResource.getLeftResource
      if(labelAvailableResource < requestResource && enableRequest) {
        logger.info(s"user want to use resource[${requestResource}] > em ${emInstanceLabel.getInstance()} available resource[${labelAvailableResource}]")
        // TODO sendAlert(moduleInstance, user, creator, requestResource, moduleAvailableResource.resource, moduleLeftResource)
        val notEnoughMessage = generateECMNotEnoughMessage(requestResource, labelAvailableResource)
        throw new RMWarnException(notEnoughMessage._1, notEnoughMessage._2)
      }
      logger.debug(s"Passed check: resource[${requestResource}] want to use em ${emInstanceLabel.getInstance()}  available resource[${labelAvailableResource}]")
      true
    } else {
      logger.warn(s"No resource available found for em ${emInstanceLabel.getInstance()} ")
      throw new RMWarnException(11201, s"No resource available found for em ${emInstanceLabel.getInstance()} ")
    }
  }

  def generateECMNotEnoughMessage(requestResource: Resource, availableResource: Resource) : (Int, String) = {
    val loadRequestResource = requestResource match {
      case li: LoadInstanceResource => li
      case driverAndYarnResource: DriverAndYarnResource => driverAndYarnResource.loadInstanceResource
      case _ => null
    }
    loadRequestResource match {
      case li: LoadInstanceResource =>
        val loadInstanceAvailable = availableResource.asInstanceOf[LoadInstanceResource]
        if(li.cores > loadInstanceAvailable.cores) {
          (RMErrorCode.ECM_CPU_INSUFFICIENT.getCode, RMErrorCode.ECM_CPU_INSUFFICIENT.getMessage)
        } else if (li.memory > loadInstanceAvailable.memory) {
          (RMErrorCode.ECM_MEMORY_INSUFFICIENT.getCode, RMErrorCode.ECM_MEMORY_INSUFFICIENT.getMessage)
        } else {
          (RMErrorCode.ECM_INSTANCES_INSUFFICIENT.getCode, RMErrorCode.ECM_INSTANCES_INSUFFICIENT.getMessage)
        }
      case _ =>
        (RMErrorCode.ECM_RESOURCE_INSUFFICIENT.getCode, RMErrorCode.ECM_RESOURCE_INSUFFICIENT.getMessage)
    }
  }


  def generateNotEnoughMessage(requestResource: Resource, availableResource: Resource) : (Int, String) = {
    requestResource match {
      case m: MemoryResource =>
        (RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getCode, RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getMessage)
      case c: CPUResource =>
        (RMErrorCode.DRIVER_CPU_INSUFFICIENT.getCode, RMErrorCode.DRIVER_CPU_INSUFFICIENT.getMessage)
      case i: InstanceResource =>
        (RMErrorCode.INSTANCES_INSUFFICIENT.getCode, RMErrorCode.INSTANCES_INSUFFICIENT.getMessage)
      case l: LoadResource =>
        val loadAvailable = availableResource.asInstanceOf[LoadResource]
        if(l.cores > loadAvailable.cores) {
          (RMErrorCode.DRIVER_CPU_INSUFFICIENT.getCode, RMErrorCode.DRIVER_CPU_INSUFFICIENT.getMessage)
        } else {
          (RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getCode, RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getMessage)
        }
      case li: LoadInstanceResource =>
        val loadInstanceAvailable = availableResource.asInstanceOf[LoadInstanceResource]
        if(li.cores > loadInstanceAvailable.cores) {
          (RMErrorCode.DRIVER_CPU_INSUFFICIENT.getCode, RMErrorCode.DRIVER_CPU_INSUFFICIENT.getMessage)
        } else if (li.memory > loadInstanceAvailable.memory) {
          (RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getCode, RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getMessage)
        } else {
          (RMErrorCode.INSTANCES_INSUFFICIENT.getCode, RMErrorCode.INSTANCES_INSUFFICIENT.getMessage)
        }
      case yarn: YarnResource =>
        val yarnAvailable = availableResource.asInstanceOf[YarnResource]
        if(yarn.queueCores > yarnAvailable.queueCores) {
          (RMErrorCode.QUEUE_CPU_INSUFFICIENT.getCode, RMErrorCode.QUEUE_CPU_INSUFFICIENT.getMessage)
        } else if (yarn.queueMemory > yarnAvailable.queueMemory) {
          (RMErrorCode.QUEUE_MEMORY_INSUFFICIENT.getCode, RMErrorCode.QUEUE_MEMORY_INSUFFICIENT.getMessage)
        } else {
          (RMErrorCode.QUEUE_INSTANCES_INSUFFICIENT.getCode, RMErrorCode.QUEUE_INSTANCES_INSUFFICIENT.getMessage)
        }
      case dy: DriverAndYarnResource =>
        val dyAvailable = availableResource.asInstanceOf[DriverAndYarnResource]
        if(dy.loadInstanceResource.memory > dyAvailable.loadInstanceResource.memory ||
          dy.loadInstanceResource.cores > dyAvailable.loadInstanceResource.cores ||
          dy.loadInstanceResource.instances > dyAvailable.loadInstanceResource.instances) {
          val detail = generateNotEnoughMessage(dy.loadInstanceResource, dyAvailable.loadInstanceResource)
          (detail._1, {detail._2})
        } else {
          val detail = generateNotEnoughMessage(dy.yarnResource, dyAvailable.yarnResource)
          (detail._1, {detail._2})
        }
      case s: SpecialResource => throw new RMWarnException(11003, " not supported resource type " + s.getClass)
      case r: Resource => throw new RMWarnException(11003, "not supported resource type " + r.getClass)
    }
  }
}

