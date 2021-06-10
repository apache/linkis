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

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.conf.Configuration
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.ecm.core.engineconn.EngineConn
import com.webank.wedatasphere.linkis.ecm.core.launch.ProcessEngineConnLaunch
import com.webank.wedatasphere.linkis.ecm.server.LinkisECMApplication
import com.webank.wedatasphere.linkis.ecm.server.conf.ECMConfiguration
import com.webank.wedatasphere.linkis.ecm.server.conf.ECMConfiguration.MANAGER_SPRING_NAME
import com.webank.wedatasphere.linkis.ecm.server.errorcode.ECMErrorConstants
import com.webank.wedatasphere.linkis.ecm.server.exception.ECMErrorException
import com.webank.wedatasphere.linkis.ecm.server.listener.EngineConnStatusChangeEvent
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus._
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineConnStatusCallbackToAM
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import com.webank.wedatasphere.linkis.rpc.Sender
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, TimeoutException}


abstract class ProcessEngineConnLaunchService extends AbstractEngineConnLaunchService {

  override def afterLaunch(request: EngineConnLaunchRequest, conn: EngineConn, duration: Long): Unit = {
    super.afterLaunch(request, conn, duration)
    conn.getEngineConnLaunchRunner.getEngineConnLaunch match {
      case launch: ProcessEngineConnLaunch => try {
        processMonitorThread(conn, launch, duration)
      } catch {
        case e: ECMErrorException =>
          warn(s"Failed to init ${conn.getServiceInstance}, status shutting down")
          val logPath = Utils.tryCatch(conn.getEngineConnManagerEnv.engineConnLogDirs) { t =>
            ECMConfiguration.ENGINECONN_ROOT_DIR + "/userName/" + conn.getTickedId + "/logs"
          }
          Sender.getSender(MANAGER_SPRING_NAME).send(EngineConnStatusCallbackToAM(conn.getServiceInstance, NodeStatus.ShuttingDown, "Failed to start EngineConn, reason: " + ExceptionUtils.getRootCauseMessage(e) + s"\n You can go to this path($logPath) to find the reason or ask the administrator for help"))
      }
      case _ =>
    }
  }

  private def processMonitorThread(engineConn: EngineConn, launch: ProcessEngineConnLaunch, timeout: Long): Unit = {
    val isCompleted: EngineConn => Boolean = engineConn => engineConn.getStatus == Success || engineConn.getStatus == Failed
    val tickedId = engineConn.getTickedId
    val errorMsg = new StringBuilder
    Future {
      val iterator = IOUtils.lineIterator(launch.getProcessInputStream,  Configuration.BDP_ENCODING.getValue)
      var count = 0
      val maxLen = ECMConfiguration.ENGINE_START_ERROR_MSG_MAX_LEN.getValue
      while (!isCompleted(engineConn) && iterator.hasNext && count < maxLen) {
        val line = iterator.next()
        println(s"${engineConn.getTickedId}:${line}")
        errorMsg.append(line).append("\n")
        count += 1
      }
      val exitCode = Option(launch.processWaitFor)
      if (exitCode.exists(_ != 0)) {
        LinkisECMApplication.getContext.getECMSyncListenerBus.postToAll(EngineConnStatusChangeEvent(tickedId, ShuttingDown))
      } else {
        LinkisECMApplication.getContext.getECMSyncListenerBus.postToAll(EngineConnStatusChangeEvent(tickedId, Success))
      }
    }
    Utils.tryThrow(Utils.waitUntil(() => engineConn.getStatus != Starting, Duration(timeout, TimeUnit.MILLISECONDS))) {
      case e: TimeoutException =>
        throw new ECMErrorException(ECMErrorConstants.ECM_ERROR, s"wait for $engineConn initial timeout.")
      case e: InterruptedException => //比如被ms cancel
        throw new ECMErrorException(ECMErrorConstants.ECM_ERROR, s"wait for $engineConn initial interrupted.")
      case t: Throwable =>
        error(s"unexpected error, now shutdown it.")
        throw t
    }
    if (engineConn.getStatus == ShuttingDown) {
      throw new ECMErrorException(ECMErrorConstants.ECM_ERROR, s"Failed to init $engineConn, status shutting down")
    }
  }

}
