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

package org.apache.linkis.manager.am.service.engine

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.entity.metrics.AMNodeMetrics
import org.apache.linkis.manager.common.protocol.engine.EngineConnStatusCallbackToAM
import org.apache.linkis.manager.persistence.NodeMetricManagerPersistence
import org.apache.linkis.manager.service.common.metrics.MetricsConverter
import org.apache.linkis.rpc.message.annotation.Receiver
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util
import java.util.Locale

@Service
class DefaultEngineConnStatusCallbackService extends EngineConnStatusCallbackService with Logging {

  @Autowired
  private var nodeMetricManagerPersistence: NodeMetricManagerPersistence = _

  @Autowired
  private var metricsConverter: MetricsConverter = _

  private val canRetryLogs = AMConfiguration.AM_CAN_RETRY_LOGS.getValue.split(";")

  // The heartBeatMsg field is of type text, mysql text max byte num is 65535
  private val initErrorMsgMaxByteNum = 60000

  @Receiver
  override def dealEngineConnStatusCallback(
      engineConnStatusCallbackToAM: EngineConnStatusCallbackToAM
  ): Unit = {

    if (null == engineConnStatusCallbackToAM.serviceInstance) {
      logger.warn(s"call back service instance is null")
    }
    logger.info(s"Start to deal engineConnStatusCallbackToAM $engineConnStatusCallbackToAM")
    val nodeMetrics = new AMNodeMetrics
    val heartBeatMsg: java.util.Map[String, Any] = new util.HashMap[String, Any]()

    var initErrorMsg = engineConnStatusCallbackToAM.initErrorMsg
    if (
        StringUtils.isNotBlank(initErrorMsg) && initErrorMsg
          .getBytes("utf-8")
          .length >= initErrorMsgMaxByteNum
    ) {
      initErrorMsg = initErrorMsg.substring(0, initErrorMsgMaxByteNum)
    }
    heartBeatMsg.put(AMConstant.START_REASON, initErrorMsg)

    if (engineConnStatusCallbackToAM.canRetry) {
      heartBeatMsg.put(AMConstant.EC_CAN_RETRY, engineConnStatusCallbackToAM.canRetry)
    } else if (matchRetryLog(engineConnStatusCallbackToAM.initErrorMsg)) {
      logger.info(s"match canRetry log ${engineConnStatusCallbackToAM.serviceInstance}")
      heartBeatMsg.put(AMConstant.EC_CAN_RETRY, engineConnStatusCallbackToAM.canRetry)
    }

    nodeMetrics.setHeartBeatMsg(BDPJettyServerHelper.jacksonJson.writeValueAsString(heartBeatMsg))
    nodeMetrics.setServiceInstance(engineConnStatusCallbackToAM.serviceInstance)
    nodeMetrics.setStatus(metricsConverter.convertStatus(engineConnStatusCallbackToAM.status))

    nodeMetricManagerPersistence.addOrupdateNodeMetrics(nodeMetrics)
    logger.info(s"Finished to deal engineConnStatusCallbackToAM $engineConnStatusCallbackToAM")

  }

  private def matchRetryLog(errorMsg: String): Boolean = {
    var flag = false
    if (StringUtils.isNotBlank(errorMsg)) {
      val errorMsgLowCase = errorMsg.toLowerCase(Locale.getDefault)
      canRetryLogs.foreach(canRetry =>
        if (errorMsgLowCase.contains(canRetry)) {
          logger.info(s"match engineConn log fatal logs,is $canRetry")
          flag = true
        }
      )
    }
    flag
  }

}
