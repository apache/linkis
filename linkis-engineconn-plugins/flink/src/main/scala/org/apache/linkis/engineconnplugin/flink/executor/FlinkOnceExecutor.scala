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
import org.apache.linkis.engineconn.once.executor.{
  ManageableOnceExecutor,
  OnceExecutorExecutionContext
}
import org.apache.linkis.engineconnplugin.flink.client.deployment.{
  ClusterDescriptorAdapter,
  ClusterDescriptorAdapterFactory
}
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration.{
  FLINK_ONCE_APP_STATUS_FETCH_FAILED_MAX,
  FLINK_ONCE_APP_STATUS_FETCH_INTERVAL
}
import org.apache.linkis.engineconnplugin.flink.config.FlinkExecutionTargetType
import org.apache.linkis.engineconnplugin.flink.errorcode.FlinkErrorCodeSummary._
import org.apache.linkis.engineconnplugin.flink.exception.ExecutorInitException
import org.apache.linkis.engineconnplugin.flink.executor.interceptor.FlinkJobSubmitInterceptor
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus

import org.apache.flink.api.common.JobStatus
import org.apache.flink.configuration.DeploymentOptions

import java.util.concurrent.{Future, TimeUnit}

import scala.collection.convert.WrapAsScala._

trait FlinkOnceExecutor[T <: ClusterDescriptorAdapter]
    extends ManageableOnceExecutor
    with FlinkExecutor {

  protected var clusterDescriptor: T = _
  private var daemonThread: Future[_] = _

  private var interceptor: FlinkJobSubmitInterceptor = _

  protected def submit(onceExecutorExecutionContext: OnceExecutorExecutionContext): Unit = {
    ClusterDescriptorAdapterFactory.create(flinkEngineConnContext.getExecutionContext) match {
      case adapter: T => clusterDescriptor = adapter
      case _ =>
        throw new ExecutorInitException(NOT_SUPPORT_FLINK.getErrorDesc)
    }
    val options = onceExecutorExecutionContext.getOnceExecutorContent.getJobContent.map {
      case (k, v: String) => k -> v
      case (k, v) if v != null => k -> v.toString
      case (k, _) => k -> null
    }.toMap
    Option(interceptor).foreach(op => op.beforeSubmit(onceExecutorExecutionContext))
    Utils.tryCatch {
      doSubmit(onceExecutorExecutionContext, options)
      Option(interceptor).foreach(op => op.afterSubmitSuccess(onceExecutorExecutionContext))
    } { t: Throwable =>
      Option(interceptor).foreach(op => op.afterSubmitFail(onceExecutorExecutionContext, t))
      throw t
    }
    if (isCompleted) return

    val flinkDeploymentTarget =
      flinkEngineConnContext.getExecutionContext.getFlinkConfig.get(DeploymentOptions.TARGET)

    if (FlinkExecutionTargetType.isYarnExecutionTargetType(flinkDeploymentTarget)) {
      if (null == clusterDescriptor.getClusterID) {
        throw new ExecutorInitException(YARN_IS_NULL.getErrorDesc)
      }
      setApplicationId(clusterDescriptor.getClusterID.toString)
      setApplicationURL(clusterDescriptor.getWebInterfaceUrl)
      logger.info(
        s"Application is started, applicationId: $getApplicationId, applicationURL: $getApplicationURL."
      )
    } else if (FlinkExecutionTargetType.isKubernetesExecutionTargetType(flinkDeploymentTarget)) {
      if (null == clusterDescriptor.getKubernetesClusterID) {
        throw new ExecutorInitException(KUBERNETES_IS_NULL.getErrorDesc)
      }
      setKubernetesClusterID(clusterDescriptor.getKubernetesClusterID)
      setApplicationURL(clusterDescriptor.getWebInterfaceUrl)
      logger.info(
        s"Application is started, applicationId: $getApplicationId, applicationURL: $getApplicationURL."
      )
    }

    if (clusterDescriptor.getJobId != null) setJobID(clusterDescriptor.getJobId.toHexString)
  }

  protected def isCompleted: Boolean = isClosed || NodeStatus.isCompleted(getStatus)

  def doSubmit(
      onceExecutorExecutionContext: OnceExecutorExecutionContext,
      options: Map[String, String]
  ): Unit

  val id: Long

  def getClusterDescriptorAdapter: T = clusterDescriptor

  def setSubmitInterceptor(interceptor: FlinkJobSubmitInterceptor): Unit = {
    this.interceptor = interceptor
  }

  override def getId: String = "FlinkOnceApp_" + id

  protected def closeDaemon(): Unit = {
    if (daemonThread != null) daemonThread.cancel(true)
  }

  override def close(): Unit = {
    super.close()
    closeDaemon()
    if (clusterDescriptor != null) {
      clusterDescriptor.cancelJob()
      clusterDescriptor.close()
    }
    flinkEngineConnContext.getExecutionContext.getClusterClientFactory.close()
  }

  override protected def waitToRunning(): Unit = {
    if (!isCompleted) {
      daemonThread = Utils.defaultScheduler.scheduleAtFixedRate(
        new Runnable {
          private var lastStatus: JobStatus = JobStatus.INITIALIZING
          private var lastPrintTime = 0L
          private val printInterval =
            math.max(FLINK_ONCE_APP_STATUS_FETCH_INTERVAL.getValue.toLong, 5 * 60 * 1000)
          private var fetchJobStatusFailedNum = 0
          override def run(): Unit = if (!isCompleted) {
            val jobStatus = Utils.tryCatch(clusterDescriptor.getJobStatus) { t =>
              if (fetchJobStatusFailedNum >= FLINK_ONCE_APP_STATUS_FETCH_FAILED_MAX.getValue) {
                logger.error(
                  s"Fetch job status has failed max ${FLINK_ONCE_APP_STATUS_FETCH_FAILED_MAX.getValue} times, now stop this FlinkEngineConn.",
                  t
                )
                tryFailed()
                close()
              } else {
                fetchJobStatusFailedNum += 1
                logger.error(s"Fetch job status failed! retried ++$fetchJobStatusFailedNum...", t)
              }
              return
            }
            fetchJobStatusFailedNum = 0
            if (
                jobStatus != lastStatus || System.currentTimeMillis - lastPrintTime >= printInterval
            ) {
              logger.info(s"The jobStatus of $getJobID is $jobStatus.")
              lastPrintTime = System.currentTimeMillis
            }
            lastStatus = jobStatus
            jobStatus match {
              case JobStatus.FAILED | JobStatus.CANCELED =>
                tryFailed()
              case JobStatus.FINISHED =>
                trySucceed()
              case _ =>
            }
          }
        },
        FLINK_ONCE_APP_STATUS_FETCH_INTERVAL.getValue.toLong,
        FLINK_ONCE_APP_STATUS_FETCH_INTERVAL.getValue.toLong,
        TimeUnit.MILLISECONDS
      )
    }
  }

  override def supportCallBackLogs(): Boolean = true

}
