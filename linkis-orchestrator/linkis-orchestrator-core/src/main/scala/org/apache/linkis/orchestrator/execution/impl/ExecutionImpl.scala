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

package org.apache.linkis.orchestrator.execution.impl

import org.apache.linkis.common.listener.Event
import org.apache.linkis.orchestrator.execution._
import org.apache.linkis.orchestrator.listener.{OrchestratorAsyncEvent, OrchestratorSyncEvent}
import org.apache.linkis.orchestrator.listener.execution.{
  ExecutionTaskCompletedEvent,
  ExecutionTaskCompletedListener
}
import org.apache.linkis.orchestrator.listener.task.{
  KillRootExecTaskEvent,
  OrchestrationKillListener
}

import scala.collection.JavaConverters._

/**
 */
class ExecutionImpl(
    override val taskScheduler: TaskScheduler,
    override val taskManager: TaskManager,
    override val taskConsumer: TaskConsumer
) extends AbstractExecution
    with ExecutionTaskCompletedListener
    with OrchestrationKillListener {

  override def onSyncEvent(event: OrchestratorSyncEvent): Unit = event match {
    case executionTaskCompletedEvent: ExecutionTaskCompletedEvent =>
      onExecutionTaskCompletedEvent(executionTaskCompletedEvent)
    case killRootExecTaskEvent: KillRootExecTaskEvent =>
      onKillRootExecTaskEvent(killRootExecTaskEvent)
    case _ =>
  }

  override def onEventError(event: Event, t: Throwable): Unit = {}

  override def onExecutionTaskCompletedEvent(
      executionTaskCompletedEvent: ExecutionTaskCompletedEvent
  ): Unit = {
    executionTaskCompletedEvent.executionTask.markCompleted(
      executionTaskCompletedEvent.taskResponse
    )
  }

  override def onKillRootExecTaskEvent(killRootExecTaskEvent: KillRootExecTaskEvent): Unit = {
    val execTask = killRootExecTaskEvent.execTask
    logger.info(s"receive killRootExecTaskEvent ${execTask.getIDInfo}")
    val runners = taskManager.getRunningTask(execTask)
    if (null != runners && runners.nonEmpty) {
      runners.foreach {
        taskScheduler.cancelTask(_, true)
      }
    } else {
      logger.info(
        s"${execTask.getIDInfo()} running task is null, now to mark ExecutionTask Failed "
      )
      execTask.getPhysicalContext.markFailed("Execution Task is cancelled", null)
    }
  }

}
