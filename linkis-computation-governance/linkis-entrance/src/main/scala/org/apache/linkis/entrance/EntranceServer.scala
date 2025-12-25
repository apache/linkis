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
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.cs.CSEntranceHelper
import org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary._
import org.apache.linkis.entrance.exception.{EntranceErrorException, SubmitFailedException}
import org.apache.linkis.entrance.execute.EntranceJob
import org.apache.linkis.entrance.log.LogReader
import org.apache.linkis.entrance.timeout.JobTimeoutManager
import org.apache.linkis.entrance.utils.{EntranceUtils, JobHistoryHelper}
import java.util.concurrent.ConcurrentHashMap
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.utils.LoggerUtils
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.conf.SchedulerConfiguration.{
  ENGINE_PRIORITY_RUNTIME_KEY,
  FIFO_QUEUE_STRATEGY,
  PFIFO_SCHEDULER_STRATEGY
}
import org.apache.linkis.scheduler.queue.{Job, SchedulerEventState}
import org.apache.linkis.server.conf.ServerConfiguration

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import java.text.MessageFormat
import java.util
import java.util.concurrent.TimeUnit

abstract class EntranceServer extends Logging {

  // Map to track which jobs have been diagnosed
  private val diagnosedJobs = new ConcurrentHashMap[String, Boolean]()

  private var entranceWebSocketService: Option[EntranceWebSocketService] = None

  private val jobTimeoutManager: JobTimeoutManager = new JobTimeoutManager()

  private val timeoutCheck = EntranceConfiguration.ENABLE_JOB_TIMEOUT_CHECK.getValue

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

    LoggerUtils.setJobIdMDC(jobRequest.getId.toString)

    val logAppender = new java.lang.StringBuilder()
    Utils.tryThrow(
      getEntranceContext
        .getOrCreateEntranceInterceptors()
        .foreach(int => jobRequest = int.apply(jobRequest, logAppender))
    ) { t =>
      LoggerUtils.removeJobIdMDC()
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
      job.setJobRetryListener(getEntranceContext.getOrCreatePersistenceManager())
      job match {
        case entranceJob: EntranceJob =>
          entranceJob.setEntranceListenerBus(getEntranceContext.getOrCreateEventListenerBus)
        case _ =>
      }

      /**
       * job.afterStateChanged() method is only called in job.run(), and job.run() is called only
       * after job is scheduled so it suggest that we lack a hook for job init, currently we call
       * this to trigger JobListener.onJobinit()
       */
      Utils.tryAndWarn(job.getJobListener.foreach(_.onJobInited(job)))
      if (logger.isDebugEnabled()) {
        logger.debug(
          s"After code preprocessing, the real execution code is:${jobRequest.getExecutionCode}"
        )
      }
      if (StringUtils.isBlank(jobRequest.getExecutionCode)) {
        throw new SubmitFailedException(
          SUBMIT_CODE_ISEMPTY.getErrorCode,
          SUBMIT_CODE_ISEMPTY.getErrorDesc
        )
      }

      Utils.tryAndWarn {
        // 如果是使用优先级队列，设置下优先级
        val configMap = params
          .getOrDefault(TaskConstant.PARAMS, new util.HashMap[String, AnyRef]())
          .asInstanceOf[util.Map[String, AnyRef]]
        val properties: util.Map[String, AnyRef] = TaskUtils.getRuntimeMap(configMap)
        val fifoStrategy: String = FIFO_QUEUE_STRATEGY
        if (
            PFIFO_SCHEDULER_STRATEGY.equalsIgnoreCase(
              fifoStrategy
            ) && properties != null && !properties.isEmpty
        ) {
          val priorityValue: AnyRef = properties.get(ENGINE_PRIORITY_RUNTIME_KEY)
          if (priorityValue != null) {
            val value: Int = getPriority(priorityValue.toString)
            logAppender.append(LogUtils.generateInfo(s"The task set priority is ${value} \n"))
            job.setPriority(value)
          }
        }
      }

      Utils.tryCatch {
        if (logAppender.length() > 0) {
          job.getLogListener.foreach(_.onLogUpdate(job, logAppender.toString.trim))
        }
      } { t =>
        logger.error("Failed to write init log, reason: ", t)
      }

      getEntranceContext.getOrCreateScheduler().submit(job)
      val msg = LogUtils.generateInfo(
        s"Job with jobId : ${jobRequest.getId} and execID : ${job.getId()} submitted "
      )
      logger.info(msg)

      job match {
        case entranceJob: EntranceJob =>
          entranceJob.getJobRequest.setReqId(job.getId())
          if (timeoutCheck && JobTimeoutManager.hasTimeoutLabel(entranceJob)) {
            jobTimeoutManager.add(job.getId(), entranceJob)
          }
          entranceJob.getLogListener.foreach(_.onLogUpdate(entranceJob, msg))
        case _ =>
      }
      LoggerUtils.removeJobIdMDC()
      job
    } { t =>
      LoggerUtils.removeJobIdMDC()
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

  def getAllUndoneTask(filterWords: String, ecType: String = null): Array[EntranceJob] = {
    val consumers = getEntranceContext
      .getOrCreateScheduler()
      .getSchedulerContext
      .getOrCreateConsumerManager
      .listConsumers()
      .toSet
    val filterConsumer = if (StringUtils.isNotBlank(filterWords)) {
      if (StringUtils.isNotBlank(ecType)) {
        consumers.filter(consumer =>
          consumer.getGroup.getGroupName.contains(filterWords) && consumer.getGroup.getGroupName
            .contains(ecType)
        )
      } else {
        consumers.filter(_.getGroup.getGroupName.contains(filterWords))
      }
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

  /**
   * to check timeout task,and kill timeout task timeout: default > 48h
   */
  def startTimeOutCheck(): Unit = {
    Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable() {
        override def run(): Unit = {
          Utils.tryCatch {

            val timeoutType = EntranceConfiguration.ENTRANCE_TASK_TIMEOUT.getHotValue()
            logger.info(s"Start to check timeout Job, timout is ${timeoutType}")
            val timeoutTime = System.currentTimeMillis() - timeoutType.toLong
            val undoneTask = getAllUndoneTask(null, null)
            undoneTask.filter(job => job.createTime < timeoutTime).foreach { job =>
              job.onFailure(s"Job has run for longer than the maximum time $timeoutType", null)
            }
            logger.info(s"Finished to check timeout Job, timout is ${timeoutType}")

            // 新增任务诊断检测逻辑
            if (EntranceConfiguration.TASK_DIAGNOSIS_ENABLE) {
              logger.info("Start to check tasks for diagnosis")
              val diagnosisTimeout = EntranceConfiguration.TASK_DIAGNOSIS_TIMEOUT
              val diagnosisTime = System.currentTimeMillis() - diagnosisTimeout
              undoneTask
                .filter { job =>
                  val engineType = LabelUtil.getEngineType(job.getJobRequest.getLabels)
                  engineType.contains(
                    EntranceConfiguration.TASK_DIAGNOSIS_ENGINE_TYPE
                  ) && job.createTime < diagnosisTime && !diagnosedJobs.containsKey(job.getId())
                }
                .foreach { job =>
                  // 异步触发诊断逻辑
                  Utils.defaultScheduler.execute(new Runnable() {
                    override def run(): Unit = {
                      try {
                        // 检查并设置诊断标记，确保每个任务只被诊断一次
                        if (diagnosedJobs.putIfAbsent(job.getId(), true) == null) {
                          // 调用Doctoris诊断系统
                          logger.info(s"Start to diagnose spark job ${job.getId()}")
                          job match {
                            case entranceJob: EntranceJob =>
                              // 调用doctoris实时诊断API
                              val response = EntranceUtils.taskRealtimeDiagnose(entranceJob.getJobRequest, null)
                              logger.info(s"Finished to diagnose job ${job
                                .getId()}, result: ${response.result}, reason: ${response.reason}")
                              // 更新诊断信息
                              if (response.success) {
                                // 构造诊断更新请求
                                JobHistoryHelper.addDiagnosis(job.getId(), response.result)
                                logger.info(s"Successfully updated diagnosis for job ${job.getId()}")
                              }
                            case _ =>
                              logger.warn(s"Job ${job.getId()} is not an EntranceJob, skip diagnosis")
                          }
                        }
                      } catch {
                        case t: Throwable =>
                          logger.warn(s"Diagnose job ${job.getId()} failed. ${t.getMessage}", t)
                          // 如果诊断失败，移除标记，允许重试
                          diagnosedJobs.remove(job.getId())
                      }
                    }
                  })
                }
              logger.info("Finished to check Spark tasks for diagnosis")
            }
            
            // 定期清理diagnosedJobs，只保留未完成任务的记录
            val undoneJobIds = undoneTask.map(_.getId()).toSet
            val iterator = diagnosedJobs.keySet().iterator()
            while (iterator.hasNext) {
              val jobId = iterator.next()
              if (!undoneJobIds.contains(jobId)) {
                iterator.remove()
              }
            }
            logger.info(s"Cleaned diagnosedJobs cache, current size: ${diagnosedJobs.size()}")
          } { case t: Throwable =>
            logger.warn(s"TimeoutDetective Job failed. ${t.getMessage}", t)
          }
        }

      },
      EntranceConfiguration.ENTRANCE_TASK_TIMEOUT_SCAN.getValue.toLong,
      EntranceConfiguration.ENTRANCE_TASK_TIMEOUT_SCAN.getValue.toLong,
      TimeUnit.MILLISECONDS
    )
  }

  if (timeoutCheck) {
    logger.info("Job time check is enabled")
    startTimeOutCheck()
  }

  val DOT = "."
  val DEFAULT_PRIORITY = 100

  private def getPriority(value: String): Int = {
    var priority: Int = -1
    Utils.tryAndWarn({
      priority =
        if (value.contains(DOT)) value.substring(0, value.indexOf(DOT)).toInt else value.toInt
    })
    if (priority < 0 || priority > Integer.MAX_VALUE - 1) {
      logger.warn(s"illegal queue priority: ${value}")
      DEFAULT_PRIORITY
    } else {
      priority
    }
  }

}

object EntranceServer {
  val DO_NOT_PRINT_PARAMS_LOG = "doNotPrintParamsLog"
}
