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
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.am.conf.AMConfiguration.{
  SUPPORT_CLUSTER_RULE_EC_TYPES,
  YARN_QUEUE_NAME_CONFIG_KEY
}
import org.apache.linkis.manager.am.vo.CanCreateECRes
import org.apache.linkis.manager.common.constant.RMConstant
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary._
import org.apache.linkis.manager.common.exception.{RMErrorException, RMWarnException}
import org.apache.linkis.manager.common.protocol.engine.EngineCreateRequest
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.manager.rm.domain.RMLabelContainer
import org.apache.linkis.manager.rm.exception.RMErrorCode
import org.apache.linkis.manager.rm.external.service.ExternalResourceService
import org.apache.linkis.manager.rm.external.yarn.YarnResourceIdentifier
import org.apache.linkis.manager.rm.utils.{RMUtils, UserConfiguration}
import org.apache.linkis.manager.rm.utils.AcrossClusterRulesJudgeUtils.{
  originClusterResourceCheck,
  targetClusterResourceCheck
}

import org.apache.commons.lang3.StringUtils

import java.text.MessageFormat
import java.util

abstract class RequestResourceService(labelResourceService: LabelResourceService) extends Logging {

  val resourceType: ResourceType = ResourceType.Default

  val enableRequest = RMUtils.RM_REQUEST_ENABLE.getValue

  var externalResourceService: ExternalResourceService = null

  def setExternalResourceService(externalResourceService: ExternalResourceService): Unit = {
    this.externalResourceService = externalResourceService
  }

  def canRequestResource(
      labelContainer: RMLabelContainer,
      resource: NodeResource,
      engineCreateRequest: EngineCreateRequest
  ): CanCreateECRes = {
    val canCreateECRes = new CanCreateECRes
    val emInstanceLabel = labelContainer.getEMInstanceLabel
    val ecmResource = labelResourceService.getLabelResource(emInstanceLabel)
    val requestResource = resource.getMinResource
    if (ecmResource != null) {
      val labelAvailableResource = ecmResource.getLeftResource
      canCreateECRes.setEcmResource(RMUtils.serializeResource(labelAvailableResource))
      if (!labelAvailableResource.notLess(requestResource)) {
        logger.info(
          s"user want to use resource[${requestResource}] > em ${emInstanceLabel.getInstance()} available resource[${labelAvailableResource}]"
        )
        val notEnoughMessage = generateECMNotEnoughMessage(
          requestResource,
          labelAvailableResource,
          ecmResource.getMaxResource
        )
        canCreateECRes.setCanCreateEC(false)
        canCreateECRes.setReason(notEnoughMessage._2)
      }
    }
    // get CombinedLabel Resource Usage
    labelContainer.setCurrentLabel(labelContainer.getCombinedResourceLabel)
    val labelResource = getCombinedLabelResourceUsage(labelContainer, resource)
    labelResourceService.setLabelResource(
      labelContainer.getCurrentLabel,
      labelResource,
      labelContainer.getCombinedResourceLabel.getStringValue
    )

    if (labelResource != null) {
      val labelAvailableResource = labelResource.getLeftResource
      canCreateECRes.setLabelResource(RMUtils.serializeResource(labelAvailableResource))
      val labelMaxResource = labelResource.getMaxResource
      if (!labelAvailableResource.notLess(requestResource)) {
        logger.info(
          s"Failed check: ${labelContainer.getUserCreatorLabel.getUser} want to use label [${labelContainer.getCurrentLabel}] resource[${requestResource}] > " +
            s"label available resource[${labelAvailableResource}]"
        )
        val notEnoughMessage =
          generateNotEnoughMessage(requestResource, labelAvailableResource, labelMaxResource)
        canCreateECRes.setCanCreateEC(false);
        canCreateECRes.setReason(notEnoughMessage._2)
      }
    }
    canCreateECRes
  }

  private def getCombinedLabelResourceUsage(
      labelContainer: RMLabelContainer,
      resource: NodeResource
  ): NodeResource = {
    // 1. get label resource from db
    var labelResource = labelResourceService.getLabelResource(labelContainer.getCurrentLabel)
    // 2. get label configuration resource only CombinedUserCreatorEngineTypeLabel
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
      labelResource.getMaxResource
        .minus(labelResource.getUsedResource)
        .minus(labelResource.getLockedResource)
    )
    logger.debug(
      s"${labelContainer.getCurrentLabel} ecmResource: Max: ${labelResource.getMaxResource}  \t " +
        s"use:  ${labelResource.getUsedResource}  \t locked: ${labelResource.getLockedResource}"
    )
    labelResource
  }

  def canRequest(
      labelContainer: RMLabelContainer,
      resource: NodeResource,
      engineCreateRequest: EngineCreateRequest
  ): Boolean = {
    if (!enableRequest) {
      logger.info("Resource judgment switch is not turned on, the judgment will be skipped")
      return true
    }
    // check ecm label resource
    labelContainer.getCurrentLabel match {
      case emInstanceLabel: EMInstanceLabel =>
        return checkEMResource(emInstanceLabel, resource)
      case _ =>
    }
    // check combined label resource
    if (!labelContainer.getCombinedResourceLabel.equals(labelContainer.getCurrentLabel)) {
      throw new RMErrorException(
        RESOURCE_LATER_ERROR.getErrorCode,
        RESOURCE_LATER_ERROR.getErrorDesc + labelContainer.getCurrentLabel
      )
    }
    val labels: util.List[Label[_]] = labelContainer.getLabels
    val engineType: String = LabelUtil.getEngineType(labels)
    val props: util.Map[String, String] = engineCreateRequest.getProperties

    // 是否是跨集群的任务
    var acrossClusterTask: Boolean = false
    if (props != null) {
      acrossClusterTask = props.getOrDefault(AMConfiguration.ACROSS_CLUSTER_TASK, "false").toBoolean
      var aiTask: Boolean = props.getOrDefault("linkis.ai.sql.enabled", "false").toBoolean
      if (aiTask) {
        props.put(AMConfiguration.ACROSS_CLUSTER_TASK, "false")
        acrossClusterTask = false
      }
    }

    // hive cluster check
    if (
        externalResourceService != null && StringUtils.isNotBlank(
          engineType
        ) && SUPPORT_CLUSTER_RULE_EC_TYPES.contains(
          engineType
        ) && props != null && acrossClusterTask && !"spark".equals(engineType)
    ) {
      val queueName = props.getOrDefault(YARN_QUEUE_NAME_CONFIG_KEY, "default")
      logger.info(s"hive cluster check with queue: $queueName")
      val yarnIdentifier = new YarnResourceIdentifier(queueName)
      val providedYarnResource =
        externalResourceService.getResource(ResourceType.Yarn, labelContainer, yarnIdentifier)
      val (maxCapacity, usedCapacity) =
        (providedYarnResource.getMaxResource, providedYarnResource.getUsedResource)
      // judge if is cross cluster task and origin cluster priority first
      originClusterResourceCheck(engineCreateRequest, maxCapacity, usedCapacity)
      // judge if is cross cluster task and target cluster priority first
      targetClusterResourceCheck(
        labelContainer,
        engineCreateRequest,
        maxCapacity,
        usedCapacity,
        externalResourceService
      )
    }

    val requestResource = resource.getMinResource
    // get CombinedLabel Resource Usage
    val labelResource = getCombinedLabelResourceUsage(labelContainer, resource)
    labelResourceService.setLabelResource(
      labelContainer.getCurrentLabel,
      labelResource,
      labelContainer.getCombinedResourceLabel.getStringValue
    )
    logger.debug(s"Label [${labelContainer.getCurrentLabel}] has resource + [${labelResource}]")
    if (labelResource != null) {
      val labelAvailableResource = labelResource.getLeftResource
      val labelMaxResource = labelResource.getMaxResource
      if (!labelAvailableResource.notLess(requestResource)) {
        logger.info(
          s"Failed check: ${labelContainer.getUserCreatorLabel.getUser} want to use label [${labelContainer.getCurrentLabel}] resource[${requestResource}] > " +
            s"label available resource[${labelAvailableResource}]"
        )
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

  private def checkEMResource(emInstanceLabel: EMInstanceLabel, resource: NodeResource): Boolean = {
    val labelResource = labelResourceService.getLabelResource(emInstanceLabel)
    val requestResource = resource.getMinResource
    logger.debug(s"emInstanceLabel resource info ${labelResource}")
    if (labelResource != null) {
      val labelAvailableResource = labelResource.getLeftResource
      if (!labelAvailableResource.notLess(requestResource)) {
        logger.info(
          s"user want to use resource[${requestResource}] > em ${emInstanceLabel.getInstance()} available resource[${labelAvailableResource}]"
        )
        val notEnoughMessage = generateECMNotEnoughMessage(
          requestResource,
          labelAvailableResource,
          labelResource.getMaxResource
        )
        throw new RMWarnException(
          notEnoughMessage._1,
          notEnoughMessage._2 + s"ECM Instance:${emInstanceLabel.getInstance()}"
        )
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
        driverAndYarnResource.getLoadInstanceResource
      case _ => null
    }
    loadRequestResource match {
      case li: LoadInstanceResource =>
        val loadInstanceAvailable = availableResource.asInstanceOf[LoadInstanceResource]
        val loadInstanceMax = maxResource.asInstanceOf[LoadInstanceResource]
        if (li.getCores > loadInstanceAvailable.getCores) {
          (
            RMErrorCode.ECM_CPU_INSUFFICIENT.getErrorCode,
            RMErrorCode.ECM_CPU_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.CPU,
                RMConstant.CPU_UNIT,
                li.getCores,
                loadInstanceAvailable.getCores,
                loadInstanceMax.getCores
              )
          )
        } else if (li.getMemory > loadInstanceAvailable.getMemory) {
          (
            RMErrorCode.ECM_MEMORY_INSUFFICIENT.getErrorCode,
            RMErrorCode.ECM_MEMORY_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.MEMORY,
                RMConstant.MEMORY_UNIT_BYTE,
                li.getMemory,
                loadInstanceAvailable.getMemory,
                loadInstanceMax.getMemory
              )
          )
        } else {
          (
            RMErrorCode.ECM_INSTANCES_INSUFFICIENT.getErrorCode,
            RMErrorCode.ECM_INSTANCES_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.APP_INSTANCE,
                RMConstant.INSTANCE_UNIT,
                li.getInstances,
                loadInstanceAvailable.getInstances,
                loadInstanceMax.getInstances
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
              m.getMemory,
              avail.getMemory,
              max.getMemory
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
              i.getInstances,
              avail.getInstances,
              max.getInstances
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
              c.getCores,
              avail.getCores,
              max.getCores
            )
        )
      case l: LoadResource =>
        val loadAvailable = availableResource.asInstanceOf[LoadResource]
        val avail = availableResource.asInstanceOf[LoadResource]
        val max = maxResource.asInstanceOf[LoadResource]
        if (l.getCores > loadAvailable.getCores) {
          (
            RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorCode,
            RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.CPU,
                RMConstant.CPU_UNIT,
                l.getCores,
                avail.getCores,
                max.getCores
              )
          )
        } else {
          (
            RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorCode,
            RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.MEMORY,
                RMConstant.MEMORY_UNIT_BYTE,
                l.getMemory,
                avail.getMemory,
                max.getMemory
              )
          )
        }
      case li: LoadInstanceResource =>
        val loadInstanceAvailable = availableResource.asInstanceOf[LoadInstanceResource]
        val avail = availableResource.asInstanceOf[LoadInstanceResource]
        val max = maxResource.asInstanceOf[LoadInstanceResource]
        if (li.getCores > loadInstanceAvailable.getCores) {
          (
            RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorCode,
            RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.CPU,
                RMConstant.CPU_UNIT,
                li.getCores,
                avail.getCores,
                max.getCores
              )
          )
        } else if (li.getMemory > loadInstanceAvailable.getMemory) {
          (
            RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorCode,
            RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.MEMORY,
                RMConstant.MEMORY_UNIT_BYTE,
                li.getMemory,
                avail.getMemory,
                max.getMemory
              )
          )
        } else {
          (
            RMErrorCode.INSTANCES_INSUFFICIENT.getErrorCode,
            RMErrorCode.INSTANCES_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.APP_INSTANCE,
                RMConstant.INSTANCE_UNIT,
                li.getInstances,
                avail.getInstances,
                max.getInstances
              )
          )
        }
      case yarn: YarnResource =>
        val yarnAvailable = availableResource.asInstanceOf[YarnResource]
        val avail = availableResource.asInstanceOf[YarnResource]
        val max = maxResource.asInstanceOf[YarnResource]
        if (yarn.getQueueCores > yarnAvailable.getQueueCores) {
          (
            RMErrorCode.QUEUE_CPU_INSUFFICIENT.getErrorCode,
            RMErrorCode.QUEUE_CPU_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.CPU,
                RMConstant.CPU_UNIT,
                yarn.getQueueCores,
                avail.getQueueCores,
                max.getQueueCores
              )
          )
        } else if (yarn.getQueueMemory > yarnAvailable.getQueueMemory) {
          (
            RMErrorCode.QUEUE_MEMORY_INSUFFICIENT.getErrorCode,
            RMErrorCode.QUEUE_MEMORY_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.MEMORY,
                RMConstant.MEMORY_UNIT_BYTE,
                yarn.getQueueMemory,
                avail.getQueueMemory,
                max.getQueueMemory
              )
          )
        } else {
          (
            RMErrorCode.QUEUE_INSTANCES_INSUFFICIENT.getErrorCode,
            RMErrorCode.QUEUE_INSTANCES_INSUFFICIENT.getErrorDesc +
              RMUtils.getResourceInfoMsg(
                RMConstant.APP_INSTANCE,
                RMConstant.INSTANCE_UNIT,
                yarn.getQueueInstances,
                avail.getQueueInstances,
                max.getQueueInstances
              )
          )
        }
      case dy: DriverAndYarnResource =>
        val dyAvailable = availableResource.asInstanceOf[DriverAndYarnResource]
        val dyMax = maxResource.asInstanceOf[DriverAndYarnResource]
        if (
            dy.getLoadInstanceResource.getMemory > dyAvailable.getLoadInstanceResource.getMemory ||
            dy.getLoadInstanceResource.getCores > dyAvailable.getLoadInstanceResource.getCores ||
            dy.getLoadInstanceResource.getInstances > dyAvailable.getLoadInstanceResource.getInstances
        ) {
          val detail = generateNotEnoughMessage(
            dy.getLoadInstanceResource,
            dyAvailable.getLoadInstanceResource,
            dyMax.getLoadInstanceResource
          )
          (detail._1, { detail._2 })
        } else {
          val detail =
            generateNotEnoughMessage(
              dy.getYarnResource,
              dyAvailable.getYarnResource,
              dyMax.getYarnResource
            )
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
