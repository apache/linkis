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
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.entrance.EntranceContext
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary._
import org.apache.linkis.entrance.event._
import org.apache.linkis.entrance.exception.EntranceErrorException
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.paser.CodeParser
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.rpc.utils.RPCUtils
import org.apache.linkis.scheduler.executer.{CompletedExecuteResponse, ErrorExecuteResponse}
import org.apache.linkis.scheduler.queue.{Job, SchedulerEventState}
import org.apache.linkis.scheduler.queue.SchedulerEventState._

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.Date
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

import scala.beans.BeanProperty

abstract class EntranceJob extends Job {

  @BeanProperty
  var creator: String = _

  @BeanProperty
  var user: String = _

  @BeanProperty
  var params: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef](4)

  @BeanProperty
  var jobRequest: JobRequest = _

  @BeanProperty
  var codeParser: CodeParser = _

  private var entranceListenerBus
      : Option[EntranceEventListenerBus[EntranceEventListener, EntranceEvent]] = None

  private var progressInfo: Array[JobProgressInfo] = Array.empty
  private val persistedResultSets = new AtomicInteger(0)

  private var entranceContext: EntranceContext = _

  private var updateMetrisFlag: Boolean = false

  def getUpdateMetrisFlag: Boolean = this.updateMetrisFlag

  def setUpdateMetrisFlag(updateDbFlag: Boolean): Unit = this.updateMetrisFlag = updateDbFlag

  /**
   * Record newest time that a client access status of this job Can be used to monitor client
   * status. e.g. server can detect if linkis-cli process has abnormally ended then kill the job
   */
  private val newestAccessByClientTimestamp: AtomicLong = new AtomicLong(-1L)

  def setEntranceListenerBus(
      entranceListenerBus: EntranceEventListenerBus[EntranceEventListener, EntranceEvent]
  ): Unit =
    this.entranceListenerBus = Option(entranceListenerBus)

  def setProgressInfo(progressInfo: Array[JobProgressInfo]): Unit = this.progressInfo = progressInfo

  def getProgressInfo: Array[JobProgressInfo] = this.progressInfo

  def setEntranceContext(entranceContext: EntranceContext): Unit = this.entranceContext =
    entranceContext

  def getEntranceContext: EntranceContext = this.entranceContext

  def getNewestAccessByClientTimestamp: Long = this.newestAccessByClientTimestamp.get()

  def updateNewestAccessByClientTimestamp(): Unit = {
    val newTime = System.currentTimeMillis()
    newestAccessByClientTimestamp.set(newTime)
  }

  def setResultSize(resultSize: Int): Unit = {
    if (resultSize >= 0) {
      persistedResultSets.set(resultSize)
    }
  }

  def addAndGetResultSize(resultSize: Int): Int = {
    logger.info(
      s"Job ${getJobRequest.getId} resultsize from ${persistedResultSets.get()} add ${resultSize}"
    )
    if (resultSize > 0) {
      persistedResultSets.addAndGet(resultSize)
    } else {
      persistedResultSets.get()
    }
  }

  @deprecated
  def incrementResultSetPersisted(): Unit = {
    //    persistedResultSets.incrementAndGet()
  }

  protected def isWaitForPersistedTimeout(startWaitForPersistedTime: Long): Boolean =
    System.currentTimeMillis - startWaitForPersistedTime >= EntranceConfiguration.JOB_MAX_PERSIST_WAIT_TIME.getValue.toLong

  override def beforeStateChanged(
      fromState: SchedulerEventState,
      toState: SchedulerEventState
  ): Unit = {
    super.beforeStateChanged(fromState, toState)
  }

  override def afterStateChanged(
      fromState: SchedulerEventState,
      toState: SchedulerEventState
  ): Unit = {
    try {
      toState match {
        case Scheduled =>
          if (getJobRequest.getMetrics == null) {
            getLogListener.foreach(
              _.onLogUpdate(this, LogUtils.generateWarn("Job Metrics has not been initialized."))
            )
          } else {
            if (getJobRequest.getMetrics.containsKey(TaskConstant.ENTRANCEJOB_SCHEDULE_TIME)) {
              getLogListener.foreach(
                _.onLogUpdate(
                  this,
                  LogUtils.generateWarn("Your job has already been scheduled before.")
                )
              )
            } else {
              getJobRequest.getMetrics.put(
                TaskConstant.ENTRANCEJOB_SCHEDULE_TIME,
                new Date(System.currentTimeMillis)
              )
            }
          }
          getLogListener.foreach(
            _.onLogUpdate(
              this,
              LogUtils.generateInfo("Your job is Scheduled. Please wait it to run.")
            )
          )
        case WaitForRetry =>
          getLogListener.foreach(
            _.onLogUpdate(
              this,
              LogUtils.generateInfo("Your job is turn to retry. Please wait it to schedule.")
            )
          )
        case Running =>
          getLogListener.foreach(
            _.onLogUpdate(
              this,
              LogUtils.generateInfo("Your job is Running now. Please wait it to complete.")
            )
          )
        case _ if SchedulerEventState.isCompleted(toState) =>
          getJobRequest.getMetrics.put(
            TaskConstant.ENTRANCEJOB_COMPLETE_TIME,
            new Date(System.currentTimeMillis())
          )
          if (getJobInfo != null) {
            getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateInfo(getJobInfo.getMetric)))
          }
          if (isSucceed) {
            getLogListener.foreach(
              _.onLogUpdate(
                this,
                LogUtils.generateInfo("Congratulations. Your job completed with status Success.")
              )
            )
          } else {
            getLogListener.foreach(
              _.onLogUpdate(
                this,
                LogUtils.generateInfo(
                  s"Sorry. Your job completed with a status $toState. You can view logs for the reason."
                )
              )
            )
          }
          this.setProgress(EntranceJob.JOB_COMPLETED_PROGRESS)
          entranceListenerBus.foreach(
            _.post(
              EntranceProgressEvent(this, EntranceJob.JOB_COMPLETED_PROGRESS, this.getProgressInfo)
            )
          )
          this.getProgressListener.foreach(listener =>
            listener.onProgressUpdate(
              this,
              EntranceJob.JOB_COMPLETED_PROGRESS,
              Array[JobProgressInfo]()
            )
          )
        case _ =>
      }
    } catch {
      case e: Exception => logger.error("Failed to match state", e)
    }
    super.afterStateChanged(fromState, toState)
    entranceListenerBus.foreach(_.post(EntranceJobEvent(this.getId())))
  }

  override def onFailure(errorMsg: String, t: Throwable): Unit = {
    if (!isCompleted) {
      val generatedMsg =
        LogUtils.generateERROR(s"Sorry, your job executed failed with reason: $errorMsg")
      getLogListener.foreach(_.onLogUpdate(this, generatedMsg))
    } else {
      val throwableMsg = {
        if (null == t) {
          null
        } else {
          t.getMessage
        }
      }
      logger.warn(
        s"There are an method who calls onFailure while job is completed, errorMsg is : ${errorMsg}, throwableMsg is : ${throwableMsg}"
      )
    }
    super.onFailure(errorMsg, t)
  }

  override protected def transitionCompleted(executeCompleted: CompletedExecuteResponse): Unit = {
    Utils.tryAndErrorMsg(clearInstanceInfo())("Failed to clear executor")
    super.transitionCompleted(executeCompleted)
  }

  private def clearInstanceInfo(): Unit = {
    val executorManager =
      entranceContext.getOrCreateScheduler().getSchedulerContext.getOrCreateExecutorManager
    executorManager.delete(getExecutor)
  }

  def transitionCompleted(executeCompleted: CompletedExecuteResponse, reason: String): Unit = {
    logger.debug("Job directly completed with reason: " + reason)
    transitionCompleted(executeCompleted)
  }

  override protected def isJobShouldRetry(errorExecuteResponse: ErrorExecuteResponse): Boolean =
    isJobSupportRetry && errorExecuteResponse != null &&
      (if (RPCUtils.isReceiverNotExists(errorExecuteResponse.t)) {
         getExecutor match {
           case e: EntranceExecutor =>
             getLogListener.foreach(
               _.onLogUpdate(
                 this,
                 LogUtils.generateSystemWarn(
                   s"Since the submitted engine rejects the connection, the system will automatically retry and exclude the engine.(由于提交的引擎拒绝连接，系统将自动进行重试，并排除引擎.)"
                 )
               )
             )
           case _ =>
         }
         true
       } else super.isJobShouldRetry(errorExecuteResponse))

  def operation[T](operate: EntranceExecutor => T): T = {
    this.getExecutor match {
      case entranceExecutor: EntranceExecutor =>
        operate(entranceExecutor)
      case _ =>
        throw new EntranceErrorException(
          UNSUPPORTED_OPERATION.getErrorCode,
          UNSUPPORTED_OPERATION.getErrorDesc
        )
    }

  }

  /*
  Update old status of internal jobRequest.
  if old status is complete, will not update the status
  if index of new status is bigger then old status, then update the old status to new status
   */
  def updateJobRequestStatus(newStatus: String): Unit = {
    val oriStatus = {
      if (StringUtils.isNotBlank(getJobRequest.getStatus)) {
        SchedulerEventState.withName(getJobRequest.getStatus)
      } else {
        SchedulerEventState.Inited
      }
    }
    if (StringUtils.isNotBlank(newStatus)) {
      Utils.tryCatch {
        val tmpStatus = SchedulerEventState.withName(newStatus)
        if (
            SchedulerEventState
              .isCompleted(oriStatus) && !SchedulerEventState.Cancelled.equals(tmpStatus)
        ) {
          logger.warn(
            s"Job ${getJobRequest.getId} status : ${getJobRequest.getStatus} is completed, will not change to : $newStatus"
          )
          return
        }
        if (tmpStatus.id > oriStatus.id) {
          getJobRequest.setStatus(tmpStatus.toString)
        } else {
          logger.warn(
            s"Job ${getJobRequest.getId} 's index of status : ${oriStatus.toString} is not smaller then new status : ${newStatus}, will not change status."
          )
        }
      } { case e: Exception =>
        logger.error(s"Invalid job status : ${newStatus}, ${e.getMessage}")
        return
      }
    } else {
      logger.error("Invalid job status : null")
    }
  }

}

object EntranceJob {

  def JOB_COMPLETED_PROGRESS: Float = 1.0f

}
