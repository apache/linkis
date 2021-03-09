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

import java.net.ConnectException

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.exception.WarnException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration.{ENGINE_SPRING_APPLICATION_NAME, JOB_STATUS_HEARTBEAT_TIME}
import com.webank.wedatasphere.linkis.entrance.exception.EntranceErrorException
import com.webank.wedatasphere.linkis.protocol.UserWithCreator
import com.webank.wedatasphere.linkis.protocol.engine._
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState._
import com.webank.wedatasphere.linkis.scheduler.executer._
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState._
import com.webank.wedatasphere.linkis.scheduler.queue.{Group, SchedulerEventState}
import org.apache.commons.lang.exception.ExceptionUtils

import scala.collection.mutable.ArrayBuffer

/**
  * Created by enjoyyin on 2018/9/10.
  */
abstract class EntranceEngine(id: Long) extends AbstractExecutor(id) with Logging {

  private var group: Group = _
  private implicit var userWithCreator: UserWithCreator = _
  private var serviceInstance: ServiceInstance = _
  protected var sender: Sender = _
  protected var engineLockListener: Option[EngineLockListener] = None
  protected val engineReturns = ArrayBuffer[EngineExecuteAsynReturn]()
  protected var interceptors: Array[ExecuteRequestInterceptor] = Array(LockExecuteRequestInterceptor, JobExecuteRequestInterceptor)

  private var overloadInfo: Option[EngineOverloadInfo] = None
  private var concurrentInfo: Option[EngineConcurrentInfo] = None

  def getOverloadInfo = overloadInfo
  def getConcurrentInfo = concurrentInfo

  def setInterceptors(interceptors: Array[ExecuteRequestInterceptor]) = if(interceptors != null && interceptors.nonEmpty) {
    this.interceptors = interceptors
  }

  def setGroup(group: Group): Unit = this.group = group
  def getGroup: Group = group

  def setUser(user: String): Unit = userWithCreator = if(userWithCreator != null) UserWithCreator(user, userWithCreator.creator)
    else UserWithCreator(user, null)
  def getUser = if(userWithCreator != null) userWithCreator.user else null
  def setCreator(creator: String): Unit = userWithCreator = if(userWithCreator != null) UserWithCreator(userWithCreator.user, creator)
    else UserWithCreator(null, creator)
  def getCreator = if(userWithCreator != null) userWithCreator.creator else null

  def setServiceInstance(applicationName: String, instance: String): Unit = if(serviceInstance == null) {
    serviceInstance = ServiceInstance(applicationName, instance)
    sender = Sender.getSender(serviceInstance)
  }

  def setInstance(instance: String): Unit = setServiceInstance(ENGINE_SPRING_APPLICATION_NAME.getValue, instance)

  def getModuleInstance: ServiceInstance = serviceInstance

  def setEngineLockListener(engineLockListener: EngineLockListener) =
    this.engineLockListener = Some(engineLockListener)

  def updateState(fromState: ExecutorState, toState: ExecutorState,
                  overloadInfo: EngineOverloadInfo, concurrentInfo: EngineConcurrentInfo): Unit = {
    warn(s"receive a stateChanged event for engine $this, from state $fromState to state $toState.")
    this.concurrentInfo = Option(concurrentInfo)
    this.overloadInfo = Option(overloadInfo)
    toState match {
      case ExecutorState.ShuttingDown => transition(toState)
      case _ if ExecutorState.isCompleted(toState) => transition(toState)
      case _ => changeState(fromState, toState)
    }
  }

  protected def changeState(fromState: ExecutorState, toState: ExecutorState): Unit = whenState(fromState, transition(toState))

  private[execute] def getEngineReturns = engineReturns.toArray

  override def execute(executeRequest: ExecuteRequest): ExecuteResponse = {
    var request: RequestTask = null
    interceptors.foreach(in => request = in.apply(request, executeRequest))
    //TODO The implementation of the HA function, you need to think about it again.(HA功能的实现，还需要再考虑一下)
    val engineReturn = if(request.getProperties != null &&
      request.getProperties.containsKey(ReconnectExecuteRequestInterceptor.PROPERTY_EXEC_ID)) ensureBusy {
      sender.ask(RequestTaskStatus(request.getProperties.get(ReconnectExecuteRequestInterceptor.PROPERTY_EXEC_ID).toString)) match {
        case ResponseTaskStatus(execId, _) =>
          new EngineExecuteAsynReturn(request, serviceInstance.getInstance, execId, _ => callback())
      }
    } else callExecute(request)
    engineReturns synchronized engineReturns += engineReturn
    engineReturn
  }

  override protected def callback(): Unit = {}

  protected def callExecute(request: RequestTask): EngineExecuteAsynReturn

  def callReconnect(): Unit = whenBusy {
    engineReturns.filter(_.getLastNotifyTime < System.currentTimeMillis - JOB_STATUS_HEARTBEAT_TIME.getValue.toLong).foreach { er =>
     Utils.tryCatch(sender.ask(RequestTaskStatus(er.execId)) match {
        case r: ResponseTaskStatus =>
          er.notifyStatus(r)
        case we: WarnException =>
          //Warn defaults to already executed(Warn默认为已经执行完成)
          warn(s"heartbeat to engine $toString caused a warn, I trust it completed and mark to Failed!", we)
          er.notifyError(we.getMessage, we)
          er.notifyStatus(ResponseTaskStatus(null, Failed.id))
      }
     ){t =>
       warn(s"Heartbeat to engine $toString caused a error, I think it is failed!", t)
       er.notifyError(s"Heartbeat to engine $toString caused a error, I think it is failed!", t)
       er.notifyStatus(ResponseTaskStatus(null, Failed.id))
     }
    }
  }

  def refreshState(): Unit = sender.ask(RequestEngineStatus(RequestEngineStatus.Status_Overload_Concurrent)) match {
      case ResponseEngineStatus(_, s, overload, concurrent, _) =>
        warn(s"heartbeat to engine $toString, refresh its state to ${ExecutorState(s)}.")
        updateState(state, ExecutorState(s), overload, concurrent)
      case warn: WarnException =>
        this.warn(s"heartbeat to engine $serviceInstance caused a warn, refresh its state failed!", warn)
  }

  override def toString: String = s"${serviceInstance.getApplicationName}Engine($getId, $getUser, $getCreator, ${serviceInstance.getInstance})"

  protected def killExecId(execId: String): Boolean = {
    info(s"begin to send RequestTaskKill to engine, execID: $execId")
    Utils.tryThrow(sender.send(RequestTaskKill(execId))) {
      case t: ConnectException =>
        engineReturns.find(_.execId == execId).foreach{ response =>
          response.notifyError("engine has dead with unknown reasons. Ask admin for more information.", t)
          response.notifyStatus(ResponseTaskStatus(execId, SchedulerEventState.Failed.id))
        }
        new EntranceErrorException(20400, "kill failed! reason: engine has already dead.").initCause(t)
      case t =>
        new EntranceErrorException(20401, s"kill failed. reason: " + ExceptionUtils.getRootCauseMessage(t)).initCause(t)
    }
    true
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[EntranceEngine]

  override def equals(other: Any): Boolean = other match {
    case that: EntranceEngine =>
      (that canEqual this) &&
        serviceInstance == that.serviceInstance
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(serviceInstance)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

class EngineExecuteAsynReturn(request: RequestTask, val instance: String,
                              val execId: String, callback: EngineExecuteAsynReturn => Unit) extends AsynReturnExecuteResponse with Logging {
  getJobId.foreach(id => info("Job " + id + " received a execId " + execId + " from engine " + instance))
  private var notifyJob: ExecuteResponse => Unit = _
  private var error: Throwable = _
  private var errorMsg: String = _
  private var lastNotifyTime = System.currentTimeMillis
  def getLastNotifyTime = lastNotifyTime
  private[execute] def notifyStatus(responseEngineStatus: ResponseTaskStatus): Unit = {
    lastNotifyTime = System.currentTimeMillis()
    val response = if(responseEngineStatus.state > SchedulerEventState.maxId) Some(IncompleteExecuteResponse(errorMsg))
    else SchedulerEventState(responseEngineStatus.state) match {
        case Succeed => Some(SuccessExecuteResponse())
        case Failed | Cancelled | Timeout => Some(ErrorExecuteResponse(errorMsg, error))
        case _ => None
    }
    response.foreach{ r =>
      getJobId.foreach(id => info("Job " + id + " with execId-" + execId + " from engine " + instance + " completed with state " + r))
      callback(this)
      if(notifyJob == null) this synchronized(while(notifyJob == null) this.wait(1000))
      notifyJob(r)
    }
  }
  private[execute] def notifyHeartbeat(): Unit = {
    lastNotifyTime = System.currentTimeMillis()
  }
  private[execute] def notifyError(errorMsg: String): Unit = {
    lastNotifyTime = System.currentTimeMillis()
    this.errorMsg = errorMsg
  }
  private[execute] def notifyError(errorMsg: String, t: Throwable): Unit = {
    lastNotifyTime = System.currentTimeMillis()
    this.errorMsg = errorMsg
    this.error = t
  }
  private[execute] def getJobId: Option[String] = {
    val jobId = request.getProperties.get(JobExecuteRequestInterceptor.PROPERTY_JOB_ID)
    jobId match {
      case j: String => Option(j)
      case _ => None
    }
  }
  override def notify(rs: ExecuteResponse => Unit): Unit = {
    notifyJob = rs
    this synchronized notify()
  }
}