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
 
package org.apache.linkis.orchestrator.computation.service

import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.governance.common.protocol.task._
import org.apache.linkis.message.annotation.Receiver
import org.apache.linkis.message.builder.ServiceMethodContext
import org.apache.linkis.orchestrator.computation.execute.CodeExecTaskExecutorManager
import org.apache.linkis.orchestrator.computation.utils.ComputationOrchestratorUtils
import org.apache.linkis.orchestrator.core.ResultSet
import org.apache.linkis.orchestrator.computation.monitor.EngineConnMonitor
import org.apache.linkis.orchestrator.ecm.service.TaskExecutionReceiver
import org.apache.linkis.orchestrator.listener.task._
import org.apache.linkis.orchestrator.listener.{OrchestratorAsyncListenerBus, OrchestratorListenerBusContext, OrchestratorSyncListenerBus}
import org.apache.linkis.rpc.utils.RPCUtils
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

/**
  *
  *
  */
@Service
class ComputationTaskExecutionReceiver extends TaskExecutionReceiver with Logging {

  private val codeExecTaskExecutorManager = CodeExecTaskExecutorManager.getCodeExecTaskExecutorManager

  //private val asyncListenerBus: OrchestratorAsyncListenerBus = OrchestratorListenerBusContext.getListenerBusContext().getOrchestratorAsyncListenerBus

  //private val syncListenerBus: OrchestratorSyncListenerBus = OrchestratorListenerBusContext.getListenerBusContext().getOrchestratorSyncListenerBus

  // TODO ListenerBus should to split into OrchestratorSessions.
  // TODO Two whole ListenerBus will cause the consume problem.

  @PostConstruct
  private def init(): Unit = {
    EngineConnMonitor.addEngineExecutorStatusMonitor(codeExecTaskExecutorManager.getAllInstanceToExecutorCache(),
      failedEngineServiceInstance => {
        val taskToExecutorCache = codeExecTaskExecutorManager.getAllExecTaskToExecutorCache()
        val failedTaskMap = synchronized {
          taskToExecutorCache.filter(_._2.getEngineConnExecutor.getServiceInstance.equals(failedEngineServiceInstance))
        }
        if (null != failedTaskMap && failedTaskMap.nonEmpty)  {
          failedTaskMap.foreach{
            case (taskId, executor) =>
              val execTask = executor.getExecTask
              Utils.tryAndError {
                warn(s"Will kill task ${execTask.getIDInfo()} because the engine ${executor.getEngineConnExecutor.getServiceInstance.toString} quited unexpectedly.")
                val errLog = LogUtils.generateERROR(s"Your job : ${execTask.getIDInfo()} was failed because the engine quitted unexpectedly(任务${execTask.getIDInfo()}失败，" +
                  s"原因是引擎意外退出,可能是复杂任务导致引擎退出，如OOM).")
                val logEvent = TaskLogEvent(execTask, errLog)
                execTask.getPhysicalContext.pushLog(logEvent)
                val errorResponseEvent = TaskErrorResponseEvent(execTask, "task failed，Engine quitted unexpectedly(任务运行失败原因是引擎意外退出,可能是复杂任务导致引擎退出，如OOM).")
                execTask.getPhysicalContext.broadcastSyncEvent(errorResponseEvent)
                val statusEvent = TaskStatusEvent(execTask, ExecutionNodeStatus.Failed)
                execTask.getPhysicalContext.broadcastSyncEvent(statusEvent)
              }
          }
        }
      })
  }

  @Receiver
  override def taskLogReceiver(taskLog: ResponseTaskLog, smc: ServiceMethodContext): Unit = {
    val serviceInstance = RPCUtils.getServiceInstanceFromSender(smc.getSender)
    codeExecTaskExecutorManager.getByEngineConnAndTaskId(serviceInstance, taskLog.execId).foreach { codeExecutor =>
      val event = TaskLogEvent(codeExecutor.getExecTask, taskLog.log)
      codeExecutor.getExecTask.getPhysicalContext.pushLog(event)
      codeExecutor.getEngineConnExecutor.updateLastUpdateTime()
      //asyncListenerBus.post(event)
    }
  }

  @Receiver
  override def taskProgressReceiver(taskProgress: ResponseTaskProgress, smc: ServiceMethodContext): Unit = {
    val serviceInstance = RPCUtils.getServiceInstanceFromSender(smc.getSender)
    codeExecTaskExecutorManager.getByEngineConnAndTaskId(serviceInstance, taskProgress.execId).foreach{ codeExecutor =>
      val event = TaskProgressEvent(codeExecutor.getExecTask, taskProgress.progress, taskProgress.progressInfo)
      codeExecutor.getExecTask.getPhysicalContext.pushProgress(event)
      codeExecutor.getEngineConnExecutor.updateLastUpdateTime()
    }
  }

  @Receiver
  override def taskStatusReceiver(taskStatus: ResponseTaskStatus, smc: ServiceMethodContext): Unit = {
    val serviceInstance = RPCUtils.getServiceInstanceFromSender(smc.getSender)
    var isExist = false
    codeExecTaskExecutorManager.getByEngineConnAndTaskId(serviceInstance, taskStatus.execId).foreach { codeExecutor =>
      val event = TaskStatusEvent(codeExecutor.getExecTask, taskStatus.status)
      logger.info(s"From engineConn receive status info:$taskStatus, now post to listenerBus event: $event")
      codeExecutor.getExecTask.getPhysicalContext.broadcastSyncEvent(event)
      codeExecutor.getEngineConnExecutor.updateLastUpdateTime()
      isExist = true
    }
    if (! isExist) {
      logger.warn(s" from $serviceInstance received ${taskStatus} cannot find execTask to deal")
    }
  }

  @Receiver
  override def taskResultSizeReceiver(taskResultSize: ResponseTaskResultSize, smc: ServiceMethodContext): Unit = {
    val serviceInstance = RPCUtils.getServiceInstanceFromSender(smc.getSender)
    var isExist = false
    codeExecTaskExecutorManager.getByEngineConnAndTaskId(serviceInstance, taskResultSize.execId).foreach { codeExecutor =>
      val event = TaskResultSetSizeEvent(codeExecutor.getExecTask, taskResultSize.resultSize)
      logger.info(s"From engineConn receive resultSet size info$taskResultSize, now post to listenerBus event: $event")
      codeExecutor.getExecTask.getPhysicalContext.broadcastSyncEvent(event)
      codeExecutor.getEngineConnExecutor.updateLastUpdateTime()
      isExist = true
    }
    if (! isExist) {
      logger.warn(s"from $serviceInstance received $taskResultSize cannot find execTask to deal")
    }
  }

  @Receiver
  override def taskResultSetReceiver(taskResultSet: ResponseTaskResultSet, smc: ServiceMethodContext): Unit = {
    val serviceInstance = RPCUtils.getServiceInstanceFromSender(smc.getSender)
    var isExist = false
    codeExecTaskExecutorManager.getByEngineConnAndTaskId(serviceInstance, taskResultSet.execId).foreach { codeExecutor =>
      val event = TaskResultSetEvent(codeExecutor.getExecTask, ResultSet(taskResultSet.output, taskResultSet.alias))
      logger.info(s"From engineConn receive resultSet  info $taskResultSet , now post to listenerBus event: $event")
      codeExecutor.getExecTask.getPhysicalContext.broadcastSyncEvent(event)
      codeExecutor.getEngineConnExecutor.updateLastUpdateTime()
      isExist = true
    }
    if (! isExist) {
      logger.warn(s"from $serviceInstance received $taskResultSet cannot find execTask to deal")
    }
  }




  @Receiver
  override def taskErrorReceiver(responseTaskError: ResponseTaskError, smc: ServiceMethodContext): Unit = {

    val serviceInstance = RPCUtils.getServiceInstanceFromSender(smc.getSender)
    var isExist = false
    codeExecTaskExecutorManager.getByEngineConnAndTaskId(serviceInstance, responseTaskError.execId).foreach { codeExecutor =>
      val event = TaskErrorResponseEvent(codeExecutor.getExecTask, responseTaskError.errorMsg)
      logger.info(s"From engineConn receive responseTaskError  info$responseTaskError, now post to listenerBus event: $event")
      codeExecutor.getExecTask.getPhysicalContext.broadcastSyncEvent(event)
      codeExecutor.getEngineConnExecutor.updateLastUpdateTime()
      isExist = true
    }
    if (! isExist) {
      logger.warn(s"from $serviceInstance received $responseTaskError cannot find execTask to deal")
    }

  }

}
