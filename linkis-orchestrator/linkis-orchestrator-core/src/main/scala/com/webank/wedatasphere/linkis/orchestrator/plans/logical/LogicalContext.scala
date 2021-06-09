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

package com.webank.wedatasphere.linkis.orchestrator.plans.logical

import com.webank.wedatasphere.linkis.common.listener.Event
import com.webank.wedatasphere.linkis.orchestrator.listener.task.{TaskLogEvent, TaskProgressEvent}
import com.webank.wedatasphere.linkis.orchestrator.plans.PlanContext

/**
  *
  */
trait LogicalContext extends PlanContext {

  def getJobTasks: Array[JobTask]

  def addJobTask(jobTask: JobTask): Unit

  def getStageTasks: Array[StageTask]

  def addStageTask(stageTask: StageTask): Unit

  def isResolved: Boolean

  def pushLog(taskLogEvent: TaskLogEvent): Unit

  def pushProgress(taskProgressEvent: TaskProgressEvent): Unit

  def broadcastAsyncEvent(event: Event): Unit

  def broadcastSyncEvent(event: Event): Unit

}
