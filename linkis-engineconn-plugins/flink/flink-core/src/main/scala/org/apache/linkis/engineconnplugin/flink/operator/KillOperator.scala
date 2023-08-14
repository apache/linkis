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
import org.apache.linkis.engineconnplugin.flink.constants.FlinkECConstant
import org.apache.linkis.engineconnplugin.flink.operator.clientmanager.FlinkRestClientManager
import org.apache.linkis.engineconnplugin.flink.util.YarnUtil
import org.apache.linkis.engineconnplugin.flink.util.YarnUtil.logAndException
import org.apache.linkis.governance.common.constant.ec.ECConstants
import org.apache.linkis.governance.common.exception.GovernanceErrorException
import org.apache.linkis.manager.common.operator.Operator
import org.apache.linkis.server.toScalaMap

import org.apache.hadoop.yarn.api.records.{ApplicationId, FinalApplicationStatus}

import java.util

import scala.collection.JavaConverters.collectionAsScalaIterableConverter
import scala.collection.mutable

class KillOperator extends Operator with Logging {

  override def getNames: Array[String] = Array("kill")

  @throws[GovernanceErrorException]
  override def apply(params: util.Map[String, Object]): util.Map[String, Object] = {

    val rsMap = new mutable.HashMap[String, String]
    val appIdStr = params.getOrElse(ECConstants.YARN_APPID_NAME_KEY, "").asInstanceOf[String]
    val snapShot = params.getOrElse(FlinkECConstant.SNAPHOT_KEY, "false").toString.toBoolean

    val appId: ApplicationId = YarnUtil.retrieveApplicationId(appIdStr)

    var isStopped = false
    val restClient =
      Utils.tryCatch {
        FlinkRestClientManager.getFlinkRestClient(appIdStr)
      } { case e: Exception =>
        val yarnClient = YarnUtil.getYarnClient()
        val appReport = yarnClient.getApplicationReport(appId)
        if (appReport.getFinalApplicationStatus != FinalApplicationStatus.UNDEFINED) {
          // Flink cluster is not running anymore
          val msg =
            s"The application ${appIdStr} doesn't run anymore. It has previously completed with final status: ${appReport.getFinalApplicationStatus.toString}"
          logAndException(msg)
          isStopped = true
          null
        } else {
          val msg = s"Get client for app ${appIdStr} failed, because : ${e.getMessage}"
          throw logAndException(msg)
        }
      }
    if (!isStopped) {
      if (snapShot) {
        val checkPointPath =
          params.getOrElse(FlinkECConstant.SAVAPOINT_PATH_KEY, null).asInstanceOf[String]
        val rs = YarnUtil.triggerSavepoint(appIdStr, checkPointPath, restClient)
        rsMap.put(FlinkECConstant.MSG_KEY, rs)
      }
      val jobs = restClient.listJobs().get()
      if (null == jobs || jobs.isEmpty) {
        val msg = s"App : ${appIdStr} have no jobs, but is not ended."
        throw logAndException(msg)
      }
      val msg = s"Try to kill ${jobs.size()} jobs of app : ${appIdStr}"
      jobs.asScala.foreach(job => restClient.cancel(job.getJobId))
      rsMap += (FlinkECConstant.MSG_KEY -> msg)
    }

    rsMap.toMap[String, String]
    val map = new util.HashMap[String, Object]()
    rsMap.foreach(entry => map.put(entry._1, entry._2))
    map
  }

}
