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

package org.apache.linkis.orchestrator.computation.service

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.governance.common.protocol.task._
import org.apache.linkis.manager.common.protocol.resource.ResponseTaskRunningInfo
import org.apache.linkis.orchestrator.computation.conf.ComputationOrchestratorConf
import org.apache.linkis.orchestrator.computation.execute.CodeExecTaskExecutorManager
import org.apache.linkis.orchestrator.computation.monitor.EngineConnMonitor
import org.apache.linkis.orchestrator.core.ResultSet
import org.apache.linkis.orchestrator.ecm.service.TaskExecutionReceiver
import org.apache.linkis.orchestrator.listener.task._
import org.apache.linkis.orchestrator.utils.OrchestratorLoggerUtils
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver
import org.apache.linkis.rpc.utils.RPCUtils

import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

@Service
class ComputationTaskExecutionReceiver extends TaskExecutionReceiver with Logging {

  private val codeExecTaskExecutorManager =
    CodeExecTaskExecutorManager.getCodeExecTaskExecutorManager

  @PostConstruct
  private def init(): Unit = {
    EngineConnMonitor.addEngineExecutorStatusMonitor(
      codeExecTaskExecutorManager.getAllInstanceToExecutorCache()
    )
  }

  @Receiver
  override def taskLogReceiver(taskLog: ResponseTaskLog, sender: Sender): Unit = {
    val serviceInstance = RPCUtils.getServiceInstanceFromSender(sender)
    codeExecTaskExecutorManager
      .getByEngineConnAndTaskId(serviceInstance, taskLog.execId)
      .foreach { codeExecutor =>
        val event = TaskLogEvent(codeExecutor.getExecTask, taskLog.log)
        codeExecutor.getExecTask.getPhysicalContext.pushLog(event)
        codeExecutor.getEngineConnExecutor.updateLastUpdateTime()
      // asyncListenerBus.post(event)
      }
  }

  @Receiver
  override def taskProgressReceiver(
      taskProgressWithResource: ResponseTaskRunningInfo,
      sender: Sender
  ): Unit = {
    val serviceInstance = RPCUtils.getServiceInstanceFromSender(sender)
    codeExecTaskExecutorManager
      .getByEngineConnAndTaskId(serviceInstance, taskProgressWithResource.getExecId)
      .foreach { codeExecutor =>
        val event = TaskRunningInfoEvent(
          codeExecutor.getExecTask,
          taskProgressWithResource.getProgress,
          taskProgressWithResource.getProgressInfo,
          taskProgressWithResource.getResourceMap,
          taskProgressWithResource.getExtraInfoMap
        )
        codeExecutor.getExecTask.getPhysicalContext.pushProgress(event)
        codeExecutor.getEngineConnExecutor.updateLastUpdateTime()
      }
  }

  @Receiver
  override def taskStatusReceiver(taskStatus: ResponseTaskStatus, sender: Sender): Unit = {
    val serviceInstance = RPCUtils.getServiceInstanceFromSender(sender)
    def postStatus(): Boolean = {
      var isExist = false
      codeExecTaskExecutorManager
        .getByEngineConnAndTaskId(serviceInstance, taskStatus.execId)
        .foreach { codeExecutor =>
          OrchestratorLoggerUtils.setJobIdMDC(codeExecutor.getExecTask)
          val event = TaskStatusEvent(codeExecutor.getExecTask, taskStatus.status)
          logger.info(
            s"From engineConn receive status info:$taskStatus, now post to listenerBus event: $event"
          )
          codeExecutor.getExecTask.getPhysicalContext.broadcastSyncEvent(event)
          codeExecutor.getEngineConnExecutor.updateLastUpdateTime()
          isExist = true
        }
      isExist
    }

    if (!postStatus() && ExecutionNodeStatus.isCompleted(taskStatus.status)) {
      logger.warn(s" from $serviceInstance received ${taskStatus} cannot find execTask to deal")
      Thread.sleep(ComputationOrchestratorConf.TASK_STATUS_COMPLETE_WAIT_TIMEOUT)
      if (!postStatus()) {
        logger.warn(
          s" from $serviceInstance received ${taskStatus} cannot find execTask to deal, by retry 2 times"
        )
      }
    }
    OrchestratorLoggerUtils.removeJobIdMDC()
  }

  @Receiver
  override def taskResultSizeReceiver(
      taskResultSize: ResponseTaskResultSize,
      sender: Sender
  ): Unit = {
    val serviceInstance = RPCUtils.getServiceInstanceFromSender(sender)
    var isExist = false
    codeExecTaskExecutorManager
      .getByEngineConnAndTaskId(serviceInstance, taskResultSize.execId)
      .foreach { codeExecutor =>
        OrchestratorLoggerUtils.setJobIdMDC(codeExecutor.getExecTask)
        val event = TaskResultSetSizeEvent(codeExecutor.getExecTask, taskResultSize.resultSize)
        logger.info(
          s"From engineConn receive resultSet size info$taskResultSize, now post to listenerBus event: $event"
        )
        codeExecutor.getExecTask.getPhysicalContext.broadcastSyncEvent(event)
        codeExecutor.getEngineConnExecutor.updateLastUpdateTime()
        isExist = true
      }
    if (!isExist) {
      logger.warn(s"from $serviceInstance received $taskResultSize cannot find execTask to deal")
    }
    OrchestratorLoggerUtils.removeJobIdMDC()
  }

  @Receiver
  override def taskResultSetReceiver(taskResultSet: ResponseTaskResultSet, sender: Sender): Unit = {
    val serviceInstance = RPCUtils.getServiceInstanceFromSender(sender)
    var isExist = false
    codeExecTaskExecutorManager
      .getByEngineConnAndTaskId(serviceInstance, taskResultSet.execId)
      .foreach { codeExecutor =>
        OrchestratorLoggerUtils.setJobIdMDC(codeExecutor.getExecTask)
        val event = TaskResultSetEvent(
          codeExecutor.getExecTask,
          ResultSet(taskResultSet.output, taskResultSet.alias)
        )
        logger.info(
          s"From engineConn receive resultSet  info $taskResultSet , now post to listenerBus event: $event"
        )
        codeExecutor.getExecTask.getPhysicalContext.broadcastSyncEvent(event)
        codeExecutor.getEngineConnExecutor.updateLastUpdateTime()
        isExist = true
      }
    if (!isExist) {
      logger.warn(s"from $serviceInstance received $taskResultSet cannot find execTask to deal")
    }
    OrchestratorLoggerUtils.removeJobIdMDC()
  }

  @Receiver
  override def taskErrorReceiver(responseTaskError: ResponseTaskError, sender: Sender): Unit = {
    var isExist = false
    val serviceInstance = RPCUtils.getServiceInstanceFromSender(sender)
    codeExecTaskExecutorManager
      .getByEngineConnAndTaskId(serviceInstance, responseTaskError.execId)
      .foreach { codeExecutor =>
        OrchestratorLoggerUtils.setJobIdMDC(codeExecutor.getExecTask)
        val event = TaskErrorResponseEvent(codeExecutor.getExecTask, responseTaskError.errorMsg)
        logger.info(
          s"From engineConn receive responseTaskError  info${responseTaskError.execId}, now post to listenerBus event: ${event.execTask.getIDInfo()}"
        )
        codeExecutor.getExecTask.getPhysicalContext.broadcastSyncEvent(event)
        codeExecutor.getEngineConnExecutor.updateLastUpdateTime()
        isExist = true
      }
    if (!isExist) {
      logger.warn(s"from $serviceInstance received $responseTaskError cannot find execTask to deal")
    }
    OrchestratorLoggerUtils.removeJobIdMDC()
  }

}
