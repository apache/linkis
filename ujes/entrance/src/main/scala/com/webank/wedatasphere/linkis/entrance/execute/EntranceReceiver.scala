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

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.EntranceContext
import com.webank.wedatasphere.linkis.entrance.annotation.EntranceContextBeanAnnotation
import com.webank.wedatasphere.linkis.entrance.event.EntranceProgressEvent
import com.webank.wedatasphere.linkis.entrance.utils.RPCUtils
import com.webank.wedatasphere.linkis.protocol.engine._
import com.webank.wedatasphere.linkis.rpc.exception.DWCRPCRetryException
import com.webank.wedatasphere.linkis.rpc.{Receiver, Sender}
import com.webank.wedatasphere.linkis.scheduler.executer.AliasOutputExecuteResponse
import com.webank.wedatasphere.linkis.scheduler.queue.{Job, SchedulerEventState}
import javax.annotation.PostConstruct

import scala.concurrent.duration.Duration

/**
  * Created by enjoyyin on 2018/9/11.
  */
class EntranceReceiver extends Receiver with Logging {
  @EntranceContextBeanAnnotation.EntranceContextAutowiredAnnotation
  private var entranceContext: EntranceContext = _

  def this(entranceContext: EntranceContext) {
    this()
    this.entranceContext = entranceContext
    init()
  }

  @PostConstruct
  def init(): Unit = {
    getEntranceExecutorManager.getOrCreateEngineManager().readAliveEngines()
  }

  private[execute] def getEntranceExecutorManager = entranceContext.getOrCreateScheduler().getSchedulerContext
    .getOrCreateExecutorManager.asInstanceOf[EntranceExecutorManager]

  private def findEngineExecuteAsynReturn(execId: String, sender: Sender, message: String): Option[EngineExecuteAsynReturn] = {
    val serviceInstance = RPCUtils.getServiceInstance(sender)
    if(serviceInstance == null || serviceInstance.getInstance == null) {
      warn(s"because of not support sender $sender, cannot update the message $message for execId $execId.")
      return None
    }
    val engine = getEntranceExecutorManager.getOrCreateEngineManager().get(serviceInstance.getInstance)
    if(engine.isEmpty) {
      warn(s"${getEntranceExecutorManager.getOrCreateEngineManager().getClass.getSimpleName} cannot find a entranceEngine to update the message $message for execId $execId from $sender.")
      None
    } else {
      val jobReturn = engine.flatMap(_.getEngineReturns.find(_.execId == execId))
      if(jobReturn.isEmpty) warn(s"${engine.get} cannot find a job to update the message $message for execId $execId from $sender, EngineReturns List is ${engine.map(_.getEngineReturns.map(_.execId).toList).get}.")
      jobReturn
    }
  }

  def onOperate(execId: String, sender: Sender, op: Job => Unit, message: String): Unit = findEngineExecuteAsynReturn(execId, sender, message).foreach { er =>
    er.notifyHeartbeat()
    er.getJobId.flatMap(entranceContext.getOrCreateScheduler().get).foreach {
      case job: Job => op(job)
      case _ =>
    }
  }

  override def receive(message: Any, sender: Sender): Unit = message match {
    case res: ResponseTaskStatus =>
      val state = SchedulerEventState(res.state)
      if(SchedulerEventState.isCompleted(state))
        info(s"The execId ${res.execId} from engine $sender is completed with state $state.")
      findEngineExecuteAsynReturn(res.execId, sender, s"ResponseTaskStatus(${res.execId}, ${res.state})")
        .foreach(_.notifyStatus(res))
    case ResponseTaskError(execId, errorMsg) =>
      findEngineExecuteAsynReturn(execId, sender, "ResponseTaskError").foreach(_.notifyError(errorMsg))
    case ResponseTaskProgress(execId, progress, progressInfo) =>
      Utils.tryAndWarn {
        onOperate(execId, sender, job => {
          entranceContext.getOrCreateEventListenerBus.post(EntranceProgressEvent(job, progress, progressInfo))
          entranceContext.getOrCreatePersistenceManager().onProgressUpdate(job, progress, progressInfo)
          job.asInstanceOf[EntranceJob].setProgressInfo(progressInfo)
        }, "ResponseTaskProgress")
      }
    case ResponseTaskLog(execId, log) =>
      onOperate(execId, sender, entranceContext.getOrCreateLogManager().onLogUpdate(_, log), "ResponseTaskLog")
    case ResponseTaskResultSet(execId, output, alias) =>
      onOperate(execId, sender, entranceContext.getOrCreatePersistenceManager().onResultSetCreated(_, AliasOutputExecuteResponse(alias, output)), "ResponseTaskResultSet")
    case ResponseTaskResultSize(execId, resultSize) =>
      onOperate(execId, sender, entranceContext.getOrCreatePersistenceManager().onResultSizeCreated(_, resultSize), "ResponseTaskResultSize")

    case responseNewEngineStatus: ResponseNewEngineStatus =>
      getEntranceExecutorManager.getOrCreateEngineRequester().reportNewEngineStatus(responseNewEngineStatus)
    case responseNewEngine: ResponseNewEngine =>
      if(getEntranceExecutorManager.getOrCreateEngineManager().listEngines(_.getModuleInstance.getInstance == responseNewEngine.instance).isEmpty)
        getEntranceExecutorManager.getOrCreateEngineRequester().reportNewEngine(responseNewEngine)
     }

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case res: ResponseTaskStatus =>
      val state = SchedulerEventState(res.state)
      if(SchedulerEventState.isCompleted(state))
        info(s"The execId ${res.execId} from engine $sender is completed with state $state.")
      askRetryWhenExecuteAsynReturnIsEmpty(res.execId, sender, s"ResponseTaskStatus(${res.execId}, ${res.state})")
        .notifyStatus(res)
    case ResponseTaskResultSize(execId, resultSize) =>
      askRetryWhenExecuteAsynReturnIsEmpty(execId, sender, "ResponseTaskResultSize")
        .getJobId.flatMap(entranceContext.getOrCreateScheduler().get).foreach{
          case j: Job => entranceContext.getOrCreatePersistenceManager().onResultSizeCreated(j, resultSize)
          case _ =>
        }
  }

  private def askRetryWhenExecuteAsynReturnIsEmpty(execId: String, sender: Sender, message: String) =
    findEngineExecuteAsynReturn(execId, sender, message).getOrElse {
      Utils.tryQuietly(Thread.sleep(50))
      findEngineExecuteAsynReturn(execId, sender, toString)
        .getOrElse(throw new DWCRPCRetryException(s"please retry, since $execId cannot be consumed by entrance!"))
    }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {}
}
