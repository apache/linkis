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

import java.util
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.entrance.EntranceContext
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.event._
import org.apache.linkis.entrance.exception.EntranceErrorException
import org.apache.linkis.entrance.persistence.HaPersistenceTask
import org.apache.linkis.governance.common.entity.job.{JobRequest, SubJobInfo}
import org.apache.linkis.governance.common.entity.task.RequestPersistTask
import org.apache.linkis.governance.common.paser.CodeParser
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.protocol.task.Task
import org.apache.linkis.rpc.utils.RPCUtils
import org.apache.linkis.scheduler.executer.{CompletedExecuteResponse, ErrorExecuteResponse, SuccessExecuteResponse}
import org.apache.linkis.scheduler.queue.SchedulerEventState._
import org.apache.linkis.scheduler.queue.{Job, SchedulerEventState}
import org.apache.commons.lang.StringUtils

import scala.beans.BeanProperty


abstract class EntranceJob extends Job {

  @BeanProperty
  var creator: String = _
  @BeanProperty
  var user: String = _
  @BeanProperty
  var params: util.Map[String, Any] = new util.HashMap[String, Any](1)
  @BeanProperty
  var jobRequest: JobRequest = _
  @BeanProperty
  var jobGroups: Array[SubJobInfo] = new Array[SubJobInfo](0)
  @BeanProperty
  var codeParser: CodeParser = _

  private var entranceListenerBus: Option[EntranceEventListenerBus[EntranceEventListener, EntranceEvent]] = None
  private var progressInfo: Array[JobProgressInfo] = Array.empty
  private val persistedResultSets = new AtomicInteger(0)
//  private var resultSize = -1
  private var entranceContext: EntranceContext = _

  def setEntranceListenerBus(entranceListenerBus: EntranceEventListenerBus[EntranceEventListener, EntranceEvent]): Unit =
    this.entranceListenerBus = Option(entranceListenerBus)

  def setProgressInfo(progressInfo: Array[JobProgressInfo]): Unit = this.progressInfo = progressInfo

  def getProgressInfo: Array[JobProgressInfo] = this.progressInfo

  def setEntranceContext(entranceContext: EntranceContext): Unit = this.entranceContext = entranceContext

  def getEntranceContext: EntranceContext = this.entranceContext

  def getRunningSubJob: SubJobInfo = {
    if (null != jobGroups && jobGroups.size > 0) {
      jobGroups(0)
    } else {
      null
    }
  }

  def setResultSize(resultSize: Int): Unit = {
//    this.resultSize = resultSize
    if (resultSize >= 0) {
      persistedResultSets.set(resultSize)
    }
  }

  def addAndGetResultSize(resultSize: Int): Int = {
    info(s"Job ${getJobRequest.getId} resultsize from ${persistedResultSets.get()} add ${resultSize}")
    if (resultSize > 0) {
      persistedResultSets.addAndGet(resultSize)
    } else {
      persistedResultSets.get()
    }
  }

  @Deprecated
  def incrementResultSetPersisted(): Unit = {
//    persistedResultSets.incrementAndGet()
  }

  protected def isWaitForPersistedTimeout(startWaitForPersistedTime: Long): Boolean =
    System.currentTimeMillis - startWaitForPersistedTime >= EntranceConfiguration.JOB_MAX_PERSIST_WAIT_TIME.getValue.toLong


  override def beforeStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {
//    if (SchedulerEventState.isCompleted(toState) && (resultSize < 0 || persistedResultSets.get() < resultSize)) {
      /*val startWaitForPersistedTime = System.currentTimeMillis
      persistedResultSets synchronized {
        while ((resultSize < 0 || persistedResultSets.get() < resultSize) && getErrorResponse == null && !isWaitForPersistedTimeout(startWaitForPersistedTime))
          persistedResultSets.wait(3000)
      }
      if (isWaitForPersistedTimeout(startWaitForPersistedTime)) onFailure("persist resultSets timeout!", new EntranceErrorException(20305, "persist resultSets timeout!"))
      if (isSucceed && getErrorResponse != null) {
        val _toState = if (getErrorResponse.t == null) Cancelled else Failed
        transition(_toState)
        return
      }*/
//    }
    super.beforeStateChanged(fromState, toState)
  }

  override def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {
    //updateJobRequestStatus(toState.toString)
    super.afterStateChanged(fromState, toState)
    toState match {
      case Scheduled =>
        //Entrance指标：任务排队结束时间
        if(getJobRequest.getMetrics == null){
          getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateWarn("Job Metrics has not been initialized.")))
        }else{
          if(getJobRequest.getMetrics.containsKey(TaskConstant.ENTRANCEJOB_SCHEDULE_TIME)){
            getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateWarn("Your job has already been scheduled before.")))
          }else{
            getJobRequest.getMetrics.put(TaskConstant.ENTRANCEJOB_SCHEDULE_TIME, new Date(System.currentTimeMillis))
          }
        }
        getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateInfo("Your job is Scheduled. Please wait it to run.")))
      case WaitForRetry =>
        getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateInfo("Your job is turn to retry. Please wait it to schedule.")))
      case Running =>
        getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateInfo("Your job is Running now. Please wait it to complete.")))
      //TODO job start event
      case _ if SchedulerEventState.isCompleted(toState) =>
        endTime = System.currentTimeMillis()
        //Entrance指标，任务完成时间
        getJobRequest.getMetrics.put(TaskConstant.ENTRANCEJOB_COMPLETE_TIME, new Date(System.currentTimeMillis()))
        if (getJobInfo != null) getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateInfo(getJobInfo.getMetric)))
        if (isSucceed)
          getLogListener.foreach(_.onLogUpdate(this,
            LogUtils.generateInfo("Congratulations. Your job completed with status Success.")))
        else getLogListener.foreach(_.onLogUpdate(this,
          LogUtils.generateInfo(s"Sorry. Your job completed with a status $toState. You can view logs for the reason.")))
        this.setProgress(EntranceJob.JOB_COMPLETED_PROGRESS)
        entranceListenerBus.foreach(_.post(EntranceProgressEvent(this, EntranceJob.JOB_COMPLETED_PROGRESS, this.getProgressInfo)))
        this.getProgressListener.foreach(listener => listener.onProgressUpdate(this, EntranceJob.JOB_COMPLETED_PROGRESS, Array[JobProgressInfo]()))
        getEntranceContext.getOrCreatePersistenceManager().createPersistenceEngine().updateIfNeeded(getJobRequest)
      case _ =>
    }
    entranceListenerBus.foreach(_.post(EntranceJobEvent(this.getId)))
  }

  override def onFailure(errorMsg: String, t: Throwable): Unit = {
    //updateJobRequestStatus(SchedulerEventState.Failed.toString)
    val generatedMsg = LogUtils.generateERROR(s"Sorry, your job executed failed with reason: $errorMsg")
    getLogListener.foreach(_.onLogUpdate(this, generatedMsg))
    super.onFailure(errorMsg, t)
  }

  override protected def transitionCompleted(executeCompleted: CompletedExecuteResponse): Unit = {
    Utils.tryAndErrorMsg(clearInstanceInfo())("Failed to clear executor")
    super.transitionCompleted(executeCompleted)
  }

  private def clearInstanceInfo(): Unit = {
    val executorManager = entranceContext.getOrCreateScheduler().getSchedulerContext.getOrCreateExecutorManager
    executorManager.delete(getExecutor)
  }

  def transitionCompleted(executeCompleted: CompletedExecuteResponse, reason: String): Unit = {
    debug("Job directly completed with reason: " + reason)
    transitionCompleted(executeCompleted)
  }

  override protected def isJobShouldRetry(errorExecuteResponse: ErrorExecuteResponse): Boolean = isJobSupportRetry && errorExecuteResponse != null &&
    (if (RPCUtils.isReceiverNotExists(errorExecuteResponse.t)) {
      getExecutor match {
        case e: EntranceExecutor =>
//          val instance = e.getInstance.getInstance
          getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateSystemWarn(s"Since the submitted engine rejects the connection, the system will automatically retry and exclude the engine.(由于提交的引擎拒绝连接，系统将自动进行重试，并排除引擎.)")))
        case _ =>
      }
      true
    } else super.isJobShouldRetry(errorExecuteResponse))

  def operation[T](operate: EntranceExecutor => T ): T = {
    this.getExecutor match {
      case entranceExecutor: EntranceExecutor =>
        operate(entranceExecutor)
      case _ => throw new EntranceErrorException(10000, "Unsupported operation (不支持的操作)")
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
      Utils.tryCatch{
        val tmpStatus = SchedulerEventState.withName(newStatus)
        if (SchedulerEventState.isCompleted(oriStatus) && !SchedulerEventState.Cancelled.equals(tmpStatus)) {
          warn(s"Job ${getJobRequest.getId} status : ${getJobRequest.getStatus} is completed, will not change to : $newStatus")
          return
        }
        if (tmpStatus.id > oriStatus.id) {
          getJobRequest.setStatus(tmpStatus.toString)
        } else {
          warn(s"Job ${getJobRequest.getId} 's index of status : ${oriStatus.toString} is not smaller then new status : ${newStatus}, will not change status.")
        }
      } {
        case e: Exception =>
          error(s"Invalid job status : ${newStatus}, ${e.getMessage}")
          return
      }
    } else {
      error("Invalid job status : null")
    }
  }
}

object EntranceJob {

  def JOB_COMPLETED_PROGRESS = 1.0f

}