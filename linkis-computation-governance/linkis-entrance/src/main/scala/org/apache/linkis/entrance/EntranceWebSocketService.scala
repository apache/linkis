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

import org.apache.linkis.common.listener.Event
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.event._
import org.apache.linkis.entrance.execute.EntranceJob
import org.apache.linkis.entrance.job.EntranceExecutionJob
import org.apache.linkis.entrance.log.LogReader
import org.apache.linkis.entrance.restful.EntranceRestfulApi
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.protocol.utils.ZuulEntranceUtils
import org.apache.linkis.rpc.Sender
import org.apache.linkis.scheduler.queue.{Job, SchedulerEventState}
import org.apache.linkis.server._
import org.apache.linkis.server.conf.ServerConfiguration
import org.apache.linkis.server.socket.controller.{
  ServerEvent,
  ServerEventService,
  SocketServerEvent
}

import org.apache.commons.lang3.StringUtils

import java.text.SimpleDateFormat
import java.util
import java.util.Date
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._

class EntranceWebSocketService
    extends ServerEventService
    with EntranceEventListener
    with EntranceLogListener {

  private val jobIdToEventId = new util.HashMap[String, Integer]
  private var entranceServer: EntranceServer = _
  private var entranceRestfulApi: EntranceRestfulApi = _
  private val websocketTagJobID = new util.HashMap[String, String]()

  private val restfulURI =
    if (ServerConfiguration.BDP_SERVER_RESTFUL_URI.getValue.endsWith("/")) {
      ServerConfiguration.BDP_SERVER_RESTFUL_URI.getValue
    } else {
      ServerConfiguration.BDP_SERVER_RESTFUL_URI.getValue + "/"
    }

  private val executePattern = restfulURI + "entrance/execute"
  private val logUrlPattern = (restfulURI + """entrance/(.+)/log""").r
  private val statusUrlPattern = (restfulURI + """entrance/(.+)/status""").r
  private val progressUrlPattern = (restfulURI + """entrance/(.+)/progress""").r
  private val killUrlPattern = (restfulURI + """entrance/(.+)/kill""").r
  private val pauseUrlPattern = (restfulURI + """entrance/(.+)/pause""").r
  private val backgroundUrlPattern = restfulURI + """entrance/backgroundservice"""

  def setEntranceServer(entranceServer: EntranceServer): Unit = this.entranceServer = entranceServer

  def setEntranceRestfulApi(entranceRestfulApi: EntranceRestfulApi): Unit =
    this.entranceRestfulApi = entranceRestfulApi

  Utils.defaultScheduler.scheduleAtFixedRate(
    new Runnable {

      override def run(): Unit =
        jobIdToEventId.keys.map(entranceServer.getJob).foreach { case Some(job) =>
          if (job.isCompleted && System.currentTimeMillis - job.getEndTime > 5000) {
            jobIdToEventId synchronized jobIdToEventId.remove(job.getId)
            websocketTagJobID synchronized websocketTagJobID.remove(job.getId)
          }
        }

    },
    30,
    60,
    TimeUnit.SECONDS
  ) // TODO Time interval(时间间隔做成参数)

  Utils.defaultScheduler.scheduleAtFixedRate(
    new Runnable {

      override def run(): Unit = {
        if (jobIdToEventId.isEmpty) return
        val queues = entranceServer.getEntranceContext
          .getOrCreateScheduler()
          .getSchedulerContext
          .getOrCreateConsumerManager
          .listConsumers()
          .map(_.getConsumeQueue)
          .toSet
        queues.foreach { queue =>
          val waitingEvents = queue.getWaitingEvents
          waitingEvents.indices.foreach { index =>
            waitingEvents(index) match {
              case job: Job =>
                if (jobIdToEventId.containsKey(job.getId)) {
                  val entranceJob = job.asInstanceOf[EntranceJob]
                  val engineTypeLabel = entranceJob.getJobRequest.getLabels.asScala
                    .find(l => l.getLabelKey.equalsIgnoreCase(LabelKeyConstant.ENGINE_TYPE_KEY))
                    .orNull
                  if (null == engineTypeLabel) {
                    logger.error("Invalid engineTpyeLabel")
                    return
                  }
                  val realID = ZuulEntranceUtils.generateExecID(
                    entranceJob.getJobRequest.getReqId,
                    engineTypeLabel.asInstanceOf[EngineTypeLabel].getEngineType,
                    Sender.getThisInstance
                  )
                  val taskID = job.asInstanceOf[EntranceJob].getJobRequest.getId
                  Utils.tryQuietly(
                    sendMsg(
                      job,
                      Message
                        .ok("Get waiting size succeed.")
                        .data("execID", realID)
                        .data("taskID", taskID)
                        .data("waitingSize", index)
                        .data(
                          "websocketTag",
                          websocketTagJobID.get(job.getId)
                        ) << restfulURI + s"entrance/$realID/waitingSize"
                    )
                  )
                }
            }
          }
        }
      }

    },
    60,
    30,
    TimeUnit.SECONDS
  ) // TODO Time interval(时间间隔做成参数)

  override val serviceName: String = restfulURI + "entrance/"

  override def onEvent(event: ServerEvent): Message = event.getMethod match {
    case `executePattern` => dealExecute(event)
    case logUrlPattern(id) => dealLog(event, id)
    case statusUrlPattern(id) => dealStatus(event, id)
    case progressUrlPattern(id) => dealProgress(event, id)
    case killUrlPattern(id) => dealKill(event, id)
    case pauseUrlPattern(id) => dealPause(event, id)
    case _ =>
      logger.warn(
        "Unresolvable webSocket request, URI is(无法解析的webSocket请求，URI为)：" + event.getMethod
      )
      Message.error(
        "Unresolvable webSocket request, URI is(无法解析的webSocket请求，URI为：" + event.getMethod
      )
  }

  def dealExecute(event: ServerEvent): Message = {
    // TODO Convert to a suitable Map(转换成合适的Map)
    val params = event.getData
    val websocketTag = event.getWebsocketTag
    params.put(TaskConstant.EXECUTE_USER, event.getUser)
    val job = entranceServer.execute(params)
    jobIdToEventId synchronized jobIdToEventId.put(job.getId(), event.getId)
    websocketTagJobID synchronized websocketTagJobID.put(job.getId(), websocketTag)
    val jobRequest = job.asInstanceOf[EntranceJob].getJobRequest
    val taskID = jobRequest.getId
    val engineTypeLabel = LabelUtil.getEngineTypeLabel(jobRequest.getLabels)
    val executeApplicationName: String = engineTypeLabel.getEngineType
    val creator: String = LabelUtil.getUserCreatorLabel(jobRequest.getLabels).getCreator
    val execID = ZuulEntranceUtils.generateExecID(
      job.getId(),
      executeApplicationName,
      Sender.getThisInstance,
      creator
    )
    val executeResponseMsg = Message.ok("Request execution succeeded(请求执行成功)")
    executeResponseMsg
      .data("execID", execID)
      .data("taskID", taskID)
      .data("websocketTag", websocketTagJobID.get(job.getId()))
    executeResponseMsg.setMethod(restfulURI + "entrance/execute")
    executeResponseMsg.setStatus(0)
    sendMsg(job, executeResponseMsg)
    val currentTime = System.currentTimeMillis()
    val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val time = simpleDateFormat.format(new Date(currentTime))
    entranceServer.getEntranceContext
      .getOrCreateLogManager()
      .onLogUpdate(job, LogUtils.generateInfo(s"You have submitted a new job at " + time))
    entranceServer.getEntranceContext
      .getOrCreateLogManager()
      .onLogUpdate(
        job,
        LogUtils.generateInfo(
          s"Your job's execution code is (after variable substitution and code check) "
        )
      )
    entranceServer.getEntranceContext
      .getOrCreateLogManager()
      .onLogUpdate(
        job,
        "************************************SCRIPT CODE************************************"
      )
    entranceServer.getEntranceContext
      .getOrCreateLogManager()
      .onLogUpdate(
        job,
        "************************************SCRIPT CODE************************************"
      )
    entranceServer.getEntranceContext
      .getOrCreateLogManager()
      .onLogUpdate(
        job,
        LogUtils.generateInfo(
          s"Your job is accepted,  jobID is ${job.getId} and taskID is $taskID. Please wait it to be scheduled"
        )
      )
    "The request was executed successfully!"
      .data("execID", execID)
      .data("taskID", taskID)
      .data("websocketTag", websocketTagJobID.get(job.getId))
    // executeResponseMsg
  }

  def dealLog(event: ServerEvent, id: String): Message = {
    var retMessage: Message = null
    val realID = ZuulEntranceUtils.parseExecID(id)(3)
    entranceServer.getJob(realID) foreach {
      case entranceExecutionJob: EntranceExecutionJob =>
        logger.info(s"begin to get job $realID log via websocket")
        val logsArr: Array[String] = new Array[String](4)
        entranceExecutionJob.getWebSocketLogReader.foreach(logReader =>
          logReader.readArray(logsArr, 0, 100)
        )
        val logList: util.List[String] = new util.ArrayList[String]()
        logsArr foreach logList.add
        retMessage = Message.ok("Successfully obtained log information(成功获取到日志信息)")
        retMessage
          .data("execID", id)
          .data("log", logList)
          .data("websocketTag", websocketTagJobID.get(realID))
        retMessage.setStatus(0)
        retMessage.setMethod(restfulURI + "entrance/" + id + "/log")
        logger.info(s"end to get job $realID log via websocket")
        return retMessage
      case _ =>
    }
    retMessage = Message.error(
      s"Failed to get the log, $id failed to find the corresponding job(获取日志失败，$id 未能找到对应的job)"
    )
    retMessage.setStatus(1)
    retMessage.setMethod(restfulURI + "entrance/" + id + "/log")
    retMessage
  }

  def dealStatus(event: ServerEvent, id: String): Message = {
    var retMessage: Message = null
    val realID: String = if (!id.contains(":")) id else ZuulEntranceUtils.parseExecID(id)(3)
    entranceServer.getJob(realID) foreach {
      case entranceExecutionJob: EntranceExecutionJob =>
        val engineTypeLabel =
          LabelUtil.getEngineTypeLabel(entranceExecutionJob.getJobRequest.getLabels)
        val userCreatorLabel =
          LabelUtil.getUserCreatorLabel(entranceExecutionJob.getJobRequest.getLabels)
        val longExecID = ZuulEntranceUtils.generateExecID(
          realID,
          engineTypeLabel.getEngineType,
          Sender.getThisInstance,
          userCreatorLabel.getCreator
        )
        if (!jobIdToEventId.containsKey(realID) && event != null) {
          jobIdToEventId synchronized jobIdToEventId.put(realID, event.getId)
        }
        val status = entranceExecutionJob.getState

        retMessage = Message.ok("Get the status of the task successfully(获取任务状态成功)")
        val taskID = entranceExecutionJob.getJobRequest.getId
        retMessage
          .data("execID", longExecID)
          .data("status", status.toString)
          .data("websocketTag", websocketTagJobID.get(realID))
          .data("taskID", taskID)
        logger.info(
          "retMessage: execID is {}, status is {}, websocketTag is {}",
          Array(longExecID, status.toString, websocketTagJobID.get(realID)): _*
        )
        retMessage.setStatus(0)
        retMessage.setMethod(restfulURI + "entrance/" + longExecID + "/status")
        if (SchedulerEventState.isCompleted(status)) {
          websocketTagJobID.remove(realID)
        }
        return retMessage
      case _ =>
    }
    retMessage = Message.error("Get task status failed(获取任务状态失败)")
    retMessage.setStatus(1)
    retMessage.setMethod(restfulURI + "entrance/" + id + "/status")
    retMessage
  }

  def dealProgress(event: ServerEvent, id: String): Message = {
    var retMessage: Message = null
    val realID = ZuulEntranceUtils.parseExecID(id)(3)
    entranceServer.getJob(realID) foreach {
      case entranceExecutionJob: EntranceExecutionJob =>
        val progress = entranceExecutionJob.getProgress
        retMessage = Message.ok("Get the task progress successfully(获取任务进度成功)")
        val taskID = entranceExecutionJob.getJobRequest.getId
        retMessage
          .data("execID", id)
          .data("progress", progress)
          .data("websocketTag", websocketTagJobID.get(realID))
          .data("taskID", taskID)
        retMessage.setStatus(0)
        retMessage.setMethod(restfulURI + "entrance/" + id + "/progress")
        return retMessage
      case _ =>
    }
    retMessage = Message.error("Get task progress failed(获取任务进度失败)")
    retMessage.setStatus(1)
    retMessage.setMethod(restfulURI + "entrance/" + id + "/progress")
    retMessage
  }

  def dealPause(event: ServerEvent, id: String): Message = {
    var retMessage: Message =
      Message.error("Temporarily does not support the task pause(暂时不支持任务暂停)")
    retMessage.setStatus(1)
    retMessage.setMethod(restfulURI + "entrance/" + id + "/progress")
    retMessage
  }

  def dealKill(event: ServerEvent, id: String): Message = {
    var retMessage: Message = null
    val realID = ZuulEntranceUtils.parseExecID(id)(3)
    entranceServer.getJob(realID) foreach {
      case entranceExecutionJob: EntranceExecutionJob =>
        try {
          entranceExecutionJob.kill()
          retMessage = Message.ok("Kill task succeeded(kill任务成功)")
          retMessage.setMethod(restfulURI + "entrance/" + id + "/kill")
          retMessage.setStatus(0)
          return retMessage
        } catch {
          case e: Exception => retMessage = Message.error("Kill task failed(kill任务失败)", e)
          case t: Throwable => retMessage = Message.error("Kill task failed(kill任务失败)", t)
        }
      case _ =>
    }
    if (retMessage == null) {
      retMessage = Message.error("Get task failed(获取任务失败)")
    }
    retMessage.setMethod(restfulURI + "entrance/" + id + "/kill")
    retMessage.setStatus(1)
    retMessage
  }

  private def concatLog(length: Int, log: String, flag: StringBuilder, all: StringBuilder): Unit = {
    if (length == 1) {
      flag ++= log ++= "\n"
      all ++= log ++= "\n"
    } else {
      flag ++= log ++= "\n"
      all ++= log ++= "\n"
    }
  }

  /**
   * Push the log message to the front end(将日志的消息推送给前端)
   * @param job
   *   required(需要)
   * @param log
   */
  def pushLogToFrontend(job: Job, log: String): Unit = {
    import LogReader._
    if (StringUtils.isBlank(log)) return
    var message: Message = null
    val logs: Array[String] = new Array[String](4)
    val logArr: Array[String] = log.split("\n\n").filter(StringUtils.isNotBlank)
    val info = new StringBuilder
    val warn = new StringBuilder
    val error = new StringBuilder
    val all = new StringBuilder
    val length = logArr.length
    logArr.foreach(singleLog => {
      if (StringUtils.isNotEmpty(singleLog)) {
        singleLog match {
          case ERROR_HEADER1() | ERROR_HEADER2() =>
            concatLog(length, singleLog, error, all)
          case WARN_HEADER1() | WARN_HEADER2() =>
            val arr =
              EntranceConfiguration.LOG_WARN_EXCLUDE.getValue.split(",").map(word => word.trim)
            var flag = false
            for (keyword <- arr) {
              flag = singleLog.contains(keyword) || flag
            }
            if (!flag) {
              val message = singleLog.split("\n")(0)
              concatLog(length, message, warn, all)
            }
          case INFO_HEADER1() | INFO_HEADER2() =>
            val hiveLogSpecial: String = EntranceConfiguration.HIVE_SPECIAL_LOG_INCLUDE.getValue
            val sparkLogSpecial: String = EntranceConfiguration.SPARK_SPECIAL_LOG_INCLUDE.getValue
            val hiveCreateTableLog: String = EntranceConfiguration.HIVE_CREATE_TABLE_LOG.getValue
            if (singleLog.contains(hiveLogSpecial) && singleLog.contains(hiveCreateTableLog)) {
              val threadName = EntranceConfiguration.HIVE_THREAD_NAME.getHotValue()
              val printInfo = EntranceConfiguration.HIVE_PRINT_INFO_LOG.getValue
              val start = singleLog.indexOf(threadName)
              val end = singleLog.indexOf(printInfo) + printInfo.length
              if (start > 0 && end > 0) {
                val realLog =
                  singleLog.substring(0, start) + singleLog.substring(end, singleLog.length)
                concatLog(length, realLog, info, all)
              }
            }
            if (
                singleLog.contains(hiveLogSpecial) && singleLog.contains("map") && singleLog
                  .contains("reduce")
            ) {
              val threadName = EntranceConfiguration.HIVE_THREAD_NAME.getHotValue()
              val stageName = EntranceConfiguration.HIVE_STAGE_NAME.getHotValue()
              val start = singleLog.indexOf(threadName)
              val end = singleLog.indexOf(stageName)
              if (start > 0 && end > 0) {
                val realLog =
                  singleLog.substring(0, start) + singleLog.substring(end, singleLog.length)
                concatLog(length, realLog, info, all)
              }
            } else if (singleLog.contains(sparkLogSpecial)) {
              val className = EntranceConfiguration.SPARK_PROGRESS_NAME.getValue
              val endFlag = EntranceConfiguration.END_FLAG.getValue
              val start = singleLog.indexOf(className)
              val end = singleLog.indexOf(endFlag) + endFlag.length
              if (start > 0 && end > 0) {
                val realLog =
                  singleLog.substring(0, start) + singleLog.substring(end, singleLog.length)
                concatLog(length, realLog, info, all)
              }
            } else {
              val arr =
                EntranceConfiguration.LOG_EXCLUDE.getValue.split(",").map(word => word.trim)
              var flag = false
              for (keyword <- arr) {
                flag = singleLog.contains(keyword) || flag
              }
              if (!flag) concatLog(length, singleLog, info, all)
            }
          case _ =>
            val arr = EntranceConfiguration.LOG_EXCLUDE.getValue.split(",").map(word => word.trim)
            var flag = false
            for (keyword <- arr) {
              flag = singleLog.contains(keyword) || flag
            }
            if (!flag) concatLog(length, singleLog, info, all)
        }
      }
    })
    if (
        StringUtils.isBlank(info.toString()) &&
        StringUtils.isBlank(warn.toString()) &&
        StringUtils.isBlank(error.toString()) &&
        StringUtils.isBlank(all.toString())
    ) {
      return
    }
    val logList: util.List[String] = new util.ArrayList[String]()
    logList.add(error.toString())
    logList.add(warn.toString())
    logList.add(info.toString())
    logList.add(all.toString())
    message = Message.ok("Return log information(返回日志信息)")
    val jobRequest = job.asInstanceOf[EntranceJob].getJobRequest
    val engineType = LabelUtil.getEngineType(jobRequest.getLabels)
    val creator = LabelUtil.getUserCreator(jobRequest.getLabels)._2
    val executeApplicationName = engineType
    val execID: String = ZuulEntranceUtils.generateExecID(
      job.getId,
      executeApplicationName,
      Sender.getThisInstance,
      creator
    )
    message.setMethod(restfulURI + "entrance/" + execID + "/log")
    val taskID = jobRequest.getId
    message
      .data("execID", execID)
      .data("log", logList)
      .data("websocketTag", websocketTagJobID.get(job.getId))
      .data("taskID", taskID)
    sendMsg(job, message)
  }

  def pushProgressToFrontend(
      job: Job,
      progress: Float,
      progressInfo: Array[JobProgressInfo]
  ): Unit = {
    val progressInfoMap = progressInfo.map(info =>
      toJavaMap(
        Map(
          "id" -> info.id,
          "succeedTasks" -> info.succeedTasks,
          "failedTasks" -> info.failedTasks,
          "runningTasks" -> info.runningTasks,
          "totalTasks" -> info.totalTasks
        )
      )
    )
    val jobRequest = job.asInstanceOf[EntranceJob].getJobRequest
    val engineType = LabelUtil.getEngineType(jobRequest.getLabels)
    val creator = LabelUtil.getUserCreator(jobRequest.getLabels)._2
    val executeApplicationName = engineType
    job.asInstanceOf[EntranceJob].setProgressInfo(progressInfo)
    val execID: String = ZuulEntranceUtils.generateExecID(
      job.getId,
      executeApplicationName,
      Sender.getThisInstance,
      creator
    )
    val message = Message.ok("return the schedule information (返回进度信息!)")
    message.setMethod(restfulURI + "entrance/" + execID + "/progress")
    val taskID = jobRequest.getId
    sendMsg(
      job,
      message
        .data("progress", progress)
        .data("progressInfo", progressInfoMap)
        .data(
          "execID",
          ZuulEntranceUtils
            .generateExecID(job.getId, executeApplicationName, Sender.getThisInstance, creator)
        )
        .data("websocketTag", websocketTagJobID.get(job.getId))
        .data("taskID", taskID)
    )
  }

  private def sendMsg(job: Job, message: Message): Unit = {
    // Determine whether the eventId exists, if it does not exist, it may be because the entity that submitted the job was hanged, and now directly push all instances of the user
    // Enclose the jobID into the Message, send out
    // 判断eventId是否存在，如果不存在，则可能是由于之前提交该job的entrance挂掉了，现在就直接推送给该用户的所有实例
    // 将jobID封装进Message，send出去
    val eventId = jobIdToEventId.get(job.getId)
    if (eventId == null) job match {
      case entranceJob: EntranceJob => sendMessageToUser(entranceJob.getUser, message)
      case _ =>
    }
    else sendMessage(eventId, message)
  }

  override def onEvent(event: EntranceEvent): Unit = event match {
    // Process job progress and pass information to the front end(处理Job进度，将信息传递给前端)
    case EntranceJobEvent(jobId) =>
      entranceServer.getJob(jobId).foreach { job =>
        val message = dealStatus(null, jobId)
        sendMsg(job, message)
      }
    case EntranceProgressEvent(job, progress, progressInfo) =>
      pushProgressToFrontend(job, progress, progressInfo)
    case _ =>
  }

  override def onEvent(event: EntranceLogEvent): Unit = event match {
    case EntrancePushLogEvent(job, log) => pushLogToFrontend(job, log)
    case _ =>
  }

  override def onEventError(event: Event, t: Throwable): Unit = event match {
    case e: EntranceEvent => onEventError(e, t)
    case e: SocketServerEvent => super.onEventError(e, t)
    case _ => logger.warn(s"cannot recognize the event type $event.", t)
  }

  override def onEventError(event: EntranceEvent, t: Throwable): Unit = event match {
    case EntranceJobEvent(jobId) =>
      logger.info(s"WebSocket send the new status of Job $jobId to webClient failed!", t)
    case EntranceProgressEvent(job, progress, _) =>
      logger.info(s"Job $job send progress $progress by webSocket to webClient failed!", t)
    case _ => logger.info(s"WebSocket send event $event to webClient failed!", t)
  }

  override def onEventError(event: EntranceLogEvent, t: Throwable): Unit = event match {
    case EntrancePushLogEvent(job, _) =>
      logger.info(s"WebSocket send the new log of Job $job to webClient failed!", t)
  }

}
