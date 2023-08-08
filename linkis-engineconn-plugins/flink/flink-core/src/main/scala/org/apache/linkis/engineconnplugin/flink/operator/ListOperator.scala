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

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.exception.EngineConnException
import org.apache.linkis.engineconnplugin.flink.util.YarnUtil
import org.apache.linkis.governance.common.constant.ec.ECConstants
import org.apache.linkis.governance.common.exception.GovernanceErrorException
import org.apache.linkis.governance.common.exception.engineconn.EngineConnExecutorErrorCode
import org.apache.linkis.manager.common.operator.Operator
import org.apache.linkis.server.{toScalaBuffer, toScalaMap, BDPJettyServerHelper}

import org.apache.hadoop.yarn.api.records.{FinalApplicationStatus, YarnApplicationState}

import java.util

import scala.collection.mutable

class ListOperator extends Operator with Logging {

  private val json = BDPJettyServerHelper.jacksonJson

  override def getNames: Array[String] = Array("list")

  @throws[GovernanceErrorException]
  override def apply(params: util.Map[String, Object]): util.Map[String, Object] = {

    val applicationTypeSet = new util.HashSet[String]()
    var appStateSet = util.EnumSet.of[YarnApplicationState](YarnApplicationState.RUNNING)
    var appName = ""

    Utils.tryCatch {
      val appTypeList = params
        .getOrElse(ECConstants.YARN_APP_TYPE_LIST_KEY, new util.ArrayList[String]())
        .asInstanceOf[util.List[String]]
      appTypeList.foreach(applicationTypeSet.add)
      val appStateList = params
        .getOrElse(ECConstants.YARN_APP_STATE_LIST_KEY, new util.ArrayList[String]())
        .asInstanceOf[util.List[String]]
      val appStateArray = new util.HashSet[YarnApplicationState]
      appStateList.foreach(e => appStateArray.add(YarnApplicationState.valueOf(e)))
      if (!appStateArray.isEmpty) {
        appStateSet = util.EnumSet.copyOf(appStateArray)
      }
      appName = params.getOrElse(ECConstants.YARN_APP_NAME_KEY, "").asInstanceOf[String]
    } { e: Throwable =>
      val msg = "Invalid params. " + e.getMessage
      logger.error(msg, e)
      throw new EngineConnException(EngineConnExecutorErrorCode.INVALID_PARAMS, msg)
    }

    val yarnClient = YarnUtil.getYarnClient()
    val appList = yarnClient.getApplications(applicationTypeSet, appStateSet)
    val rsMap = new mutable.HashMap[String, String]
    Utils.tryCatch {
      val appTypeStr = json.writeValueAsString(applicationTypeSet)
      val appStateStr = json.writeValueAsString(appStateSet)
      val rsAppList = new util.ArrayList[util.Map[String, String]]()
      appList.foreach(report => {
        if (report.getName.contains(appName)) {
          val tmpMap = new util.HashMap[String, String]()
          tmpMap.put(ECConstants.YARN_APP_NAME_KEY, report.getName)
          tmpMap.put(ECConstants.YARN_APP_TYPE_KEY, report.getApplicationType)
          tmpMap.put(ECConstants.YARN_APPID_NAME_KEY, report.getApplicationId.toString)
          tmpMap.put(ECConstants.YARN_APP_URL_KEY, report.getTrackingUrl)
          val appStatus =
            if (report.getFinalApplicationStatus != FinalApplicationStatus.UNDEFINED) {
              report.getFinalApplicationStatus
            } else {
              report.getYarnApplicationState
            }
          tmpMap.put(
            ECConstants.NODE_STATUS_KEY,
            YarnUtil
              .convertYarnStateToNodeStatus(report.getApplicationId.toString, appStatus.toString)
              .toString
          )
          rsAppList.add(tmpMap)
        }
      })
      val listStr = json.writeValueAsString(rsAppList)

      logger.info(
        s"List yarn apps, params : appTypeSet : ${appTypeStr}, appStateSet : ${appStateStr}, list : ${listStr}"
      )

      rsMap += (ECConstants.YARN_APP_RESULT_LIST_KEY -> listStr)
    } { case e: Exception =>
      val msg = "convert listStr failed. Because : " + e.getMessage
      logger.error(msg)
      throw e
    }

    val map = new util.HashMap[String, Object]()
    rsMap.foreach(e => map.put(e._1, e._2))
    map
  }

}
