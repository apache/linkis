/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.engineconn.acessible.executor.service

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.DataWorkCloudApplication
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.conf.AccessibleExecutorConfiguration
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.listener.event.{ExecutorCompletedEvent, ExecutorCreateEvent, ExecutorStatusChangedEvent}
import com.webank.wedatasphere.linkis.engineconn.core.EngineConnObject
import com.webank.wedatasphere.linkis.engineconn.core.engineconn.EngineConnManager
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineconn.core.hook.ShutdownHook
import com.webank.wedatasphere.linkis.engineconn.executor.entity.{Executor, SensibleExecutor}
import com.webank.wedatasphere.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import com.webank.wedatasphere.linkis.engineconn.executor.service.ManagerService
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineConnReleaseRequest, EngineSuicideRequest}
import com.webank.wedatasphere.linkis.manager.common.protocol.node.{RequestNodeStatus, ResponseNodeStatus}
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext
import com.webank.wedatasphere.linkis.rpc.Sender
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._


@Service
class DefaultAccessibleService extends AccessibleService with Logging {

  @Autowired
  private var executorHeartbeatService: ExecutorHeartbeatService = _

  private val asyncListenerBusContext = ExecutorListenerBusContext.getExecutorListenerBusContext.getEngineConnAsyncListenerBus

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

  private def executorShutDownHook(): Unit = {
    var executor: Executor = ExecutorManager.getInstance().getDefaultExecutor
    if (null != executor) {
      executor.asInstanceOf[SensibleExecutor].transition(NodeStatus.ShuttingDown)
    } else {
      executor = SensibleExecutor.getDefaultErrorSensibleExecutor()
    }
    executorHeartbeatService.reportHeartBeatMsg(executor)
    info("Reported status shuttingDown to manager.")
  }

  override def stopExecutor: Unit = {
    // todo
  }


  override def pauseExecutor: Unit = {

  }

  override def reStartExecutor: Boolean = {
    true
  }

  /**
    * Service启动后则启动定时任务 空闲释放
    */
  @PostConstruct
  private def init(): Unit = {
    val context = EngineConnObject.getEngineCreationContext
    val maxFreeTimeVar = AccessibleExecutorConfiguration.ENGINECONN_MAX_FREE_TIME.getValue(context.getOptions)
    val maxFreeTimeStr = maxFreeTimeVar.toString
    val maxFreeTime = maxFreeTimeVar.toLong
    Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = Utils.tryAndWarn {
        val executor = ExecutorManager.getInstance().getDefaultExecutor
        if (null == executor || !executor.isInstanceOf[AccessibleExecutor]) {
          warn("Default is not AccessibleExecutor,do noting")
          return
        }
        info("maxFreeTimeMills is " + maxFreeTime)
        val accessibleExecutor = ExecutorManager.getInstance().getDefaultExecutor.asInstanceOf[AccessibleExecutor]
        if (NodeStatus.isCompleted(accessibleExecutor.getStatus)) {
          error(s"${accessibleExecutor.getId()} has completed with status ${accessibleExecutor.getStatus}, now stop it.")
          ShutdownHook.getShutdownHook.notifyStop()
        } else if (accessibleExecutor.getStatus == NodeStatus.ShuttingDown) {
          warn(s"${accessibleExecutor.getId()} is ShuttingDown...")
          ShutdownHook.getShutdownHook.notifyStop()
        } else if (maxFreeTime > 0 && NodeStatus.isIdle(accessibleExecutor.getStatus) && System.currentTimeMillis - accessibleExecutor.getLastActivityTime > maxFreeTime) {
          warn(s"${accessibleExecutor.getId()} has not been used for $maxFreeTimeStr, now try to shutdown it.")
          requestManagerReleaseExecutor(" idle release")
        }
      }
    }, 10 * 60 * 1000, AccessibleExecutorConfiguration.ENGINECONN_HEARTBEAT_TIME.getValue.toLong, TimeUnit.MILLISECONDS)
    asyncListenerBusContext.addListener(this)
    Utils.addShutdownHook(executorShutDownHook())
  }

  private def stopEngine(): Unit = {
    Utils.tryAndWarn {
      ExecutorManager.getInstance().getDefaultExecutor.asInstanceOf[SensibleExecutor].transition(NodeStatus.ShuttingDown)
      val executors = ExecutorManager.getInstance().getAllExecutorsMap()
      executors.asScala.map{
        kv => kv._2.tryShutdown()
      }
      executors.clear()
    }
  }

  /**
    * service 需要加定时任务判断Executor是否空闲很久，然后调用该方法进行释放
    */

  override def requestManagerReleaseExecutor(msg: String): Unit = {
    val engineReleaseRequest = new EngineConnReleaseRequest(Sender.getThisServiceInstance, Utils.getJvmUser, msg, EngineConnManager.getEngineConnManager.getEngineConn().getEngineCreationContext.getTicketId)
    ManagerService.getManagerService.requestReleaseEngineConn(engineReleaseRequest)
  }

  @Receiver
  override def dealRequestNodeStatus(requestNodeStatus: RequestNodeStatus): ResponseNodeStatus = {
    val status = ExecutorManager.getInstance().getDefaultExecutor match {
      case executor: SensibleExecutor =>
        executor.getStatus
      case _ => NodeStatus.Starting
    }
    val responseNodeStatus = new ResponseNodeStatus
    responseNodeStatus.setNodeStatus(status)
    responseNodeStatus
  }

  override def onExecutorCreated(executorCreateEvent: ExecutorCreateEvent): Unit = {

  }

  override def onExecutorCompleted(executorCompletedEvent: ExecutorCompletedEvent): Unit = {

  }

  override def onExecutorStatusChanged(executorStatusChangedEvent: ExecutorStatusChangedEvent): Unit = {
    if (!ExecutorManager.getInstance().getDefaultExecutor.isInstanceOf[AccessibleExecutor]) {
      warn("Default is not AccessibleExecutor,do noting")
      return
    }
    val executor = ExecutorManager.getInstance().getDefaultExecutor.asInstanceOf[AccessibleExecutor]
    executorHeartbeatService.reportHeartBeatMsg(executor)
  }

}
