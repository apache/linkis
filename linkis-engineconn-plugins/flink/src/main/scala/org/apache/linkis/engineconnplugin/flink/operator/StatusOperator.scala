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

package org.apache.linkis.engineconnplugin.flink.operator

import org.apache.linkis.common.exception.{LinkisException, LinkisRuntimeException}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.exception.EngineConnException
import org.apache.linkis.engineconnplugin.flink.util.{ManagerUtil, YarnUtil}
import org.apache.linkis.engineconnplugin.flink.util.YarnUtil.logAndException
import org.apache.linkis.governance.common.constant.ec.ECConstants
import org.apache.linkis.governance.common.exception.engineconn.EngineConnExecutorErrorCode
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.operator.Operator
import org.apache.linkis.server.toScalaMap

import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.yarn.api.records.{ApplicationId, ApplicationReport, FinalApplicationStatus}
import org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException

import java.util

import scala.collection.mutable

class StatusOperator extends Operator with Logging {

  override def getNames: Array[String] = Array("status")

  override def apply(params: util.Map[String, Object]): util.Map[String, Object] = {

    val appIdStr = params.getOrElse(ECConstants.YARN_APPID_NAME_KEY, "").asInstanceOf[String]

    val parts = appIdStr.split("_")
    val clusterTimestamp = parts(1).toLong
    val sequenceNumber = parts(2).toInt

    // Create an ApplicationId object using newInstance method
    val appId = ApplicationId.newInstance(clusterTimestamp, sequenceNumber)
    val rsMap = new mutable.HashMap[String, String]

    val yarnClient = YarnUtil.getYarnClient()
    var appReport: ApplicationReport = null
    Utils.tryCatch {
      appReport = yarnClient.getApplicationReport(appId)
      if (null == appReport) {
        throw logAndException(s"Got null appReport for appid : ${appIdStr}")
      }
    } { case notExist: ApplicationNotFoundException =>
      logger.error(s"Application : ${appIdStr} not exists, will set the status to failed.")
      val map = new util.HashMap[String, Object]()
      map.put(ECConstants.NODE_STATUS_KEY, NodeStatus.Failed.toString)
      map.put(ECConstants.YARN_APPID_NAME_KEY, appIdStr)
      return map
    }

    // Get the application status (YarnApplicationState)
    val appStatus = if (appReport.getFinalApplicationStatus != FinalApplicationStatus.UNDEFINED) {
      appReport.getFinalApplicationStatus
    } else {
      appReport.getYarnApplicationState
    }

    val nodeStatus: NodeStatus = YarnUtil.convertYarnStateToNodeStatus(appIdStr, appStatus.toString)

    logger.info(s"try to get appid: ${appIdStr}, status ${nodeStatus.toString}.")
    rsMap += (ECConstants.NODE_STATUS_KEY -> nodeStatus.toString)
    rsMap += (ECConstants.YARN_APPID_NAME_KEY -> appIdStr)
    val map = new util.HashMap[String, Object]()
    rsMap.foreach(entry => map.put(entry._1, entry._2))
    map
  }

}

object StatusOperator extends Logging {

  private var handshaked: Boolean = false

  def addHandshake(): Unit = {
    handshaked = true
  }

  def isHandshaked: Boolean = handshaked

}
