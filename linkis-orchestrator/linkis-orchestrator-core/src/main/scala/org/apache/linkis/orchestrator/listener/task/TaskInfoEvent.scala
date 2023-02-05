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

package org.apache.linkis.orchestrator.listener.task

import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.manager.common.protocol.resource.ResourceWithStatus
import org.apache.linkis.orchestrator.core.ResultSet
import org.apache.linkis.orchestrator.execution.CompletedTaskResponse
import org.apache.linkis.orchestrator.listener.{OrchestratorAsyncEvent, OrchestratorSyncEvent}
import org.apache.linkis.orchestrator.plans.physical.ExecTask
import org.apache.linkis.protocol.engine.JobProgressInfo

import java.util

/**
 */
trait TaskInfoEvent {
  val execTask: ExecTask
}

case class TaskStatusEvent(execTask: ExecTask, status: ExecutionNodeStatus)
    extends TaskInfoEvent
    with OrchestratorSyncEvent
    with OrchestratorAsyncEvent {

  override def toString: String = {
    s"task ${execTask.getIDInfo()}, status ${status}"
  }

}

case class TaskResultSetEvent(execTask: ExecTask, resultSet: ResultSet)
    extends TaskInfoEvent
    with OrchestratorSyncEvent {

  override def toString: String = {
    s"task ${execTask.getIDInfo()}"
  }

}

case class TaskResultSetSizeEvent(execTask: ExecTask, resultSize: Int)
    extends TaskInfoEvent
    with OrchestratorSyncEvent {

  override def toString: String = {
    s"task ${execTask.getIDInfo()}, size ${resultSize}"
  }

}

case class TaskErrorResponseEvent(execTask: ExecTask, errorMsg: String)
    extends TaskInfoEvent
    with OrchestratorSyncEvent {

  override def toString: String = {
    s"task ${execTask.getIDInfo()}"
  }

}

// case class TaskProgressEvent(execId: ExecTask, progress: Float, progressInfo: Array[JobProgressInfo]) extends TaskInfoEvent

case class TaskLogEvent(execTask: ExecTask, log: String)
    extends TaskInfoEvent
    with OrchestratorAsyncEvent

case class RootTaskResponseEvent(execTask: ExecTask, taskResponse: CompletedTaskResponse)
    extends TaskInfoEvent
    with OrchestratorSyncEvent

case class KillRootExecTaskEvent(execTask: ExecTask)
    extends TaskInfoEvent
    with OrchestratorAsyncEvent

case class TaskReheaterEvent(execTask: ExecTask) extends TaskInfoEvent with OrchestratorAsyncEvent

case class TaskRunningInfoEvent(
    execTask: ExecTask,
    progress: Float,
    progressInfo: Array[JobProgressInfo],
    resourceMap: util.HashMap[String, ResourceWithStatus],
    infoMap: util.HashMap[String, AnyRef]
) extends TaskInfoEvent
    with OrchestratorAsyncEvent {

  override def toString: String = {
    s"task ${execTask.getIDInfo()}, progress ${progress}"
  }

}

case class TaskConsumerEvent(execTask: ExecTask) extends TaskInfoEvent with OrchestratorAsyncEvent

case class TaskYarnResourceEvent(
    execTask: ExecTask,
    resourceMap: util.HashMap[String, ResourceWithStatus]
) extends TaskInfoEvent
    with OrchestratorAsyncEvent
