/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.orchestrator.strategy.async

import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.orchestrator.execution.ExecTaskRunner
import com.webank.wedatasphere.linkis.orchestrator.execution.impl.DefaultTaskManager
import com.webank.wedatasphere.linkis.orchestrator.listener.OrchestratorSyncEvent
import com.webank.wedatasphere.linkis.orchestrator.listener.execution.ExecTaskRunnerCompletedEvent
import com.webank.wedatasphere.linkis.orchestrator.listener.task._
import com.webank.wedatasphere.linkis.orchestrator.plans.physical.ExecTask

/**
  *
  *
  */
class AsyncTaskManager extends DefaultTaskManager with TaskStatusListener with TaskResultSetListener {

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
      case _ =>
    }
  }

  override def onResultSetCreate(taskResultSetEvent: TaskResultSetEvent): Unit = {
    info(s"received taskResultSetEvent ${taskResultSetEvent.execTask.getId}")
    val execTask = taskResultSetEvent.execTask
    val rootExecTask = execTask.getPhysicalContext.getRootTask
    val runners = getRunningTask(rootExecTask)
    runners.find(_.task.getId.equals(execTask.getId)).foreach {
      case asyncExecTaskRunner: AsyncExecTaskRunner =>
        asyncExecTaskRunner.addResultSet(taskResultSetEvent.resultSet)
      case _ =>
    }
  }

  override def onResultSizeCreated(taskResultSetSizeEvent: TaskResultSetSizeEvent): Unit = {
    info(s"received taskResultSetSizeEvent $taskResultSetSizeEvent")
    val execTask = taskResultSetSizeEvent.execTask
    val rootExecTask = execTask.getPhysicalContext.getRootTask
    val runners = getRunningTask(rootExecTask)
    runners.find(_.task.getId.equals(execTask.getId)).foreach  {
      case asyncExecTaskRunner: AsyncExecTaskRunner =>
        asyncExecTaskRunner.setResultSize(taskResultSetSizeEvent.resultSize)
      case _ =>
    }
  }

  override def onTaskErrorResponseEvent(taskErrorResponseEvent: TaskErrorResponseEvent): Unit = {
    info(s"received taskErrorResponseEvent $taskErrorResponseEvent")
    val execTask = taskErrorResponseEvent.execTask
    val rootExecTask = execTask.getPhysicalContext.getRootTask
    val runners = getRunningTask(rootExecTask)
    runners.find(_.task.getId.equals(execTask.getId)).foreach {
      case asyncExecTaskRunner: AsyncExecTaskRunner =>
        asyncExecTaskRunner.markFailed(taskErrorResponseEvent.errorMsg, null)
      case _ =>
    }
  }

  override def onStatusUpdate(taskStatusEvent: TaskStatusEvent): Unit = {
    info(s"received taskStatusEvent $taskStatusEvent")
    if (ExecutionNodeStatus.isCompleted(taskStatusEvent.status)) {
      val execTask = taskStatusEvent.execTask
      val rootExecTask = execTask.getPhysicalContext.getRootTask
      val runners = getRunningTask(rootExecTask)
      runners.find(_.task.getId.equals(execTask.getId)).foreach { runner =>
        info(s"Task(${execTask.getIDInfo}) is completed, status ${taskStatusEvent.status}")
        //To transient taskRunner status
        runner.transientStatus(taskStatusEvent.status)
        //addCompletedTask(runner)
      }
    }
  }


  override protected def execTaskToTaskRunner(execTask: ExecTask): ExecTaskRunner = {
    new AsyncExecTaskRunnerImpl(execTask)
  }

}
