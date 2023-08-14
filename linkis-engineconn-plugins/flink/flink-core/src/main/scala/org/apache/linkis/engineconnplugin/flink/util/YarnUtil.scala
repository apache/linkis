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

package org.apache.linkis.engineconnplugin.flink.util

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.executor.entity.YarnExecutor
import org.apache.linkis.engineconnplugin.flink.client.config.FlinkVersionThreadLocal
import org.apache.linkis.engineconnplugin.flink.client.shims.FlinkShims
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.JobExecutionException
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.constant.ec.ECConstants
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus

import org.apache.commons.lang3.StringUtils
import org.apache.flink
import org.apache.flink.client.program.rest.RestClusterClient
import org.apache.flink.configuration.{HighAvailabilityOptions, JobManagerOptions, RestOptions}
import org.apache.flink.runtime.client.JobStatusMessage
import org.apache.flink.yarn.configuration.YarnConfigOptions
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.yarn.api.records.{
  ApplicationId,
  ApplicationReport,
  FinalApplicationStatus,
  YarnApplicationState
}
import org.apache.hadoop.yarn.client.api.YarnClient
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hadoop.yarn.util.ConverterUtils

import java.util

import scala.collection.JavaConverters.collectionAsScalaIterableConverter
import scala.collection.mutable.ArrayBuffer

object YarnUtil extends Logging {

  val CORE_SITE = "core-site.xml"
  val YARN_SITE = "yarn-site.xml"
  val HDFS_SITE = "hdfs-site.xml"
  val MAPRED_SITE = "mapred-site.xml"

  private var yarnClient: YarnClient = _

  def getYarnClient(): YarnClient = {
    if (null == yarnClient) {
      YarnUtil.getClass.synchronized {
        if (null == yarnClient) {
          yarnClient = createYarnClient()
        }
      }
    }
    yarnClient
  }

  private def createYarnClient(): YarnClient = {
    val yarnClient = YarnClient.createYarnClient()
    val hadoopConf = getHadoopConf()
    val yarnConfiguration = new YarnConfiguration(hadoopConf)
    yarnClient.init(yarnConfiguration)
    yarnClient.start()
    yarnClient
  }

  private def getHadoopConf(): Configuration = {
    val conf = new Configuration()
    var confRoot = FlinkEnvConfiguration.HADOOP_CONF_DIR.getValue
    if (StringUtils.isBlank(confRoot)) {
      throw new JobExecutionException("HADOOP_CONF_DIR or linkis.flink.hadoop.conf.dir not set!")
    }
    confRoot = confRoot + "/"
    conf.addResource(confRoot + HDFS_SITE)
    conf.addResource(confRoot + CORE_SITE)
    conf.addResource(confRoot + MAPRED_SITE)
    conf.addResource(confRoot + YARN_SITE)
    conf
  }

  def setClusterEntrypointInfoToConfig(
      flinkConfiguration: flink.configuration.Configuration,
      appReport: ApplicationReport
  ): Unit = {
    if (null == appReport) {
      val msg = "Invalid null appReport"
      logger.error(msg)
      throw new JobExecutionException(msg)
    }

    val appId = appReport.getApplicationId
    val host = appReport.getHost
    val port = appReport.getRpcPort

    logger.info(s"Found Web Interface ${host}:${port} of application '${appId}'.")

    flinkConfiguration.setString(JobManagerOptions.ADDRESS, host)
    flinkConfiguration.setInteger(JobManagerOptions.PORT, port)

    flinkConfiguration.setString(RestOptions.ADDRESS, host)
    flinkConfiguration.setInteger(RestOptions.PORT, port)

    flinkConfiguration.set(YarnConfigOptions.APPLICATION_ID, ConverterUtils.toString(appId))

    if (!flinkConfiguration.contains(HighAvailabilityOptions.HA_CLUSTER_ID)) {
      flinkConfiguration.set(HighAvailabilityOptions.HA_CLUSTER_ID, ConverterUtils.toString(appId))
    }
  }

  def logAndException(msg: String, t: Throwable = null): ErrorException = {
    logger.error(msg, t)
    new JobExecutionException(msg)
  }

  def retrieveApplicationId(appIdStr: String): ApplicationId = {
    val parts = appIdStr.split("_")
    val clusterTimestamp = parts(1).toLong
    val sequenceNumber = parts(2).toInt
    // Create an ApplicationId object using newInstance method
    val appId = ApplicationId.newInstance(clusterTimestamp, sequenceNumber)
    appId
  }

  def triggerSavepoint(
      appIdStr: String,
      checkPointPath: String,
      restClient: RestClusterClient[ApplicationId]
  ): String = {
    val jobs = restClient.listJobs().get()
    if (null == jobs || jobs.size() > 1) {
      val size = if (null == jobs) {
        0
      } else {
        jobs.size()
      }
      val msg = s"App : ${appIdStr} have ${size} jobs, cannot do snapshot."
      throw logAndException(msg)
    }
    if (StringUtils.isBlank(checkPointPath)) {
      val msg = s"App : ${appIdStr} checkpoint path is null, cannot do checkpoint"
      throw logAndException(msg)
    } else {
      val firstJob = jobs.asScala.headOption.getOrElse(null).asInstanceOf[JobStatusMessage]
      if (null == firstJob) {
        val msg = s"App : ${appIdStr} got no head job, cannot do checkPoint and cancel."
        throw new JobExecutionException(msg)
      }
//      val rs = restClient.triggerSavepoint(firstJob.getJobId, checkPointPath).get()
      // todo For compatibility with different versions of flink
      val rs = FlinkShims
        .getInstance(FlinkVersionThreadLocal.getFlinkVersion)
        .triggerSavepoint(restClient, firstJob.getJobId, checkPointPath)
        .get()
      rs
    }
  }

  def convertYarnStateToNodeStatus(appIdStr: String, appStatus: String): NodeStatus = {
    val nodeStatus = appStatus match {
      case finalState if (FinalApplicationStatus.values().map(_.toString).contains(finalState)) =>
        FinalApplicationStatus.valueOf(finalState) match {
          case FinalApplicationStatus.KILLED | FinalApplicationStatus.FAILED =>
            NodeStatus.Failed
          case FinalApplicationStatus.SUCCEEDED =>
            NodeStatus.Success
          case _ =>
            val msg: String = if (null != appStatus) {
              s"Application : ${appIdStr} has unknown state : ${appStatus.toString}"
            } else {
              s"Application : ${appIdStr} has null state"
            }
            throw new JobExecutionException(msg)
        }
      case yarnState if (YarnApplicationState.values().map(_.toString).contains(yarnState)) =>
        YarnApplicationState.valueOf(yarnState) match {
          case YarnApplicationState.FINISHED =>
            val msg: String = "Invalid yarn app state : FINISHED"
            throw new JobExecutionException(msg)
          case YarnApplicationState.KILLED | YarnApplicationState.FAILED =>
            NodeStatus.Failed
          case _ =>
            NodeStatus.Running
        }
      case _ =>
        val msg: String = if (null != appStatus) {
          s"Application : ${appIdStr} has unknown state : ${appStatus.toString}"
        } else {
          s"Application : ${appIdStr} has null state"
        }
        throw new JobExecutionException(msg)
    }
    nodeStatus
  }

  def isDetach(params: util.Map[String, Any]): Boolean = {
    val managerOn = params.getOrDefault(
      FlinkEnvConfiguration.FLINK_MANAGER_MODE_CONFIG_KEY.key,
      FlinkEnvConfiguration.FLINK_MANAGER_MODE_CONFIG_KEY.getValue
    )
    if (null != managerOn && managerOn.toString.toBoolean) {
      return true
    }
    val clientType = params
      .getOrDefault(
        GovernanceCommonConf.EC_APP_MANAGE_MODE.key,
        GovernanceCommonConf.EC_APP_MANAGE_MODE.getValue
      )
      .toString
    logger.info(s"clientType : ${clientType}")
    clientType.toLowerCase() match {
      case ECConstants.EC_CLIENT_TYPE_DETACH =>
        true
      case _ =>
        false
    }
  }

  def getAppIds: Array[String] = {
    val ids = new ArrayBuffer[String]
    ExecutorManager.getInstance.getExecutors.foreach(executor => {
      executor match {
        case yarnExecutor: YarnExecutor =>
          ids.append(yarnExecutor.getApplicationId)
        case _ =>
      }
    })
    if (ids.size > 1) {
      logger.error(
        "There are more than one yarn application running, please check it. Ids : " + ids
          .mkString(",")
      )
    }
    ids.toArray
  }

}
