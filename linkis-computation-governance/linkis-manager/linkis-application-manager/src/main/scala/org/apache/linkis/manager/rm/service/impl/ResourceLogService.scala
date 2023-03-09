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

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.utils.ECPathUtils
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord
import org.apache.linkis.manager.common.entity.resource.Resource
import org.apache.linkis.manager.dao.ECResourceRecordMapper
import org.apache.linkis.manager.label.entity.CombinedLabel
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel
import org.apache.linkis.manager.rm.domain.RMLabelContainer
import org.apache.linkis.manager.rm.utils.RMUtils

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.io.File
import java.util.Date

@Component
class ResourceLogService extends Logging {

  @Autowired
  private var ecResourceRecordMapper: ECResourceRecordMapper = _

  private def printLog(
      changeType: String,
      resource: Resource,
      status: String,
      engineLabel: EngineInstanceLabel = null,
      ecmLabel: EMInstanceLabel = null
  ): String = {
    val logString = new StringBuilder(changeType + " ")
    logString ++= (status + ", ")
    if (engineLabel != null && resource != null) {
      logString ++= ("engine current resource:")
      logString ++= (engineLabel.getServiceInstance.getInstance)
      logString ++= (resource.toJson + " ")
    }
    if (ecmLabel != null & resource != null) {
      logString ++= ("ecm current resource:")
      logString ++= (ecmLabel.getServiceInstance.getInstance)
      logString ++= (resource.toJson + " ")
    }
    logString.toString()
  }

  def failed(
      changeType: String,
      resource: Resource,
      engineLabel: EngineInstanceLabel = null,
      ecmLabel: EMInstanceLabel = null,
      exception: Exception = null
  ): Unit = Utils.tryAndWarn {
    if (changeType != null) {
      val log: String = changeType match {
        case ChangeType.ENGINE_INIT =>
          printLog(changeType, resource, ChangeType.FAILED, engineLabel, ecmLabel)
        case ChangeType.ENGINE_CLEAR =>
          printLog(changeType, resource, ChangeType.FAILED, engineLabel, ecmLabel)
        case ChangeType.ECM_INIT =>
          printLog(changeType, resource, ChangeType.FAILED, null, ecmLabel)
        case ChangeType.ECM_CLEAR =>
          printLog(changeType, resource, ChangeType.FAILED, null, ecmLabel)
        case ChangeType.ECM_RESOURCE_ADD =>
          printLog(changeType, resource, ChangeType.FAILED, engineLabel, ecmLabel)
        case ChangeType.ECM_Resource_MINUS =>
          printLog(changeType, resource, ChangeType.FAILED, engineLabel, ecmLabel)
        case _ => " "
      }
      if (exception != null) {
        logger.error(log, exception)
      } else {
        logger.error(log)
      }
    }
  }

  def success(
      changeType: String,
      resource: Resource,
      engineLabel: EngineInstanceLabel = null,
      ecmLabel: EMInstanceLabel = null
  ): Unit = Utils.tryAndWarn {
    if (changeType != null) {
      val log: String = changeType match {
        case ChangeType.ENGINE_INIT =>
          printLog(changeType, resource, ChangeType.SUCCESS, engineLabel, ecmLabel)
        case ChangeType.ENGINE_CLEAR =>
          printLog(changeType, resource, ChangeType.SUCCESS, engineLabel, ecmLabel)
        case ChangeType.ECM_INIT =>
          printLog(changeType, resource, ChangeType.SUCCESS, null, ecmLabel)
        case ChangeType.ECM_CLEAR =>
          printLog(changeType, resource, ChangeType.SUCCESS, null, ecmLabel)
        case ChangeType.ECM_RESOURCE_ADD =>
          printLog(changeType, resource, ChangeType.SUCCESS, engineLabel, ecmLabel)
        case ChangeType.ECM_Resource_MINUS =>
          printLog(changeType, resource, ChangeType.SUCCESS, engineLabel, ecmLabel)
        case _ => " "
      }
      logger.info(log)
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
    logger.info(log)
  }

  def recordUserResourceAction(
      labelContainer: RMLabelContainer,
      ticketId: String,
      changeType: String,
      resource: Resource,
      status: NodeStatus = NodeStatus.Starting,
      metrics: String = null
  ): Unit = if (RMUtils.RM_RESOURCE_ACTION_RECORD.getValue) Utils.tryAndWarn {
    val userCreatorEngineType: CombinedLabel =
      labelContainer.getCombinedUserCreatorEngineTypeLabel
    val engineInstanceLabel: EngineInstanceLabel = labelContainer.getEngineInstanceLabel
    val eMInstanceLabel = labelContainer.getEMInstanceLabel
    if (null == userCreatorEngineType) return
    var ecResourceInfoRecord = ecResourceRecordMapper.getECResourceInfoRecord(ticketId)
    if (ecResourceInfoRecord == null) {
      val logDirSuffix = getECLogDirSuffix(labelContainer, ticketId)
      val user =
        if (null != labelContainer.getUserCreatorLabel) labelContainer.getUserCreatorLabel.getUser
        else ""
      ecResourceInfoRecord = new ECResourceInfoRecord(
        userCreatorEngineType.getStringValue,
        user,
        ticketId,
        resource,
        logDirSuffix
      )
      ecResourceRecordMapper.insertECResourceInfoRecord(ecResourceInfoRecord)
    }
    if (null != engineInstanceLabel) {
      ecResourceInfoRecord.setServiceInstance(engineInstanceLabel.getInstance)
    }
    if (null != eMInstanceLabel) {
      ecResourceInfoRecord.setEcmInstance(eMInstanceLabel.getInstance())
    }
    changeType match {
      case ChangeType.ENGINE_REQUEST =>
        ecResourceInfoRecord.setRequestTimes(ecResourceInfoRecord.getRequestTimes + 1)
        if (null != resource) {
          ecResourceInfoRecord.setRequestResource(resource.toJson)
        }
      case ChangeType.ENGINE_INIT =>
        ecResourceInfoRecord.setUsedTimes(ecResourceInfoRecord.getUsedTimes + 1)
        if (null != resource) {
          ecResourceInfoRecord.setUsedResource(resource.toJson)
        }
        ecResourceInfoRecord.setUsedTime(new Date(System.currentTimeMillis));
      case ChangeType.ENGINE_CLEAR =>
        ecResourceInfoRecord.setReleaseTimes(ecResourceInfoRecord.getReleaseTimes + 1)
        if (null != resource) {
          ecResourceInfoRecord.setReleasedResource(resource.toJson)
        }
        ecResourceInfoRecord.setReleaseTime(new Date(System.currentTimeMillis))
        if (null != metrics) {
          ecResourceInfoRecord.setMetrics(metrics)
        }
    }
    if (
        StringUtils.isBlank(ecResourceInfoRecord.getStatus) || !NodeStatus
          .isCompleted(NodeStatus.toNodeStatus(ecResourceInfoRecord.getStatus))
    ) {
      ecResourceInfoRecord.setStatus(status.toString)
    }
    ecResourceRecordMapper.updateECResourceInfoRecord(ecResourceInfoRecord)
  }

  def getECLogDirSuffix(labelContainer: RMLabelContainer, ticketId: String): String = {
    val engineTypeLabel = labelContainer.getEngineTypeLabel
    val userCreatorLabel = labelContainer.getUserCreatorLabel
    if (null == engineTypeLabel || null == userCreatorLabel) {
      return ""
    }
    val suffix = ECPathUtils.getECWOrkDirPathSuffix(
      userCreatorLabel.getUser,
      ticketId,
      engineTypeLabel.getEngineType
    )
    suffix + File.separator + "logs"
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
