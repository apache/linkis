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

package org.apache.linkis.orchestrator.strategy.async

import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.orchestrator.execution.ExecTaskRunner
import org.apache.linkis.orchestrator.execution.impl.DefaultTaskManager
import org.apache.linkis.orchestrator.listener.OrchestratorSyncEvent
import org.apache.linkis.orchestrator.listener.execution.ExecTaskRunnerCompletedEvent
import org.apache.linkis.orchestrator.listener.task._
import org.apache.linkis.orchestrator.plans.physical.ExecTask

/**
 */
class AsyncTaskManager
    extends DefaultTaskManager
    with TaskStatusListener
    with TaskResultSetListener {

  override def onSyncEvent(event: OrchestratorSyncEvent): Unit = {
    super.onSyncEvent(event)
    event match {

      case taskStatusEvent: TaskStatusEvent =>
        onStatusUpdate(taskStatusEvent)
      case taskResultSetSizeEvent: TaskResultSetSizeEvent =>
        onResultSizeCreated(taskResultSetSizeEvent)
      case taskResultSetEvent: TaskResultSetEvent =>
        onResultSetCreate(taskResultSetEvent)
      case taskErrorResponseEvent: TaskErrorResponseEvent =>
        onTaskErrorResponseEvent(taskErrorResponseEvent)
      case ExecTaskRunnerCompletedEvent(execTaskRunner) =>
        addCompletedTask(execTaskRunner)
      case event: EngineQuitedUnexpectedlyEvent =>
        onEngineQuitedUnexpectedly(event)
      case _ =>
    }
  }

  override def onResultSetCreate(taskResultSetEvent: TaskResultSetEvent): Unit = {
    logger.debug(s"received taskResultSetEvent ${taskResultSetEvent.execTask.getId}")
    findDealEventTaskRunner(taskResultSetEvent).foreach {
      case asyncExecTaskRunner: AsyncExecTaskRunner =>
        asyncExecTaskRunner.addResultSet(taskResultSetEvent.resultSet)
      case _ =>
    }
  }

  override def onResultSizeCreated(taskResultSetSizeEvent: TaskResultSetSizeEvent): Unit = {
    logger.debug(s"received taskResultSetSizeEvent $taskResultSetSizeEvent")
    findDealEventTaskRunner(taskResultSetSizeEvent).foreach {
      case asyncExecTaskRunner: AsyncExecTaskRunner =>
        asyncExecTaskRunner.setResultSize(taskResultSetSizeEvent.resultSize)
      case _ =>
    }
  }

  override def onTaskErrorResponseEvent(taskErrorResponseEvent: TaskErrorResponseEvent): Unit = {

    findDealEventTaskRunner(taskErrorResponseEvent).foreach {
      case asyncExecTaskRunner: AsyncExecTaskRunner =>
        logger.info(s"received taskErrorResponseEvent $taskErrorResponseEvent")
        asyncExecTaskRunner.markFailed(taskErrorResponseEvent.errorMsg, null)
      case _ =>
    }
  }

  override def onStatusUpdate(taskStatusEvent: TaskStatusEvent): Unit = {
    logger.debug(s"received taskStatusEvent $taskStatusEvent")
    if (ExecutionNodeStatus.isCompleted(taskStatusEvent.status)) {
      findDealEventTaskRunner(taskStatusEvent).foreach { runner =>
        logger.info(
          s"Task(${taskStatusEvent.execTask.getIDInfo()}) is completed, status ${taskStatusEvent.status}"
        )
        // To transient taskRunner status
        runner.transientStatus(taskStatusEvent.status)
      // addCompletedTask(runner)
      }
    }
  }

  private def findDealEventTaskRunner(event: TaskInfoEvent): Option[ExecTaskRunner] = {
    val execTask = event.execTask
    val rootExecTask = execTask.getPhysicalContext.getRootTask
    val runners = getRunningTask(rootExecTask).filter { taskRunner =>
      taskRunner.task match {
        case asyncExecTask: AsyncExecTask =>
          asyncExecTask.canDealEvent(event)
        case _ => false
      }
    }
    runners.headOption
  }

  override protected def execTaskToTaskRunner(execTask: ExecTask): ExecTaskRunner = {
    new AsyncExecTaskRunnerImpl(execTask)
  }

  def onEngineQuitedUnexpectedly(event: EngineQuitedUnexpectedlyEvent): Unit = {
    logger.info(s"received EngineUnexpectedlyQuitedEvent $event")
    findDealEventTaskRunner(event).foreach {
      case asyncExecTaskRunner: AsyncExecTaskRunner =>
        if (asyncExecTaskRunner.isCompleted) {
          logger.warn(
            s"task ${event.execTask.getIDInfo()} already complete, ignore engine ${event.serviceInstance} quited unexpectedly"
          )
        } else {
          val execTask = event.execTask
          val errLog = LogUtils.generateERROR(
            s"Your job : ${execTask.getIDInfo()} was failed because the engine quited unexpectedly(任务${execTask.getIDInfo()}失败，原因是引擎意外退出,可能是复杂任务导致引擎退出，如OOM)."
          )
          val logEvent = TaskLogEvent(execTask, errLog)
          execTask.getPhysicalContext.pushLog(logEvent)
          val errorMsg =
            s"task ${execTask.getIDInfo()} failed，Engine ${event.serviceInstance} quited unexpectedly(任务运行失败原因是引擎意外退出,可能是复杂任务导致引擎退出，如OOM)";
          asyncExecTaskRunner.markFailed(errorMsg, null)
          asyncExecTaskRunner.transientStatus(ExecutionNodeStatus.Failed)
        }
      case _ =>
    }
  }

}
