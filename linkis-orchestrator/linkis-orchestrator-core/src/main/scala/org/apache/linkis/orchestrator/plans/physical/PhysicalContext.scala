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

package org.apache.linkis.orchestrator.plans.physical

import org.apache.linkis.common.listener.Event
import org.apache.linkis.orchestrator.execution.TaskResponse
import org.apache.linkis.orchestrator.listener.task.{
  TaskLogEvent,
  TaskRunningInfoEvent,
  TaskYarnResourceEvent
}
import org.apache.linkis.orchestrator.plans.PlanContext

/**
 */
trait PhysicalContext extends PlanContext {

  def isCompleted: Boolean

  def markFailed(errorMsg: String, cause: Throwable): Unit

  def markSucceed(response: TaskResponse): Unit

  def broadcastAsyncEvent(event: Event): Unit

  def broadcastSyncEvent(event: Event): Unit

  def broadcastToAll(event: Event): Unit

  def pushLog(taskLogEvent: TaskLogEvent): Unit

  def pushProgress(taskProgressEvent: TaskRunningInfoEvent): Unit

  def belongsTo(execTask: ExecTask): Boolean

  def getRootTask: ExecTask

  def getLeafTasks: Array[ExecTask]

}
