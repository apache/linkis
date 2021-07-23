/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.entrance.execute

import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.exception.{EntranceErrorCode, EntranceErrorException}
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecuteRequest
import com.webank.wedatasphere.linkis.entrance.orchestrator.EntranceOrchestrationFactory
import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.governance.common.protocol.task.ResponseTaskStatus
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.CodeLanguageLabel
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtil
import com.webank.wedatasphere.linkis.orchestrator.Orchestration
import com.webank.wedatasphere.linkis.orchestrator.computation.entity.ComputationJobReq
import com.webank.wedatasphere.linkis.orchestrator.computation.operation.log.{LogOperation, LogProcessor}
import com.webank.wedatasphere.linkis.orchestrator.core.{OrchestrationFuture, OrchestrationResponse}
import com.webank.wedatasphere.linkis.orchestrator.domain.JobReq
import com.webank.wedatasphere.linkis.orchestrator.execution.impl.DefaultFailedTaskResponse
import com.webank.wedatasphere.linkis.orchestrator.execution.{ArrayResultSetTaskResponse, FailedTaskResponse, ResultSetTaskResponse, SucceedTaskResponse}
import com.webank.wedatasphere.linkis.orchestrator.plans.unit.CodeLogicalUnit
import com.webank.wedatasphere.linkis.scheduler.executer.{AliasOutputExecuteResponse, ConcurrentTaskOperateSupport, ErrorExecuteResponse, ExecuteRequest, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import java.util
import java.util.Date
import com.webank.wedatasphere.linkis.governance.common.entity.job.SubJobInfo
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.orchestrator.computation.operation.progress.{DefaultProgressOperation, ProgressProcessor}



class DefaultEntranceExecutor(id: Long, mark: MarkReq, entranceExecutorManager: EntranceExecutorManager) extends EntranceExecutor(id, mark) with ConcurrentTaskOperateSupport with Logging {


  /* private def doMethod[T](exec: String => T): T = if (engineReturns.isEmpty)
     throw new EntranceErrorException(20001, s"Engine${id} could not find a job in RUNNING state(Engine${id}找不到处于RUNNING状态的Job)")
   else exec(engineReturns(0).execId)*/


  /**
   *1. get logProcessor by log operate
   *2. update log by logListener
   *
   * @param orchestratorFuture
   */
  def dealLog(orchestratorFuture: OrchestrationFuture): LogProcessor = {
    val logProcessor = orchestratorFuture.operate[LogProcessor](LogOperation.LOG)
    logProcessor.registerLogNotify(logEvent => {
      val entranceJob = entranceExecutorManager.getEntranceJobByOrchestration(logEvent.orchestration).getOrElse {
        error("Cannot get entranceJob by orchestration : {}" + BDPJettyServerHelper.gson.toJson(logEvent.orchestration))
        null
      }
      if (null != entranceJob) {
        entranceJob.getLogListener.foreach(_.onLogUpdate(entranceJob, logEvent.log))
        entranceJob.getJobGroups
      }
    })
    logProcessor
  }

  def dealProgress(orchestratorFuture: OrchestrationFuture): ProgressProcessor = {
    val progressProcessor = orchestratorFuture.operate[ProgressProcessor](DefaultProgressOperation.PROGRESS_NAME)
    progressProcessor.doOnObtain(progressInfoEvent => {
      val entranceJob = entranceExecutorManager.getEntranceJobByOrchestration(progressInfoEvent.orchestration).getOrElse {
        error("Cannot get entranceJob by orchestration : {}" + BDPJettyServerHelper.gson.toJson(progressInfoEvent.orchestration))
        null
      }
      if (null != entranceJob) {
        val jobGroups = entranceJob.getJobGroups
        if (jobGroups.length > 0) {
          val subJobInfo = entranceJob.getRunningSubJob
          if (null != subJobInfo) {
            //Update progress value
            subJobInfo.setProgress(progressInfoEvent.progress)
            //Update progress info
            progressInfoEvent.progressInfo.foreach(progressInfo =>
              subJobInfo.getProgressInfoMap.put(progressInfo.id, progressInfo))
            entranceJob.getProgressListener.foreach(_.onProgressUpdate(entranceJob, entranceJob.getProgress,
              entranceJob.getProgressInfo))
          }

        } else {
          entranceJob.getProgressListener.foreach(_.onProgressUpdate(entranceJob, progressInfoEvent.progress,
            entranceJob.getProgressInfo))
        }
      }
    })
    progressProcessor
  }

  def searchJobGroupInProgress(jobGroups: Array[SubJobInfo]): SubJobInfo = {
    for (jobGroup <- jobGroups) {
      if (jobGroup.getProgress > 0 && jobGroup.getProgress < 1.0) {
        return jobGroup
      }
    }
    null
  }
  def dealResponse(orchestrationResponse: OrchestrationResponse, entranceExecuteRequest: EntranceExecuteRequest, orchestration: Orchestration): Unit = {
    orchestrationResponse match {
      case succeedResponose: SucceedTaskResponse =>
        succeedResponose match {
          case resultSetResp: ResultSetTaskResponse =>
            info(s"SubJob : ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail.getId} succeed to execute task, and get result.")
            // todo check null alias
            entranceExecuteRequest.getJob.asInstanceOf[EntranceJob].addAndGetResultSize(0)
            entranceExecuteRequest.getJob.getEntranceContext.getOrCreatePersistenceManager()
              .onResultSetCreated(entranceExecuteRequest, AliasOutputExecuteResponse(null, resultSetResp.getResultSet))
          //
          case arrayResultSetPathResp: ArrayResultSetTaskResponse =>
            info(s"SubJob : ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail.getId} succeed to execute task, and get result array.")
            // tod check
            /*arrayResultSetPathResp.getResultSets.foreach(result => {
              persistenceManager.onResultSetCreated(entranceExecuteRequest, AliasOutputExecuteResponse(result.alias, result.result))
            })*/
            if (null != arrayResultSetPathResp.getResultSets && arrayResultSetPathResp.getResultSets.size > 0) {
              val resultsetSize = arrayResultSetPathResp.getResultSets.size
              entranceExecuteRequest.getSubJobInfo.getSubJobDetail().setResultSize(resultsetSize)
              entranceExecuteRequest.getJob.asInstanceOf[EntranceJob].addAndGetResultSize(resultsetSize)
            }
            val firstResultSet = arrayResultSetPathResp.getResultSets.headOption.getOrElse(null)
            if (null != firstResultSet) {
              // assert that all result set files have same parent path, so we get the first
              Utils.tryCatch{
                entranceExecuteRequest.getJob.asInstanceOf[EntranceJob].getEntranceContext.getOrCreatePersistenceManager.onResultSetCreated(entranceExecuteRequest, AliasOutputExecuteResponse(firstResultSet.alias, firstResultSet.result))
              } {
                case e: Exception => {
                  val msg = s"Persist resultSet error. ${e.getMessage}"
                  error(msg)
                  val errorExecuteResponse = new DefaultFailedTaskResponse(msg, EntranceErrorCode.RESULT_NOT_PERSISTED_ERROR.getErrCode, e)
                  dealResponse(errorExecuteResponse, entranceExecuteRequest, orchestration)
                  return
                }
              }
            }
          case _ =>
            info(s"SubJob : ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail.getId} succeed to execute task,no result.")
        }
        /*val jobReturn = entranceExecutorManager.getAsyncJobReturnByOrchestration(orchestration).getOrElse {
          error(s"Cannot get jobReturn for orchestration : ${EntranceUtil.GSON.toJson(orchestration)}")
          null
        }
        if (null != jobReturn) {
//          jobReturn.notifyStatus(ResponseTaskStatus(jobReturn.execId, ExecutionNodeStatus.Succeed))
        }*/
        entranceExecuteRequest.getSubJobInfo.setStatus(SchedulerEventState.Succeed.toString)
        entranceExecuteRequest.getJob.getEntranceContext.getOrCreatePersistenceManager().createPersistenceEngine().updateIfNeeded(entranceExecuteRequest.getSubJobInfo)
        entranceExecuteRequest.getJob.getLogListener.foreach(_.onLogUpdate(entranceExecuteRequest.getJob, s"Your subjob : ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail().getId} execue with state succeed, has ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail().getResultSize} resultsets."))
        // clear subjob cache
        entranceExecutorManager.clearOrchestrationCache(orchestration)
        // submit next subJob
        val executeRequest = entranceExecuteRequest.getJob.jobToExecuteRequest()
        if (null != executeRequest) {
          val jobReturn = callExecute(executeRequest)
          engineReturns synchronized engineReturns += jobReturn
        } else {
          entranceExecuteRequest.getJob.getLogListener.foreach(_.onLogUpdate(entranceExecuteRequest.getJob, s"Congratuaions! Your job : ${entranceExecuteRequest.getJob.getId} executed with status succeed and ${entranceExecuteRequest.getJob.addAndGetResultSize(0)} results."))
          Utils.tryAndWarn(entranceExecuteRequest.getJob.transitionCompleted(SuccessExecuteResponse(), "All subjob runs succeed."))
          entranceExecutorManager.clearOrchestrationCache(orchestration)
          entranceExecutorManager.clearJobCache(entranceExecuteRequest.getJob.getId)
        }
      case failedResponse: FailedTaskResponse =>
        val msg = s"SubJob : ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail.getId} failed to execute task, code : ${failedResponse.getErrorCode}, reason : ${failedResponse.getErrorMsg}."
        debug(msg)
        entranceExecuteRequest.getSubJobInfo.setStatus(SchedulerEventState.Failed.toString)
        entranceExecuteRequest.getJob.getEntranceContext.getOrCreatePersistenceManager().createPersistenceEngine().updateIfNeeded(entranceExecuteRequest.getSubJobInfo)
        entranceExecutorManager.clearOrchestrationCache(orchestration)
        entranceExecutorManager.clearJobCache(entranceExecuteRequest.getJob.getId)
        Utils.tryAndWarn{
          doOnFailed(entranceExecuteRequest, orchestration, failedResponse)
          entranceExecuteRequest.getJob.getEntranceContext.getOrCreatePersistenceManager().createPersistenceEngine().updateIfNeeded(entranceExecuteRequest.getJob.getJobRequest)
        }
      case o =>
        val msg = s"Job : ${entranceExecuteRequest.getJob.getId} , subJob : ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail.getId} returnd unknown response : ${BDPJettyServerHelper.gson.toJson(o)}"
        error(msg)
        entranceExecuteRequest.getJob.getLogListener.foreach(_.onLogUpdate(entranceExecuteRequest.getJob, LogUtils.generateERROR(msg)))
      // todo
    }
  }

  def requestToComputationJobReq(entranceExecuteRequest: EntranceExecuteRequest): JobReq = {
    val jobReqBuilder = ComputationJobReq.newBuilder()
    val subJobId = String.valueOf(entranceExecuteRequest.getSubJobInfo.getSubJobDetail.getId)
    jobReqBuilder.setId(subJobId)
    jobReqBuilder.setSubmitUser(entranceExecuteRequest.submitUser())
    jobReqBuilder.setExecuteUser(entranceExecuteRequest.executeUser())
    val codeTypeLabel: Label[_] = LabelUtil.getCodeTypeLabel(entranceExecuteRequest.getLables)
    if (null == codeTypeLabel) {
      throw new EntranceErrorException(EntranceErrorCode.EXECUTE_REQUEST_INVALID.getErrCode, s"code Type Label is needed")
    }
    val codes = new util.ArrayList[String]()
    codes.add(entranceExecuteRequest.code())
    val codeLogicalUnit = new CodeLogicalUnit(codes, codeTypeLabel.asInstanceOf[CodeLanguageLabel])
    jobReqBuilder.setCodeLogicalUnit(codeLogicalUnit)
    jobReqBuilder.setLabels(entranceExecuteRequest.getLables)
    jobReqBuilder.setExecuteUser(entranceExecuteRequest.executeUser())
    jobReqBuilder.setParams(entranceExecuteRequest.properties().asInstanceOf[util.Map[String, Any]])
    jobReqBuilder.build()
  }

  override def close(): Unit = {
    if (engineReturns.nonEmpty) engineReturns.foreach { e =>
      e.notifyError(s"$toString has already been completed with state $state.")
    }
  }

  private def doOnFailed(entranceExecuteRequest: EntranceExecuteRequest, orchestration: Orchestration, failedResponse: FailedTaskResponse) = {
    val msg = failedResponse.getErrorCode + ", " + failedResponse.getErrorMsg
    val failedMsg = s"Your job execution failed because subjob : ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail.getId} failed, the reason: ${msg}"
    if (null != orchestration) {
      val jobReturn = entranceExecutorManager.getAsyncJobReturnByOrchestration(orchestration).getOrElse {
        debug(s"Cannot get jobReturn for orchestration : ${orchestration.toString}")
        null
      }
      if (null != jobReturn) {
        jobReturn.notifyError(msg, failedResponse.getCause)
        jobReturn.notifyStatus(ResponseTaskStatus(jobReturn.subJobId, ExecutionNodeStatus.Failed))
        jobReturn.request match {
          case entranceExecuteRequest: EntranceExecuteRequest =>
            entranceExecuteRequest.getJob.getLogListener.foreach(_.onLogUpdate(entranceExecuteRequest.getJob, failedMsg))
      }
    }
    }
    entranceExecuteRequest.getJob.transitionCompleted(ErrorExecuteResponse(failedResponse.getErrorMsg, failedResponse.getCause), failedMsg)
    entranceExecuteRequest.getJob.onFailure(msg, failedResponse.getCause)
  }

  override def kill(execId: String): Boolean = {
    warn(s"start to kill job $execId")
    Utils.tryAndWarn {
        val msg = s"You job with id ${execId} was cancelled by user!"
      val runningJobFutureOption = getRunningOrchestrationFuture
      if (None != runningJobFutureOption && null != runningJobFutureOption.get) {
        runningJobFutureOption.get.cancel(msg)
      } else {
        error(s"Invalid jobFuture while cancelling job ${execId}.")
      }
        val job = entranceExecutorManager.getEntranceJobByExecId(execId).getOrElse(null)
        if (null != job) {
        job.updateJobRequestStatus(SchedulerEventState.Cancelled.toString)
          job.getLogListener.foreach(_.onLogUpdate(job, msg))
          job.transitionCompleted(ErrorExecuteResponse(msg, null), msg)
        true
      } else {
        warn(s"Cannot get job by execId : ${execId}, will not kill.")
        false
      }
    }
  }

  override def killAll(): Boolean = {
    engineReturns.foreach(f => Utils.tryQuietly(killExecId(f, f.subJobId)))
    true
  }

  override def pause(jobId: String): Boolean = {
    //TODO
    true
  }

  override def pauseAll(): Boolean = {
    //TODO
    true
  }

  override def resume(jobId: String): Boolean = {
    //TODO
    true
  }

  override def resumeAll(): Boolean = {
    //TODO
    true
  }

  override protected def callExecute(request: ExecuteRequest): EngineExecuteAsynReturn = {


    val entranceExecuteRequest: EntranceExecuteRequest = request match {
      case request: EntranceExecuteRequest =>
        request
      case _ =>
        throw new EntranceErrorException(EntranceErrorCode.EXECUTE_REQUEST_INVALID.getErrCode, s"Invalid entranceExecuteRequest : ${BDPJettyServerHelper.gson.toJson(request)}")
    }
    // 1. create JobReq
    val compJobReq = requestToComputationJobReq(entranceExecuteRequest)
    Utils.tryCatch {
      // 2. orchestrate compJobReq get Orchestration
      val orchestration = EntranceOrchestrationFactory.getOrchestrationSession().orchestrate(compJobReq)
      //      val planMsg = orchestration.explain(true)
      //      val planLog = s"Job : ${compJobReq.getId} plan msg : ${planMsg}"
      //      info(planLog)
      //      entranceExecuteRequest.getJob.getLogListener.foreach(_.onLogUpdate(entranceExecuteRequest.getJob, planLog))
      val orchestratorFuture = orchestration.asyncExecute()
      val msg = s"Job with jobGroupId : ${entranceExecuteRequest.getJob.getJobRequest.getId} and subJobId : ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail.getId} was submitted to Orchestrator."
      info(msg)
      entranceExecuteRequest.getJob.getLogListener.foreach(_.onLogUpdate(entranceExecuteRequest.getJob, msg))
      //Entrance指标：任务提供给orchestrator时间
      if (entranceExecuteRequest.getJob.getJobRequest.getMetrics == null) {
        warn("Job Metrics has not been initialized")
      } else {
        if (!entranceExecuteRequest.getJob.getJobRequest.getMetrics.containsKey(TaskConstant.ENTRANCEJOB_TO_ORCHESTRATOR)) {
          entranceExecuteRequest.getJob.getJobRequest.getMetrics.put(TaskConstant.ENTRANCEJOB_TO_ORCHESTRATOR, new Date(System.currentTimeMillis()))
        }
      }
      // 2. deal log And Response
      val logProcessor = dealLog(orchestratorFuture)
      val progressProcessor = dealProgress(orchestratorFuture)
      orchestratorFuture.notifyMe(orchestrationResponse => {
        dealResponse(orchestrationResponse, entranceExecuteRequest, orchestration)
      }
      )
      // cache job
      entranceExecutorManager.setOrchestrationAndEntranceJob(orchestration, entranceExecuteRequest.getJob)
      val jobReturn = new EngineExecuteAsynReturn(request, orchestratorFuture,
        compJobReq.getId, logProcessor, progressProcessor, null)
      entranceExecutorManager.setOrchestrationAndExecutorAsyncReturn(orchestration, jobReturn)
      jobReturn
    } {
      case t: Throwable =>
        val failedResponse = new DefaultFailedTaskResponse(s"Submit subjob : ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail.getId} failed.", EntranceErrorCode.SUBMIT_JOB_ERROR.getErrCode, t)
        doOnFailed(entranceExecuteRequest, null, failedResponse)
        null
    }
  }


}
