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

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.ecm.core.engineconn.{EngineConn, EngineConnInfo}
import org.apache.linkis.ecm.core.launch._
import org.apache.linkis.ecm.server.LinkisECMApplication
import org.apache.linkis.ecm.server.conf.ECMConfiguration._
import org.apache.linkis.ecm.server.engineConn.DefaultEngineConn
import org.apache.linkis.ecm.server.hook.ECMHook
import org.apache.linkis.ecm.server.listener.EngineConnStopEvent
import org.apache.linkis.ecm.server.service.{EngineConnLaunchService, ResourceLocalizationService}
import org.apache.linkis.ecm.server.util.ECMUtils
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.utils.{ECPathUtils, JobUtils, LoggerUtils}
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.node.{AMEngineNode, EngineNode}
import org.apache.linkis.manager.common.protocol.engine.{
  EngineConnStatusCallbackToAM,
  EngineStopRequest
}
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import org.apache.linkis.manager.label.constant.LabelValueConstant
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.rpc.Sender

import org.apache.commons.lang3.exception.ExceptionUtils

import scala.concurrent.ExecutionContextExecutorService

abstract class AbstractEngineConnLaunchService extends EngineConnLaunchService with Logging {

  protected implicit val executor: ExecutionContextExecutorService =
    Utils.newCachedExecutionContext(ECM_LAUNCH_MAX_THREAD_SIZE, "EngineConn-Manager-Thread-")

  protected var resourceLocalizationService: ResourceLocalizationService = _

  def setResourceLocalizationService(service: ResourceLocalizationService): Unit =
    this.resourceLocalizationService = service

  def beforeLaunch(request: EngineConnLaunchRequest, conn: EngineConn, duration: Long): Unit = {
    Utils.tryAndWarn(getECMHooks(request).foreach(_.beforeLaunch(request, conn)))
  }

  def afterLaunch(request: EngineConnLaunchRequest, conn: EngineConn, duration: Long): Unit = {
    getECMHooks(request).foreach(_.afterLaunch(conn))
  }

  override def launchEngineConn(request: EngineConnLaunchRequest, duration: Long): EngineNode = {
    //  create engineConn/runner/launch
    val taskId = JobUtils.getJobIdFromStringMap(request.creationDesc.properties)
    LoggerUtils.setJobIdMDC(taskId)
    logger.info("TaskId: {} try to launch a new EngineConn with {}.", taskId: Any, request: Any)
    val conn = createEngineConn
    val runner = createEngineConnLaunchRunner
    val launch = createEngineConnLaunch
    launch.setEngineConnLaunchRequest(request)
    runner.setEngineConnLaunch(launch)
    conn.setEngineConnLaunchRunner(runner)
    conn.setLabels(request.labels)
    conn.setCreationDesc(request.creationDesc)
    conn.setResource(request.nodeResource)
    conn.setTickedId(request.ticketId)
    conn.setStatus(NodeStatus.Starting)
    conn.setEngineConnInfo(new EngineConnInfo)
    conn.setEngineConnManagerEnv(launch.getEngineConnManagerEnv())
    // get ec Resource
    getResourceLocalizationServie.handleInitEngineConnResources(request, conn)
    // start ec
    Utils.tryCatch {
      beforeLaunch(request, conn, duration)
      runner.run()
      launch match {
        case pro: ProcessEngineConnLaunch =>
          val serviceInstance = ServiceInstance(
            GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue,
            ECMUtils.getInstanceByPort(pro.getEngineConnPort)
          )
          conn.setServiceInstance(serviceInstance)
        case _ =>
      }
      afterLaunch(request, conn, duration)
      logger.info(
        "TaskId: {} with request {} wait engineConn {} start",
        Array(taskId, request, conn.getServiceInstance): _*
      )
      // start ec monitor thread
      startEngineConnMonitorStart(request, conn)
    } { t =>
      logger.error(
        "TaskId: {} init {} failed, {}, with request {} now stop and delete it. message: {}",
        Array(
          taskId,
          conn.getServiceInstance,
          conn.getEngineConnLaunchRunner.getEngineConnLaunch
            .getEngineConnManagerEnv()
            .engineConnWorkDir,
          request,
          t.getMessage,
          t
        ): _*
      )
      Sender
        .getSender(MANAGER_SERVICE_NAME)
        .send(
          new EngineConnStatusCallbackToAM(
            conn.getServiceInstance,
            NodeStatus.Failed,
            " wait init failed , reason " + ExceptionUtils.getRootCauseMessage(t),
            true
          )
        )
      conn.setStatus(NodeStatus.Failed)
      val engineType = LabelUtil.getEngineType(request.labels)
      val logPath = Utils.tryCatch(conn.getEngineConnManagerEnv.engineConnLogDirs) { t =>
        ECPathUtils.getECWOrkDirPathSuffix(request.user, request.ticketId, engineType)
      }
      val engineStopRequest = new EngineStopRequest
      engineStopRequest.setEngineType(engineType)
      engineStopRequest.setUser(request.user)
      engineStopRequest.setIdentifier(conn.getPid)
      engineStopRequest.setIdentifierType(AMConstant.PROCESS_MARK)
      engineStopRequest.setLogDirSuffix(logPath)
      engineStopRequest.setServiceInstance(conn.getServiceInstance)
      LinkisECMApplication.getContext.getECMAsyncListenerBus.post(
        EngineConnStopEvent(conn, engineStopRequest)
      )
      LoggerUtils.removeJobIdMDC()
      throw t
    }
    LoggerUtils.removeJobIdMDC()

    val label = LabelUtil.getEngingeConnRuntimeModeLabel(request.labels)
    val isYarnClusterMode: Boolean =
      if (null != label && label.getModeValue.equals(LabelValueConstant.YARN_CLUSTER_VALUE)) true
      else false

    val engineNode = new AMEngineNode()
    engineNode.setLabels(conn.getLabels)
    engineNode.setServiceInstance(conn.getServiceInstance)
    engineNode.setOwner(request.user)
    if (isYarnClusterMode) {
      engineNode.setMark(AMConstant.CLUSTER_PROCESS_MARK)
    } else {
      engineNode.setMark(AMConstant.PROCESS_MARK)
    }
    engineNode
  }

  def startEngineConnMonitorStart(request: EngineConnLaunchRequest, conn: EngineConn): Unit

  def createEngineConn: EngineConn = new DefaultEngineConn

  def createEngineConnLaunchRunner: EngineConnLaunchRunner = new EngineConnLaunchRunnerImpl

  def createEngineConnLaunch: EngineConnLaunch

  def getResourceLocalizationServie: ResourceLocalizationService = {
    // TODO: null 抛出异常
    this.resourceLocalizationService
  }

  def getECMHooks(request: EngineConnLaunchRequest): Array[ECMHook] = {
    ECMHook.getECMHooks.filter(h =>
      if (null != request.engineConnManagerHooks) {
        request.engineConnManagerHooks.contains(h.getName)
      } else {
        false
      }
    )
  }

}
