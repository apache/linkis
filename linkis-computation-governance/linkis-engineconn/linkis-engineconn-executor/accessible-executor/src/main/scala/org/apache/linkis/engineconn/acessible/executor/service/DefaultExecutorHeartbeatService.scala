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

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.conf.AccessibleExecutorConfiguration
import org.apache.linkis.engineconn.acessible.executor.info.{
  NodeHealthyInfoManager,
  NodeHeartbeatMsgManager,
  NodeOverLoadInfoManager
}
import org.apache.linkis.engineconn.acessible.executor.listener.NodeHealthyListener
import org.apache.linkis.engineconn.acessible.executor.listener.event.NodeHealthyUpdateEvent
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.executor.entity.{Executor, ResourceExecutor, SensibleExecutor}
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import org.apache.linkis.engineconn.executor.service.ManagerService
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.node.{NodeHeartbeatMsg, NodeHeartbeatRequest}
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

import java.util.concurrent.TimeUnit

@Service
class DefaultExecutorHeartbeatService
    extends ExecutorHeartbeatService
    with NodeHealthyListener
    with Logging {

  @Autowired
  private var nodeHealthyInfoManager: NodeHealthyInfoManager = _

  @Autowired
  private var nodeOverLoadInfoManager: NodeOverLoadInfoManager = _

  @Autowired(required = false)
  private var nodeHeartbeatMsgManager: NodeHeartbeatMsgManager = _

  private val asyncListenerBusContext =
    ExecutorListenerBusContext.getExecutorListenerBusContext.getEngineConnAsyncListenerBus

  @PostConstruct
  private def init(): Unit = {
    asyncListenerBusContext.addListener(this)
    val heartbeatTime = AccessibleExecutorConfiguration.ENGINECONN_HEARTBEAT_TIME.getValue.toLong
    Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = Utils.tryAndWarn {
          if (EngineConnObject.isReady) {
            val executor = ExecutorManager.getInstance.getReportExecutor
            reportHeartBeatMsg(executor)
          }
        }
      },
      3 * 60 * 1000,
      heartbeatTime,
      TimeUnit.MILLISECONDS
    )
  }

  /**
   * 定时上报心跳信息，依据Executor不同进行实现
   *
   * @param executor
   */
  override def reportHeartBeatMsg(executor: Executor = null): Unit = {
    ManagerService.getManagerService.heartbeatReport(generateHeartBeatMsg(executor))
  }

  @Receiver
  override def dealNodeHeartbeatRequest(
      nodeHeartbeatRequest: NodeHeartbeatRequest
  ): NodeHeartbeatMsg = generateHeartBeatMsg(null)

  override def onNodeHealthyUpdate(nodeHealthyUpdateEvent: NodeHealthyUpdateEvent): Unit = {
    logger.warn(s"node healthy update, tiger heartbeatReport")
    // val executor = ExecutorManager.getInstance.getReportExecutor
    reportHeartBeatMsg()
  }

  /**
   * Generate heartbeat information through report by default If engine conn is not initialized, the
   * default information is generated
   * @param executor
   * @return
   */
  override def generateHeartBeatMsg(executor: Executor = null): NodeHeartbeatMsg = {
    val realExecutor = if (null == executor) {
      if (EngineConnObject.isReady) ExecutorManager.getInstance.getReportExecutor else null
    } else {
      executor
    }

    val nodeHeartbeatMsg = new NodeHeartbeatMsg

    nodeHeartbeatMsg.setServiceInstance(Sender.getThisServiceInstance)
    if (null == realExecutor) {
      nodeHeartbeatMsg.setStatus(NodeStatus.Starting)
      return nodeHeartbeatMsg
    }
    nodeHeartbeatMsg.setOverLoadInfo(nodeOverLoadInfoManager.getNodeOverLoadInfo)
    nodeHeartbeatMsg.setHealthyInfo(nodeHealthyInfoManager.getNodeHealthyInfo())
    realExecutor match {
      case sensibleExecutor: SensibleExecutor =>
        nodeHeartbeatMsg.setStatus(sensibleExecutor.getStatus)
      case _ =>
    }
    realExecutor match {
      case resourceExecutor: ResourceExecutor =>
        nodeHeartbeatMsg.setNodeResource(resourceExecutor.getCurrentNodeResource())
      case _ =>
    }
    if (null != nodeHeartbeatMsgManager) {
      nodeHeartbeatMsg.setHeartBeatMsg(nodeHeartbeatMsgManager.getHeartBeatMsg(realExecutor))
    }
    nodeHeartbeatMsg
  }

}
