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

package com.webank.wedatasphere.linkis.entrance.log

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.EntranceContext
import com.webank.wedatasphere.linkis.entrance.event.{EntranceJobLogEvent, EntranceLogEvent, EntranceLogListener}
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob
import com.webank.wedatasphere.linkis.scheduler.listener.LogListener
import com.webank.wedatasphere.linkis.scheduler.queue.Job


abstract class LogManager extends LogListener with Logging with EntranceLogListener{

  protected var errorCodeListener: Option[ErrorCodeListener] = None
  protected var errorCodeManager: Option[ErrorCodeManager] = None
  protected var entranceContext: EntranceContext = _

  def setEntranceContext(entranceContext: EntranceContext): Unit = this.entranceContext = entranceContext
  def setErrorCodeListener(errorCodeListener: ErrorCodeListener): Unit = this.errorCodeListener = Option(errorCodeListener)
  def setErrorCodeManager(errorCodeManager: ErrorCodeManager): Unit = this.errorCodeManager = Option(errorCodeManager)

  def getLogReader(execId: String): LogReader

  def createLogWriter(job: Job): LogWriter

  def dealLogEvent(job: Job, log: String): Unit = {
    Utils.tryCatch{
      //     warn(s"jobid :${job.getId()}\nlog : ${log}")
      job match{
        case entranceExecutionJob: EntranceExecutionJob =>
          if (entranceExecutionJob.getLogWriter.isEmpty) entranceExecutionJob synchronized {
            if (entranceExecutionJob.getLogWriter.isEmpty) createLogWriter(entranceExecutionJob)
          }
          entranceExecutionJob.getLogWriter.foreach(logWriter => logWriter.write(log))
          entranceExecutionJob.getWebSocketLogWriter.foreach(writer => writer.write(log))
          errorCodeManager.foreach(_.errorMatch(log).foreach { case (code, errorMsg) =>
            errorCodeListener.foreach(_.onErrorCodeCreated(job, code, errorMsg))
          })
        case _ =>
      }
    }{
      case e: Exception => logger.warn(s"write log for job ${job.getId} failed", e)
      case t: Throwable => logger.warn(s"write log for job ${job.getId} failed", t)
    }
  }

  override def onEvent(event: EntranceLogEvent): Unit = {
    event match {
      case event: EntranceJobLogEvent => {
        entranceContext.getOrCreateLogManager().dealLogEvent(event.job, event.log)
      }
      case _ =>
    }
  }

  override def onEventError(event: EntranceLogEvent, t: Throwable): Unit = {
    event match {
      case EntranceJobLogEvent(job,log) => info(s"jobId:${job.getId()} log append error, reason:${t}")
      case _ =>
    }
  }

  override def onLogUpdate(job: Job, log: String): Unit = {
    entranceContext.getOrCreateLogListenerBus.post(EntranceJobLogEvent(job,log))
  }

}