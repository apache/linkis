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

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.EntranceContext
import com.webank.wedatasphere.linkis.entrance.annotation.EntranceContextBeanAnnotation
import com.webank.wedatasphere.linkis.entrance.conf.{EntranceConfiguration, EntranceConstant}
import com.webank.wedatasphere.linkis.entrance.event.EntranceProgressEvent
import com.webank.wedatasphere.linkis.governance.common.conf.GovernanceCommonConf
import com.webank.wedatasphere.linkis.governance.common.entity.{ExecutionNodeStatus, NodeExistStatus}
import com.webank.wedatasphere.linkis.governance.common.protocol.engineconn.{RequestEngineStatusBatch, ResponseEngineStatusBatch}
import com.webank.wedatasphere.linkis.governance.common.protocol.task._
import com.webank.wedatasphere.linkis.governance.common.utils.GovernanceConstant
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.protocol.node.{RequestNodeStatus, ResponseNodeStatus}
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext
import com.webank.wedatasphere.linkis.orchestrator.ecm.service.{EngineConnExecutor, TaskExecutionReceiver}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.rpc.exception.DWCRPCRetryException
import com.webank.wedatasphere.linkis.rpc.utils.RPCUtils
import com.webank.wedatasphere.linkis.scheduler.executer.AliasOutputExecuteResponse
import com.webank.wedatasphere.linkis.scheduler.queue.Job
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import scala.collection.JavaConverters._
import java.util
import java.util.concurrent.TimeUnit


@Service
class EntranceExecutionService extends TaskExecutionReceiver with Logging {

  @EntranceContextBeanAnnotation.EntranceContextAutowiredAnnotation
  private var entranceContext: EntranceContext = _

  @PostConstruct
  private def init(): Unit = {
    addEngineExecutorStatusMonitor()
  }

  private[execute] def getEntranceExecutorManager = entranceContext.getOrCreateScheduler().getSchedulerContext
    .getOrCreateExecutorManager.asInstanceOf[EntranceExecutorManager]

  private def findEngineExecuteAsyncReturn(execId: String, sender: Sender, message: String): Option[EngineExecuteAsynReturn] = {
    val serviceInstance = RPCUtils.getServiceInstanceFromSender(sender)
    if (serviceInstance == null || serviceInstance.getInstance == null) {
      warn(s"because of not support sender $sender, cannot update the message $message for execId $execId.")
      return None
    }
    val engine = getEntranceExecutorManager.getEntranceExecutorByInstance(serviceInstance.getInstance)
    if (engine.isEmpty) {
      warn(s"${getEntranceExecutorManager.getClass.getSimpleName} cannot find a entranceEngine to update the message $message for execId $execId from $sender.")
      None
    } else {
      val jobReturn = engine.flatMap(_.getEngineReturns.find(_.execId == execId))
      if (jobReturn.isEmpty) warn(s"${engine.get} cannot find a job to update the message $message for execId $execId from $sender, EngineReturns List is ${engine.map(_.getEngineReturns.map(_.execId).toList).get}.")
      jobReturn
    }
  }

  def onOperate(execId: String, sender: Sender, op: Job => Unit, message: String): Unit = findEngineExecuteAsyncReturn(execId, sender, message).foreach { er =>
    er.notifyHeartbeat()
    er.getJobId.flatMap(entranceContext.getOrCreateScheduler().get).foreach {
      case job: Job => op(job)
      case _ =>
    }
  }

  private def askRetryWhenExecuteAsyncReturnIsEmpty(execId: String, sender: Sender, message: String) =
    findEngineExecuteAsyncReturn(execId, sender, message).getOrElse {
      Utils.tryQuietly(Thread.sleep(50))
      findEngineExecuteAsyncReturn(execId, sender, toString)
        .getOrElse(throw new DWCRPCRetryException(s"please retry, since $execId cannot be consumed by entrance!"))
    }

  @Receiver
  override def taskLogReceiver(taskLog: ResponseTaskLog, smc: ServiceMethodContext): Unit = {
    val sender = smc.getSender
    onOperate(taskLog.execId, sender, entranceContext.getOrCreateLogManager().onLogUpdate(_, taskLog.log), "ResponseTaskLog")
    updateExecutorActivityTime(RPCUtils.getServiceInstanceFromSender(sender))
  }

  private def updateExecutorActivityTime(serviceInstance: ServiceInstance) = {
    if (null != serviceInstance) {
      val engineConnExecutor = getEntranceExecutorManager.engineConnManager.getEngineConnExecutorCache().getOrDefault(serviceInstance, null)
      if (null != engineConnExecutor) synchronized {
        engineConnExecutor.updateLastUpdateTime()
      } else {
        warn(s"EngineConnExecutor ${serviceInstance.toString} cannot be found in engineConnExecutorCache")
      }
    }
  }

  @Receiver
  override def taskProgressReceiver(taskProgress: ResponseTaskProgress, smc: ServiceMethodContext): Unit = {
    val sender = smc.getSender
    Utils.tryAndWarn {
      onOperate(taskProgress.execId, sender, job => {
        entranceContext.getOrCreateEventListenerBus.post(EntranceProgressEvent(job, taskProgress.progress, taskProgress.progressInfo))
        entranceContext.getOrCreatePersistenceManager().onProgressUpdate(job, taskProgress.progress, taskProgress.progressInfo)
        job.asInstanceOf[EntranceJob].setProgressInfo(taskProgress.progressInfo)
      }, "ResponseTaskProgress")
    }
    updateExecutorActivityTime(RPCUtils.getServiceInstanceFromSender(sender))
  }

  @Receiver
  override def taskStatusReceiver(taskStatus: ResponseTaskStatus, smc: ServiceMethodContext): Unit = {
    val sender = smc.getSender
    if (ExecutionNodeStatus.isCompleted(taskStatus.status))
      info(s"The execId ${taskStatus.execId} from engine $sender is completed with state ${taskStatus.status}.")
    askRetryWhenExecuteAsyncReturnIsEmpty(taskStatus.execId, sender, s"ResponseTaskStatus(${taskStatus.execId}, ${taskStatus.status})")
      .notifyStatus(taskStatus)
    updateExecutorActivityTime(RPCUtils.getServiceInstanceFromSender(sender))
  }

  @Receiver
  override def taskResultSizeReceiver(taskResultSize: ResponseTaskResultSize, smc: ServiceMethodContext): Unit = {
    val sender = smc.getSender
    askRetryWhenExecuteAsyncReturnIsEmpty(taskResultSize.execId, sender, "ResponseTaskResultSize")
      .getJobId.flatMap(entranceContext.getOrCreateScheduler().get).foreach {
      case j: Job => entranceContext.getOrCreatePersistenceManager().onResultSizeCreated(j, taskResultSize.resultSize)
      case _ =>
    }
  }

  @Receiver
  override def taskResultSetReceiver(taskResultSet: ResponseTaskResultSet, smc: ServiceMethodContext): Unit = {
    val sender = smc.getSender
    onOperate(taskResultSet.execId, sender
      , entranceContext.getOrCreatePersistenceManager()
        .onResultSetCreated(_, AliasOutputExecuteResponse(taskResultSet.alias, taskResultSet.output))
      , "ResponseTaskResultSet")
  }

  @Receiver
  override def taskErrorReceiver(taskTaskError: ResponseTaskError, smc: ServiceMethodContext): Unit = {
    val sender = smc.getSender
//    askRetryWhenExecuteAsyncReturnIsEmpty(taskTaskError.execId, sender, s"ResponseTaskError(${taskTaskError.execId}, ${taskTaskError.errorMsg})")
//      .notifyError(taskTaskError.errorMsg)
    onOperate(taskTaskError.execId, sender, entranceContext.getOrCreateLogManager().onLogUpdate(_, taskTaskError.errorMsg), "ResponseTaskErrorLog")
  }

  private def addEngineExecutorStatusMonitor(): Unit = {
    val task = new Runnable {
      override def run(): Unit = {
        val startTime = System.currentTimeMillis()
        val engineExecutorCache = getEntranceExecutorManager.engineConnManager.getEngineConnExecutorCache()
        if (engineExecutorCache.size() > 0) {
          info(s"Entrance Executor cache num : ${engineExecutorCache.size()}")
        }
        val unActivityEngines = engineExecutorCache.asScala.filter(startTime - _._2.getLastUpdateTime() > EntranceConfiguration.ENTRANCE_ENGINE_LASTUPDATE_TIMEOUT.getValue.toLong).keySet
        if (null != unActivityEngines && !unActivityEngines.isEmpty) {
          info(s"There are ${unActivityEngines.size} unActivity engines.")
          val engineList = new util.ArrayList[ServiceInstance]()
          unActivityEngines.foreach(engine => {
            engineList.add(engine)
            if (engineList.size() >= GovernanceConstant.REQUEST_ENGINE_STATUS_BATCH_LIMIT) {
              queryEngineStatusAndHandle(engineList, engineExecutorCache)
              engineList.clear()
            }
          })
          if (!engineList.isEmpty) {
            queryEngineStatusAndHandle(engineList, engineExecutorCache)
            engineList.clear()
          }
        }
        val endTime = System.currentTimeMillis()
        if (endTime - startTime >= EntranceConfiguration.ENTRANCE_ENGINE_ACTIVITY_MONITOR_INTERVAL.getValue.toLong) {
          error("Query engines status costs longer time than query task interval, you should increase interval.")
        }
      }
    }
    Utils.defaultScheduler.scheduleWithFixedDelay(task, 10000, EntranceConfiguration.ENTRANCE_ENGINE_ACTIVITY_MONITOR_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)
    info("Entrance engineStatusMonitor inited.")
  }

  private def queryEngineStatusAndHandle(engineList: util.List[ServiceInstance], engineExecutorCache: util.Map[ServiceInstance, EngineConnExecutor]): Unit = {
    val requestEngineStatus = RequestEngineStatusBatch(engineList)
    Utils.tryAndError {
      Sender.getSender(GovernanceCommonConf.MANAGER_SPRING_NAME.getValue).ask(requestEngineStatus) match {
        case response: ResponseEngineStatusBatch =>
          if (null != response.msg) {
            info(s"ResponseEngineStatusBatch msg : ${response.msg}")
          }
          if (response.engineStatus.size() != requestEngineStatus.engineList.size()) {
            error("ResponseEngineStatusBatch engines size is not euqal requet.")
          }
          response.engineStatus.asScala.foreach(status => {
            status._2 match {
              case NodeExistStatus.UnExist =>
                warn(s"Engine ${status._1.toString} is Failed, now go to clear its task.")
                endJobByEngineInstance(status._1, NodeStatus.Failed)
              case NodeExistStatus.Exist | NodeExistStatus.Unknown =>
                val engineConnExecutor = engineExecutorCache.getOrDefault(status._1, null)
                if (null != engineConnExecutor) {
                  Utils.tryCatch {
                    // todo check - only for engine with accessible executor
                    val requestNodeStatus = new RequestNodeStatus
                    Sender.getSender(status._1).ask(requestNodeStatus) match {
                      case rs: ResponseNodeStatus =>
                        if (NodeStatus.isCompleted(rs.getNodeStatus)) {
                          endJobByEngineInstance(status._1, rs.getNodeStatus)
                        } else {
                          warn("Will update engineConnExecutor lastupdated time")
                          updateExecutorActivityTime(status._1)
                        }
                      case o: Any =>
                        Utils.tryAndWarn(warn(s"Unknown response : ${EntranceConstant.GSON.toJson(o)} for request : ${EntranceConstant.GSON.toJson(requestNodeStatus)}"))
                    }
                  } {
                    case t: Throwable =>
                      error(s"Failed to get status of engineConn : ${status._1.toString}, now end the job. ", t)
                      endJobByEngineInstance(status._1, NodeStatus.Failed)
                  }
                }
              case o: Any =>
                error(s"Status of engine ${status._1.toString} is ${status._2}")
            }
          })
        case _ =>
          error(s"Invalid response. request : ${EntranceConstant.GSON.toJson(requestEngineStatus)}")
      }
    }
  }

  private def endJobByEngineInstance(engineServiceInstance: ServiceInstance, engineNodeStatus: NodeStatus) = {
    Utils.tryQuietly {
      val entranceExecutor = getEntranceExecutorManager.getEntranceExecutorByInstance(engineServiceInstance.getInstance).getOrElse(null)
      if (null != entranceExecutor) {
        entranceExecutor.getEngineReturns.foreach(er => {
          er.notifyError(s"Engine has exited unexpectedly with status : ${engineNodeStatus}")
          er.notifyStatus(ResponseTaskStatus(er.execId, ExecutionNodeStatus.Failed))
        })
      } else {
        warn(s"Cannot find entranceExecutor for ${engineServiceInstance.toString}")
      }
    }
  }
}
