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

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.exception.{ErrorException, LinkisException, LinkisRuntimeException}
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.cs.CSEntranceHelper
import org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary
import org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary._
import org.apache.linkis.entrance.exception.{EntranceErrorException, SubmitFailedException}
import org.apache.linkis.entrance.execute.EntranceJob
import org.apache.linkis.entrance.job.EntranceExecutionJob
import org.apache.linkis.entrance.log.LogReader
import org.apache.linkis.entrance.parser.ParserUtils
import org.apache.linkis.entrance.timeout.JobTimeoutManager
import org.apache.linkis.entrance.utils.JobHistoryHelper
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.protocol.task.RequestTaskKill
import org.apache.linkis.governance.common.utils.LoggerUtils
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest
import org.apache.linkis.manager.label.entity.entrance.ExecuteOnceLabel
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.conf.RPCConfiguration
import org.apache.linkis.scheduler.conf.SchedulerConfiguration.{
  ENGINE_PRIORITY_RUNTIME_KEY,
  FIFO_QUEUE_STRATEGY,
  PFIFO_SCHEDULER_STRATEGY
}
import org.apache.linkis.scheduler.queue.{Job, SchedulerEventState}
import org.apache.linkis.server.conf.ServerConfiguration

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import java.text.{MessageFormat, SimpleDateFormat}
import java.util
import java.util.Date
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._

abstract class EntranceServer extends Logging {

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

  def updateAllNotExecutionTaskInstances(retryWhenUpdateFail: Boolean): Unit = {
    val consumeQueueTasks = getAllConsumeQueueTask()

    clearAllConsumeQueue()
    logger.info("Finished to clean all ConsumeQueue")

    if (consumeQueueTasks != null && consumeQueueTasks.length > 0) {
      val taskIds = new util.ArrayList[Long]()
      consumeQueueTasks.foreach(job => {
        taskIds.add(job.getJobRequest.getId.asInstanceOf[Long])
        job match {
          case entranceExecutionJob: EntranceExecutionJob =>
            val msg = LogUtils.generateWarn(
              s"job ${job.getJobRequest.getId} clean from ConsumeQueue, wait for failover"
            )
            entranceExecutionJob.getLogListener.foreach(_.onLogUpdate(entranceExecutionJob, msg))
            entranceExecutionJob.getLogWriter.foreach(_.close())
          case _ =>
        }
      })

      JobHistoryHelper.updateAllConsumeQueueTask(taskIds, retryWhenUpdateFail)
      logger.info("Finished to update all not execution task instances")
    }
  }

  def getAllConsumeQueueTask(): Array[EntranceJob] = {
    val consumers = getEntranceContext
      .getOrCreateScheduler()
      .getSchedulerContext
      .getOrCreateConsumerManager
      .listConsumers()
      .toSet

    consumers
      .flatMap { consumer =>
        consumer.getConsumeQueue.getWaitingEvents
      }
      .filter(job => job != null && job.isInstanceOf[EntranceJob])
      .map(_.asInstanceOf[EntranceJob])
      .toArray
  }

  def clearAllConsumeQueue(): Unit = {
    getEntranceContext
      .getOrCreateScheduler()
      .getSchedulerContext
      .getOrCreateConsumerManager
      .listConsumers()
      .foreach(_.getConsumeQueue.clearAll())
  }

  /**
   * execute failover job (提交故障转移任务，返回新的execId)
   *
   * @param jobRequest
   */
  def failoverExecute(jobRequest: JobRequest): Unit = {

    if (null == jobRequest || null == jobRequest.getId || jobRequest.getId <= 0) {
      throw new EntranceErrorException(
        PERSIST_JOBREQUEST_ERROR.getErrorCode,
        PERSIST_JOBREQUEST_ERROR.getErrorDesc
      )
    }

    val logAppender = new java.lang.StringBuilder()
    logAppender.append(
      "*************************************FAILOVER************************************** \n"
    )

    // try to kill ec
    killOldEC(jobRequest, logAppender);

    // deal Inited jobRequest, if status is Inited, need to deal by all Interceptors, such as set log_path
    if (SchedulerEventState.isInitedByStr(jobRequest.getStatus)) {
      dealInitedJobRequest(jobRequest, logAppender)
    }

    if (
        EntranceConfiguration.ENTRANCE_FAILOVER_RUNNING_KILL_ENABLED.getValue &&
        SchedulerEventState.isRunningByStr(jobRequest.getStatus)
    ) {
      // deal Running jobRequest, if enabled, status changed from Running to Cancelled
      dealRunningJobRequest(jobRequest, logAppender)
    } else {
      // init and submit
      initAndSubmitJobRequest(jobRequest, logAppender)
    }
  }

  def killOldEC(jobRequest: JobRequest, logAppender: java.lang.StringBuilder): Unit = {
    Utils.tryCatch {
      logAppender.append(
        LogUtils
          .generateInfo(s"job ${jobRequest.getId} start to kill old ec \n")
      )
      if (
          !SchedulerEventState.isRunning(SchedulerEventState.withName(jobRequest.getStatus))
          || !SchedulerEventState.isScheduled(SchedulerEventState.withName(jobRequest.getStatus))
      ) {
        val msg = s"job ${jobRequest.getId} status is not running or scheduled, ignore it"
        logger.info(msg)
        logAppender.append(LogUtils.generateInfo(msg) + "\n")
        return
      }

      if (
          jobRequest.getMetrics == null
          || !jobRequest.getMetrics.containsKey(TaskConstant.JOB_ENGINECONN_MAP)
      ) {
        val msg = s"job ${jobRequest.getId} not have EC info, ignore it"
        logger.info(msg)
        logAppender.append(LogUtils.generateInfo(msg) + "\n")
        return
      }

      val engineMap = jobRequest.getMetrics
        .get(TaskConstant.JOB_ENGINECONN_MAP)
        .asInstanceOf[util.Map[String, Object]]

      val engineInstance =
        engineMap.asScala
          .map(_._2.asInstanceOf[util.Map[String, Object]])
          .filter(_.containsKey(TaskConstant.ENGINE_INSTANCE))
          .maxBy(_.getOrDefault(TaskConstant.ENGINE_CONN_SUBMIT_TIME, "0").toString)

      if (engineInstance == null || engineInstance.containsKey(TaskConstant.FAILOVER_FLAG)) {
        val msg =
          s"job ${jobRequest.getId} do not submit to EC or already failover, not need kill ec"
        logger.info(msg)
        logAppender.append(LogUtils.generateInfo(msg) + "\n")
        return
      }
      engineInstance.put(TaskConstant.FAILOVER_FLAG, "")

      val ecInstance = ServiceInstance(
        GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue,
        engineInstance.get(TaskConstant.ENGINE_INSTANCE).toString
      )
      if (jobRequest.getLabels.asScala.exists(_.isInstanceOf[ExecuteOnceLabel])) {
        // kill ec by linkismanager
        val engineStopRequest = new EngineStopRequest
        engineStopRequest.setServiceInstance(ecInstance)
        // send to linkismanager kill ec
        Sender
          .getSender(RPCConfiguration.LINKIS_MANAGER_SERVICE_NAME.getValue)
          .send(engineStopRequest)
        val msg =
          s"job ${jobRequest.getId} send EngineStopRequest to linkismanager, kill EC instance $ecInstance"
        logger.info(msg)
        logAppender.append(LogUtils.generateInfo(msg) + "\n")
      } else if (engineInstance.containsKey(TaskConstant.ENGINE_CONN_TASK_ID)) {
        // get ec taskId
        val engineTaskId = engineInstance.get(TaskConstant.ENGINE_CONN_TASK_ID).toString
        // send to ec kill task
        Sender
          .getSender(ecInstance)
          .send(RequestTaskKill(engineTaskId))
        val msg =
          s"job ${jobRequest.getId} send RequestTaskKill to kill engineConn $ecInstance, execID $engineTaskId"
        logger.info(msg)
        logAppender.append(LogUtils.generateInfo(msg) + "\n")
      }
    } { t =>
      logger.error(s"job ${jobRequest.getId} kill ec error", t)
    }
  }

  def dealInitedJobRequest(jobReq: JobRequest, logAppender: java.lang.StringBuilder): JobRequest = {
    var jobRequest = jobReq
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
    jobRequest
  }

  def dealRunningJobRequest(jobRequest: JobRequest, logAppender: java.lang.StringBuilder): Unit = {
    Utils.tryCatch {
      // error_msg
      val msg =
        MessageFormat.format(
          EntranceErrorCodeSummary.FAILOVER_RUNNING_TO_CANCELLED.getErrorDesc,
          jobRequest.getId.toString
        )
      // init jobRequest properties
      jobRequest.setStatus(SchedulerEventState.Cancelled.toString)
      jobRequest.setProgress("1.0")
      jobRequest.setInstances(Sender.getThisInstance)
      jobRequest.setErrorCode(EntranceErrorCodeSummary.FAILOVER_RUNNING_TO_CANCELLED.getErrorCode)
      jobRequest.setErrorDesc(msg)

      // update jobRequest
      getEntranceContext
        .getOrCreatePersistenceManager()
        .createPersistenceEngine()
        .updateIfNeeded(jobRequest)

      // getOrGenerate log_path
      var logPath = jobRequest.getLogPath
      if (StringUtils.isBlank(logPath)) {
        ParserUtils.generateLogPath(jobRequest, null)
        logPath = jobRequest.getLogPath
        logAppender.append(
          LogUtils.generateInfo(s"job ${jobRequest.getId} generate new logPath $logPath \n")
        )
      }
      val job = getEntranceContext.getOrCreateEntranceParser().parseToJob(jobRequest)
      val logWriter = getEntranceContext.getOrCreateLogManager().createLogWriter(job)
      if (logAppender.length() > 0) {
        logWriter.write(logAppender.toString.trim)
      }

      logWriter.write(LogUtils.generateInfo(msg) + "\n")
      logWriter.flush()
      logWriter.close()

    } { case e: Exception =>
      logger.error(s"Job ${jobRequest.getId} failover, change status error", e)
    }
  }

  def initAndSubmitJobRequest(
      jobRequest: JobRequest,
      logAppender: java.lang.StringBuilder
  ): Unit = {
    // init properties
    initJobRequestProperties(jobRequest, logAppender)

    // update jobRequest
    getEntranceContext
      .getOrCreatePersistenceManager()
      .createPersistenceEngine()
      .updateIfNeeded(jobRequest)

    // reset `UpdateOrderFlag`
    jobRequest.setUpdateOrderFlag(true)

    logger.info(s"job ${jobRequest.getId} update JobRequest success")

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
        logger.error("Failed to write init JobRequest log, reason: ", t)
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
      getEntranceContext.getOrCreateScheduler().submit(job)
      val msg = LogUtils.generateInfo(
        s"Job with jobId : ${jobRequest.getId} and execID : ${job.getId()} submitted, success to failover"
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
    } { t =>
      job.onFailure("Submitting the query failed!(提交查询失败！)", t)
      val _jobRequest =
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

  private def initJobRequestProperties(
      jobRequest: JobRequest,
      logAppender: java.lang.StringBuilder
  ): Unit = {
    logger.info(s"job ${jobRequest.getId} start to initialize the properties")
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val initInstance = Sender.getThisInstance
    val initDate = new Date(System.currentTimeMillis)
    val initStatus = SchedulerEventState.Inited.toString
    val initProgress = "0.0"
    val initReqId = ""

    logAppender.append(
      LogUtils
        .generateInfo(s"job ${jobRequest.getId} start to Initialize the properties \n")
    )
    logAppender.append(
      LogUtils.generateInfo(s"the instances ${jobRequest.getInstances} -> ${initInstance} \n")
    )
    logAppender.append(
      LogUtils.generateInfo(
        s"the created_time ${sdf.format(jobRequest.getCreatedTime)} -> ${sdf.format(initDate)} \n"
      )
    )
    logAppender.append(
      LogUtils.generateInfo(s"the status ${jobRequest.getStatus} -> $initStatus \n")
    )
    logAppender.append(
      LogUtils.generateInfo(s"the progress ${jobRequest.getProgress} -> $initProgress \n")
    )

    val metricMap = new util.HashMap[String, Object]()
    if (EntranceConfiguration.ENTRANCE_FAILOVER_RETAIN_METRIC_ENGINE_CONN_ENABLED.getValue) {
      if (
          jobRequest.getMetrics != null && jobRequest.getMetrics.containsKey(
            TaskConstant.JOB_ENGINECONN_MAP
          )
      ) {
        val oldEngineconnMap = jobRequest.getMetrics
          .get(TaskConstant.JOB_ENGINECONN_MAP)
          .asInstanceOf[util.Map[String, Object]]
        metricMap.put(TaskConstant.JOB_ENGINECONN_MAP, oldEngineconnMap)
      }
    }

    if (EntranceConfiguration.ENTRANCE_FAILOVER_RETAIN_METRIC_YARN_RESOURCE_ENABLED.getValue) {
      if (
          jobRequest.getMetrics != null && jobRequest.getMetrics.containsKey(
            TaskConstant.JOB_YARNRESOURCE
          )
      ) {
        val oldResourceMap = jobRequest.getMetrics
          .get(TaskConstant.JOB_YARNRESOURCE)
          .asInstanceOf[util.Map[String, Object]]
        metricMap.put(TaskConstant.JOB_YARNRESOURCE, oldResourceMap)
      }
    }

    jobRequest.setInstances(initInstance)
    jobRequest.setCreatedTime(initDate)
    jobRequest.setStatus(initStatus)
    jobRequest.setProgress(initProgress)
    jobRequest.setReqId(initReqId)
    jobRequest.setErrorCode(0)
    jobRequest.setErrorDesc("")
    jobRequest.setMetrics(metricMap)
    jobRequest.getMetrics.put(TaskConstant.JOB_SUBMIT_TIME, initDate)
    // Allow task status updates to be unordered
    jobRequest.setUpdateOrderFlag(false)

    logAppender.append(
      LogUtils.generateInfo(s"job ${jobRequest.getId} success to initialize the properties \n")
    )
    logger.info(s"job ${jobRequest.getId} success to initialize the properties")
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
            getAllUndoneTask(null, null).filter(job => job.createTime < timeoutTime).foreach {
              job =>
                job.onFailure(s"Job has run for longer than the maximum time $timeoutType", null)
            }
            logger.info(s"Finished to check timeout Job, timout is ${timeoutType}")
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
