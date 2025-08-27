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

package org.apache.linkis.manager.rm.utils

import org.apache.linkis.common.conf.{CommonVars, TimeType}
import org.apache.linkis.common.utils.{ByteTimeUtils, JsonUtils, Logging, Utils}
import org.apache.linkis.manager.am.vo.EMNodeVo
import org.apache.linkis.manager.common.constant.RMConstant
import org.apache.linkis.manager.common.entity.persistence.{
  PersistenceLabelRel,
  PersistenceResource
}
import org.apache.linkis.manager.common.entity.resource.{Resource, _}
import org.apache.linkis.manager.common.utils.ResourceUtils
import org.apache.linkis.manager.label.LabelManagerUtils.labelFactory
import org.apache.linkis.manager.label.builder.CombinedLabelBuilder
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.entity.engine.{EngineType, EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.manager.rm.conf.ResourceStatus
import org.apache.linkis.manager.rm.restful.vo.{UserCreatorEngineType, UserResourceVo}

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.{List, UUID}

import scala.collection.JavaConverters.asScalaBufferConverter

import com.google.common.collect.Lists

object RMUtils extends Logging {

  val jacksonUtil = JsonUtils.jackson

  val MANAGER_KILL_ENGINE_EAIT =
    CommonVars("wds.linkis.manager.rm.kill.engine.wait", new TimeType("30s"))

  val RM_REQUEST_ENABLE = CommonVars("wds.linkis.manager.rm.request.enable", true)

  val RM_RESOURCE_LOCK_WAIT_TIME = CommonVars("wds.linkis.manager.rm.lock.wait", 5 * 60 * 1000)

  val RM_DEBUG_ENABLE = CommonVars("wds.linkis.manager.rm.debug.enable", false)

  val RM_DEBUG_LOG_PATH =
    CommonVars("wds.linkis.manager.rm.debug.log.path", "file:///tmp/linkis/rmLog")

  val EXTERNAL_RESOURCE_REFRESH_TIME =
    CommonVars("wds.linkis.manager.rm.external.resource.regresh.time", new TimeType("30m"))

  var COMBINED_USERCREATOR_ENGINETYPE: String = _

  val ENGINE_TYPE = CommonVars.apply(
    "wds.linkis.configuration.engine.type",
    EngineType.getAllEngineTypes().asScala.mkString(",")
  )

  val RM_RESOURCE_ACTION_RECORD = CommonVars("wds.linkis.manager.rm.resource.action.record", true)

  def deserializeResource(plainResource: String): Resource = {
    jacksonUtil.readValue(plainResource, classOf[Resource])
  }

  def serializeResource(resource: Resource): String = {
    jacksonUtil.writeValueAsString(resource)
  }

  def toUserResourceVo(userResource: UserResource): UserResourceVo = {
    val userResourceVo = new UserResourceVo
    if (userResource.getCreator != null) userResourceVo.setCreator(userResource.getCreator)
    if (userResource.getEngineType != null) {
      userResourceVo.setEngineTypeWithVersion(
        userResource.getEngineType + "-" + userResource.getVersion
      )
    }
    if (userResource.getUsername != null) userResourceVo.setUsername(userResource.getUsername)
    if (userResource.getCreateTime != null) {
      userResourceVo.setCreateTime(userResource.getCreateTime)
    }
    if (userResource.getUpdateTime != null) {
      userResourceVo.setUpdateTime(userResource.getUpdateTime)
    }
    if (userResource.getId != null) userResourceVo.setId(userResource.getId)
    if (userResource.getUsedResource != null) {
      userResourceVo.setUsedResource(
        jacksonUtil.readValue(
          jacksonUtil.writeValueAsString(userResource.getUsedResource),
          classOf[util.Map[String, Any]]
        )
      )
    }
    if (userResource.getLeftResource != null) {
      userResourceVo.setLeftResource(
        jacksonUtil.readValue(
          jacksonUtil.writeValueAsString(userResource.getLeftResource),
          classOf[util.Map[String, Any]]
        )
      )
    }
    if (userResource.getLockedResource != null) {
      userResourceVo.setLockedResource(
        jacksonUtil.readValue(
          jacksonUtil.writeValueAsString(userResource.getLockedResource),
          classOf[util.Map[String, Any]]
        )
      )
    }
    if (userResource.getMaxResource != null) {
      userResourceVo.setMaxResource(
        jacksonUtil.readValue(
          jacksonUtil.writeValueAsString(userResource.getMaxResource),
          classOf[util.Map[String, Any]]
        )
      )
    }
    if (userResource.getMinResource != null) {
      userResourceVo.setMinResource(
        jacksonUtil.readValue(
          jacksonUtil.writeValueAsString(userResource.getMinResource),
          classOf[util.Map[String, Any]]
        )
      )
    }
    if (userResource.getResourceType != null) {
      userResourceVo.setResourceType(userResource.getResourceType)
    }
    if (userResource.getLeftResource != null && userResource.getMaxResource != null) {
      if (userResource.getResourceType.equals(ResourceType.DriverAndYarn)) {
        val leftDriverResource =
          userResource.getLeftResource.asInstanceOf[DriverAndYarnResource].getLoadInstanceResource
        val leftYarnResource =
          userResource.getLeftResource.asInstanceOf[DriverAndYarnResource].getYarnResource
        val maxDriverResource =
          userResource.getMaxResource.asInstanceOf[DriverAndYarnResource].getLoadInstanceResource
        val maxYarnResource =
          userResource.getMaxResource.asInstanceOf[DriverAndYarnResource].getYarnResource
        userResourceVo.setLoadResourceStatus(
          ResourceStatus.measure(leftDriverResource, maxDriverResource)
        )
        userResourceVo.setQueueResourceStatus(
          ResourceStatus.measure(leftYarnResource, maxYarnResource)
        )
      } else {
        userResourceVo.setLoadResourceStatus(
          ResourceStatus.measure(userResource.getLeftResource, userResource.getMaxResource)
        )
      }
    }
    userResourceVo
  }

  def toPersistenceResource(nodeResource: NodeResource): PersistenceResource = {
    val persistenceResource = new PersistenceResource
    if (nodeResource.getMaxResource != null) {
      persistenceResource.setMaxResource(serializeResource(nodeResource.getMaxResource))
    }
    if (nodeResource.getMinResource != null) {
      persistenceResource.setMinResource(serializeResource(nodeResource.getMinResource))
    }
    if (nodeResource.getLockedResource != null) {
      persistenceResource.setLockedResource(serializeResource(nodeResource.getLockedResource))
    }
    if (nodeResource.getExpectedResource != null) {
      persistenceResource.setExpectedResource(serializeResource(nodeResource.getExpectedResource))
    }
    if (nodeResource.getLeftResource != null) {
      persistenceResource.setLeftResource(serializeResource(nodeResource.getLeftResource))
    }
    persistenceResource.setResourceType(nodeResource.getResourceType.toString())
    persistenceResource
  }

  def aggregateNodeResource(
      firstNodeResource: NodeResource,
      secondNodeResource: NodeResource
  ): CommonNodeResource = {
    if (firstNodeResource != null && secondNodeResource != null) {
      val aggregatedNodeResource = new CommonNodeResource
      aggregatedNodeResource.setResourceType(firstNodeResource.getResourceType)
      aggregatedNodeResource.setMaxResource(
        aggregateResource(firstNodeResource.getMaxResource, secondNodeResource.getMaxResource)
      )
      aggregatedNodeResource.setMinResource(
        aggregateResource(firstNodeResource.getMinResource, secondNodeResource.getMinResource)
      )
      aggregatedNodeResource.setUsedResource(
        aggregateResource(firstNodeResource.getUsedResource, secondNodeResource.getUsedResource)
      )
      aggregatedNodeResource.setLockedResource(
        aggregateResource(firstNodeResource.getLockedResource, secondNodeResource.getLockedResource)
      )
      aggregatedNodeResource.setLeftResource(
        aggregateResource(firstNodeResource.getLeftResource, secondNodeResource.getLeftResource)
      )
      return aggregatedNodeResource
    }
    if (firstNodeResource == null && secondNodeResource == null) {
      return null
    }
    if (firstNodeResource == null) {
      secondNodeResource.asInstanceOf[CommonNodeResource]
    } else {
      firstNodeResource.asInstanceOf[CommonNodeResource]
    }
  }

  def aggregateResource(firstResource: Resource, secondResource: Resource): Resource = {
    (firstResource, secondResource) match {
      case (null, null) => null
      case (null, secondResource) => secondResource
      case (firstResource, null) => firstResource
      case (firstResource, secondResource)
          if firstResource.getClass.equals(secondResource.getClass) =>
        firstResource.add(secondResource)
      case _ => null
    }
  }

  def getResourceInfoMsg(
      resourceType: String,
      unitType: String,
      requestResource: Any,
      availableResource: Any,
      maxResource: Any,
      queueName: String = ""
  ): String = {

    def dealMemory(resourceType: String, unitType: String, resource: Any): String = {
      if (RMConstant.MEMORY.equals(resourceType) && RMConstant.MEMORY_UNIT_BYTE.equals(unitType)) {
        Utils.tryCatch {
          if (logger.isDebugEnabled()) {
            logger.debug(s"Will change ${resource.toString} from ${unitType} to GB")
          }
          ByteTimeUtils.negativeByteStringAsGb(resource.toString + "b").toString + "GB"
        } { case e: Exception =>
          logger.error(s"Cannot convert ${resource} to Gb, " + e.getMessage)
          resource.toString + unitType
        }
      } else {
        resource.toString + unitType
      }
    }

    val reqMsg =
      if (null == requestResource) "null" + unitType
      else dealMemory(resourceType, unitType, requestResource)
    val availMsg =
      if (null == availableResource) "null" + unitType
      else dealMemory(resourceType, unitType, availableResource.toString)
    val maxMsg =
      if (null == maxResource) "null" + unitType
      else dealMemory(resourceType, unitType, maxResource.toString)
    if (StringUtils.isEmpty(queueName)) {
      s" use ${resourceType}, requestResource : ${reqMsg} > availableResource : ${availMsg},  maxResource : ${maxMsg}."
    } else {
      s" use ${resourceType}, requestResource : ${reqMsg} > availableResource : ${availMsg},  maxResource : ${maxMsg}, queueName : ${queueName}."
    }
  }

  def getECTicketID: String = UUID.randomUUID().toString

  def getCombinedLabel: String = {
    if (COMBINED_USERCREATOR_ENGINETYPE == null) {
      val userCreatorLabel = labelFactory.createLabel(classOf[UserCreatorLabel])
      val engineTypeLabel = labelFactory.createLabel(classOf[EngineTypeLabel])
      val combinedLabelBuilder = new CombinedLabelBuilder
      val combinedLabel =
        combinedLabelBuilder.build("", Lists.newArrayList(userCreatorLabel, engineTypeLabel))
      COMBINED_USERCREATOR_ENGINETYPE = combinedLabel.getLabelKey
      COMBINED_USERCREATOR_ENGINETYPE
    } else {
      COMBINED_USERCREATOR_ENGINETYPE
    }
  }

  def getUserYarnResources(
      resources: util.List[PersistenceResource]
  ): util.HashMap[String, util.HashMap[String, Any]] = {
    val userResourceMap = new util.HashMap[String, util.HashMap[String, Any]]
    resources.asScala.foreach(resource => {
      val userResource = ResourceUtils.fromPersistenceResourceAndUser(resource)
      userResourceMap.put("maxResource", getYarnResourceMap(userResource.getMaxResource))
      userResourceMap.put("usedResource", getYarnResourceMap(userResource.getUsedResource))
      userResourceMap.put("lockedResource", getYarnResourceMap(userResource.getLockedResource))
      userResourceMap.put("leftResource", getYarnResourceMap(userResource.getLeftResource))
    })
    userResourceMap
  }

  def getLinkisResources(
      resources: util.List[PersistenceResource],
      engineType: String
  ): util.HashMap[String, util.HashMap[String, Any]] = {
    val userResourceMap = new util.HashMap[String, util.HashMap[String, Any]]
    resources.asScala.foreach(resource => {
      val userResource = ResourceUtils.fromPersistenceResourceAndUser(resource)
      userResourceMap.put("maxResource", getEcmResourceMap(userResource.getMaxResource, engineType))
      userResourceMap.put(
        "usedResource",
        getEcmResourceMap(userResource.getUsedResource, engineType)
      )
      userResourceMap.put(
        "lockedResource",
        getEcmResourceMap(userResource.getLockedResource, engineType)
      )
      userResourceMap.put(
        "leftResource",
        getEcmResourceMap(userResource.getLeftResource, engineType)
      )
    })
    userResourceMap
  }

  def getYarnResourceMap(resource: Resource): util.HashMap[String, Any] = {
    val resourceMap = new util.HashMap[String, Any]
    val yarnResource = resource.asInstanceOf[DriverAndYarnResource].getYarnResource
    resourceMap.put(
      "queueMemory",
      ByteTimeUtils.negativeByteStringAsGb(yarnResource.getQueueMemory + "b") + "G"
    )
    resourceMap.put("queueCpu", yarnResource.getQueueCores)
    resourceMap.put("instance", yarnResource.getQueueInstances)
    resourceMap
  }

  def getEcmResourceMap(resource: Resource, engineType: String): util.HashMap[String, Any] = {
    val resourceMap = new util.HashMap[String, Any]
    var loadInstanceResource = new LoadInstanceResource(0, 0, 0)
    if (engineType.contains("spark")) {
      loadInstanceResource = resource.asInstanceOf[DriverAndYarnResource].getLoadInstanceResource
    } else {
      loadInstanceResource = resource.asInstanceOf[LoadInstanceResource]
    }

    resourceMap.put(
      "memory",
      ByteTimeUtils.negativeByteStringAsGb(loadInstanceResource.getMemory + "b") + "G"
    )
    resourceMap.put("core", loadInstanceResource.getCores)
    resourceMap
  }

  def getUserCreator(userCreatorLabel: UserCreatorLabel): String = {
    "(" + userCreatorLabel.getUser + "," + userCreatorLabel.getCreator + ")"
  }

  def getEngineType(engineTypeLabel: EngineTypeLabel): String = {
    "(" + engineTypeLabel.getEngineType + "," + engineTypeLabel.getVersion + ")"
  }

}
