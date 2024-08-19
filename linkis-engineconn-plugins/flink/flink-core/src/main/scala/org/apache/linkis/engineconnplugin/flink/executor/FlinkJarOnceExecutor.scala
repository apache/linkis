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

package org.apache.linkis.engineconnplugin.flink.executor

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.acessible.executor.service.ExecutorHeartbeatServiceHolder
import org.apache.linkis.engineconn.executor.service.ManagerService
import org.apache.linkis.engineconn.once.executor.OnceExecutorExecutionContext
import org.apache.linkis.engineconnplugin.flink.client.deployment.{
  AbstractApplicationClusterDescriptorAdapter,
  YarnApplicationClusterDescriptorAdapter
}
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration._
import org.apache.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import org.apache.linkis.engineconnplugin.flink.operator.StatusOperator
import org.apache.linkis.engineconnplugin.flink.util.YarnUtil
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.constant.ec.ECConstants
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.concurrent.{Future, TimeUnit}

import scala.concurrent.duration.Duration

class FlinkJarOnceExecutor(
    override val id: Long,
    override protected val flinkEngineConnContext: FlinkEngineConnContext
) extends FlinkOnceExecutor[AbstractApplicationClusterDescriptorAdapter] {

  private var daemonThread: Future[_] = _

  private var firstReportAppIdTimestampMills: Long = 0L

  override def doSubmit(
      onceExecutorExecutionContext: OnceExecutorExecutionContext,
      options: Map[String, String]
  ): Unit = {
    val args = FLINK_APPLICATION_ARGS.getValue(options)
    val programArguments =
      if (StringUtils.isNotEmpty(args)) args.split(" ") else Array.empty[String]
    val mainClass = FLINK_APPLICATION_MAIN_CLASS.getValue(options)
    logger.info(s"Ready to submit flink application, mainClass: $mainClass, args: $args.")
    clusterDescriptor.deployCluster(programArguments, mainClass)
  }

  override protected def waitToRunning(): Unit = {
    Utils.waitUntil(() => clusterDescriptor.initJobId(), Duration.Inf)
    setJobID(clusterDescriptor.getJobId.toHexString)
    super.waitToRunning()
    if (YarnUtil.isDetach(flinkEngineConnContext.getEnvironmentContext.getExtraParams())) {
      waitToExit()
    }
  }

  override def close(): Unit = {
    super.close()
    if (null != daemonThread) {
      daemonThread.cancel(true)
    }
  }

  override protected def closeYarnApp(): Unit = {
    if (YarnUtil.isDetach(flinkEngineConnContext.getEnvironmentContext.getExtraParams())) {
      if (getStatus == NodeStatus.Failed) {
        logger.info("Will kill yarn app on close with clientType : detach, because status failed.")
        super.closeYarnApp()
      } else {
        logger.info("Skip to kill yarn app on close with clientType : detach.")
      }
    } else {
      logger.info("Will kill yarn app on close with clientType : attach.")
      super.closeYarnApp()
    }
  }

  private def waitToExit(): Unit = {
    // upload applicationId to manager and then exit
    val thisExecutor = this
    if (!isCompleted) {
      daemonThread = Utils.defaultScheduler.scheduleWithFixedDelay(
        new Runnable {
          override def run(): Unit = {
            if (!isCompleted) {
              Utils.waitUntil(() => StringUtils.isNotBlank(getApplicationId), Duration.apply("10s"))
              if (StringUtils.isNotBlank(getApplicationId)) {
                Utils.tryAndWarn {
                  val heartbeatService = ExecutorHeartbeatServiceHolder.getDefaultHeartbeatService()
                  if (null == heartbeatService) {
                    logger.error("HeartbeatService not inited.")
                    return null
                  }
                  val heartbeatMsg = heartbeatService.generateHeartBeatMsg(thisExecutor)
                  ManagerService.getManagerService.heartbeatReport(heartbeatMsg)
                  logger.info(
                    s"Succeed to report heatbeatMsg : ${heartbeatMsg.getHeartBeatMsg}, will add handshake."
                  )
                  if (0L >= firstReportAppIdTimestampMills) {
                    firstReportAppIdTimestampMills = System.currentTimeMillis()
                  }
                  if (!StatusOperator.isHandshaked) {
                    StatusOperator.addHandshake()
                  } else {
                    logger.info("Will exit with handshaked.")
                    trySucceed()
                  }
                }
              }
            }
          }
        },
        1000,
        FlinkEnvConfiguration.FLINK_ONCE_JAR_APP_REPORT_APPLICATIONID_INTERVAL.getValue.toLong,
        TimeUnit.MILLISECONDS
      )
      logger.info("waitToExit submited.")
    }
  }

}
