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

package com.webank.wedatasphere.linkis.orchestrator.listener.task

import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.orchestrator.core.ResultSet
import com.webank.wedatasphere.linkis.orchestrator.execution.CompletedTaskResponse
import com.webank.wedatasphere.linkis.orchestrator.listener.{OrchestratorAsyncEvent, OrchestratorSyncEvent}
import com.webank.wedatasphere.linkis.orchestrator.plans.physical.ExecTask
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo

/**
  *
  *
  */
trait TaskInfoEvent {
  val execTask: ExecTask
}

case class TaskStatusEvent(execTask: ExecTask, status: ExecutionNodeStatus) extends TaskInfoEvent with OrchestratorSyncEvent with OrchestratorAsyncEvent {
  override def toString: String = {
    s"task ${execTask.getIDInfo()}, status ${status}"
  }
}

case class TaskResultSetEvent(execTask: ExecTask, resultSet: ResultSet) extends TaskInfoEvent with OrchestratorSyncEvent {
  override def toString: String = {
    s"task ${execTask.getIDInfo()}"
  }
}

case class TaskResultSetSizeEvent(execTask: ExecTask, resultSize: Int) extends TaskInfoEvent with OrchestratorSyncEvent {
  override def toString: String = {
    s"task ${execTask.getIDInfo()}, size ${resultSize}"
  }
}

case class TaskErrorResponseEvent(execTask: ExecTask, errorMsg: String) extends TaskInfoEvent with OrchestratorSyncEvent {
  override def toString: String = {
    s"task ${execTask.getIDInfo()}, errorMsg ${errorMsg}"
  }
}

//case class TaskProgressEvent(execId: ExecTask, progress: Float, progressInfo: Array[JobProgressInfo]) extends TaskInfoEvent

case class TaskLogEvent(execTask: ExecTask, log: String) extends TaskInfoEvent with OrchestratorAsyncEvent


case class RootTaskResponseEvent(execTask: ExecTask, taskResponse: CompletedTaskResponse) extends TaskInfoEvent with OrchestratorSyncEvent

case class KillRootExecTaskEvent(execTask: ExecTask) extends  TaskInfoEvent with  OrchestratorAsyncEvent

case class TaskReheaterEvent(execTask: ExecTask) extends TaskInfoEvent with OrchestratorAsyncEvent

case class TaskProgressEvent(execTask: ExecTask, progress: Float, progressInfo: Array[JobProgressInfo]) extends TaskInfoEvent with OrchestratorAsyncEvent {
  override def toString: String = {
    s"task ${execTask.getIDInfo()}, progress ${progress}"
  }
}

case class TaskConsumerEvent(execTask: ExecTask) extends TaskInfoEvent with OrchestratorAsyncEvent
