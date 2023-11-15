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

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.acessible.executor.service.ExecutorHeartbeatServiceHolder
import org.apache.linkis.engineconn.executor.service.ManagerService
import org.apache.linkis.engineconn.once.executor.OnceExecutorExecutionContext
import org.apache.linkis.engineconnplugin.flink.client.deployment.YarnApplicationClusterDescriptorAdapter
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration._
import org.apache.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import org.apache.linkis.engineconnplugin.flink.operator.StatusOperator
import org.apache.linkis.engineconnplugin.flink.util.YarnUtil
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus

import org.apache.commons.lang3.StringUtils
import org.apache.flink.configuration.ConfigOption
import org.apache.flink.yarn.configuration.{YarnConfigOptions, YarnConfigOptionsInternal}

import java.io.{File, FileNotFoundException}
import java.nio.file.{Files, Paths}
import java.util
import java.util.Properties
import java.util.concurrent.{Future, TimeUnit}

import scala.collection.JavaConverters.dictionaryAsScalaMapConverter
import scala.concurrent.duration.Duration

class FlinkJarOnceExecutor(
    override val id: Long,
    override protected val flinkEngineConnContext: FlinkEngineConnContext
) extends FlinkOnceExecutor[YarnApplicationClusterDescriptorAdapter] {

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
    if (LINKIS_FLINK_LOG4J_CHECK_ENABLE.getHotValue()) {
      val yarnShipLog4jPath = getLog4jPath(YarnConfigOptions.SHIP_FILES)
      var firstLog4jPath: Option[String] = yarnShipLog4jPath
      if (null == firstLog4jPath && firstLog4jPath.isEmpty) {
        val internalYarnLog4jPath = getLog4jPath(
          YarnConfigOptionsInternal.APPLICATION_LOG_CONFIG_FILE
        )
        firstLog4jPath = internalYarnLog4jPath
      }
      val configMap = new Properties()
      firstLog4jPath match {
        case Some(log4jPath) =>
          try {
            configMap.load(Files.newBufferedReader(Paths.get(log4jPath)))
          } catch {
            case e: Exception =>
              logger.error("error occurred while reading or parsing the file: " + e.getMessage)
          }
          val ecOptions = onceExecutorExecutionContext.getEngineCreationContext.getOptions
          LINKIS_FLINK_LOG4J_CHECK_KEYWORDS.getValue(ecOptions).split(",").foreach {
            appenderConfig =>
              if (null != appenderConfig && appenderConfig.nonEmpty) {
                if (!configMap.values().contains(appenderConfig)) {
                  throw new ErrorException(30000, s"log4j.properties 不符合规范，请检测内容")
                }
              } else {
                logger.info("log4j.properties does not need check")
              }
          }
        case None =>
      }
    }
    clusterDescriptor.deployCluster(programArguments, mainClass)
  }

  def getLog4jPath(configOption: ConfigOption[_]): Option[String] = {
    val internalYarnLogConfigFile =
      flinkEngineConnContext.getEnvironmentContext.getFlinkConfig.getValue(configOption)
    logger.info(internalYarnLogConfigFile)
    val paths = internalYarnLogConfigFile.stripPrefix("[").stripSuffix("]").split(",").toList
    val option = paths.find(path => path.contains("log4j.properties"))
    option
  }

  override protected def waitToRunning(): Unit = {
    if (YarnUtil.isDetach(flinkEngineConnContext.getEnvironmentContext.getExtraParams())) {
      tryToHeartbeat()
    } else {
      Utils.waitUntil(() => clusterDescriptor.initJobId(), Duration.Inf)
      setJobID(clusterDescriptor.getJobId.toHexString)
      super.waitToRunning()
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

  private def tryToHeartbeat(): Unit = {
    // upload applicationId to manager and then exit
    logger.info(s"try to send heartbeat to LinkisManager with applicationId: $getApplicationId.")
    daemonThread = Utils.defaultScheduler.scheduleWithFixedDelay(
      new Runnable {
        override def run(): Unit = Utils.tryAndWarn {
          val heartbeatService = ExecutorHeartbeatServiceHolder.getDefaultHeartbeatService()
          if (null == heartbeatService) {
            logger.warn("HeartbeatService is not inited.")
            return
          }
          val heartbeatMsg = heartbeatService.generateHeartBeatMsg(FlinkJarOnceExecutor.this)
          ManagerService.getManagerService.heartbeatReport(heartbeatMsg)
          logger.info(
            s"Succeed to report heartbeatMsg: ${heartbeatMsg.getHeartBeatMsg}, will add handshake."
          )
          if (0L >= firstReportAppIdTimestampMills) {
            firstReportAppIdTimestampMills = System.currentTimeMillis()
          }
          if (!StatusOperator.isHandshaked) {
            StatusOperator.addHandshake()
          } else {
            logger.info(
              "submit to yarn, report heartbeat to LinkisManager, and add handshake succeed, now exit this detach ec."
            )
            trySucceed()
          }
        }
      },
      1000,
      FlinkEnvConfiguration.FLINK_ONCE_JAR_APP_REPORT_APPLICATIONID_INTERVAL.getValue.toLong,
      TimeUnit.MILLISECONDS
    )
  }

}
