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

package com.webank.wedatasphere.linkis.ecm.server.service.impl

import java.util.Date
import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.ecm.core.listener.{ECMEvent, ECMEventListener}
import com.webank.wedatasphere.linkis.ecm.core.report.ECMHealthReport
import com.webank.wedatasphere.linkis.ecm.server.LinkisECMApplication
import com.webank.wedatasphere.linkis.ecm.server.conf.ECMConfiguration._
import com.webank.wedatasphere.linkis.ecm.server.listener.{ECMClosedEvent, ECMReadyEvent}
import com.webank.wedatasphere.linkis.ecm.server.report.DefaultECMHealthReport
import com.webank.wedatasphere.linkis.ecm.server.service.{ECMHealthService, EngineConnListService}
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.{NodeHealthy, NodeStatus}
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.{NodeHealthyInfo, NodeOverLoadInfo}
import com.webank.wedatasphere.linkis.manager.common.entity.resource.{CommonNodeResource, LoadInstanceResource}
import com.webank.wedatasphere.linkis.manager.common.protocol.node.{NodeHeartbeatMsg, NodeHeartbeatRequest}
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.rpc.Sender
import org.springframework.beans.factory.annotation.Autowired


class DefaultECMHealthService extends ECMHealthService with ECMEventListener {

  val maxResource = new LoadInstanceResource(ECM_MAX_MEMORY_AVAILABLE, ECM_MAX_CORES_AVAILABLE, ECM_MAX_CREATE_INSTANCES)
  val minResource = new LoadInstanceResource(ECM_PROTECTED_MEMORY, ECM_PROTECTED_CORES, ECM_PROTECTED_INSTANCES)
  private val runtime: Runtime = Runtime.getRuntime

  private val future = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = {
      if (LinkisECMApplication.isReady) {
        reportHealth(getLastEMHealthReport)
      }
    }
  }, ECM_HEALTH_REPORT_DELAY, ECM_HEALTH_REPORT_PERIOD, TimeUnit.SECONDS)

  @Autowired
  private var engineConnListService: EngineConnListService = _

  override def getLastEMHealthReport: ECMHealthReport = {
    val report = new DefaultECMHealthReport
    report.setNodeId(LinkisECMApplication.getECMServiceInstance.toString)
    report.setNodeStatus(NodeStatus.Running)
    //todo report right metrics
    report.setTotalResource(maxResource)
    report.setProtectedResource(minResource)
    report.setUsedResource(engineConnListService.getUsedResources)
    report.setReportTime(new Date().getTime)
    report.setRunningEngineConns(LinkisECMApplication.getContext.getECMMetrics.getRunningEngineConns)
    val info = new NodeOverLoadInfo
    info.setMaxMemory(runtime.maxMemory())
    info.setUsedMemory(runtime.totalMemory() - runtime.freeMemory())
    // TODO: 根据系统获取当前操作系统负载
    report.setOverload(info)
    report
  }

  // TODO: 可能还需要个判断health状态的方法

  override def reportHealth(report: ECMHealthReport): Unit = {
    val heartbeat: NodeHeartbeatMsg = transferECMHealthReportToNodeHeartbeatMsg(report)
    Sender.getSender(MANAGER_SPRING_NAME).send(heartbeat)
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
    resource.setUsedResource(report.getUsedResource)
    heartbeat.setNodeResource(resource)
    heartbeat.setHeartBeatMsg("")
    val nodeHealthyInfo = new NodeHealthyInfo
    nodeHealthyInfo.setMsg("")
    nodeHealthyInfo.setNodeHealthy(NodeHealthy.Healthy)
    heartbeat.setHealthyInfo(nodeHealthyInfo)
    heartbeat
  }

  override def generateHealthReport(reportTime: Long): ECMHealthReport = {
    // TODO: 历史查询
    null
  }

  private def emShutdownHealthReport(event: ECMClosedEvent): Unit = {
    val report = getLastEMHealthReport
    report.setNodeStatus(NodeStatus.ShuttingDown)
    reportHealth(report)
  }

  private def emReadyHealthReport(event: ECMReadyEvent): Unit = reportHealth(getLastEMHealthReport)


  override def onEvent(event: ECMEvent): Unit = event match {
    case event: ECMReadyEvent =>
      emReadyHealthReport(event)
    case event: ECMClosedEvent =>
      cancelHealthReportThread(event)
      emShutdownHealthReport(event)
      presistenceLeftReports(event)
    case _ =>
  }

  private def cancelHealthReportThread(event: ECMClosedEvent): Unit = {

  }

  private def presistenceLeftReports(event: ECMClosedEvent): Unit = {
    // TODO: 持久化 剩余的reports
  }

  @Receiver
  override def dealNodeHeartbeatRequest(nodeHeartbeatRequest: NodeHeartbeatRequest): NodeHeartbeatMsg = {
    val hearlthReport = getLastEMHealthReport
    transferECMHealthReportToNodeHeartbeatMsg(hearlthReport)
  }


  }
