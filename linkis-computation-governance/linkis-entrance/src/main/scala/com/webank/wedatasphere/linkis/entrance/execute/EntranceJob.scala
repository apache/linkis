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

package com.webank.wedatasphere.linkis.entrance.execute

import java.util
import java.util.concurrent.atomic.AtomicInteger

import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.entrance.EntranceContext
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.event._
import com.webank.wedatasphere.linkis.entrance.exception.EntranceErrorException
import com.webank.wedatasphere.linkis.entrance.persistence.HaPersistenceTask
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.protocol.task.Task
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.rpc.utils.RPCUtils
import com.webank.wedatasphere.linkis.scheduler.executer.{CompletedExecuteResponse, ErrorExecuteResponse}
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState._
import com.webank.wedatasphere.linkis.scheduler.queue.{LockJob, SchedulerEventState}

/**
  * Created by enjoyyin on 2018/9/26.
  */
abstract class EntranceJob extends LockJob {

  private var creator: String = _
  private var user: String = _
  private var params: util.Map[String, Any] = new util.HashMap[String, Any](1)
  private var task:Task = _
  //entranceListenerBus may not exist(entranceListenerBus可能不存在)
  private var entranceListenerBus: Option[EntranceEventListenerBus[EntranceEventListener, EntranceEvent]] = None
  private var progressInfo:Array[JobProgressInfo] = Array.empty
  private val persistedResultSets = new AtomicInteger(0)
  private var resultSize = -1
  private var entranceContext:EntranceContext = _
  def getTask:Task = task
  def setTask(task:Task):Unit = this.task = task
  def setCreator(creator: String): Unit = this.creator = creator
  def getCreator:String = creator
  def setUser(user: String):Unit = this.user = user
  def getUser:String = user
  def setParams(params: util.Map[String, Any]):Unit = this.params = params
  def getParams:util.Map[String, Any] = params
  def setEntranceListenerBus(entranceListenerBus: EntranceEventListenerBus[EntranceEventListener, EntranceEvent]):Unit =
    this.entranceListenerBus = Option(entranceListenerBus)
  def getEntranceListenerBus = this.entranceListenerBus
  def setProgressInfo(progressInfo:Array[JobProgressInfo]):Unit = this.progressInfo = progressInfo
  def getProgressInfo:Array[JobProgressInfo] = this.progressInfo
  def setEntranceContext(entranceContext: EntranceContext):Unit = this.entranceContext = entranceContext
  def getEntraceCotnext:EntranceContext = this.entranceContext


  def setResultSize(resultSize: Int): Unit = {
    this.resultSize = resultSize
    persistedResultSets synchronized persistedResultSets.notify()
  }
  def incrementResultSetPersisted(): Unit = {
    persistedResultSets.incrementAndGet()
    persistedResultSets synchronized persistedResultSets.notify()
  }

  protected def isWaitForPersistedTimeout(startWaitForPersistedTime: Long): Boolean =
    System.currentTimeMillis - startWaitForPersistedTime >= EntranceConfiguration.JOB_MAX_PERSIST_WAIT_TIME.getValue.toLong

  override def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = {
    if(SchedulerEventState.isCompleted(toState) && (resultSize < 0 || persistedResultSets.get() < resultSize)) {
      val startWaitForPersistedTime = System.currentTimeMillis
      persistedResultSets synchronized {
        while((resultSize < 0 || persistedResultSets.get() < resultSize) && getErrorResponse == null && !isWaitForPersistedTimeout(startWaitForPersistedTime))
          persistedResultSets.wait(3000)
      }
      if(isWaitForPersistedTimeout(startWaitForPersistedTime)) onFailure("persist resultSets timeout!", new EntranceErrorException(20305, "persist resultSets timeout!"))
      if(isSucceed && getErrorResponse != null){
        val _toState = if(getErrorResponse.t == null) Cancelled else Failed
        transition(_toState)
        return
      }
    }
    if(SchedulerEventState.isRunning(toState)) {
      def setEngineInstance(task: Task): Unit = task match {
        case requestTask: RequestPersistTask => getExecutor match {
          case engine: EntranceEngine => requestTask.setEngineInstance(engine.getModuleInstance.getInstance)
          case _ =>
        }
        case haTask: HaPersistenceTask => setEngineInstance(haTask.task)
        case _ =>
      }
      setEngineInstance(task)
    }
    super.afterStateChanged(fromState, toState)
    toState match {
      case Scheduled =>
        getLogListener.foreach(_.onLogUpdate(this,  LogUtils.generateInfo( "Your job is Scheduled. Please wait it to run.")))
      case WaitForRetry =>
        getLogListener.foreach(_.onLogUpdate(this,  LogUtils.generateInfo( "Your job is turn to retry. Please wait it to schedule.")))
      case Running =>
        getLogListener.foreach(_.onLogUpdate(this,  LogUtils.generateInfo( "Your job is Running now. Please wait it to complete.")))
      //TODO job start event
      case _ if SchedulerEventState.isCompleted(toState) =>
        endTime = System.currentTimeMillis()
        if(getJobInfo != null) getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateInfo(getJobInfo.getMetric)))
        if(isSucceed)
          getLogListener.foreach(_.onLogUpdate(this,
            LogUtils.generateInfo( "Congratulations. Your job completed with status Success.")))
        else getLogListener.foreach(_.onLogUpdate(this,
          LogUtils.generateInfo( s"Sorry. Your job completed with a status $toState. You can view logs for the reason.")))
        this.setProgress(1.0f)
        entranceListenerBus.foreach(_.post(EntranceProgressEvent(this, 1.0f, this.getProgressInfo)))
        this.getProgressListener.foreach(listener => listener.onProgressUpdate(this, 1.0f, Array[JobProgressInfo]()))
      case _ =>
    }
    entranceListenerBus.foreach(_.post(EntranceJobEvent(this.getId)))
  }

  override def onFailure(errorMsg: String, t: Throwable): Unit = {
    this.entranceListenerBus.foreach(_.post(
      EntranceLogEvent(this, LogUtils.generateERROR(s"Sorry, your job executed failed with reason: $errorMsg"))))
    super.onFailure(errorMsg, t)
  }

  override protected def transitionCompleted(executeCompleted: CompletedExecuteResponse): Unit = {
    executeCompleted match {
      case error: ErrorExecuteResponse if RPCUtils.isReceiverNotExists(error.t) =>
        entranceListenerBus.foreach(_.post(MissingEngineNotifyEvent(this, error.t, getExecutor)))
      case _ =>
    }
    super.transitionCompleted(executeCompleted)
  }

  def transitionCompleted(executeCompleted: CompletedExecuteResponse, reason: String): Unit = {
    info("Job directly completed with reason: " + reason)
    transitionCompleted(executeCompleted)
  }

  override protected def isJobShouldRetry(errorExecuteResponse: ErrorExecuteResponse): Boolean = isJobSupportRetry && errorExecuteResponse != null &&
    (if(RPCUtils.isReceiverNotExists(errorExecuteResponse.t)) {
      getExecutor match {
        case e: EntranceEngine =>
          val instance = e.getModuleInstance.getInstance
          getLogListener.foreach(_.onLogUpdate(this, LogUtils.generateSystemWarn(s"Since the submitted engine rejects the connection, the system will automatically retry and exclude the engine $instance.(由于提交的引擎拒绝连接，系统将自动进行重试，并排除引擎 $instance.)")))
          val specialMap = TaskUtils.getSpecialMap(getParams)
          if(specialMap.isEmpty) {
            TaskUtils.addSpecialMap(getParams, specialMap)
            specialMap.put(ExceptInstanceEntranceExecutorRuler.EXCEPT_INSTANCES, instance)
          } else if(specialMap.containsKey(ExceptInstanceEntranceExecutorRuler.EXCEPT_INSTANCES)) {
            val instances = ExceptInstanceEntranceExecutorRuler.deserializable(specialMap.get(ExceptInstanceEntranceExecutorRuler.EXCEPT_INSTANCES).toString) :+ instance
            specialMap.put(ExceptInstanceEntranceExecutorRuler.EXCEPT_INSTANCES, ExceptInstanceEntranceExecutorRuler.serializable(instances))
          } else specialMap.put(ExceptInstanceEntranceExecutorRuler.EXCEPT_INSTANCES, instance)
        case _ =>
      }
      true
    } else super.isJobShouldRetry(errorExecuteResponse))
}
