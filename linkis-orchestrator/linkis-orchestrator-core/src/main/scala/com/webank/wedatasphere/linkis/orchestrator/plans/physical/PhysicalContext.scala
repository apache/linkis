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

package com.webank.wedatasphere.linkis.orchestrator.plans.physical

import com.webank.wedatasphere.linkis.common.listener.Event
import com.webank.wedatasphere.linkis.orchestrator.execution.TaskResponse
import com.webank.wedatasphere.linkis.orchestrator.listener.task.{TaskLogEvent, TaskProgressEvent}
import com.webank.wedatasphere.linkis.orchestrator.plans.PlanContext

/**
  *
  */
trait PhysicalContext extends PlanContext {

  def isCompleted: Boolean

  def markFailed(errorMsg: String, cause: Throwable): Unit

  def markSucceed(response: TaskResponse): Unit

  def broadcastAsyncEvent(event: Event): Unit

  def broadcastSyncEvent(event: Event): Unit

  def broadcastToAll(event: Event): Unit

  def pushLog(taskLogEvent: TaskLogEvent): Unit

  def pushProgress(taskProgressEvent: TaskProgressEvent): Unit

  def belongsTo(execTask: ExecTask): Boolean

  def getRootTask: ExecTask

  def getLeafTasks: Array[ExecTask]

}