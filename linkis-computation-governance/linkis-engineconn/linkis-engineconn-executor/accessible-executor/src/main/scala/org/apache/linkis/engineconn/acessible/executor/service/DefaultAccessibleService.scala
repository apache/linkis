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
 
package org.apache.linkis.engineconn.acessible.executor.service

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import org.apache.linkis.engineconn.acessible.executor.listener.event.{ExecutorCompletedEvent, ExecutorCreateEvent, ExecutorStatusChangedEvent}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.core.hook.ShutdownHook
import org.apache.linkis.engineconn.executor.entity.{Executor, SensibleExecutor}
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import org.apache.linkis.engineconn.executor.service.ManagerService
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.engine.{EngineConnReleaseRequest, EngineSuicideRequest}
import org.apache.linkis.manager.common.protocol.node.{RequestNodeStatus, ResponseNodeStatus}
import org.apache.linkis.message.annotation.Receiver
import org.apache.linkis.message.builder.ServiceMethodContext
import org.apache.linkis.rpc.Sender

import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.{ContextClosedEvent, EventListener}
import org.springframework.stereotype.Service


@Service
class DefaultAccessibleService extends AccessibleService with Logging {

  @Autowired
  private var executorHeartbeatService: ExecutorHeartbeatService = _

  private val asyncListenerBusContext = ExecutorListenerBusContext.getExecutorListenerBusContext().getEngineConnAsyncListenerBus

  @Receiver
  override def dealEngineStopRequest(engineSuicideRequest: EngineSuicideRequest, smc: ServiceMethodContext): Unit = {
    // todo check user
    if (DataWorkCloudApplication.getServiceInstance.equals(engineSuicideRequest.getServiceInstance)) {
      stopEngine()
      info(s"engine will suiside now.")
      ShutdownHook.getShutdownHook.notifyStop()
    } else {
      if (null != engineSuicideRequest.getServiceInstance) {
        error(s"Invalid serviceInstance : ${engineSuicideRequest.getServiceInstance.toString}, will not suicide.")
      } else {
        error("Invalid empty serviceInstance.")
      }
    }
  }

  @EventListener
  def executorShutDownHook(event: ContextClosedEvent): Unit = {
    info("executorShutDownHook  start to execute.")
    if (! EngineConnObject.isReady) {
      warn("EngineConn not ready, do not shutdown")
      return
    }
    var executor: Executor = ExecutorManager.getInstance.getReportExecutor
    if (null != executor){
      Utils.tryAndWarn{
        executor.tryShutdown()
      }
      warn(s"Engine : ${Sender.getThisInstance} with state has stopped successfully.")

    } else {
      executor = SensibleExecutor.getDefaultErrorSensibleExecutor
    }
    ExecutorManager.getInstance.getExecutors.foreach{ closeExecutor =>
      Utils.tryAndWarn(closeExecutor.close())
      warn(s"executorShutDownHook  start to close executor... $executor")
    }
    executorHeartbeatService.reportHeartBeatMsg(executor)
    info("Reported status shuttingDown to manager.")
    Thread.sleep(2000)
  }

  override def stopExecutor: Unit = {
    // todo
  }

  override def pauseExecutor: Unit = {

  }

  override def reStartExecutor: Boolean = {
    true
  }


  @PostConstruct
  def init(): Unit = {
    asyncListenerBusContext.addListener(this)
  }

  private def stopEngine(): Unit = {
    Utils.tryAndWarn {
      ExecutorManager.getInstance.getExecutors.foreach(_.tryShutdown())
    }
  }

  /**
   * service 需要加定时任务判断Executor是否空闲很久，然后调用该方法进行释放
   */

  override def requestManagerReleaseExecutor(msg: String): Unit = {
    val engineReleaseRequest = new EngineConnReleaseRequest(Sender.getThisServiceInstance, Utils.getJvmUser, msg, EngineConnObject.getEngineCreationContext.getTicketId)
    ManagerService.getManagerService.requestReleaseEngineConn(engineReleaseRequest)
  }

  @Receiver
  override def dealRequestNodeStatus(requestNodeStatus: RequestNodeStatus): ResponseNodeStatus = {
    val status = if (EngineConnObject.isReady) {
      ExecutorManager.getInstance.getReportExecutor match {
      case executor: SensibleExecutor =>
        executor.getStatus
      case _ => NodeStatus.Starting
    }
    } else {
      NodeStatus.Starting
    }
    val responseNodeStatus = new ResponseNodeStatus
    responseNodeStatus.setNodeStatus(status)
    responseNodeStatus
  }

  override def onExecutorCreated(executorCreateEvent: ExecutorCreateEvent): Unit = {
    info(s"Executor(${executorCreateEvent.executor.getId}) created")
  }

  override def onExecutorCompleted(executorCompletedEvent: ExecutorCompletedEvent): Unit = {
    reportHeartBeatMsg(executorCompletedEvent.executor)
  }

  override def onExecutorStatusChanged(executorStatusChangedEvent: ExecutorStatusChangedEvent): Unit = {
    reportHeartBeatMsg(executorStatusChangedEvent.executor)
  }

  private def reportHeartBeatMsg(executor: Executor): Unit = {
    val reportExecutor = executor match {
      case accessibleExecutor: AccessibleExecutor => accessibleExecutor
      case e: Executor =>
        warn(s"Executor(${e.getId}) is not a AccessibleExecutor, do noting on status changed.")
        return
    }
    executorHeartbeatService.reportHeartBeatMsg(reportExecutor)
  }

}

