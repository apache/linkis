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

package com.webank.wedatasphere.linkis.orchestrator.strategy

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.core.ResultSet
import com.webank.wedatasphere.linkis.orchestrator.exception.OrchestratorErrorCodeSummary
import com.webank.wedatasphere.linkis.orchestrator.execution.impl.{DefaultFailedTaskResponse, DefaultResultSetTaskResponse}
import com.webank.wedatasphere.linkis.orchestrator.execution.{CompletedTaskResponse, SucceedTaskResponse, TaskResponse}
import com.webank.wedatasphere.linkis.orchestrator.plans.logical.{EndJobTaskDesc, StartJobTaskDesc}
import com.webank.wedatasphere.linkis.orchestrator.plans.physical.{ExecTask, JobExecTask, ReheatableExecTask}

import scala.collection.mutable.ArrayBuffer

/**
 *
 *
 */
class GatherStrategyJobExecTask(parents: Array[ExecTask],
                                children: Array[ExecTask]) extends JobExecTask(parents, children)
  with ReheatableExecTask with ResultSetExecTask with StatusInfoExecTask with Logging {

  /**
   * Job End Task 汇总结果响应
   *
   * @return
   */
  override def execute(): TaskResponse = getTaskDesc match {
    case _: StartJobTaskDesc =>
      super.execute()
    case _: EndJobTaskDesc =>
      if (getPhysicalContext.isCompleted) {
        val msg = s"PhysicalContext is completed, Job${getIDInfo()} will be mark Failed "
        info(msg)
        new DefaultFailedTaskResponse(msg, OrchestratorErrorCodeSummary.EXECUTION_FOR_EXECUTION_ERROR_CODE, null) {}
      } else {

        val errorExecTasks = getErrorChildrenExecTasks
        if (errorExecTasks.isDefined) {
          val errorReason = parseChildrenErrorInfo(errorExecTasks.get)
          getPhysicalContext.markFailed(errorReason, null)
          return new DefaultFailedTaskResponse(errorReason, OrchestratorErrorCodeSummary.STAGE_ERROR_CODE, null)
        }

        val execIdToResponse = getChildrenResultSet()
        val response = if (null != execIdToResponse && execIdToResponse.nonEmpty) {
          val resultSets = new ArrayBuffer[ResultSet]()
          execIdToResponse.values.foreach { response =>
            resultSets ++= response.getResultSets
          }
          val response = new DefaultResultSetTaskResponse(resultSets.toArray)
          response
        } else {
          new SucceedTaskResponse() {}
        }
        debug(s"Job${getIDInfo()} end execute finished, now to mark executionTask succeed")
        getPhysicalContext.markSucceed(response)
        response
      }
  }

  override protected def newNode(): ExecTask = {
    val task = new GatherStrategyJobExecTask(null, null)
    task.setTaskDesc(getTaskDesc)
    task
  }

  override def canExecute: Boolean = {
    !getReheating
  }
}
