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

package org.apache.linkis.manager.rm.service

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.constant.RMConstant
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary._
import org.apache.linkis.manager.common.exception.RMWarnException
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel
import org.apache.linkis.manager.rm.domain.RMLabelContainer
import org.apache.linkis.manager.rm.exception.RMErrorCode
import org.apache.linkis.manager.rm.utils.{RMUtils, UserConfiguration}

import java.text.MessageFormat

abstract class RequestResourceService(labelResourceService: LabelResourceService) extends Logging {

  val resourceType: ResourceType = ResourceType.Default

  val enableRequest = RMUtils.RM_REQUEST_ENABLE.getValue

  def canRequest(labelContainer: RMLabelContainer, resource: NodeResource): Boolean = {

    labelContainer.getCurrentLabel match {
      case emInstanceLabel: EMInstanceLabel =>
        return checkEMResource(
          labelContainer.getUserCreatorLabel.getUser,
          emInstanceLabel,
          resource
        )
      case _ =>
    }

    var labelResource = labelResourceService.getLabelResource(labelContainer.getCurrentLabel)
    val requestResource = resource.getMinResource
    // for configuration resource
    if (
        labelContainer.getCombinedUserCreatorEngineTypeLabel.equals(labelContainer.getCurrentLabel)
    ) {
      if (labelResource == null) {
        labelResource = new CommonNodeResource
        labelResource.setResourceType(resource.getResourceType)
        labelResource.setUsedResource(Resource.initResource(resource.getResourceType))
        labelResource.setLockedResource(Resource.initResource(resource.getResourceType))
        logger.info(s"ResourceInit: ${labelContainer.getCurrentLabel.getStringValue} ")
      }
      val configuredResource = UserConfiguration.getUserConfiguredResource(
        resource.getResourceType,
        labelContainer.getUserCreatorLabel,
        labelContainer.getEngineTypeLabel
      )
      logger.debug(
        s"Get configured resource ${configuredResource} for [${labelContainer.getUserCreatorLabel}] and [${labelContainer.getEngineTypeLabel}] "
      )
      labelResource.setMaxResource(configuredResource)
      labelResource.setMinResource(Resource.initResource(labelResource.getResourceType))
      labelResource.setLeftResource(
        labelResource.getMaxResource - labelResource.getUsedResource - labelResource.getLockedResource
      )
      labelResourceService.setLabelResource(
        labelContainer.getCurrentLabel,
        labelResource,
        labelContainer.getCombinedUserCreatorEngineTypeLabel.getStringValue
      )
      logger.debug(
        s"${labelContainer.getCurrentLabel} to request [${requestResource}]  \t labelResource: Max: ${labelResource.getMaxResource}  \t " +
          s"use:  ${labelResource.getUsedResource}  \t locked: ${labelResource.getLockedResource}"
      )
    }
    logger.debug(s"Label [${labelContainer.getCurrentLabel}] has resource + [${labelResource}]")
    if (labelResource != null) {
      val labelAvailableResource = labelResource.getLeftResource
      val labelMaxResource = labelResource.getMaxResource
      if (labelAvailableResource < requestResource && enableRequest) {
        logger.info(
          s"Failed check: ${labelContainer.getUserCreatorLabel.getUser} want to use label [${labelContainer.getCurrentLabel}] resource[${requestResource}] > " +
            s"label available resource[${labelAvailableResource}]"
        )
        // TODO sendAlert(moduleInstance, user, creator, requestResource, moduleAvailableResource.resource, moduleLeftResource)
        val notEnoughMessage =
          generateNotEnoughMessage(requestResource, labelAvailableResource, labelMaxResource)
        throw new RMWarnException(notEnoughMessage._1, notEnoughMessage._2)
      }
      logger.debug(
        s"Passed check: ${labelContainer.getUserCreatorLabel.getUser} want to use label [${labelContainer.getCurrentLabel}] resource[${requestResource}] <= " +
          s"label available resource[${labelAvailableResource}]"
      )
      true
    } else {
      logger.warn(s"No resource available found for label ${labelContainer.getCurrentLabel}")
      throw new RMWarnException(
        NO_RESOURCE.getErrorCode,
        MessageFormat.format(NO_RESOURCE.getErrorDesc(), labelContainer.getCurrentLabel)
      )
    }
  }

  private def checkEMResource(
      user: String,
      emInstanceLabel: EMInstanceLabel,
      resource: NodeResource
  ): Boolean = {
    val labelResource = labelResourceService.getLabelResource(emInstanceLabel)
    val requestResource = resource.getMinResource
    logger.debug(s"emInstanceLabel resource info ${labelResource}")
    if (labelResource != null) {
      val labelAvailableResource = labelResource.getLeftResource
      if (labelAvailableResource < requestResource && enableRequest) {
        logger.info(
          s"user want to use resource[${requestResource}] > em ${emInstanceLabel.getInstance()} available resource[${labelAvailableResource}]"
        )
        // TODO sendAlert(moduleInstance, user, creator, requestResource, moduleAvailableResource.resource, moduleLeftResource)
        val notEnoughMessage = generateECMNotEnoughMessage(
          requestResource,
          labelAvailableResource,
          labelResource.getMaxResource
        )
        throw new RMWarnException(notEnoughMessage._1, notEnoughMessage._2)
      }
      logger.debug(s"Passed check: resource[${requestResource}] want to use em ${emInstanceLabel
        .getInstance()}  available resource[${labelAvailableResource}]")
      true
    } else {
      logger.warn(s"No resource available found for em ${emInstanceLabel.getInstance()} ")
      throw new RMWarnException(
        NO_RESOURCE_AVAILABLE.getErrorCode,
        MessageFormat.format(NO_RESOURCE_AVAILABLE.getErrorDesc, emInstanceLabel.getInstance())
      )
    }
  }

  def generateECMNotEnoughMessage(
      requestResource: Resource,
      availableResource: Resource,
      maxResource: Resource
  ): (Int, String) = {
    val loadRequestResource = requestResource match {
      case li: LoadInstanceResource => li
      case driverAndYarnResource: DriverAndYarnResource =>
        driverAndYarnResource.loadInstanceResource
      case _ => null
    }
    loadRequestResource match {
      case li: LoadInstanceResource =>
        val loadInstanceAvailable = availableResource.asInstanceOf[LoadInstanceResource]
        val loadInstanceMax = maxResource.asInstanceOf[LoadInstanceResource]
        if (li.cores > loadInstanceAvailable.cores) {
          (
            RMErrorCode.ECM_CPU_INSUFFICIENT.getErrorCode,
            RMErrorCode.ECM_CPU_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.CPU,
                RMConstant.CPU_UNIT,
                li.cores,
                loadInstanceAvailable.cores,
                loadInstanceMax.cores
              )
          )
        } else if (li.memory > loadInstanceAvailable.memory) {
          (
            RMErrorCode.ECM_MEMORY_INSUFFICIENT.getErrorCode,
            RMErrorCode.ECM_MEMORY_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.MEMORY,
                RMConstant.MEMORY_UNIT_BYTE,
                li.memory,
                loadInstanceAvailable.memory,
                loadInstanceMax.memory
              )
          )
        } else {
          (
            RMErrorCode.ECM_INSTANCES_INSUFFICIENT.getErrorCode,
            RMErrorCode.ECM_INSTANCES_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.APP_INSTANCE,
                RMConstant.INSTANCE_UNIT,
                li.instances,
                loadInstanceAvailable.instances,
                loadInstanceMax.instances
              )
          )
        }
      case _ =>
        (
          RMErrorCode.ECM_RESOURCE_INSUFFICIENT.getErrorCode,
          RMErrorCode.ECM_RESOURCE_INSUFFICIENT.getErrorDesc + " Unusual insufficient queue memory."
        )
    }
  }

  def generateNotEnoughMessage(
      requestResource: Resource,
      availableResource: Resource,
      maxResource: Resource
  ): (Int, String) = {
    requestResource match {
      case m: MemoryResource =>
        val avail = availableResource.asInstanceOf[MemoryResource]
        val max = maxResource.asInstanceOf[MemoryResource]
        (
          RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorCode,
          RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorDesc +
            RMUtils.getResourceInfoMsg(
              RMConstant.MEMORY,
              RMConstant.MEMORY_UNIT_BYTE,
              m.memory,
              avail.memory,
              max.memory
            )
        )
      case i: InstanceResource =>
        val avail = availableResource.asInstanceOf[InstanceResource]
        val max = maxResource.asInstanceOf[InstanceResource]
        (
          RMErrorCode.INSTANCES_INSUFFICIENT.getErrorCode,
          RMErrorCode.INSTANCES_INSUFFICIENT.getErrorDesc +
            RMUtils.getResourceInfoMsg(
              RMConstant.APP_INSTANCE,
              RMConstant.INSTANCE_UNIT,
              i.instances,
              avail.instances,
              max.instances
            )
        )
      case c: CPUResource =>
        val avail = availableResource.asInstanceOf[CPUResource]
        val max = maxResource.asInstanceOf[CPUResource]
        (
          RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorCode,
          RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorDesc +
            RMUtils.getResourceInfoMsg(
              RMConstant.CPU,
              RMConstant.CPU_UNIT,
              c.cores,
              avail.cores,
              max.cores
            )
        )
      case l: LoadResource =>
        val loadAvailable = availableResource.asInstanceOf[LoadResource]
        val avail = availableResource.asInstanceOf[LoadResource]
        val max = maxResource.asInstanceOf[LoadResource]
        if (l.cores > loadAvailable.cores) {
          (
            RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorCode,
            RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.CPU,
                RMConstant.CPU_UNIT,
                l.cores,
                avail.cores,
                max.cores
              )
          )
        } else {
          (
            RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorCode,
            RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.MEMORY,
                RMConstant.MEMORY_UNIT_BYTE,
                l.memory,
                avail.memory,
                max.memory
              )
          )
        }
      case li: LoadInstanceResource =>
        val loadInstanceAvailable = availableResource.asInstanceOf[LoadInstanceResource]
        val avail = availableResource.asInstanceOf[LoadInstanceResource]
        val max = maxResource.asInstanceOf[LoadInstanceResource]
        if (li.cores > loadInstanceAvailable.cores) {
          (
            RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorCode,
            RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.CPU,
                RMConstant.CPU_UNIT,
                li.cores,
                avail.cores,
                max.cores
              )
          )
        } else if (li.memory > loadInstanceAvailable.memory) {
          (
            RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorCode,
            RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.MEMORY,
                RMConstant.MEMORY_UNIT_BYTE,
                li.memory,
                avail.memory,
                max.memory
              )
          )
        } else {
          (
            RMErrorCode.INSTANCES_INSUFFICIENT.getErrorCode,
            RMErrorCode.INSTANCES_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.APP_INSTANCE,
                RMConstant.INSTANCE_UNIT,
                li.instances,
                avail.instances,
                max.instances
              )
          )
        }
      case yarn: YarnResource =>
        val yarnAvailable = availableResource.asInstanceOf[YarnResource]
        val avail = availableResource.asInstanceOf[YarnResource]
        val max = maxResource.asInstanceOf[YarnResource]
        if (yarn.queueCores > yarnAvailable.queueCores) {
          (
            RMErrorCode.QUEUE_CPU_INSUFFICIENT.getErrorCode,
            RMErrorCode.QUEUE_CPU_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.CPU,
                RMConstant.CPU_UNIT,
                yarn.queueCores,
                avail.queueCores,
                max.queueCores
              )
          )
        } else if (yarn.queueMemory > yarnAvailable.queueMemory) {
          (
            RMErrorCode.QUEUE_MEMORY_INSUFFICIENT.getErrorCode,
            RMErrorCode.QUEUE_MEMORY_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.MEMORY,
                RMConstant.MEMORY_UNIT_BYTE,
                yarn.queueMemory,
                avail.queueMemory,
                max.queueMemory
              )
          )
        } else {
          (
            RMErrorCode.QUEUE_INSTANCES_INSUFFICIENT.getErrorCode,
            RMErrorCode.QUEUE_INSTANCES_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.APP_INSTANCE,
                RMConstant.INSTANCE_UNIT,
                yarn.queueInstances,
                avail.queueInstances,
                max.queueInstances
              )
          )
        }
      case dy: DriverAndYarnResource =>
        val dyAvailable = availableResource.asInstanceOf[DriverAndYarnResource]
        val dyMax = maxResource.asInstanceOf[DriverAndYarnResource]
        if (
            dy.loadInstanceResource.memory > dyAvailable.loadInstanceResource.memory ||
            dy.loadInstanceResource.cores > dyAvailable.loadInstanceResource.cores ||
            dy.loadInstanceResource.instances > dyAvailable.loadInstanceResource.instances
        ) {
          val detail = generateNotEnoughMessage(
            dy.loadInstanceResource,
            dyAvailable.loadInstanceResource,
            dyMax.loadInstanceResource
          )
          (detail._1, { detail._2 })
        } else {
          val detail =
            generateNotEnoughMessage(dy.yarnResource, dyAvailable.yarnResource, dyMax.yarnResource)
          (detail._1, { detail._2 })
        }
      case s: SpecialResource =>
        throw new RMWarnException(
          NOT_RESOURCE_TYPE.getErrorCode,
          MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, s.getClass)
        )
      case r: Resource =>
        throw new RMWarnException(
          NOT_RESOURCE_TYPE.getErrorCode,
          MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc, r.getClass)
        )
    }
  }

}
