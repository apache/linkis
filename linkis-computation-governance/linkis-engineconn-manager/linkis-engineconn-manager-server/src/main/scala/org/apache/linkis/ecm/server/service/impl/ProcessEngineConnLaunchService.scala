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

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.ecm.core.engineconn.EngineConn
import org.apache.linkis.ecm.core.launch.ProcessEngineConnLaunch
import org.apache.linkis.ecm.server.LinkisECMApplication
import org.apache.linkis.ecm.server.conf.ECMConfiguration
import org.apache.linkis.ecm.server.conf.ECMConfiguration.MANAGER_SERVICE_NAME
import org.apache.linkis.ecm.server.listener.EngineConnStopEvent
import org.apache.linkis.ecm.server.service.LocalDirsHandleService
import org.apache.linkis.governance.common.utils.{JobUtils, LoggerUtils}
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.engine.{
  EngineConnStatusCallbackToAM,
  EngineStopRequest
}
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.rpc.Sender

import org.apache.commons.io.IOUtils

import scala.concurrent.Future

abstract class ProcessEngineConnLaunchService extends AbstractEngineConnLaunchService {

  private var localDirsHandleService: LocalDirsHandleService = _

  def setLocalDirsHandleService(localDirsHandleService: LocalDirsHandleService): Unit =
    this.localDirsHandleService = localDirsHandleService

  override def startEngineConnMonitorStart(
      request: EngineConnLaunchRequest,
      conn: EngineConn
  ): Unit = {
    conn.getEngineConnLaunchRunner.getEngineConnLaunch match {
      case launch: ProcessEngineConnLaunch =>
        launch.getPid().foreach(conn.setPid)
        processMonitorThread(conn, launch)
      case _ =>
    }
  }

  private def processMonitorThread(
      engineConn: EngineConn,
      launch: ProcessEngineConnLaunch
  ): Unit = {
    Future {
      val tickedId = engineConn.getTickedId
      val errorMsg = new StringBuilder
      val taskId =
        JobUtils.getJobIdFromStringMap(launch.getEngineConnLaunchRequest.creationDesc.properties)
      LoggerUtils.setJobIdMDC(taskId)
      Utils.tryAndWarnMsg {
        val iterator =
          IOUtils.lineIterator(launch.getProcessInputStream, Configuration.BDP_ENCODING.getValue)
        var count = 0
        val maxLen = ECMConfiguration.ENGINE_START_ERROR_MSG_MAX_LEN.getValue
        while (launch.isAlive && iterator.hasNext && count < maxLen) {
          val line = iterator.next()
          errorMsg.append(line).append("\n")
          count += 1
        }
        val exitCode = launch.processWaitFor
        val engineType = LabelUtil.getEngineType(launch.getEngineConnLaunchRequest.labels)
        val logPath = Utils.tryCatch(engineConn.getEngineConnManagerEnv.engineConnLogDirs) { t =>
          localDirsHandleService.getEngineConnLogDir(
            launch.getEngineConnLaunchRequest.user,
            tickedId,
            engineType
          )
        }
        if (exitCode != 0) {
          val canRetry = if (errorMsg.isEmpty) true else false
          logger.warn(
            s"Failed to start ec ${engineConn.getServiceInstance}, status shutting down exit code ${exitCode}, canRetry ${canRetry}, logPath ${logPath}"
          )
          Sender
            .getSender(MANAGER_SERVICE_NAME)
            .send(
              new EngineConnStatusCallbackToAM(
                engineConn.getServiceInstance,
                NodeStatus.ShuttingDown,
                "Failed to start EngineConn, reason: " + errorMsg + s"\n You can go to this path($logPath) to find the reason or ask the administrator for help",
                canRetry
              )
            )
          engineConn.setStatus(NodeStatus.ShuttingDown)
        } else {
          engineConn.setStatus(NodeStatus.Success)
        }
        val engineStopRequest = new EngineStopRequest
        engineStopRequest.setEngineType(engineType)
        engineStopRequest.setUser(launch.getEngineConnLaunchRequest.user)
        engineStopRequest.setIdentifier(engineConn.getPid)
        engineStopRequest.setIdentifierType(AMConstant.PROCESS_MARK)
        engineStopRequest.setLogDirSuffix(logPath)
        engineStopRequest.setServiceInstance(engineConn.getServiceInstance)
        LinkisECMApplication.getContext.getECMAsyncListenerBus.post(
          EngineConnStopEvent(engineConn, engineStopRequest)
        )
      } { s"EngineConns: ${engineConn.getServiceInstance} monitor Failed" }
      LoggerUtils.removeJobIdMDC()
    }
  }

}
