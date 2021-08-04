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
import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.orchestrator.core.ResultSet
import com.webank.wedatasphere.linkis.orchestrator.exception.OrchestratorErrorCodeSummary
import com.webank.wedatasphere.linkis.orchestrator.execution.impl.{DefaultFailedTaskResponse, DefaultResultSetTaskResponse}
import com.webank.wedatasphere.linkis.orchestrator.execution.{FailedTaskResponse, SucceedTaskResponse, TaskResponse}
import com.webank.wedatasphere.linkis.orchestrator.plans.physical.{ExecTask, StageExecTask}

import scala.collection.mutable.ArrayBuffer

/**
  *
  *
  */
class GatherStrategyStageInfoExecTask(parents: Array[ExecTask],
                                      children: Array[ExecTask])
  extends StageExecTask(parents, children) with ResultSetExecTask with StatusInfoExecTask with Logging {


  /**
   *
   * 1. Determine whether the subtask is executed successfully, if the execution fails, call the context to mark ExecutionTask as a failure
   * 2. If the task is executed successfully, the result set is summarized
   * 1. 判断子task是否执行成功，如果执行失败，则调用context标记ExecutionTask为失败
   * 2. 如果Task执行成功，则结果集汇总
   *
   * @return
   */
  override def execute(): TaskResponse = {
    val errorExecTasks = getErrorChildrenExecTasks
    if (errorExecTasks.isDefined) {
      val errorReason = parseChildrenErrorInfo(errorExecTasks.get)
      getPhysicalContext.markFailed(errorReason, null)
      return new DefaultFailedTaskResponse(errorReason, OrchestratorErrorCodeSummary.STAGE_ERROR_CODE, null)
    }
    val execIdToResponse = getChildrenResultSet()
    if (null != execIdToResponse && execIdToResponse.nonEmpty) {
      val resultSets = new ArrayBuffer[ResultSet]()
      execIdToResponse.values.foreach { response =>
        resultSets ++= response.getResultSets
      }
      val response = new DefaultResultSetTaskResponse(resultSets.toArray)
      response
    } else {
      new SucceedTaskResponse() {}
    }
  }

  override protected def newNode(): ExecTask = {
    val task = new GatherStrategyStageInfoExecTask(null, null)
    task.setTaskDesc(getTaskDesc)
    task
  }
}
