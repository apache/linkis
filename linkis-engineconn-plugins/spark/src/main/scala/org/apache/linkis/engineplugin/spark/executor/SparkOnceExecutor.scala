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

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.once.executor.{
  ManageableOnceExecutor,
  OnceExecutorExecutionContext
}
import org.apache.linkis.engineplugin.spark.client.deployment.{
  ClusterDescriptorAdapter,
  ClusterDescriptorAdapterFactory
}
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration.{
  SPARK_ONCE_APP_STATUS_FETCH_FAILED_MAX,
  SPARK_ONCE_APP_STATUS_FETCH_INTERVAL
}
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary
import org.apache.linkis.engineplugin.spark.exception.ExecutorInitException
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus

import org.apache.spark.launcher.SparkAppHandle

import java.util.concurrent.{Future, TimeUnit}

import scala.collection.convert.WrapAsScala._

trait SparkOnceExecutor[T <: ClusterDescriptorAdapter]
    extends ManageableOnceExecutor
    with SparkExecutor {

  protected var clusterDescriptorAdapter: T = _
  private var daemonThread: Future[_] = _

  protected def submit(onceExecutorExecutionContext: OnceExecutorExecutionContext): Unit = {
    ClusterDescriptorAdapterFactory.create(sparkEngineConnContext.getExecutionContext) match {
      case adapter: T => clusterDescriptorAdapter = adapter
      case _ =>
        throw new ExecutorInitException(
          SparkErrorCodeSummary.NOT_SUPPORT_ADAPTER.getErrorCode,
          SparkErrorCodeSummary.NOT_SUPPORT_ADAPTER.getErrorDesc
        )
    }
    val options = onceExecutorExecutionContext.getOnceExecutorContent.getJobContent.map {
      case (k, v: String) => k -> v
      case (k, v) if v != null => k -> v.toString
      case (k, _) => k -> null
    }.toMap
    doSubmit(onceExecutorExecutionContext, options)
    if (isCompleted) return
    if (null == clusterDescriptorAdapter.getApplicationId)
      throw new ExecutorInitException(
        SparkErrorCodeSummary.YARN_APPLICATION_START_FAILED.getErrorCode,
        SparkErrorCodeSummary.YARN_APPLICATION_START_FAILED.getErrorDesc
      )
    setApplicationId(clusterDescriptorAdapter.getApplicationId)
    logger.info(s"Application is started, applicationId: $getApplicationId.")
  }

  protected def isCompleted: Boolean = isClosed || NodeStatus.isCompleted(getStatus)

  def doSubmit(
      onceExecutorExecutionContext: OnceExecutorExecutionContext,
      options: Map[String, String]
  ): Unit

  val id: Long

  override def getId: String = "SparkOnceApp_" + id

  protected def closeDaemon(): Unit = {
    if (daemonThread != null) daemonThread.cancel(true)
  }

  override def close(): Unit = {
    super.close()
    closeDaemon()
    if (clusterDescriptorAdapter != null) {
      clusterDescriptorAdapter.close()
    }
  }

  override protected def waitToRunning(): Unit = {
    var waitingToFinished = false
    if (!isCompleted)
      daemonThread = Utils.defaultScheduler.scheduleAtFixedRate(
        new Runnable {
          private var lastStatus: SparkAppHandle.State = _
          private var lastPrintTime = 0L
          private val printInterval =
            math.max(SPARK_ONCE_APP_STATUS_FETCH_INTERVAL.getValue.toLong, 5 * 60 * 1000)
          private var fetchJobStatusFailedNum = 0

          override def run(): Unit = if (!isCompleted && !waitingToFinished) {
            val jobState = Utils.tryCatch(clusterDescriptorAdapter.getJobState) { t =>
              val maxFailedNum = SPARK_ONCE_APP_STATUS_FETCH_FAILED_MAX.getValue
              if (fetchJobStatusFailedNum >= maxFailedNum) {
                val errMsg =
                  s"Fetch job status has failed max $maxFailedNum times, now stop this SparkEngineConn."
                logger.error(errMsg, t)
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
                jobState != lastStatus || System.currentTimeMillis - lastPrintTime >= printInterval
            ) {
              logger.info(s"The jobState of $getApplicationId is $jobState.")
              lastPrintTime = System.currentTimeMillis
            }
            lastStatus = jobState
            if (SparkAppHandle.State.FINISHED == lastStatus) {
              waitingToFinished = true
              logger.info("Job has finished, waiting for final status.")
              Thread.sleep(5000)
              logger.info(s"Job's final status ${clusterDescriptorAdapter.getJobState}.")
            }
            clusterDescriptorAdapter.getJobState match {
              case SparkAppHandle.State.FAILED | SparkAppHandle.State.KILLED |
                  SparkAppHandle.State.LOST =>
                tryFailed()
              case SparkAppHandle.State.FINISHED =>
                trySucceed()
              case _ =>
            }
            waitingToFinished = false
          }
        },
        SPARK_ONCE_APP_STATUS_FETCH_INTERVAL.getValue.toLong,
        SPARK_ONCE_APP_STATUS_FETCH_INTERVAL.getValue.toLong,
        TimeUnit.MILLISECONDS
      )
  }

  override def supportCallBackLogs(): Boolean = true

}
