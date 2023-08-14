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

package org.apache.linkis.engineconn.computation.executor.async

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.governance.common.utils.{JobUtils, LoggerUtils}
import org.apache.linkis.scheduler.executer.{
  CompletedExecuteResponse,
  ErrorExecuteResponse,
  ExecuteRequest,
  SuccessExecuteResponse
}
import org.apache.linkis.scheduler.queue.{Job, JobInfo}
import org.apache.linkis.scheduler.queue.SchedulerEventState.SchedulerEventState

class AsyncEngineConnJob(task: EngineConnTask, engineExecutionContext: EngineExecutionContext)
    extends Job {

  override def init(): Unit = {}

  override protected def jobToExecuteRequest: ExecuteRequest = {
    new AsyncExecuteRequest(task, engineExecutionContext)
  }

  override def getName: String = getId()

  override def getJobInfo: JobInfo = null

  def getEngineExecutionContext: EngineExecutionContext = this.engineExecutionContext

  def getEngineConnTask: EngineConnTask = task

  override def close(): Unit = {}

  override def transition(state: SchedulerEventState): Unit = Utils.tryFinally {
    val jobId = JobUtils.getJobIdFromMap(task.getProperties)
    LoggerUtils.setJobIdMDC(jobId)
    super.transition(state)
  } {
    LoggerUtils.removeJobIdMDC()
  }

  override def transitionCompleted(executeCompleted: CompletedExecuteResponse): Unit = {
    var executeCompletedNew: CompletedExecuteResponse = executeCompleted
    executeCompleted match {
      case _: SuccessExecuteResponse =>
        Utils.tryCatch {
          logger.info(
            s"job ${task.getTaskId} execute success, Start to  close engineExecutionContext"
          )
          engineExecutionContext.close()
          logger.info(
            s"job ${task.getTaskId} execute success, Finished to  close engineExecutionContext"
          )
        } { t =>
          executeCompletedNew = ErrorExecuteResponse("send resultSet to entrance failed!", t)
        }
      case _ =>
        Utils.tryQuietly(engineExecutionContext)
    }
    super.transitionCompleted(executeCompleted)
  }

}
