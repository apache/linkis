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

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.common.entity.persistence.PersistenceResourceActionRecord
import org.apache.linkis.manager.common.entity.resource.{Resource, ResourceActionRecord, ResourceType}
import org.apache.linkis.manager.common.exception.ResourceWarnException
import org.apache.linkis.manager.common.utils.ResourceUtils
import org.apache.linkis.manager.label.entity.{CombinedLabel, Label}
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel
import org.apache.linkis.manager.persistence.ResourceManagerPersistence
import org.apache.linkis.resourcemanager.service.LabelResourceService
import org.apache.linkis.resourcemanager.utils.RMUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
case class ResourceLogService() extends Logging{

  @Autowired
  var labelResourceService: LabelResourceService = _

  @Autowired
  var resourceManagerPersistence: ResourceManagerPersistence = _



  private def printLog(changeType: String, status: String, engineLabel: EngineInstanceLabel = null, ecmLabel: EMInstanceLabel = null): String = {
    val logString = new StringBuilder(changeType + " ")
    logString ++= (status + ", ")
    if (engineLabel != null) {
      val engineResource = labelResourceService.getLabelResource(engineLabel)
      var usedResource = Resource.initResource(ResourceType.Default)
      if (engineResource != null && engineResource.getUsedResource != null) {
        usedResource = engineResource.getUsedResource
      }
      logString ++= ("engine current resource:")
      logString ++= (engineLabel.getServiceInstance.getInstance)
      logString ++= (usedResource.toJson + " ")
    }
    if (ecmLabel != null) {
      val ecmResource = labelResourceService.getLabelResource(ecmLabel)
      var usedResource = Resource.initResource(ResourceType.Default)
      if (ecmResource != null && ecmResource.getUsedResource != null) {
        usedResource = ecmResource.getUsedResource
      }
      logString ++= ("ecm current resource:")
      logString ++= (ecmLabel.getServiceInstance.getInstance)
      logString ++= (usedResource.toJson + " ")
    }
    logString.toString()
  }

  def failed(changeType: String, engineLabel: EngineInstanceLabel = null, ecmLabel: EMInstanceLabel = null, exception: Exception = null): Unit = {
    if (changeType != null) {
      val log: String = changeType match {
        case ChangeType.ENGINE_INIT => {
          printLog(changeType, ChangeType.FAILED, engineLabel, ecmLabel)
        }
        case ChangeType.ENGINE_CLEAR => {
          printLog(changeType, ChangeType.FAILED, engineLabel, ecmLabel)
        }
        case ChangeType.ECM_INIT => {
          printLog(changeType, ChangeType.FAILED, null, ecmLabel)
        }
        case ChangeType.ECM_CLEAR => {
          printLog(changeType, ChangeType.FAILED, null, ecmLabel)
        }
        case ChangeType.ECM_RESOURCE_ADD => {
          printLog(changeType, ChangeType.FAILED, engineLabel, ecmLabel)
        }
        case ChangeType.ECM_Resource_MINUS => {
          printLog(changeType, ChangeType.FAILED, engineLabel, ecmLabel)
        }
        case _ => " "
      }
      if (exception != null) {
        error(log, exception)
      } else {
        error(log)
      }
    }
  }
  def success(changeType: String, engineLabel: EngineInstanceLabel = null, ecmLabel: EMInstanceLabel = null): Unit = {
    if (changeType != null) {
      val log: String = changeType match {
        case ChangeType.ENGINE_INIT => {
          printLog(changeType, ChangeType.SUCCESS, engineLabel, ecmLabel)
        }
        case ChangeType.ENGINE_CLEAR => {
          printLog(changeType, ChangeType.SUCCESS, engineLabel, ecmLabel)
        }
        case ChangeType.ECM_INIT => {
          printLog(changeType, ChangeType.SUCCESS, null, ecmLabel)
        }
        case ChangeType.ECM_CLEAR => {
          printLog(changeType, ChangeType.SUCCESS, null, ecmLabel)
        }
        case ChangeType.ECM_RESOURCE_ADD => {
          printLog(changeType, ChangeType.SUCCESS, engineLabel, ecmLabel)
        }
        case ChangeType.ECM_Resource_MINUS => {
          printLog(changeType, ChangeType.SUCCESS, engineLabel, ecmLabel)
        }
        case _ => " "
      }
      info(log)
    }
  }

  def printUsedResourceNode(nodeLabel: EngineInstanceLabel, source: CombinedLabel): Unit = {
    printNode(nodeLabel, source)
  }

  def printReleaseResourceNode(nodeLabel: EngineInstanceLabel, source: CombinedLabel): Unit = {
    printNode(nodeLabel, source)
  }

  def printNode(nodeLabel: EngineInstanceLabel, source: CombinedLabel): Unit = {
    val log = s"${nodeLabel.getInstance()}\t${source.getStringValue}"
    info(log)
  }

  def deserialize(persistenceResourceActionRecord: PersistenceResourceActionRecord): ResourceActionRecord = {
    val resourceActionRecord = new ResourceActionRecord
    resourceActionRecord.setId(persistenceResourceActionRecord.getId)
    resourceActionRecord.setLabelValue(persistenceResourceActionRecord.getLabelValue)
    resourceActionRecord.setTicketId(persistenceResourceActionRecord.getTicketId)
    resourceActionRecord.setRequestTimes(persistenceResourceActionRecord.getRequestTimes)
    resourceActionRecord.setRequestResourceAll(ResourceUtils.deserializeResource(persistenceResourceActionRecord.getRequestResourceAll))
    resourceActionRecord.setUsedTimes(persistenceResourceActionRecord.getUsedTimes)
    resourceActionRecord.setUsedResourceAll(ResourceUtils.deserializeResource(persistenceResourceActionRecord.getUsedResourceAll))
    resourceActionRecord.setReleaseTimes(persistenceResourceActionRecord.getReleaseTimes)
    resourceActionRecord.setReleaseResourceAll(ResourceUtils.deserializeResource(persistenceResourceActionRecord.getReleaseResourceAll))
    resourceActionRecord.setCreateTime(persistenceResourceActionRecord.getCreateTime)
    resourceActionRecord.setUpdateTime(persistenceResourceActionRecord.getUpdateTime)
    resourceActionRecord
  }


  def serialize(resourceActionRecord: ResourceActionRecord): PersistenceResourceActionRecord = {
    val persistenceResourceActionRecord = new PersistenceResourceActionRecord
    persistenceResourceActionRecord.setId(resourceActionRecord.getId)
    persistenceResourceActionRecord.setLabelValue(resourceActionRecord.getLabelValue)
    persistenceResourceActionRecord.setTicketId(resourceActionRecord.getTicketId)
    persistenceResourceActionRecord.setRequestTimes(resourceActionRecord.getRequestTimes)
    persistenceResourceActionRecord.setRequestResourceAll(ResourceUtils.serializeResource(resourceActionRecord.getRequestResourceAll))
    persistenceResourceActionRecord.setUsedTimes(resourceActionRecord.getUsedTimes)
    persistenceResourceActionRecord.setUsedResourceAll(ResourceUtils.serializeResource(resourceActionRecord.getUsedResourceAll))
    persistenceResourceActionRecord.setReleaseTimes(resourceActionRecord.getReleaseTimes)
    persistenceResourceActionRecord.setReleaseResourceAll(ResourceUtils.serializeResource(resourceActionRecord.getReleaseResourceAll))
    persistenceResourceActionRecord.setCreateTime(resourceActionRecord.getCreateTime)
    persistenceResourceActionRecord.setUpdateTime(resourceActionRecord.getUpdateTime)
    persistenceResourceActionRecord
  }

  def recordUserResourceAction(userCreatorEngineType: CombinedLabel, ticketId: String, changeType: String, resource: Resource): Unit = {
    if (RMUtils.RM_RESOURCE_ACTION_RECORD.getValue) {
      var persistenceResourceActionRecord = resourceManagerPersistence.getResourceActionRecord(ticketId)
      if(persistenceResourceActionRecord == null) {
        persistenceResourceActionRecord = new PersistenceResourceActionRecord(userCreatorEngineType.getStringValue, ticketId, resource)
        Utils.tryQuietly(resourceManagerPersistence.insertResourceActionRecord(persistenceResourceActionRecord))
      }
      val resourceActionRecord = deserialize(persistenceResourceActionRecord)
      changeType match {
        case ChangeType.ENGINE_REQUEST => {
          resourceActionRecord.setRequestTimes(resourceActionRecord.getRequestTimes + 1)
          resourceActionRecord.setRequestResourceAll(resourceActionRecord.getRequestResourceAll + resource)
        }
        case ChangeType.ENGINE_INIT => {
          resourceActionRecord.setUsedTimes(resourceActionRecord.getUsedTimes + 1)
          resourceActionRecord.setUsedResourceAll(resourceActionRecord.getUsedResourceAll + resource)
        }
        case ChangeType.ENGINE_CLEAR => {
          resourceActionRecord.setReleaseTimes(resourceActionRecord.getReleaseTimes + 1)
          resourceActionRecord.setReleaseResourceAll(resourceActionRecord.getReleaseResourceAll + resource)
        }
      }
      Utils.tryCatch(resourceManagerPersistence.updateResourceActionRecord(serialize(resourceActionRecord))) {
        case exception: Exception => {
          warn(s"ResourceActionRecord failed, ${userCreatorEngineType.getStringValue} with ticketId ${ticketId} after ${changeType}, ${resource}", exception)
        }
      }
    }
  }

  def recordUserResourceAction(userCreatorEngineType: CombinedLabel, engineInstanceLabel: EngineInstanceLabel, changeType: String, resource: Resource): Unit = {
    if (RMUtils.RM_RESOURCE_ACTION_RECORD.getValue) {
      val instanceResource = labelResourceService.getPersistenceResource(engineInstanceLabel)
      if(instanceResource == null) {
        throw new ResourceWarnException(11005, s"${engineInstanceLabel} resource is null, resource action will not be record")
      }
      recordUserResourceAction(userCreatorEngineType, instanceResource.getTicketId, changeType, resource)
    }
  }

}

object ChangeType {

  val ENGINE_REQUEST = "EngineResourceRequest"

  val ENGINE_INIT = "EngineResourceInit"

  val ENGINE_CLEAR = "EngineResourceClear"

  val ECM_INIT = "ECMResourceInit"

  val ECM_RESOURCE_LOCK = "ECMResourceLock"

  val ECM_RESOURCE_ADD = "ECMResourceAdd"

  val ECM_Resource_MINUS = "ECMResourceMinus"

  val ECM_CLEAR = "ECMResourceClear"

  val SUCCESS = "success"

  val FAILED = "failed"
}