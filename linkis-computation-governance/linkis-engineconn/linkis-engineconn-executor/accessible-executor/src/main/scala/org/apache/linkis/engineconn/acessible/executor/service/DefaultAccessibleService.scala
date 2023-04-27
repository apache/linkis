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

package org.apache.linkis.engineconn.acessible.executor.service

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.conf.AccessibleExecutorConfiguration
import org.apache.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import org.apache.linkis.engineconn.acessible.executor.listener.event.{
  ExecutorCompletedEvent,
  ExecutorCreateEvent,
  ExecutorStatusChangedEvent
}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.core.hook.ShutdownHook
import org.apache.linkis.engineconn.executor.entity.{Executor, SensibleExecutor}
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import org.apache.linkis.engineconn.executor.service.ManagerService
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.engine.{
  EngineConnReleaseRequest,
  EngineSuicideRequest
}
import org.apache.linkis.manager.common.protocol.node.{RequestNodeStatus, ResponseNodeStatus}
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.{ContextClosedEvent, EventListener}
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

@Service
class DefaultAccessibleService extends AccessibleService with Logging {

  @Autowired
  private var executorHeartbeatService: ExecutorHeartbeatService = _

  private val asyncListenerBusContext =
    ExecutorListenerBusContext.getExecutorListenerBusContext().getEngineConnAsyncListenerBus

  private var shutDownHooked: Boolean = false

  private var lastStatusChanged: Long = System.currentTimeMillis()

  private var lastStatus: NodeStatus = null

  private var lastThreadName: String = null

  @Receiver
  override def dealEngineStopRequest(
      engineSuicideRequest: EngineSuicideRequest,
      sender: Sender
  ): Unit = {
    // todo check user
    if (
        DataWorkCloudApplication.getServiceInstance.equals(engineSuicideRequest.getServiceInstance)
    ) {
      stopEngine()
      logger.info(s"engine will suiside now.")
      ShutdownHook.getShutdownHook.notifyStop()
    } else {
      if (null != engineSuicideRequest.getServiceInstance) {
        logger.error(
          s"Invalid serviceInstance : ${engineSuicideRequest.getServiceInstance.toString}, will not suicide."
        )
      } else {
        logger.error("Invalid empty serviceInstance.")
      }
    }
  }

  @EventListener
  def executorShutDownHook(event: ContextClosedEvent): Unit = {
    logger.info("executorShutDownHook  start to execute.")
    if (!EngineConnObject.isReady) {
      logger.warn("EngineConn not ready, do not shutdown")
      return
    }
    if (shutDownHooked) {
      logger.warn("had stop, do not  shutdown")
      return
    }
    var executor: Executor = ExecutorManager.getInstance.getReportExecutor
    if (null == executor) {
      executor = SensibleExecutor.getDefaultErrorSensibleExecutor
    }
    Utils.tryAndWarn(executor.tryShutdown())
    logger.warn(s"Engine : ${Sender.getThisInstance} with state has stopped successfully.")

    ExecutorManager.getInstance.getExecutors.foreach { closeExecutor =>
      Utils.tryAndWarn(closeExecutor.close())
      logger.warn(s"executorShutDownHook  start to close executor... $executor")
    }
    executorHeartbeatService.reportHeartBeatMsg(executor)
    logger.info("Reported status shuttingDown to manager.")
    Utils.tryQuietly(Thread.sleep(2000))
    shutDownHooked = true
    ShutdownHook.getShutdownHook.notifyStop()
  }

  override def stopExecutor: Unit = {
    // todo
  }

  override def pauseExecutor: Unit = {}

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

  override def requestManagerReleaseExecutor(msg: String): Unit = {
    val engineReleaseRequest = new EngineConnReleaseRequest(
      Sender.getThisServiceInstance,
      Utils.getJvmUser,
      msg,
      EngineConnObject.getEngineCreationContext.getTicketId
    )
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
    logger.info(s"Executor(${executorCreateEvent.executor.getId}) created")
  }

  override def onExecutorCompleted(executorCompletedEvent: ExecutorCompletedEvent): Unit = {
    reportHeartBeatMsg(executorCompletedEvent.executor)
  }

  override def onExecutorStatusChanged(
      executorStatusChangedEvent: ExecutorStatusChangedEvent
  ): Unit = {
    val sinceLastTime = System.currentTimeMillis() - lastStatusChanged
    val reportDelay = AccessibleExecutorConfiguration.REPORTING_DELAY_MS
    if (
        reportDelay > 0 && executorStatusChangedEvent.toStatus != lastStatus && reportDelay > sinceLastTime
    ) {
      logger.info(
        "In order to ensure that the previous state is consumed first, sleep here {} ms",
        reportDelay * 2
      )

      Thread.sleep(reportDelay * 2)
    }
    val ignoreTime = AccessibleExecutorConfiguration.REPORTING_IGNORE_MS
    val currentThreadName = Thread.currentThread().getName
    if (
        ignoreTime > 0 && executorStatusChangedEvent.toStatus == lastStatus && ignoreTime > sinceLastTime && currentThreadName
          .equals(lastThreadName)
    ) {
      logger.info(
        "If the status is the same and the time is short and the thread is the same, no status report is performed {}",
        executorStatusChangedEvent
      )
    } else if (
        NodeStatus.Busy == lastStatus && executorStatusChangedEvent.toStatus == NodeStatus.Idle
    ) {
      logger.info("The state transition from Busy to Idle is not reported")
    } else {
      reportHeartBeatMsg(executorStatusChangedEvent.executor)
    }
    logger.info("Finished to report status {}", executorStatusChangedEvent)
    lastStatusChanged = System.currentTimeMillis()
    lastStatus = executorStatusChangedEvent.toStatus
    lastThreadName = currentThreadName
  }

  private def reportHeartBeatMsg(executor: Executor): Unit = {
    val reportExecutor = executor match {
      case accessibleExecutor: AccessibleExecutor => accessibleExecutor
      case e: Executor =>
        logger.warn(
          s"Executor(${e.getId}) is not a AccessibleExecutor, do noting on status changed."
        )
        return
    }
    executorHeartbeatService.reportHeartBeatMsg(reportExecutor)
  }

}
