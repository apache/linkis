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
import org.apache.linkis.ecm.core.conf.ECMErrorCode
import org.apache.linkis.ecm.core.engineconn.EngineConn
import org.apache.linkis.ecm.core.launch.ProcessEngineConnLaunch
import org.apache.linkis.ecm.errorcode.EngineconnServerErrorCodeSummary._
import org.apache.linkis.ecm.server.LinkisECMApplication
import org.apache.linkis.ecm.server.conf.ECMConfiguration
import org.apache.linkis.ecm.server.conf.ECMConfiguration.MANAGER_SERVICE_NAME
import org.apache.linkis.ecm.server.exception.ECMErrorException
import org.apache.linkis.ecm.server.listener.EngineConnStatusChangeEvent
import org.apache.linkis.ecm.server.service.LocalDirsHandleService
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus._
import org.apache.linkis.manager.common.protocol.engine.EngineConnStatusCallbackToAM
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.rpc.Sender

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import java.util.concurrent.TimeUnit

import scala.concurrent.{Future, TimeoutException}
import scala.concurrent.duration.Duration

abstract class ProcessEngineConnLaunchService extends AbstractEngineConnLaunchService {

  private var localDirsHandleService: LocalDirsHandleService = _

  def setLocalDirsHandleService(localDirsHandleService: LocalDirsHandleService): Unit =
    this.localDirsHandleService = localDirsHandleService

  override def waitEngineConnStart(
      request: EngineConnLaunchRequest,
      conn: EngineConn,
      duration: Long
  ): Unit = {
    conn.getEngineConnLaunchRunner.getEngineConnLaunch match {
      case launch: ProcessEngineConnLaunch =>
        Utils.tryCatch {
          // Set the pid of the shell script before the pid callBack returns
          launch.getPid().foreach(conn.setPid)
          processMonitorThread(conn, launch, duration)
        } { case e: Throwable =>
          val logPath = Utils.tryCatch(conn.getEngineConnManagerEnv.engineConnLogDirs) { t =>
            localDirsHandleService.getEngineConnLogDir(
              request.user,
              request.ticketId,
              LabelUtil.getEngineType(request.labels)
            )
          }
          val canRetry = e match {
            case ecmError: ECMErrorException =>
              if (ECMErrorCode.EC_START_TIME_OUT == ecmError.getErrCode) {
                true
              } else if (StringUtils.isBlank(ecmError.getDesc)) {
                logger.info("exception desc is null, can be retry")
                true
              } else {
                false
              }
            case _ => false
          }
          logger.warn(
            s"Failed to init ${conn.getServiceInstance}, status shutting down, canRetry $canRetry, logPath $logPath",
            e
          )
          Sender
            .getSender(MANAGER_SERVICE_NAME)
            .send(
              EngineConnStatusCallbackToAM(
                conn.getServiceInstance,
                NodeStatus.ShuttingDown,
                "Failed to start EngineConn, reason: " + ExceptionUtils.getRootCauseMessage(
                  e
                ) + s"\n You can go to this path($logPath) to find the reason or ask the administrator for help",
                canRetry
              )
            )
          throw e
        }
      case _ =>
    }
  }

  private def processMonitorThread(
      engineConn: EngineConn,
      launch: ProcessEngineConnLaunch,
      timeout: Long
  ): Unit = {
    val isCompleted: EngineConn => Boolean = engineConn =>
      engineConn.getStatus == Success || engineConn.getStatus == Failed
    val tickedId = engineConn.getTickedId
    val errorMsg = new StringBuilder
    Future {
      val iterator =
        IOUtils.lineIterator(launch.getProcessInputStream, Configuration.BDP_ENCODING.getValue)
      var count = 0
      val maxLen = ECMConfiguration.ENGINE_START_ERROR_MSG_MAX_LEN.getValue
      while (!isCompleted(engineConn) && iterator.hasNext && count < maxLen) {
        val line = iterator.next()
        errorMsg.append(line).append("\n")
        count += 1
      }
      val exitCode = Option(launch.processWaitFor)
      if (exitCode.exists(_ != 0)) {
        logger.info(s"engine ${tickedId} process exit ")
        LinkisECMApplication.getContext.getECMSyncListenerBus.postToAll(
          EngineConnStatusChangeEvent(tickedId, ShuttingDown)
        )
      } else {
        LinkisECMApplication.getContext.getECMSyncListenerBus.postToAll(
          EngineConnStatusChangeEvent(tickedId, Success)
        )
      }
    }
    Utils.tryThrow(
      Utils
        .waitUntil(() => engineConn.getStatus != Starting, Duration(timeout, TimeUnit.MILLISECONDS))
    ) {
      case e: TimeoutException =>
        throw new ECMErrorException(
          EC_START_TIME_OUT.getErrorCode,
          EC_START_TIME_OUT.getErrorDesc + s" $engineConn ."
        )
      case e: InterruptedException => // 比如被ms cancel
        throw new ECMErrorException(
          EC_INTERRUPT_TIME_OUT.getErrorCode,
          EC_INTERRUPT_TIME_OUT.getErrorDesc + s" $engineConn ."
        )
      case t: Throwable =>
        logger.error(s"unexpected error, now shutdown it.")
        throw t
    }
    if (engineConn.getStatus == ShuttingDown) {
      throw new ECMErrorException(EC_START_FAILED.getErrorCode, errorMsg.toString())
    }
  }

}
