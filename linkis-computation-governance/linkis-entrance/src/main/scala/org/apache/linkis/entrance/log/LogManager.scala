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

package org.apache.linkis.entrance.log

import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.EntranceContext
import org.apache.linkis.entrance.job.EntranceExecutionJob
import org.apache.linkis.scheduler.listener.LogListener
import org.apache.linkis.scheduler.queue.Job

abstract class LogManager extends LogListener with Logging {

  protected var errorCodeListener: Option[ErrorCodeListener] = None
  protected var errorCodeManager: Option[ErrorCodeManager] = None
  protected var entranceContext: EntranceContext = _

  def setEntranceContext(entranceContext: EntranceContext): Unit = this.entranceContext =
    entranceContext

  def setErrorCodeListener(errorCodeListener: ErrorCodeListener): Unit = this.errorCodeListener =
    Option(errorCodeListener)

  def setErrorCodeManager(errorCodeManager: ErrorCodeManager): Unit = this.errorCodeManager =
    Option(errorCodeManager)

  def getErrorCodeManager(): Option[ErrorCodeManager] = errorCodeManager
  def getErrorCodeListener: Option[ErrorCodeListener] = errorCodeListener

  def getLogReader(execId: String): LogReader

  def createLogWriter(job: Job): LogWriter

  def dealLogEvent(job: Job, log: String): Unit = {
    Utils.tryCatch {
      job match {
        case entranceExecutionJob: EntranceExecutionJob =>
          if (entranceExecutionJob.getLogWriter.isEmpty) {
            entranceExecutionJob.getLogWriterLocker synchronized {
              if (entranceExecutionJob.getLogWriter.isEmpty) {
                val logWriter = createLogWriter(entranceExecutionJob)
                if (null == logWriter) {
                  return
                }
              }
            }
          }
          var writeLog = log
          errorCodeManager.foreach(_.errorMatchAndGetContent(log).foreach {
            case (code, errorMsg, targetMsg) =>
              if (!targetMsg.contains(LogUtils.ERROR_STR) && log.contains(LogUtils.ERROR_STR)) {
                writeLog = LogUtils.generateERROR(
                  s"error code: $code, errorMsg: $errorMsg, errorLine: $targetMsg \n" + log
                )
              }
              errorCodeListener.foreach(_.onErrorCodeCreated(job, code, errorMsg))
            case _ =>
          })
          entranceExecutionJob.getLogWriter.foreach(logWriter => logWriter.write(writeLog))

        case _ =>
      }
    } {
      case e: Exception => logger.warn(s"write log for job ${job.getId} failed", e)
      case t: Throwable => logger.warn(s"write log for job ${job.getId} failed", t)
    }
  }

  override def onLogUpdate(job: Job, log: String): Unit = {
    dealLogEvent(job, log)
  }

}
