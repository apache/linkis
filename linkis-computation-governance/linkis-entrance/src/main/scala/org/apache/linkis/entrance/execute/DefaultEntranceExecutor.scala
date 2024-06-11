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

package org.apache.linkis.entrance.execute

import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.exception.{EntranceErrorCode, EntranceErrorException}
import org.apache.linkis.entrance.job.EntranceExecuteRequest
import org.apache.linkis.entrance.orchestrator.EntranceOrchestrationFactory
import org.apache.linkis.entrance.utils.JobHistoryHelper
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.governance.common.protocol.task.ResponseTaskStatus
import org.apache.linkis.governance.common.utils.LoggerUtils
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.orchestrator.Orchestration
import org.apache.linkis.orchestrator.computation.entity.ComputationJobReq
import org.apache.linkis.orchestrator.computation.operation.log.{LogOperation, LogProcessor}
import org.apache.linkis.orchestrator.computation.operation.progress.{
  DefaultProgressOperation,
  ProgressProcessor
}
import org.apache.linkis.orchestrator.core.{OrchestrationFuture, OrchestrationResponse}
import org.apache.linkis.orchestrator.domain.JobReq
import org.apache.linkis.orchestrator.execution.{
  ArrayResultSetTaskResponse,
  FailedTaskResponse,
  ResultSetTaskResponse,
  SucceedTaskResponse
}
import org.apache.linkis.orchestrator.execution.impl.DefaultFailedTaskResponse
import org.apache.linkis.orchestrator.plans.unit.CodeLogicalUnit
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.scheduler.executer._
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.exception.ExceptionUtils

import java.util
import java.util.Date

class DefaultEntranceExecutor(id: Long)
    extends EntranceExecutor(id)
    with SingleTaskOperateSupport
    with Logging {

  /**
   *   1. get logProcessor by log operate 2. update log by logListener
   *
   * @param orchestratorFuture
   */
  def dealLog(orchestratorFuture: OrchestrationFuture, job: EntranceJob): LogProcessor = {
    val logProcessor = orchestratorFuture.operate[LogProcessor](LogOperation.LOG)
    logProcessor.registerLogNotify(logEvent => {
      if (null != job) {
        job.getLogListener.foreach(_.onLogUpdate(job, logEvent.log))
      }
    })
    logProcessor
  }

  def dealProgressWithResource(
      orchestratorFuture: OrchestrationFuture,
      entranceJob: EntranceJob
  ): ProgressProcessor = {
    val progressProcessor =
      orchestratorFuture.operate[ProgressProcessor](DefaultProgressOperation.PROGRESS_NAME)
    progressProcessor.doOnObtain(progressInfoEvent => {
      if (null != entranceJob) {
        // Make sure to update the database, put it in front
        try {
          JobHistoryHelper.updateJobRequestMetrics(
            entranceJob.getJobRequest,
            progressInfoEvent.resourceMap,
            progressInfoEvent.infoMap
          )
        } catch {
          case e: Exception =>
            logger.error("update job metrics error", e)
        }
        entranceJob.getProgressListener.foreach(
          _.onProgressUpdate(entranceJob, progressInfoEvent.progress, entranceJob.getProgressInfo)
        )
      }
    })
    progressProcessor
  }

  def dealResponse(
      orchestrationResponse: OrchestrationResponse,
      entranceExecuteRequest: EntranceExecuteRequest,
      orchestration: Orchestration
  ): Unit = {
    LoggerUtils.setJobIdMDC(getId.toString)
    orchestrationResponse match {
      case succeedResponse: SucceedTaskResponse =>
        succeedResponse match {
          case resultSetResp: ResultSetTaskResponse =>
            logger.info(
              s"JobRequest : ${entranceExecuteRequest.jobId()} succeed to execute task, and get result."
            )
            entranceExecuteRequest.getJob.getEntranceContext
              .getOrCreatePersistenceManager()
              .onResultSetCreated(
                entranceExecuteRequest.getJob,
                AliasOutputExecuteResponse(null, resultSetResp.getResultSet)
              )
          //
          case arrayResultSetPathResp: ArrayResultSetTaskResponse =>
            logger.info(
              s"JobRequest :  ${entranceExecuteRequest.jobId()} succeed to execute task, and get result array."
            )
            if (
                null != arrayResultSetPathResp.getResultSets && arrayResultSetPathResp.getResultSets.length > 0
            ) {
              val resultsetSize = arrayResultSetPathResp.getResultSets.length
              entranceExecuteRequest.getJob.setResultSize(resultsetSize)
              entranceExecuteRequest.getJob
                .asInstanceOf[EntranceJob]
                .addAndGetResultSize(resultsetSize)
            }
            val firstResultSet = arrayResultSetPathResp.getResultSets.headOption.orNull
            if (null != firstResultSet) {
              // assert that all result set files have same parent path, so we get the first
              Utils.tryCatch {
                entranceExecuteRequest.getJob
                  .asInstanceOf[EntranceJob]
                  .getEntranceContext
                  .getOrCreatePersistenceManager()
                  .onResultSetCreated(
                    entranceExecuteRequest.getJob,
                    AliasOutputExecuteResponse(firstResultSet.alias, firstResultSet.result)
                  )
              } { case e: Exception =>
                val msg = s"Persist resultSet error. ${e.getMessage}"
                logger.error(msg)
                val errorExecuteResponse = new DefaultFailedTaskResponse(
                  msg,
                  EntranceErrorCode.RESULT_NOT_PERSISTED_ERROR.getErrCode,
                  e
                )
                dealResponse(errorExecuteResponse, entranceExecuteRequest, orchestration)
                return
              }
            }
          case _ =>
            logger.info(
              s"JobRequest : ${entranceExecuteRequest.jobId()} succeed to execute task,no result."
            )
        }
        entranceExecuteRequest.getJob.getLogListener.foreach(
          _.onLogUpdate(
            entranceExecuteRequest.getJob,
            LogUtils.generateInfo(
              s"Congratulations! Your job : ${entranceExecuteRequest.getJob.getId} executed with status succeed and ${entranceExecuteRequest.getJob
                .addAndGetResultSize(0)} results."
            )
          )
        )
        Utils.tryAndWarn(doOnSucceed(entranceExecuteRequest))
      case failedResponse: FailedTaskResponse =>
        Utils.tryAndWarn {
          doOnFailed(entranceExecuteRequest, orchestration, failedResponse)
        }
      case _ =>
        val msg =
          s"Job : ${entranceExecuteRequest.getJob.getId} , JobRequest id: ${entranceExecuteRequest.jobId()} returned unknown response}"
        logger.error(msg)
        entranceExecuteRequest.getJob.getLogListener.foreach(
          _.onLogUpdate(entranceExecuteRequest.getJob, LogUtils.generateERROR(msg))
        )
    }
    LoggerUtils.removeJobIdMDC()
  }

  def requestToComputationJobReq(entranceExecuteRequest: EntranceExecuteRequest): JobReq = {
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
    jobReqBuilder.build()
  }

  override def close(): Unit = {
    getEngineExecuteAsyncReturn.foreach { e =>
      e.notifyError(s"$toString has already been completed with state $state.")
    }
  }

  private def doOnSucceed(entranceExecuteRequest: EntranceExecuteRequest): Unit = {
    getEngineExecuteAsyncReturn.foreach { jobReturn =>
      jobReturn.notifyStatus(
        ResponseTaskStatus(entranceExecuteRequest.getJob.getId, ExecutionNodeStatus.Succeed)
      )
    }
  }

  private def doOnFailed(
      entranceExecuteRequest: EntranceExecuteRequest,
      orchestration: Orchestration,
      failedResponse: FailedTaskResponse
  ) = {
    val msg = failedResponse.getErrorCode + ", " + failedResponse.getErrorMsg
    getEngineExecuteAsyncReturn.foreach { jobReturn =>
      jobReturn.notifyError(msg, failedResponse.getCause)
      jobReturn.notifyStatus(
        ResponseTaskStatus(entranceExecuteRequest.getJob.getId, ExecutionNodeStatus.Failed)
      )
    }
  }

  override def kill(): Boolean = {
    LoggerUtils.setJobIdMDC(getId.toString)
    logger.info("Entrance start to kill job {} invoke Orchestrator ", this.getId)
    Utils.tryAndWarn {
      val msg = s"You job with id  was cancelled by user!"
      getRunningOrchestrationFuture.foreach(_.cancel(msg))
    }
    LoggerUtils.removeJobIdMDC()
    true
  }

  override def pause(): Boolean = {
    // TODO
    true
  }

  override def resume(): Boolean = {
    // TODO
    true
  }

  def getRunningOrchestrationFuture: Option[OrchestrationFuture] = {
    val asyncReturn = getEngineExecuteAsyncReturn
    if (asyncReturn.isDefined) {
      asyncReturn.get.getOrchestrationFuture()
    } else {
      None
    }
  }

  override protected def callExecute(request: ExecuteRequest): ExecuteResponse = {

    val entranceExecuteRequest: EntranceExecuteRequest = request match {
      case request: EntranceExecuteRequest =>
        request
      case _ =>
        throw new EntranceErrorException(
          EntranceErrorCode.EXECUTE_REQUEST_INVALID.getErrCode,
          s"Invalid entranceExecuteRequest : ${BDPJettyServerHelper.gson.toJson(request)}"
        )
    }
    // 1. create JobReq
    val compJobReq = requestToComputationJobReq(entranceExecuteRequest)
    Utils.tryCatch[ExecuteResponse] {
      // 2. orchestrate compJobReq get Orchestration
      val orchestration =
        EntranceOrchestrationFactory.getOrchestrationSession().orchestrate(compJobReq)
      val orchestratorFuture = orchestration.asyncExecute()
      val msg = s"JobRequest (${entranceExecuteRequest.jobId()}) was submitted to Orchestrator."
      logger.info(msg)
      entranceExecuteRequest.getJob.getLogListener.foreach(
        _.onLogUpdate(
          entranceExecuteRequest.getJob,
          LogUtils.generateInfo(msg + "(您的任务已经提交给Orchestrator进行编排执行)")
        )
      )

      if (entranceExecuteRequest.getJob.getJobRequest.getMetrics == null) {
        logger.warn("Job Metrics has not been initialized")
      } else {
        if (
            !entranceExecuteRequest.getJob.getJobRequest.getMetrics.containsKey(
              TaskConstant.JOB_TO_ORCHESTRATOR
            )
        ) {
          entranceExecuteRequest.getJob.getJobRequest.getMetrics
            .put(TaskConstant.JOB_TO_ORCHESTRATOR, new Date(System.currentTimeMillis()))
        }
      }
      // 2. deal log And Response
      val logProcessor = dealLog(orchestratorFuture, entranceExecuteRequest.getJob)
      val progressAndResourceProcessor =
        dealProgressWithResource(orchestratorFuture, entranceExecuteRequest.getJob)
      orchestratorFuture.notifyMe(orchestrationResponse => {
        dealResponse(orchestrationResponse, entranceExecuteRequest, orchestration)
      })

      val jobReturn = if (getEngineExecuteAsyncReturn.isDefined) {
        getEngineExecuteAsyncReturn.foreach(_.closeOrchestration())
        getEngineExecuteAsyncReturn.get
      } else {
        logger.info(
          s"For job ${entranceExecuteRequest.jobId()} and orchestrator task id ${compJobReq.getId} to create EngineExecuteAsyncReturn"
        )
        new EngineExecuteAsyncReturn(request, null)
      }
      jobReturn.setOrchestrationObjects(
        orchestratorFuture,
        logProcessor,
        progressAndResourceProcessor
      )
      setEngineReturn(jobReturn)
      jobReturn
    } { t: Throwable =>
      if (getEngineExecuteAsyncReturn.isEmpty) {
        val msg =
          s"JobRequest (${entranceExecuteRequest.jobId()}) submit failed, reason, ${ExceptionUtils.getMessage(t)}"
        entranceExecuteRequest.getJob.getLogListener.foreach(
          _.onLogUpdate(
            entranceExecuteRequest.getJob,
            LogUtils.generateERROR(ExceptionUtils.getStackTrace(t))
          )
        )
        ErrorExecuteResponse(msg, t)
      } else {
        val msg =
          s"JobRequest (${entranceExecuteRequest.jobId()}) submit failed, reason, ${ExceptionUtils.getMessage(t)}"
        val failedResponse =
          new DefaultFailedTaskResponse(msg, EntranceErrorCode.SUBMIT_JOB_ERROR.getErrCode, t)
        doOnFailed(entranceExecuteRequest, null, failedResponse)
        null
      }
    }
  }

}
