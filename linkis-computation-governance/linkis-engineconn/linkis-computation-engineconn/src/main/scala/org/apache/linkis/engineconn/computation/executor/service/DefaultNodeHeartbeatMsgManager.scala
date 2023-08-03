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

package org.apache.linkis.engineconn.computation.executor.service

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.info.NodeHeartbeatMsgManager
import org.apache.linkis.engineconn.computation.executor.metrics.ComputationEngineConnMetrics
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.executor.entity.{Executor, SensibleExecutor, YarnExecutor}
import org.apache.linkis.governance.common.constant.ec.ECConstants
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.StringUtils

import org.springframework.stereotype.Component

import java.util

import scala.collection.JavaConverters.mapAsScalaMapConverter

@Component
class DefaultNodeHeartbeatMsgManager extends NodeHeartbeatMsgManager with Logging {

  /*
  add unlock-to-shutdown time, total unlock time, total idle time, total busy time, total lock time
   */
  override def getHeartBeatMsg(executor: Executor): String = {
    val msgMap = new util.HashMap[String, Object]()
    msgMap.put(ECConstants.EC_TICKET_ID_KEY, EngineConnObject.getEngineCreationContext.getTicketId)
    msgMap.put(
      ECConstants.EC_UNLOCK_TO_SHUTDOWN_TIME_MILLS_KEY,
      ComputationEngineConnMetrics.getUnlockToShutdownDurationMills().asInstanceOf[Object]
    )
    executor match {
      case sensibleExecutor: SensibleExecutor =>
        val status = sensibleExecutor.getStatus
        msgMap.put(
          ECConstants.EC_TOTAL_UNLOCK_TIME_MILLS_KEY,
          ComputationEngineConnMetrics.getTotalUnLockTimeMills(status).asInstanceOf[Object]
        )
        msgMap.put(
          ECConstants.EC_TOTAL_IDLE_TIME_MILLS_KEY,
          ComputationEngineConnMetrics.getTotalIdleTimeMills(status).asInstanceOf[Object]
        )
        msgMap.put(
          ECConstants.EC_TOTAL_BUSY_TIME_MILLS_KEY,
          ComputationEngineConnMetrics.getTotalBusyTimeMills(status).asInstanceOf[Object]
        )
        msgMap.put(
          ECConstants.EC_TOTAL_LOCK_TIME_MILLS_KEY,
          ComputationEngineConnMetrics.getTotalLockTimeMills(status).asInstanceOf[Object]
        )
      case _ =>
    }
    val engineParams = EngineConnObject.getEngineCreationContext.getOptions
    if (engineParams.containsKey(ECConstants.YARN_QUEUE_NAME_CONFIG_KEY)) {
      msgMap.put(
        ECConstants.YARN_QUEUE_NAME_KEY,
        engineParams.get(ECConstants.YARN_QUEUE_NAME_CONFIG_KEY).asInstanceOf[Object]
      )
    }
    executor match {
      case yarnExecutor: YarnExecutor =>
        if (StringUtils.isNotBlank(yarnExecutor.getQueue)) {
          msgMap.put(ECConstants.YARN_QUEUE_NAME_KEY, yarnExecutor.getQueue)
        }
        if (StringUtils.isNotBlank(yarnExecutor.getApplicationId)) {
          msgMap.put(ECConstants.YARN_APPID_NAME_KEY, yarnExecutor.getApplicationId)
        }
        if (StringUtils.isNotBlank(yarnExecutor.getApplicationURL)) {
          msgMap.put(ECConstants.YARN_APP_URL_KEY, yarnExecutor.getApplicationURL)
        }
        if (StringUtils.isNotBlank(yarnExecutor.getYarnMode)) {
          msgMap.put(ECConstants.YARN_MODE_KEY, yarnExecutor.getYarnMode)
        }
      case _ =>
    }
    Utils.tryCatch(BDPJettyServerHelper.gson.toJson(msgMap)) { case e: Exception =>
      val msgs = msgMap.asScala
        .map { case (k, v) => if (null == v) s"${k}->null" else s"${k}->${v.toString}" }
        .mkString(",")
      val errMsg = e.getMessage
      logger.error(s"Convert msgMap to json failed because : ${errMsg}, msgMap values : {${msgs}}")
      "{\"errorMsg\":\"Convert msgMap to json failed because : " + errMsg + "\"}"
    }
  }

}
