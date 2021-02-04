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

package com.webank.wedatasphere.linkis.entrance

import com.webank.wedatasphere.linkis.common.exception.{DWCException, DWCRuntimeException, ErrorException}
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.exception.{EntranceErrorException, SubmitFailedException}
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob
import com.webank.wedatasphere.linkis.entrance.log.LogReader
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.scheduler.queue.{Job, SchedulerEventState}
import com.webank.wedatasphere.linkis.server.conf.ServerConfiguration
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils

/**
  * Created by enjoyyin on 2018/9/4.
  */
abstract class EntranceServer extends Logging {

  private var entranceWebSocketService: Option[EntranceWebSocketService] = None

  def init(): Unit

  def getName: String

  def getEntranceContext: EntranceContext


  /**
    * Execute a task and return an execId(执行一个task，返回一个execId)
    * @param params
    * @return
    */
  def execute(params: java.util.Map[String, Any]): String = {
    if(!params.containsKey(EntranceServer.DO_NOT_PRINT_PARAMS_LOG)) info("received a request: " + params)
    else params.remove(EntranceServer.DO_NOT_PRINT_PARAMS_LOG)
    var task = getEntranceContext.getOrCreateEntranceParser().parseToTask(params)
    task match {
      case t: RequestPersistTask => if(StringUtils.isBlank(t.getRequestApplicationName))
        throw new EntranceErrorException(20038, "requestApplicationName cannot be empty.")
      case _ =>
    }
    //After parse the map into a task, we need to store it in the database, and the task can get a unique taskID.
    //将map parse 成 task 之后，我们需要将它存储到数据库中，task可以获得唯一的taskID
    getEntranceContext.getOrCreatePersistenceManager().createPersistenceEngine().persist(task)
    val logAppender = new java.lang.StringBuilder()
    Utils.tryThrow(getEntranceContext.getOrCreateEntranceInterceptors().foreach(int => task = int.apply(task, logAppender))) { t =>
      val error = t match {
        case error: ErrorException => error
        case t1:Throwable => val exception = new EntranceErrorException(20039, "解析task失败！原因：" + ExceptionUtils.getRootCauseMessage(t1))
          exception.initCause(t1)
          exception
        case _ => new EntranceErrorException(20039, "解析task失败！原因：" + ExceptionUtils.getRootCauseMessage(t))
      }
      task match {
        case t: RequestPersistTask =>
          t.setErrCode(error.getErrCode)
          t.setErrDesc(error.getDesc)
          t.setStatus(SchedulerEventState.Failed.toString)
          t.setProgress(1.0f)
        case _ =>
      }
      getEntranceContext.getOrCreatePersistenceManager().createPersistenceEngine().updateIfNeeded(task)
      error
    }
    getEntranceContext.getOrCreatePersistenceManager().createPersistenceEngine().updateIfNeeded(task)
    val job = getEntranceContext.getOrCreateEntranceParser().parseToJob(task)
    job.init()
    job.setLogListener(getEntranceContext.getOrCreateLogManager())
    job.setProgressListener(getEntranceContext.getOrCreatePersistenceManager())
    job.setJobListener(getEntranceContext.getOrCreatePersistenceManager())
    job match {
      case entranceJob: EntranceJob => entranceJob.setEntranceListenerBus(getEntranceContext.getOrCreateEventListenerBus)
      case _ =>
    }
    Utils.tryCatch{
      if(logAppender.length() > 0) job.getLogListener.foreach(_.onLogUpdate(job, logAppender.toString.trim))
    }{
      t => logger.error("Failed to write init log, reason: ", t)
    }

    Utils.tryThrow{
      getEntranceContext.getOrCreateScheduler().submit(job)
    }{t =>
      job.onFailure("Submitting the query failed!(提交查询失败！)", t)
      val _task = getEntranceContext.getOrCreateEntranceParser().parseToTask(job)
      getEntranceContext.getOrCreatePersistenceManager().createPersistenceEngine().updateIfNeeded(_task)
      t match {
        case e: DWCException => e
        case e: DWCRuntimeException => e
        case t: Throwable =>
          new SubmitFailedException(30009, "Submitting the query failed!(提交查询失败！)" + ExceptionUtils.getRootCauseMessage(t), t)
      }
    }
    task match {
      case requestPersistTask:RequestPersistTask => logger.info(s"Job ${job.getId} submitted and taskID is ${requestPersistTask.getTaskID}")
      case _ => info(s"Job $job submitted!")
    }
    job.getId
  }

  def logReader(execId: String): LogReader

  def getJob(execId: String): Option[Job] = getEntranceContext.getOrCreateScheduler().get(execId).map(_.asInstanceOf[Job])

  private[entrance] def getEntranceWebSocketService: Option[EntranceWebSocketService] = if(ServerConfiguration.BDP_SERVER_SOCKET_MODE.getValue) {
    if(entranceWebSocketService.isEmpty) synchronized {
      if(entranceWebSocketService.isEmpty) {
        entranceWebSocketService = Some(new EntranceWebSocketService)
        entranceWebSocketService.foreach(_.setEntranceServer(this))
        entranceWebSocketService.foreach(getEntranceContext.getOrCreateEventListenerBus.addListener)
      }
    }
    entranceWebSocketService
  } else None

}
object EntranceServer {
  val DO_NOT_PRINT_PARAMS_LOG = "doNotPrintParamsLog"
}