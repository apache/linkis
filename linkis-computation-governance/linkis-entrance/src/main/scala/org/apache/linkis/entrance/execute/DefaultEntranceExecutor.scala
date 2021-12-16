/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.governance.common.protocol.task.ResponseTaskStatus
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.orchestrator.Orchestration
import org.apache.linkis.orchestrator.computation.entity.ComputationJobReq
import org.apache.linkis.orchestrator.computation.operation.log.{LogOperation, LogProcessor}
import org.apache.linkis.orchestrator.core.{OrchestrationFuture, OrchestrationResponse}
import org.apache.linkis.orchestrator.domain.JobReq
import org.apache.linkis.orchestrator.execution.impl.DefaultFailedTaskResponse
import org.apache.linkis.orchestrator.execution.{ArrayResultSetTaskResponse, FailedTaskResponse, ResultSetTaskResponse, SucceedTaskResponse}
import org.apache.linkis.orchestrator.plans.unit.CodeLogicalUnit
import org.apache.linkis.scheduler.executer.{AliasOutputExecuteResponse, ConcurrentTaskOperateSupport, ErrorExecuteResponse, ExecuteRequest, ExecuteResponse, SingleTaskOperateSupport, SuccessExecuteResponse}
import org.apache.linkis.scheduler.queue.SchedulerEventState
import org.apache.linkis.server.BDPJettyServerHelper
import java.util
import java.util.Date

import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.linkis.governance.common.entity.job.SubJobInfo
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.orchestrator.computation.operation.progress.{DefaultProgressOperation, ProgressProcessor}



class DefaultEntranceExecutor(id: Long, mark: MarkReq, entranceExecutorManager: EntranceExecutorManager) extends EntranceExecutor(id, mark) with SingleTaskOperateSupport with Logging {


  /* private def doMethod[T](exec: String => T): T = if (engineReturns.isEmpty)
     throw new EntranceErrorException(20001, s"Engine${id} could not find a job in RUNNING state(Engine${id}找不到处于RUNNING状态的Job)")
   else exec(engineReturns(0).execId)*/


  /**
   *1. get logProcessor by log operate
   *2. update log by logListener
   *
   * @param orchestratorFuture
   */
  def dealLog(orchestratorFuture: OrchestrationFuture, job: EntranceJob): LogProcessor = {
    val logProcessor = orchestratorFuture.operate[LogProcessor](LogOperation.LOG)
    logProcessor.registerLogNotify(logEvent => {
      if (null != job) {
        job.getLogListener.foreach(_.onLogUpdate(job, logEvent.log))
        job.getJobGroups
      }
    })
    logProcessor
  }

  def dealProgress(orchestratorFuture: OrchestrationFuture, entranceJob: EntranceJob): ProgressProcessor = {
    val progressProcessor = orchestratorFuture.operate[ProgressProcessor](DefaultProgressOperation.PROGRESS_NAME)
    progressProcessor.doOnObtain(progressInfoEvent => {
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
              .onResultSetCreated(entranceExecuteRequest.getJob, AliasOutputExecuteResponse(null, resultSetResp.getResultSet))
          //
          case arrayResultSetPathResp: ArrayResultSetTaskResponse =>
            info(s"SubJob : ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail.getId} succeed to execute task, and get result array.")
            if (null != arrayResultSetPathResp.getResultSets && arrayResultSetPathResp.getResultSets.length > 0) {
              val resultsetSize = arrayResultSetPathResp.getResultSets.length
              entranceExecuteRequest.getSubJobInfo.getSubJobDetail().setResultSize(resultsetSize)
              entranceExecuteRequest.getJob.asInstanceOf[EntranceJob].addAndGetResultSize(resultsetSize)
            }
            val firstResultSet = arrayResultSetPathResp.getResultSets.headOption.orNull
            if (null != firstResultSet) {
              // assert that all result set files have same parent path, so we get the first
              Utils.tryCatch {
                entranceExecuteRequest.getJob.asInstanceOf[EntranceJob].getEntranceContext
                  .getOrCreatePersistenceManager()
                  .onResultSetCreated(entranceExecuteRequest.getJob, AliasOutputExecuteResponse(firstResultSet.alias, firstResultSet.result))
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
        entranceExecuteRequest.getSubJobInfo.setStatus(SchedulerEventState.Succeed.toString)
        entranceExecuteRequest.getJob.getEntranceContext.getOrCreatePersistenceManager().createPersistenceEngine().updateIfNeeded(entranceExecuteRequest.getSubJobInfo)
        entranceExecuteRequest.getJob.getLogListener.foreach(_.onLogUpdate(entranceExecuteRequest.getJob, LogUtils.generateInfo(s"Your subjob : ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail().getId} execue with state succeed, has ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail().getResultSize} resultsets.")))
        // submit next subJob
        val executeRequest = entranceExecuteRequest.getJob.jobToExecuteRequest()
        if (null != executeRequest) {
          // clear subjob cache
          callExecute(executeRequest)
        } else {
          entranceExecuteRequest.getJob.getLogListener.foreach(_.onLogUpdate(entranceExecuteRequest.getJob, LogUtils.generateInfo(s"Congratuaions! Your job : ${entranceExecuteRequest.getJob.getId} executed with status succeed and ${entranceExecuteRequest.getJob.addAndGetResultSize(0)} results.")))
          Utils.tryAndWarn(doOnSucceed(entranceExecuteRequest))
        }
      case failedResponse: FailedTaskResponse =>
        entranceExecuteRequest.getSubJobInfo.setStatus(SchedulerEventState.Failed.toString)
        entranceExecuteRequest.getJob.getEntranceContext.getOrCreatePersistenceManager().createPersistenceEngine().updateIfNeeded(entranceExecuteRequest.getSubJobInfo)

        Utils.tryAndWarn {
          doOnFailed(entranceExecuteRequest, orchestration, failedResponse)
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
    getEngineExecuteAsyncReturn.foreach { e =>
      e.notifyError(s"$toString has already been completed with state $state.")
    }
  }

  private def doOnSucceed(entranceExecuteRequest: EntranceExecuteRequest): Unit = {
    getEngineExecuteAsyncReturn.foreach { jobReturn =>
      jobReturn.notifyStatus(ResponseTaskStatus(entranceExecuteRequest.getJob.getId, ExecutionNodeStatus.Succeed))
    }
  }

  private def doOnFailed(entranceExecuteRequest: EntranceExecuteRequest, orchestration: Orchestration, failedResponse: FailedTaskResponse) = {
    val msg = failedResponse.getErrorCode + ", " + failedResponse.getErrorMsg
    getEngineExecuteAsyncReturn.foreach { jobReturn =>
      jobReturn.notifyError(msg, failedResponse.getCause)
      jobReturn.notifyStatus(ResponseTaskStatus(entranceExecuteRequest.getJob.getId, ExecutionNodeStatus.Failed))
    }
  }

  override def kill(): Boolean = {
    Utils.tryAndWarn {
      val msg = s"You job with id  was cancelled by user!"
      getRunningOrchestrationFuture.foreach(_.cancel(msg))
    }
    true
  }



  override def pause(): Boolean = {
    //TODO
    true
  }



  override def resume(): Boolean = {
    //TODO
    true
  }


  override protected def callExecute(request: ExecuteRequest): ExecuteResponse = {


    val entranceExecuteRequest: EntranceExecuteRequest = request match {
      case request: EntranceExecuteRequest =>
        request
      case _ =>
        throw new EntranceErrorException(EntranceErrorCode.EXECUTE_REQUEST_INVALID.getErrCode, s"Invalid entranceExecuteRequest : ${BDPJettyServerHelper.gson.toJson(request)}")
    }
    // 1. create JobReq
    val compJobReq = requestToComputationJobReq(entranceExecuteRequest)
    Utils.tryCatch[ExecuteResponse] {
      // 2. orchestrate compJobReq get Orchestration
      val orchestration = EntranceOrchestrationFactory.getOrchestrationSession().orchestrate(compJobReq)
      val orchestratorFuture = orchestration.asyncExecute()
      val msg = s"Job with jobGroupId : ${entranceExecuteRequest.getJob.getJobRequest.getId} and subJobId : ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail.getId} was submitted to Orchestrator."
      info(msg)
      entranceExecuteRequest.getJob.getLogListener.foreach(_.onLogUpdate(entranceExecuteRequest.getJob, LogUtils.generateInfo(msg)))
      //Entrance指标：任务提供给orchestrator时间
      if (entranceExecuteRequest.getJob.getJobRequest.getMetrics == null) {
        warn("Job Metrics has not been initialized")
      } else {
        if (!entranceExecuteRequest.getJob.getJobRequest.getMetrics.containsKey(TaskConstant.ENTRANCEJOB_TO_ORCHESTRATOR)) {
          entranceExecuteRequest.getJob.getJobRequest.getMetrics.put(TaskConstant.ENTRANCEJOB_TO_ORCHESTRATOR, new Date(System.currentTimeMillis()))
        }
      }
      // 2. deal log And Response
      val logProcessor = dealLog(orchestratorFuture, entranceExecuteRequest.getJob)
      val progressProcessor = dealProgress(orchestratorFuture, entranceExecuteRequest.getJob)
      orchestratorFuture.notifyMe(orchestrationResponse => {
        dealResponse(orchestrationResponse, entranceExecuteRequest, orchestration)
      }
      )
      /**
        * 只有第一次会new，后续都是reset
        */
      val jobReturn = if (getEngineExecuteAsyncReturn.isDefined) {
        getEngineExecuteAsyncReturn.foreach(_.closeOrchestration())
        getEngineExecuteAsyncReturn.get
      } else {
        logger.info(s"For job ${entranceExecuteRequest.getJob.getId} and subJob ${compJobReq.getId} to create EngineExecuteAsyncReturn")
        new EngineExecuteAsyncReturn(request, null)
      }
      jobReturn.setOrchestrationObjects(orchestratorFuture, logProcessor, progressProcessor)
      jobReturn.setSubJobId(compJobReq.getId)
      setEngineReturn(jobReturn)
      jobReturn
    } { t: Throwable =>
      if (getEngineExecuteAsyncReturn.isEmpty) {

        val msg = s"task submit failed,reason, ${ExceptionUtils.getFullStackTrace(t)}"
        entranceExecuteRequest.getJob.getLogListener.foreach(_.onLogUpdate(entranceExecuteRequest.getJob, LogUtils.generateERROR(msg)))
        ErrorExecuteResponse(msg, t)
      } else {
        val failedResponse = new DefaultFailedTaskResponse(s"Submit subjob : ${entranceExecuteRequest.getSubJobInfo.getSubJobDetail.getId} failed.", EntranceErrorCode.SUBMIT_JOB_ERROR.getErrCode, t)
        doOnFailed(entranceExecuteRequest, null, failedResponse)
        null
      }
    }
  }


}
