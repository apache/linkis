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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconnplugin.flink.util.YarnUtil
import org.apache.linkis.governance.common.constant.ec.ECConstants
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.operator.Operator

import org.apache.hadoop.yarn.api.records.{ApplicationId, FinalApplicationStatus}

import scala.collection.mutable

class StatusOperator extends Operator with Logging {

  override def getNames: Array[String] = Array("status")

  override def apply(implicit params: Map[String, Any]): Map[String, Any] = {

    val appIdStr = params.getOrElse(ECConstants.YARN_APPID_NAME_KEY, "").asInstanceOf[String]

    val parts = appIdStr.split("_")
    val clusterTimestamp = parts(1).toLong
    val sequenceNumber = parts(2).toInt

    // Create an ApplicationId object using newInstance method
    val appId = ApplicationId.newInstance(clusterTimestamp, sequenceNumber)

    val yarnClient = YarnUtil.getYarnClient()

    val appReport = yarnClient.getApplicationReport(appId)

    // Get the application status (YarnApplicationState)
    val appStatus = if (appReport.getFinalApplicationStatus != FinalApplicationStatus.UNDEFINED) {
      appReport.getFinalApplicationStatus
    } else {
      appReport.getYarnApplicationState
    }

    val nodeStatus: NodeStatus = YarnUtil.convertYarnStateToNodeStatus(appIdStr, appStatus.toString)

    logger.info(s"try to get appid: ${appIdStr}, status ${nodeStatus.toString}.")
    val rsMap = new mutable.HashMap[String, String]
    rsMap += (ECConstants.NODE_STATUS_KEY -> nodeStatus.toString)
    rsMap += (ECConstants.YARN_APPID_NAME_KEY -> appIdStr)
    rsMap.toMap[String, String]
  }

}
