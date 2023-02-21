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

package org.apache.linkis.entrance

import org.apache.linkis.common.exception.{ErrorException, LinkisException, LinkisRuntimeException}
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.cs.CSEntranceHelper
import org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary._
import org.apache.linkis.entrance.exception.{EntranceErrorException, SubmitFailedException}
import org.apache.linkis.entrance.execute.EntranceJob
import org.apache.linkis.entrance.log.LogReader
import org.apache.linkis.entrance.timeout.JobTimeoutManager
import org.apache.linkis.entrance.utils.JobHistoryHelper
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.queue.{Job, SchedulerEventState}
import org.apache.linkis.server.conf.ServerConfiguration

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import java.text.MessageFormat
import java.util

abstract class EntranceServer extends Logging {

  private var entranceWebSocketService: Option[EntranceWebSocketService] = None

  private val jobTimeoutManager: JobTimeoutManager = new JobTimeoutManager()

  def init(): Unit

  def getName: String

  def getEntranceContext: EntranceContext

  /**
   * Execute a task and return an job(执行一个task，返回一个job)
   * @param params
   * @return
   */
  def execute(params: java.util.Map[String, AnyRef]): Job = {
    if (!params.containsKey(EntranceServer.DO_NOT_PRINT_PARAMS_LOG)) {
      logger.debug("received a request: " + params)
    } else params.remove(EntranceServer.DO_NOT_PRINT_PARAMS_LOG)
    var jobRequest = getEntranceContext.getOrCreateEntranceParser().parseToTask(params)
    // todo: multi entrance instances
    jobRequest.setInstances(Sender.getThisInstance)
    Utils.tryAndWarn(CSEntranceHelper.resetCreator(jobRequest))
    // After parse the map into a jobRequest, we need to store it in the database, and the jobRequest can get a unique taskID.
    // 将map parse 成 jobRequest 之后，我们需要将它存储到数据库中，task可以获得唯一的taskID
    getEntranceContext
      .getOrCreatePersistenceManager()
      .createPersistenceEngine()
      .persist(jobRequest)
    if (null == jobRequest.getId || jobRequest.getId <= 0) {
      throw new EntranceErrorException(
        PERSIST_JOBREQUEST_ERROR.getErrorCode,
        PERSIST_JOBREQUEST_ERROR.getErrorDesc
      )
    }
    logger.info(s"received a request,convert $jobRequest")

    val logAppender = new java.lang.StringBuilder()
    Utils.tryThrow(
      getEntranceContext
        .getOrCreateEntranceInterceptors()
        .foreach(int => jobRequest = int.apply(jobRequest, logAppender))
    ) { t =>
      val error = t match {
        case error: ErrorException => error
        case t1: Throwable =>
          val exception = new EntranceErrorException(
            FAILED_ANALYSIS_TASK.getErrorCode,
            MessageFormat.format(
              FAILED_ANALYSIS_TASK.getErrorDesc,
              ExceptionUtils.getRootCauseMessage(t)
            )
          )
          exception.initCause(t1)
          exception
        case _ =>
          new EntranceErrorException(
            FAILED_ANALYSIS_TASK.getErrorCode,
            MessageFormat.format(
              FAILED_ANALYSIS_TASK.getErrorDesc,
              ExceptionUtils.getRootCauseMessage(t)
            )
          )
      }
      jobRequest match {
        case t: JobRequest =>
          t.setErrorCode(error.getErrCode)
          t.setErrorDesc(error.getDesc)
          t.setStatus(SchedulerEventState.Failed.toString)
          t.setProgress(EntranceJob.JOB_COMPLETED_PROGRESS.toString)
          val infoMap = new util.HashMap[String, AnyRef]
          infoMap.put(TaskConstant.ENGINE_INSTANCE, "NULL")
          infoMap.put(TaskConstant.TICKET_ID, "")
          infoMap.put("message", "Task interception failed and cannot be retried")
          JobHistoryHelper.updateJobRequestMetrics(jobRequest, null, infoMap)
        case _ =>
      }
      getEntranceContext
        .getOrCreatePersistenceManager()
        .createPersistenceEngine()
        .updateIfNeeded(jobRequest)
      error
    }

    val job = getEntranceContext.getOrCreateEntranceParser().parseToJob(jobRequest)
    Utils.tryThrow {
      job.init()
      job.setLogListener(getEntranceContext.getOrCreateLogManager())
      job.setProgressListener(getEntranceContext.getOrCreatePersistenceManager())
      job.setJobListener(getEntranceContext.getOrCreatePersistenceManager())
      job match {
        case entranceJob: EntranceJob =>
          entranceJob.setEntranceListenerBus(getEntranceContext.getOrCreateEventListenerBus)
        case _ =>
      }
      Utils.tryCatch {
        if (logAppender.length() > 0) {
          job.getLogListener.foreach(_.onLogUpdate(job, logAppender.toString.trim))
        }
      } { t =>
        logger.error("Failed to write init log, reason: ", t)
      }

      /**
       * job.afterStateChanged() method is only called in job.run(), and job.run() is called only
       * after job is scheduled so it suggest that we lack a hook for job init, currently we call
       * this to trigger JobListener.onJobinit()
       */
      Utils.tryAndWarn(job.getJobListener.foreach(_.onJobInited(job)))
      getEntranceContext.getOrCreateScheduler().submit(job)
      val msg = LogUtils.generateInfo(
        s"Job with jobId : ${jobRequest.getId} and execID : ${job.getId()} submitted "
      )
      logger.info(msg)

      job match {
        case entranceJob: EntranceJob =>
          entranceJob.getJobRequest.setReqId(job.getId())
          if (jobTimeoutManager.timeoutCheck && JobTimeoutManager.hasTimeoutLabel(entranceJob)) {
            jobTimeoutManager.add(job.getId(), entranceJob)
          }
          entranceJob.getLogListener.foreach(_.onLogUpdate(entranceJob, msg))
        case _ =>
      }
      job
    } { t =>
      job.onFailure("Submitting the query failed!(提交查询失败！)", t)
      val _jobRequest: JobRequest =
        getEntranceContext.getOrCreateEntranceParser().parseToJobRequest(job)
      getEntranceContext
        .getOrCreatePersistenceManager()
        .createPersistenceEngine()
        .updateIfNeeded(_jobRequest)
      t match {
        case e: LinkisException => e
        case e: LinkisRuntimeException => e
        case t: Throwable =>
          new SubmitFailedException(
            SUBMITTING_QUERY_FAILED.getErrorCode,
            SUBMITTING_QUERY_FAILED.getErrorDesc + ExceptionUtils.getRootCauseMessage(t),
            t
          )
      }
    }
  }

  def logReader(execId: String): LogReader

  def getJob(execId: String): Option[Job] =
    getEntranceContext.getOrCreateScheduler().get(execId).map(_.asInstanceOf[Job])

  private[entrance] def getEntranceWebSocketService: Option[EntranceWebSocketService] =
    if (ServerConfiguration.BDP_SERVER_SOCKET_MODE.getValue) {
      if (entranceWebSocketService.isEmpty) synchronized {
        if (entranceWebSocketService.isEmpty) {
          entranceWebSocketService = Some(new EntranceWebSocketService)
          entranceWebSocketService.foreach(_.setEntranceServer(this))
          entranceWebSocketService.foreach(
            getEntranceContext.getOrCreateEventListenerBus.addListener
          )
        }
      }
      entranceWebSocketService
    } else None

  def getAllUndoneTask(filterWords: String): Array[EntranceJob] = {
    val consumers = getEntranceContext
      .getOrCreateScheduler()
      .getSchedulerContext
      .getOrCreateConsumerManager
      .listConsumers()
      .toSet
    val filterConsumer = if (StringUtils.isNotBlank(filterWords)) {
      consumers.filter(_.getGroup.getGroupName.contains(filterWords))
    } else {
      consumers
    }
    filterConsumer
      .flatMap { consumer =>
        consumer.getRunningEvents ++ consumer.getConsumeQueue.getWaitingEvents
      }
      .filter(job => job != null && job.isInstanceOf[EntranceJob])
      .map(_.asInstanceOf[EntranceJob])
      .toArray
  }

}

object EntranceServer {
  val DO_NOT_PRINT_PARAMS_LOG = "doNotPrintParamsLog"
}
