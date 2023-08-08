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
import org.apache.linkis.engineconn.launch.EngineConnServer
import org.apache.linkis.engineconn.once.executor.creation.OnceExecutorManager
import org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.JobExecutionException
import org.apache.linkis.engineconnplugin.flink.constants.FlinkECConstant
import org.apache.linkis.engineconnplugin.flink.executor.FlinkOnceExecutor
import org.apache.linkis.engineconnplugin.flink.operator.clientmanager.FlinkRestClientManager
import org.apache.linkis.engineconnplugin.flink.util.YarnUtil
import org.apache.linkis.engineconnplugin.flink.util.YarnUtil.logAndException
import org.apache.linkis.governance.common.constant.ec.ECConstants
import org.apache.linkis.governance.common.exception.GovernanceErrorException
import org.apache.linkis.manager.common.operator.Operator

import org.apache.hadoop.yarn.api.records.FinalApplicationStatus

import java.text.MessageFormat
import java.util

import scala.collection.mutable

class TriggerSavepointOperator extends Operator with Logging {

  override def getNames: Array[String] = Array("doSavepoint")

  @throws[GovernanceErrorException]
  override def apply(params: util.Map[String, Object]): util.Map[String, Object] = {
    val rsMap = new mutable.HashMap[String, String]

    val savepointPath = getAsThrow[String](params, FlinkECConstant.SAVAPOINT_PATH_KEY)
    val appIdStr = getAsThrow[String](params, ECConstants.YARN_APPID_NAME_KEY)
    val mode = getAsThrow[String](params, FlinkECConstant.SAVEPOINT_MODE_KEY)

    val appId = YarnUtil.retrieveApplicationId(appIdStr)
    val yarnClient = YarnUtil.getYarnClient()
    val appReport = yarnClient.getApplicationReport(appId)
    if (appReport.getFinalApplicationStatus != FinalApplicationStatus.UNDEFINED) {
      // Flink cluster is not running anymore
      val msg =
        s"The application ${appIdStr} doesn't run anymore. It has previously completed with final status: ${appReport.getFinalApplicationStatus.toString}"
      throw logAndException(msg)
    }

    logger.info(s"try to $mode savepoint with path $savepointPath.")
    if (
        YarnUtil.isDetach(
          EngineConnServer.getEngineCreationContext.getOptions.asInstanceOf[util.Map[String, Any]]
        )
    ) {
      logger.info("The flink cluster is detached, use rest api to trigger savepoint.")
      val restClient = FlinkRestClientManager.getFlinkRestClient(appIdStr)
      val rs = YarnUtil.triggerSavepoint(appIdStr, savepointPath, restClient)
      rsMap.put(FlinkECConstant.RESULT_SAVEPOINT_KEY, rs)
    } else {
      logger.info("The flink cluster is not detached, use flink client to trigger savepoint.")
      OnceExecutorManager.getInstance.getReportExecutor match {
        case flinkExecutor: FlinkOnceExecutor[_] =>
          val writtenSavepoint =
            flinkExecutor.getClusterDescriptorAdapter.doSavepoint(savepointPath, mode)
          rsMap.put(FlinkECConstant.RESULT_SAVEPOINT_KEY, writtenSavepoint)
        case executor =>
          throw new JobExecutionException(
            FlinkErrorCodeSummary.NOT_SUPPORT_SAVEPOTION.getErrorDesc + executor.getClass.getSimpleName
              + MessageFormat
                .format(
                  FlinkErrorCodeSummary.NOT_SUPPORT_SAVEPOTION.getErrorDesc,
                  executor.getClass.getSimpleName
                )
          )
      }
    }
    val map = new util.HashMap[String, Object]()
    rsMap.foreach(entry => map.put(entry._1, entry._2))
    map
  }

}
