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

package org.apache.linkis.entrance.execute.simple

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.exception.{EntranceErrorCode, EntranceErrorException}
import org.apache.linkis.entrance.execute.{EngineExecuteAsyncReturn, EntranceExecutor}
import org.apache.linkis.entrance.job.EntranceExecuteRequest
import org.apache.linkis.governance.common.utils.LoggerUtils
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.orchestrator.code.plans.ast.CodeJob
import org.apache.linkis.orchestrator.code.plans.logical.CodeLogicalUnitTaskDesc
import org.apache.linkis.orchestrator.computation.entity.ComputationJobReq
import org.apache.linkis.orchestrator.computation.physical.CodeLogicalUnitExecTask
import org.apache.linkis.orchestrator.converter.ASTContextImpl
import org.apache.linkis.orchestrator.execution.{
  AsyncTaskResponse,
  FailedTaskResponse,
  SucceedTaskResponse
}
import org.apache.linkis.orchestrator.listener.OrchestratorListenerBusContext
import org.apache.linkis.orchestrator.plans.physical.{ExecTask, PhysicalContextImpl}
import org.apache.linkis.orchestrator.plans.unit.CodeLogicalUnit
import org.apache.linkis.scheduler.executer._

import java.util

class SimpleEntranceExecutor(
    id: Long,
    orchestratorListenerBusContext: OrchestratorListenerBusContext
) extends EntranceExecutor(id)
    with SingleTaskOperateSupport
    with Logging {

  private var codeUnitExecTask: CodeLogicalUnitExecTask = null

  override protected def callExecute(request: ExecuteRequest): ExecuteResponse = {
    val entranceExecuteRequest: EntranceExecuteRequest = request match {
      case request: EntranceExecuteRequest =>
        request
      case _ =>
        throw new EntranceErrorException(
          EntranceErrorCode.EXECUTE_REQUEST_INVALID.getErrCode,
          s"Invalid entranceExecuteRequest : ${request.code}"
        )
    }
    // 1. create JobReq
    val computationJobReq = requestToComputationJobReq(entranceExecuteRequest)
    // 2. create code job
    val codeJob = new CodeJob(null, null)
    val astContext = ASTContextImpl.newBuilder().setJobReq(computationJobReq).build()
    codeJob.setAstContext(astContext)
    codeJob.setCodeLogicalUnit(computationJobReq.getCodeLogicalUnit)
    codeJob.setParams(computationJobReq.getParams)
    codeJob.setName(computationJobReq.getName + "_Job")
    codeJob.setSubmitUser(computationJobReq.getSubmitUser)
    codeJob.setExecuteUser(computationJobReq.getExecuteUser)
    codeJob.setLabels(computationJobReq.getLabels)
    codeJob.setPriority(computationJobReq.getPriority)
    codeUnitExecTask = new CodeLogicalUnitExecTask(Array[ExecTask](), Array[ExecTask]())
    // set job id, can find by getEntranceContext.getOrCreateScheduler().get(execId).map(_.asInstanceOf[Job])
    codeUnitExecTask.setId(entranceExecuteRequest.getJob.getId)
    // 3.set code unit
    codeUnitExecTask.setCodeLogicalUnit(computationJobReq.getCodeLogicalUnit)
    codeUnitExecTask.setTaskDesc(CodeLogicalUnitTaskDesc(codeJob))
    // 4. set context
    val context = new PhysicalContextImpl(codeUnitExecTask, Array.empty)
    context.setSyncBus(orchestratorListenerBusContext.getOrchestratorSyncListenerBus)
    context.setAsyncBus(orchestratorListenerBusContext.getOrchestratorAsyncListenerBus)
    // 5. execute
    val response = codeUnitExecTask.execute()
    response match {
      case async: AsyncTaskResponse =>
        new EngineExecuteAsyncReturn(request, null)
      case succeed: SucceedTaskResponse =>
        logger.info(s"Succeed to execute ExecTask(${getId})")
        SuccessExecuteResponse()
      case failedTaskResponse: FailedTaskResponse =>
        logger.info(s"Failed to execute ExecTask(${getId})")
        ErrorExecuteResponse(failedTaskResponse.getErrorMsg, failedTaskResponse.getCause)
      case _ =>
        logger.warn(s"ExecTask(${getId}) need to retry")
        ErrorExecuteResponse("unknown response: " + response, null)
    }
  }

  def requestToComputationJobReq(
      entranceExecuteRequest: EntranceExecuteRequest
  ): ComputationJobReq = {
    val jobReqBuilder = ComputationJobReq.newBuilder()
    jobReqBuilder.setId(entranceExecuteRequest.jobId())
    jobReqBuilder.setSubmitUser(entranceExecuteRequest.submitUser())
    jobReqBuilder.setExecuteUser(entranceExecuteRequest.executeUser())
    val codeTypeLabel: Label[_] = LabelUtil.getCodeTypeLabel(entranceExecuteRequest.getLabels)
    if (null == codeTypeLabel) {
      throw new EntranceErrorException(
        EntranceErrorCode.EXECUTE_REQUEST_INVALID.getErrCode,
        s"code Type Label is needed"
      )
    }
    val codes = new util.ArrayList[String]()
    codes.add(entranceExecuteRequest.code())
    val codeLogicalUnit =
      new CodeLogicalUnit(codes, codeTypeLabel.asInstanceOf[CodeLanguageLabel])
    jobReqBuilder.setCodeLogicalUnit(codeLogicalUnit)
    jobReqBuilder.setLabels(entranceExecuteRequest.getLabels)
    jobReqBuilder.setExecuteUser(entranceExecuteRequest.executeUser())
    jobReqBuilder.setParams(entranceExecuteRequest.properties())
    jobReqBuilder.build().asInstanceOf[ComputationJobReq]
  }

  override def kill(): Boolean = {
    LoggerUtils.setJobIdMDC(getId.toString)
    logger.info("Entrance start to kill job {} invoke Orchestrator ", this.getId)
    Utils.tryAndWarn {
      if (null != codeUnitExecTask) {
        codeUnitExecTask.kill()
      }
    }
    LoggerUtils.removeJobIdMDC()
    true
  }

  override def pause(): Boolean = {
    true
  }

  override def resume(): Boolean = {
    true
  }

  override def close(): Unit = {
    getEngineExecuteAsyncReturn.foreach { e =>
      e.notifyError(s"$toString has already been completed with state $state.")
    }
  }

}
