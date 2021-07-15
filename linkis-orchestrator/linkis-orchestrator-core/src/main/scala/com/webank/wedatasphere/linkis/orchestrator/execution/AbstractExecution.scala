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

package com.webank.wedatasphere.linkis.orchestrator.execution

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.exception.{OrchestratorErrorCodeSummary, OrchestratorErrorException}
import com.webank.wedatasphere.linkis.orchestrator.execution.AsyncTaskResponse.NotifyListener
import com.webank.wedatasphere.linkis.orchestrator.plans.physical.ExecTask

/**
  *
  */
abstract class AbstractExecution extends Execution with Logging{

  val taskScheduler: TaskScheduler
  val taskManager: TaskManager
  val taskConsumer: TaskConsumer

  //TODO 容器清理
  protected val execTaskToExecutionTasks = new util.concurrent.ConcurrentHashMap[ExecTask, ExecutionTask]()

  def getAllExecutionTasks(): Array[ExecutionTask]

  def getExecutionTask(execTask: ExecTask): ExecutionTask = execTaskToExecutionTasks.get(execTask)

  def start(): Unit = {
    info("execution start")
    taskScheduler.start()
    taskConsumer.init(this)
    taskConsumer.start()
  }

  override def execute(rootExecTask: ExecTask): TaskResponse = {
    val executionTask = taskManager.putExecTask(rootExecTask)
    execTaskToExecutionTasks.put(rootExecTask, executionTask)
    executionTask.notifyMe(new ExecutionClearListener(rootExecTask))
    executionTask.waitForCompleted()
    executionTask.getResponse
  }

  override def executeAsync(rootExecTask: ExecTask): AsyncTaskResponse = {
    if (null == rootExecTask) {
      throw new OrchestratorErrorException(OrchestratorErrorCodeSummary.EXECUTION_ERROR_CODE, "physicalPlan is null")
    }
    val executionTask = taskManager.putExecTask(rootExecTask)
    execTaskToExecutionTasks.put(rootExecTask, executionTask)
    executionTask.notifyMe(new ExecutionClearListener(rootExecTask))
    new AsyncTaskResponse {
      override def notifyMe(listener: NotifyListener): Unit = executionTask.notifyMe(listener)

      override def waitForCompleted(): TaskResponse = {
        executionTask.waitForCompleted()
        executionTask.getResponse
      }
    }
  }

  class ExecutionClearListener(rootExecTask: ExecTask) extends NotifyListener {
    override def apply(taskResponse: TaskResponse): Unit = taskResponse match {
      case t: CompletedTaskResponse => {
        info(s"${rootExecTask.getIDInfo()} completed, Now to remove from execTaskToExecutionTasks")
        execTaskToExecutionTasks.remove(rootExecTask)
      }
      case _ =>
    }
  }
}