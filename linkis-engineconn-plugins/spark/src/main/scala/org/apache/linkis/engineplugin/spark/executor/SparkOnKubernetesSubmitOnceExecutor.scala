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

package org.apache.linkis.engineplugin.spark.executor

import org.apache.linkis.common.utils.{ByteTimeUtils, Utils}
import org.apache.linkis.engineconn.once.executor.{
  OnceExecutorExecutionContext,
  OperableOnceExecutor
}
import org.apache.linkis.engineplugin.spark.client.deployment.{
  KubernetesApplicationClusterDescriptorAdapter,
  YarnApplicationClusterDescriptorAdapter
}
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration.{
  SPARK_APP_CONF,
  SPARK_APPLICATION_ARGS,
  SPARK_APPLICATION_MAIN_CLASS
}
import org.apache.linkis.engineplugin.spark.context.SparkEngineConnContext
import org.apache.linkis.engineplugin.spark.utils.SparkJobProgressUtil
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.common.utils.ResourceUtils
import org.apache.linkis.protocol.engine.JobProgressInfo

import org.apache.commons.lang3.StringUtils

import java.util

import scala.concurrent.duration.Duration

import io.fabric8.kubernetes.api.model.Quantity

class SparkOnKubernetesSubmitOnceExecutor(
    override val id: Long,
    override protected val sparkEngineConnContext: SparkEngineConnContext
) extends SparkOnceExecutor[KubernetesApplicationClusterDescriptorAdapter]
    with OperableOnceExecutor {

  private var oldProgress: Float = 0f

  override def doSubmit(
      onceExecutorExecutionContext: OnceExecutorExecutionContext,
      options: Map[String, String]
  ): Unit = {
    val args = SPARK_APPLICATION_ARGS.getValue(options)
    val mainClass = SPARK_APPLICATION_MAIN_CLASS.getValue(options)
    val extConf = SPARK_APP_CONF.getValue(options)
    val confMap = new util.HashMap[String, String]()
    if (StringUtils.isNotBlank(extConf)) {
      for (conf <- extConf.split("\n")) {
        if (StringUtils.isNotBlank(conf)) {
          val pair = conf.trim.split("=")
          if (pair.length == 2) {
            confMap.put(pair(0), pair(1))
          } else {
            logger.warn(s"ignore spark conf: $conf")
          }
        }
      }
    }
    logger.info(
      s"Ready to submit spark application to kubernetes, mainClass: $mainClass, args: $args."
    )
    clusterDescriptorAdapter.deployCluster(mainClass, args, confMap)
  }

  override protected def waitToRunning(): Unit = {
    // Wait until the task return applicationId (等待返回applicationId)
    Utils.waitUntil(() => clusterDescriptorAdapter.initJobId(), Duration.Inf)
    // Synchronize applicationId to EC SparkOnceExecutor to facilitate user operations,
    // such as obtaining progress and killing jobs(将applicationId同步给EC执行器，方便用户操作，如获取进度，kill任务等)
    setApplicationId(clusterDescriptorAdapter.getApplicationId)
    super.waitToRunning()
  }

  override def getApplicationURL: String = ""

  override def getCurrentNodeResource(): NodeResource = {
    logger.info("Begin to get actual used resources!")
    Utils.tryCatch({
      val sparkConf = sparkEngineConnContext.getExecutionContext.getSparkConfig
      val sparkNamespace = sparkConf.getK8sNamespace

      val executorNum: Int = sparkConf.getNumExecutors
      val executorMem: Long =
        ByteTimeUtils.byteStringAsBytes(sparkConf.getExecutorMemory) * executorNum
      val driverMem: Long = ByteTimeUtils.byteStringAsBytes(sparkConf.getDriverMemory)

      val executorCoresQuantity = Quantity.parse(sparkConf.getK8sExecutorRequestCores)
      val executorCores: Long =
        (Quantity.getAmountInBytes(executorCoresQuantity).doubleValue() * 1000).toLong * executorNum
      val driverCoresQuantity = Quantity.parse(sparkConf.getK8sDriverRequestCores)
      val driverCores: Long =
        (Quantity.getAmountInBytes(driverCoresQuantity).doubleValue() * 1000).toLong

      logger.info(
        "Current actual used resources is driverMem:" + driverMem + ",driverCores:" + driverCores + ",executorMem:" + executorMem + ",executorCores:" + executorCores + ",namespace:" + sparkNamespace
      )
      val usedResource = new DriverAndKubernetesResource(
        new LoadInstanceResource(0, 0, 0),
        new KubernetesResource(executorMem + driverMem, executorCores + driverCores, sparkNamespace)
      )
      val nodeResource = new CommonNodeResource
      nodeResource.setUsedResource(usedResource)
      nodeResource.setResourceType(ResourceUtils.getResourceTypeByResource(usedResource))
      nodeResource
    })(t => {
      logger.warn("Get actual used resource exception", t)
      null
    })
  }

  override def getProgress: Float = {
    val jobIsFinal = clusterDescriptorAdapter != null &&
      clusterDescriptorAdapter.getJobState != null &&
      clusterDescriptorAdapter.getJobState.isFinal
    if (oldProgress >= 1 || jobIsFinal) {
      1
    } else {
      val sparkDriverPodIP = this.clusterDescriptorAdapter.getSparkDriverPodIP
      if (StringUtils.isNotBlank(sparkDriverPodIP)) {
        val newProgress = SparkJobProgressUtil.getProgress(this.getApplicationId, sparkDriverPodIP)
        if (newProgress > oldProgress) {
          oldProgress = newProgress
        }
      }
      oldProgress
    }
  }

  override def getProgressInfo: Array[JobProgressInfo] = {
    val sparkDriverPodIP = this.clusterDescriptorAdapter.getSparkDriverPodIP
    if (StringUtils.isNotBlank(sparkDriverPodIP)) {
      SparkJobProgressUtil.getSparkJobProgressInfo(this.getApplicationId, sparkDriverPodIP)
    } else {
      Array.empty
    }
  }

  override def getMetrics: util.Map[String, Any] = {
    new util.HashMap[String, Any]()
  }

  override def getDiagnosis: util.Map[String, Any] = {
    new util.HashMap[String, Any]()
  }

}
