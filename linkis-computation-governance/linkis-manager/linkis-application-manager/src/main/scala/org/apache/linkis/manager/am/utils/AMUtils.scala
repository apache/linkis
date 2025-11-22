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

package org.apache.linkis.manager.am.utils

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.constant.job.JobRequestConstants
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.protocol.job.{JobReqQuery, JobReqUpdate, JobRespProtocol}
import org.apache.linkis.governance.common.utils.ECPathUtils
import org.apache.linkis.manager.am.vo.{AMEngineNodeVo, EMNodeVo}
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.node.{EMNode, EngineNode}
import org.apache.linkis.manager.common.entity.resource.{
  DriverAndYarnResource,
  Resource,
  ResourceType
}
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel
import org.apache.linkis.manager.label.utils.{LabelUtil, LabelUtils}
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.rpc.Sender
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.StringUtils

import java.io.File
import java.util

import scala.collection.JavaConverters._

import com.google.gson.JsonObject

object AMUtils extends Logging {

  lazy val GSON = BDPJettyServerHelper.gson

  private val SUCCESS_FLAG = 0

  val mapper = BDPJettyServerHelper.jacksonJson

  def copyToEMVo(EMNodes: Array[EMNode]): util.ArrayList[EMNodeVo] = {
    val EMNodeVos = new util.ArrayList[EMNodeVo]()
    EMNodes.foreach(node => {
      val EMNodeVo = new EMNodeVo
      EMNodeVo.setLabels(node.getLabels)
      EMNodeVo.setApplicationName(node.getServiceInstance.getApplicationName)
      EMNodeVo.setInstance(node.getServiceInstance.getInstance)
      if (node.getStartTime != null) EMNodeVo.setStartTime(node.getStartTime)
      if (node.getNodeResource != null) {
        if (node.getNodeResource.getResourceType != null) {
          EMNodeVo.setResourceType(node.getNodeResource.getResourceType)
        }
        if (node.getNodeResource.getMaxResource != null) {
          EMNodeVo.setMaxResource(
            mapper
              .readValue(
                mapper.writeValueAsString(node.getNodeResource.getMaxResource),
                classOf[util.Map[String, Any]]
              )
          )
        }
        if (node.getNodeResource.getMinResource != null) {
          EMNodeVo.setMinResource(
            mapper
              .readValue(
                mapper.writeValueAsString(node.getNodeResource.getMinResource),
                classOf[util.Map[String, Any]]
              )
          )
        }
        if (node.getNodeResource.getUsedResource != null) {
          EMNodeVo.setUsedResource(
            mapper.readValue(
              mapper.writeValueAsString(node.getNodeResource.getUsedResource),
              classOf[util.Map[String, Any]]
            )
          )
        } else {
          EMNodeVo.setUsedResource(
            mapper.readValue(
              mapper.writeValueAsString(Resource.initResource(ResourceType.Default)),
              classOf[util.Map[String, Any]]
            )
          )
        }
        if (node.getNodeResource.getLockedResource != null) {
          EMNodeVo.setLockedResource(
            mapper.readValue(
              mapper.writeValueAsString(node.getNodeResource.getLockedResource),
              classOf[util.Map[String, Any]]
            )
          )
        }
        if (node.getNodeResource.getExpectedResource != null) {
          EMNodeVo.setExpectedResource(
            mapper.readValue(
              mapper.writeValueAsString(node.getNodeResource.getExpectedResource),
              classOf[util.Map[String, Any]]
            )
          )
        }
        if (node.getNodeResource.getLeftResource != null) {
          EMNodeVo.setLeftResource(
            mapper.readValue(
              mapper.writeValueAsString(node.getNodeResource.getLeftResource),
              classOf[util.Map[String, Any]]
            )
          )
        }
      }
      EMNodeVo.setOwner(node.getOwner)
      if (node.getNodeTaskInfo != null) {
        if (node.getNodeTaskInfo.getRunningTasks >= 0) {
          EMNodeVo.setRunningTasks(node.getNodeTaskInfo.getRunningTasks)
        }
        if (node.getNodeTaskInfo.getPendingTasks >= 0) {
          EMNodeVo.setPendingTasks(node.getNodeTaskInfo.getPendingTasks)
        }
        if (node.getNodeTaskInfo.getSucceedTasks >= 0) {
          EMNodeVo.setSucceedTasks(node.getNodeTaskInfo.getSucceedTasks)
        }
        if (node.getNodeTaskInfo.getFailedTasks >= 0) {
          EMNodeVo.setFailedTasks(node.getNodeTaskInfo.getFailedTasks)
        }
      }
      if (node.getNodeOverLoadInfo != null) {
        if (node.getNodeOverLoadInfo.getMaxMemory != null) {
          EMNodeVo.setMaxMemory(node.getNodeOverLoadInfo.getMaxMemory)
        }
        if (node.getNodeOverLoadInfo.getUsedMemory != null) {
          EMNodeVo.setUsedMemory(node.getNodeOverLoadInfo.getUsedMemory)
        }
        if (node.getNodeOverLoadInfo.getSystemCPUUsed != null) {
          EMNodeVo.setSystemCPUUsed(node.getNodeOverLoadInfo.getSystemCPUUsed)
        }
        if (node.getNodeOverLoadInfo.getSystemLeftMemory != null) {
          EMNodeVo.setSystemLeftMemory(node.getNodeOverLoadInfo.getSystemLeftMemory)
        }
      }
      if (node.getNodeHealthyInfo != null) {
        if (node.getNodeHealthyInfo.getNodeHealthy != null) {
          EMNodeVo.setNodeHealthy(node.getNodeHealthyInfo.getNodeHealthy)
        }
        if (node.getNodeHealthyInfo.getMsg != null) {
          EMNodeVo.setMsg(node.getNodeHealthyInfo.getMsg)
        }
      }
      if (StringUtils.isNotBlank(node.getNodeDescription)) {
        EMNodeVo.setDescription(node.getNodeDescription)
      }
      EMNodeVos.add(EMNodeVo)
    })
    EMNodeVos
  }

  def copyToAMEngineNodeVo(
      AMEngineNode: java.util.List[EngineNode]
  ): java.util.ArrayList[AMEngineNodeVo] = {
    val AMEngineNodeVos = new util.ArrayList[AMEngineNodeVo]()
    if (!AMEngineNode.isEmpty) {
      AMEngineNode.asScala.foreach(node => {
        val AMEngineNodeVo = new AMEngineNodeVo
        AMEngineNodeVo.setLabels(node.getLabels)
        AMEngineNodeVo.setApplicationName(node.getServiceInstance.getApplicationName)
        AMEngineNodeVo.setInstance(node.getServiceInstance.getInstance)
        if (null != node.getEMNode) {
          AMEngineNodeVo.setEmInstance(node.getEMNode.getServiceInstance.getInstance)
        }

        if (!node.getLabels.isEmpty) {
          val engineTypeLabel =
            node.getLabels.asScala.find(_.isInstanceOf[EngineTypeLabel]).orNull
          if (engineTypeLabel != null) {
            AMEngineNodeVo.setEngineType(
              engineTypeLabel.asInstanceOf[EngineTypeLabel] getEngineType
            )
          }
        }
        if (node.getStartTime != null) AMEngineNodeVo.setStartTime(node.getStartTime)
        if (node.getNodeStatus != null) {
          AMEngineNodeVo.setNodeStatus(node.getNodeStatus)
        } else {
          AMEngineNodeVo.setNodeStatus(NodeStatus.Starting)
        }
        if (node.getLock != null) AMEngineNodeVo.setLock(node.getLock)
        if (node.getNodeResource != null) {
          if (node.getNodeResource.getResourceType != null) {
            AMEngineNodeVo.setResourceType(node.getNodeResource.getResourceType)
          }
          if (node.getNodeResource.getMaxResource != null) {
            AMEngineNodeVo.setMaxResource(createUnlimitedResource)
          }
          if (node.getNodeResource.getMinResource != null) {
            AMEngineNodeVo.setMinResource(createZeroResource)
          }
          if (node.getNodeResource.getUsedResource != null) {
            val realResource = node.getNodeResource.getUsedResource match {
              case dy: DriverAndYarnResource => dy.getLoadInstanceResource
              case _ => node.getNodeResource.getUsedResource
            }
            AMEngineNodeVo.setUsedResource(
              mapper
                .readValue(mapper.writeValueAsString(realResource), classOf[util.Map[String, Any]])
            )
          } else {
            AMEngineNodeVo.setUsedResource(
              mapper.readValue(
                mapper.writeValueAsString(Resource.initResource(ResourceType.Default)),
                classOf[util.Map[String, Any]]
              )
            )
          }
          if (node.getNodeResource.getLockedResource != null) {
            AMEngineNodeVo.setLockedResource(
              mapper.readValue(
                mapper.writeValueAsString(node.getNodeResource.getLockedResource),
                classOf[util.Map[String, Any]]
              )
            )
          }
          if (node.getNodeResource.getExpectedResource != null) {
            AMEngineNodeVo.setExpectedResource(
              mapper.readValue(
                mapper.writeValueAsString(node.getNodeResource.getExpectedResource),
                classOf[util.Map[String, Any]]
              )
            )
          }
          if (node.getNodeResource.getLeftResource != null) {
            AMEngineNodeVo.setLeftResource(
              mapper.readValue(
                mapper.writeValueAsString(node.getNodeResource.getLeftResource),
                classOf[util.Map[String, Any]]
              )
            )
          }
        }
        AMEngineNodeVo.setOwner(node.getOwner)
        if (node.getNodeTaskInfo != null) {
          if (node.getNodeTaskInfo.getRunningTasks >= 0) {
            AMEngineNodeVo.setRunningTasks(node.getNodeTaskInfo.getRunningTasks)
          }
          if (node.getNodeTaskInfo.getPendingTasks >= 0) {
            AMEngineNodeVo.setPendingTasks(node.getNodeTaskInfo.getPendingTasks)
          }
          if (node.getNodeTaskInfo.getSucceedTasks >= 0) {
            AMEngineNodeVo.setSucceedTasks(node.getNodeTaskInfo.getSucceedTasks)
          }
          if (node.getNodeTaskInfo.getFailedTasks >= 0) {
            AMEngineNodeVo.setFailedTasks(node.getNodeTaskInfo.getFailedTasks)
          }
        }
        if (node.getNodeOverLoadInfo != null) {
          if (node.getNodeOverLoadInfo.getMaxMemory != null) {
            AMEngineNodeVo.setMaxMemory(node.getNodeOverLoadInfo.getMaxMemory)
          }
          if (node.getNodeOverLoadInfo.getUsedMemory != null) {
            AMEngineNodeVo.setUsedMemory(node.getNodeOverLoadInfo.getUsedMemory)
          }
          if (node.getNodeOverLoadInfo.getSystemCPUUsed != null) {
            AMEngineNodeVo.setSystemCPUUsed(node.getNodeOverLoadInfo.getSystemCPUUsed)
          }
          if (node.getNodeOverLoadInfo.getSystemLeftMemory != null) {
            AMEngineNodeVo.setSystemCPUUsed(node.getNodeOverLoadInfo.getSystemCPUUsed)
          }
        }
        if (node.getNodeHealthyInfo != null) {
          if (node.getNodeHealthyInfo.getNodeHealthy != null) {
            AMEngineNodeVo.setNodeHealthy(node.getNodeHealthyInfo.getNodeHealthy)
          }
          if (node.getNodeHealthyInfo.getMsg != null) {
            AMEngineNodeVo.setMsg(node.getNodeHealthyInfo.getMsg)
          }
        }
        AMEngineNodeVos.add(AMEngineNodeVo)
      })
    }
    AMEngineNodeVos
  }

  def createUnlimitedResource(): util.Map[String, Long] = {
    val map = new util.HashMap[String, Long]()
    map.put("core", 128)
    map.put("memory", 512 * 1024 * 1024 * 1024)
    map.put("instance", 512)
    map
  }

  def createZeroResource(): util.Map[String, Long] = {
    val map = new util.HashMap[String, Long]()
    map.put("core", 1)
    map.put("memory", 512 * 1024 * 1024)
    map.put("instance", 0)
    map
  }

  def isJson(str: String): Boolean = {
    try {
      GSON.fromJson(str, classOf[JsonObject])
      true
    } catch {
      case _ => false
    }
  }

  def getTaskByTaskID(taskID: Long): JobRequest = Utils.tryCatch {
    val jobRequest = new JobRequest()
    jobRequest.setId(taskID)
    jobRequest.setSource(null)
    val jobReqQuery = JobReqQuery(jobRequest)
    Sender
      .getSender(Configuration.JOBHISTORY_SPRING_APPLICATION_NAME.getValue)
      .ask(jobReqQuery) match {
      case response: JobRespProtocol if response.getStatus == SUCCESS_FLAG =>
        response.getData.get(JobRequestConstants.JOB_HISTORY_LIST) match {
          case tasks: util.List[JobRequest] if !tasks.isEmpty => tasks.get(0)
          case _ => null
        }
      case _ => null
    }
  } { case e: Exception =>
    throw new RuntimeException(s"Failed to get task by ID: $taskID", e)
  }

  def updateMetrics(
      taskId: String,
      resourceTicketId: String,
      emInstance: String,
      ecmInstance: String,
      engineLogPath: String,
      isReuse: Boolean
  ): Unit =
    Utils.tryCatch {
      if (taskId != null) {
        val job = getTaskByTaskID(taskId.toLong)
        val engineMetrics = job.getMetrics
        val engineconnMap = new util.HashMap[String, Object]
        val ticketIdMap = new util.HashMap[String, Object]
        ticketIdMap.put(TaskConstant.ENGINE_INSTANCE, emInstance)
        ticketIdMap.put(TaskConstant.TICKET_ID, resourceTicketId)
        engineconnMap.put(resourceTicketId, ticketIdMap)
        engineMetrics.put(TaskConstant.JOB_ENGINECONN_MAP, engineconnMap)
        engineMetrics.put(TaskConstant.ECM_INSTANCE, ecmInstance: String)
        engineMetrics.put(TaskConstant.ENGINE_INSTANCE, emInstance)
        val pathSuffix = if (isReuse && StringUtils.isNotBlank(engineLogPath)) {
          engineLogPath
        } else {
          ECPathUtils.getECWOrkDirPathSuffix(
            job.getExecuteUser,
            resourceTicketId,
            LabelUtil.getEngineType(job.getLabels)
          ) + File.separator + "logs"
        }
        engineMetrics.put(TaskConstant.ENGINE_LOG_PATH, pathSuffix)
        // 通过RPC调用JobHistory服务更新metrics
        job.setMetrics(engineMetrics)
        val jobReqUpdate = JobReqUpdate(job)
        // 发送RPC请求到JobHistory服务
        val sender: Sender =
          Sender.getSender(Configuration.JOBHISTORY_SPRING_APPLICATION_NAME.getValue)
        sender.ask(jobReqUpdate)
      } else {
        logger.debug("No taskId found in properties, skip updating job history metrics")
      }
    } { t =>
      logger.warn(s"Failed to update job history metrics for engine ${emInstance}", t)
    }

  /**
   * 异步更新job history metrics
   * @param taskId
   *   任务ID
   * @param resourceTicketId
   *   资源票据ID
   * @param emInstance
   *   引擎实例
   * @param ecmInstance
   *   ECM实例
   * @param engineLogPath
   *   引擎日志路径
   * @param isReuse
   *   是否复用引擎
   */
  def updateMetricsAsync(
      taskId: String,
      resourceTicketId: String,
      emInstance: String,
      ecmInstance: String,
      engineLogPath: String,
      isReuse: Boolean
  ): Unit = {
    import scala.concurrent.Future
    import scala.util.{Failure, Success}

    Future {
      updateMetrics(taskId, resourceTicketId, emInstance, ecmInstance, engineLogPath, isReuse)
    }(Utils.newCachedExecutionContext(1, "UpdateMetrics-Thread-")).onComplete {
      case Success(_) =>
        logger.debug(s"Task: $taskId metrics update completed successfully for engine: $emInstance")
      case Failure(t) =>
        logger.warn(s"Task: $taskId metrics update failed for engine: $emInstance", t)
    }(Utils.newCachedExecutionContext(1, "UpdateMetrics-Thread-"))
  }

}
