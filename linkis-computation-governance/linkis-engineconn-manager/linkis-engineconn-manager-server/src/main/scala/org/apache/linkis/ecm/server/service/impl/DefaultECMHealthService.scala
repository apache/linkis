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

package org.apache.linkis.ecm.server.service.impl

import org.apache.linkis.common.utils.{ByteTimeUtils, HardwareUtils, OverloadUtils, Utils}
import org.apache.linkis.ecm.core.listener.{ECMEvent, ECMEventListener}
import org.apache.linkis.ecm.core.report.ECMHealthReport
import org.apache.linkis.ecm.server.LinkisECMApplication
import org.apache.linkis.ecm.server.conf.ECMConfiguration
import org.apache.linkis.ecm.server.conf.ECMConfiguration._
import org.apache.linkis.ecm.server.listener.{ECMClosedEvent, ECMReadyEvent}
import org.apache.linkis.ecm.server.report.DefaultECMHealthReport
import org.apache.linkis.ecm.server.service.ECMHealthService
import org.apache.linkis.ecm.server.util.ECMUtils
import org.apache.linkis.manager.common.entity.enumeration.{NodeHealthy, NodeStatus}
import org.apache.linkis.manager.common.entity.metrics.{NodeHealthyInfo, NodeOverLoadInfo}
import org.apache.linkis.manager.common.entity.resource.{CommonNodeResource, LoadInstanceResource}
import org.apache.linkis.manager.common.protocol.node.{
  NodeHealthyRequest,
  NodeHeartbeatMsg,
  NodeHeartbeatRequest
}
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver

import java.util.Date
import java.util.concurrent.TimeUnit

class DefaultECMHealthService extends ECMHealthService with ECMEventListener {

  private val maxResource = ECMUtils.initMaxResource

  private val minResource = ECMUtils.initMinResource

  private var status: NodeStatus = NodeStatus.Starting

  private var healthy: NodeHealthy = NodeHealthy.Healthy

  private var setByManager: Boolean = false

  private var lastCpuLoad: Double = 0d

  private var lastFreeMemory: Long = ECMUtils.inferDefaultMemory()

  private val statusLocker = new Object()

  private val healthyLocker = new Object()

  private val future = Utils.defaultScheduler.scheduleAtFixedRate(
    new Runnable {

      override def run(): Unit = Utils.tryAndWarn {
        if (LinkisECMApplication.isReady) {
          checkMachinePerformance()
          reportHealth(getLastEMHealthReport)
        }
      }

    },
    ECM_HEALTH_REPORT_DELAY,
    ECM_HEALTH_REPORT_PERIOD,
    TimeUnit.SECONDS
  )

  override def getLastEMHealthReport: ECMHealthReport = {
    val report = new DefaultECMHealthReport
    report.setNodeId(LinkisECMApplication.getECMServiceInstance.toString)
    report.setNodeStatus(getNodeStatus)
    // todo report right metrics
    report.setTotalResource(maxResource)
    report.setProtectedResource(minResource)
    report.setReportTime(new Date().getTime)
    report.setRunningEngineConns(
      LinkisECMApplication.getContext.getECMMetrics.getRunningEngineConns
    )
    val info = new NodeOverLoadInfo
    val (max, free) = HardwareUtils.getTotalAndAvailableMemory()
    info.setMaxMemory(max)
    info.setSystemLeftMemory(free)
    info.setUsedMemory(max - free)
    report.setOverload(info)
    report
  }

  override def reportHealth(report: ECMHealthReport): Unit = {
    val heartbeat: NodeHeartbeatMsg = transferECMHealthReportToNodeHeartbeatMsg(report)
    Sender.getSender(MANAGER_SERVICE_NAME).send(heartbeat)
  }

  private def transferECMHealthReportToNodeHeartbeatMsg(report: ECMHealthReport) = {
    val heartbeat = new NodeHeartbeatMsg
    heartbeat.setOverLoadInfo(report.getOverload)
    heartbeat.setStatus(report.getNodeStatus)
    heartbeat.setServiceInstance(LinkisECMApplication.getECMServiceInstance)
    val resource = new CommonNodeResource
    // todo report latest engineconn metrics
    resource.setMaxResource(maxResource)
    resource.setMinResource(minResource)
    heartbeat.setNodeResource(resource)
    heartbeat.setHeartBeatMsg("")
    val nodeHealthyInfo = new NodeHealthyInfo
    nodeHealthyInfo.setMsg("")
    nodeHealthyInfo.setNodeHealthy(getNodeHealthy)
    heartbeat.setHealthyInfo(nodeHealthyInfo)
    heartbeat
  }

  override def generateHealthReport(reportTime: Long): ECMHealthReport = {
    // TODO: 历史查询
    null
  }

  private def emShutdownHealthReport(event: ECMClosedEvent): Unit = {
    transitionStatus(NodeStatus.ShuttingDown)
    val report = getLastEMHealthReport
    reportHealth(report)
  }

  private def emReadyHealthReport(event: ECMReadyEvent): Unit = {
    transitionStatus(NodeStatus.Running)
    reportHealth(getLastEMHealthReport)
  }

  override def onEvent(event: ECMEvent): Unit = event match {
    case event: ECMReadyEvent =>
      emReadyHealthReport(event)
    case event: ECMClosedEvent =>
      emShutdownHealthReport(event)
      presistenceLeftReports(event)
      cancelHealthReportThread(event)
    case _ =>
  }

  private def cancelHealthReportThread(event: ECMClosedEvent): Unit = {
    Utils.tryAndWarn(future.cancel(true))
  }

  private def presistenceLeftReports(event: ECMClosedEvent): Unit = {
    // TODO: 持久化 剩余的reports
  }

  @Receiver
  override def dealNodeHeartbeatRequest(
      nodeHeartbeatRequest: NodeHeartbeatRequest
  ): NodeHeartbeatMsg = {
    val healthReport = getLastEMHealthReport
    transferECMHealthReportToNodeHeartbeatMsg(healthReport)
  }

  override def getNodeStatus: NodeStatus = {
    this.status
  }

  override def getNodeHealthy: NodeHealthy = {
    this.healthy
  }

  override def isSetByManager: Boolean = {
    this.setByManager
  }

  override def transitionStatus(toStatus: NodeStatus): Unit = statusLocker synchronized {
    this.status match {
      case NodeStatus.Failed | NodeStatus.Success =>
        logger.warn(s"$toString attempt to change status ${this.status} => $toStatus, ignore it .")
        return
      case NodeStatus.ShuttingDown =>
        toStatus match {
          case NodeStatus.Failed | NodeStatus.Success =>
          case _ =>
            logger.warn(
              s"$toString attempt to change a Executor from ShuttingDown to $toStatus, ignore it."
            )
            return
        }
      case _ =>

    }
    logger.info(s"$toString changed status $status => $toStatus.")
    this.status = toStatus
  }

  override def transitionHealthy(toHealthy: NodeHealthy): Unit = healthyLocker synchronized {
    logger.info(s"nodeHealthy from ${healthy} to ${toHealthy}")
    this.healthy = toHealthy
  }

  @Receiver
  override def dealNodeHealthyRequest(nodeHealthyRequest: NodeHealthyRequest): Unit = {
    if (NodeHealthy.isAvailable(nodeHealthyRequest.getNodeHealthy)) {
      this.setByManager = false
      transitionHealthy(nodeHealthyRequest.getNodeHealthy)
    } else {
      this.setByManager = true
      transitionHealthy(nodeHealthyRequest.getNodeHealthy)
    }
  }

  private def checkMachinePerformance(): Unit = {
    if (!ECMConfiguration.ECM_PROTECTED_LOAD_ENABLED) {
      return
    }
    val cpuLoad = OverloadUtils.getOSBean.getSystemCpuLoad
    val freeMemory = HardwareUtils.getAvailableMemory()
    if (
        (cpuLoad > ECM_PROTECTED_CPU_LOAD && lastCpuLoad > ECM_PROTECTED_CPU_LOAD)
        || (freeMemory < ECM_PROTECTED_MEMORY && lastFreeMemory < ECM_PROTECTED_MEMORY)
    ) {
      logger.warn(
        s"cpuLoad(${cpuLoad}) and freeMemory(${ByteTimeUtils.bytesToString(freeMemory)}) overload prepare to mark ecm to StockAvailable"
      )
      if (NodeHealthy.isAvailable(getNodeHealthy)) {
        transitionHealthy(NodeHealthy.StockAvailable)
      }
    } else {
      if (!NodeHealthy.isAvailable(getNodeHealthy) && !isSetByManager) {
        logger.warn(
          s"cpuLoad(${cpuLoad}) and freeMemory(${ByteTimeUtils.bytesToString(freeMemory)}) recover prepare to mark ecm to Healthy"
        )
        transitionHealthy(NodeHealthy.Healthy)
      }
    }
    lastCpuLoad = cpuLoad
    lastFreeMemory = freeMemory
  }

}
