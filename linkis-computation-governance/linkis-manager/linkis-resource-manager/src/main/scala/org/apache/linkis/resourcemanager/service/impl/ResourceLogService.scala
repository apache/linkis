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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.entity.resource.{Resource, ResourceType}
import org.apache.linkis.manager.label.entity.{CombinedLabel, Label}
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel
import org.apache.linkis.resourcemanager.service.LabelResourceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
case class ResourceLogService() extends Logging{

  @Autowired
  var labelResourceService: LabelResourceService = _



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