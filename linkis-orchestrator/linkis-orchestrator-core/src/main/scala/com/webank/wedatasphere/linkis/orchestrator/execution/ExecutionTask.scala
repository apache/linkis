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

import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.orchestrator.domain.Node
import com.webank.wedatasphere.linkis.orchestrator.execution.AsyncTaskResponse.NotifyListener
import com.webank.wedatasphere.linkis.orchestrator.plans.physical.ExecTask

/**
  *
  */
trait ExecutionTask extends Node {

  def getMaxParallelism: Int

  def waitForCompleted(): Unit

  def notifyMe(listener: NotifyListener): Unit

  def getRootExecTask: ExecTask

  def getResponse: TaskResponse

  def getStatus: ExecutionNodeStatus

  def transientStatus(status: ExecutionNodeStatus): Unit

  def markCompleted(taskCompletedTaskResponse: CompletedTaskResponse)

}