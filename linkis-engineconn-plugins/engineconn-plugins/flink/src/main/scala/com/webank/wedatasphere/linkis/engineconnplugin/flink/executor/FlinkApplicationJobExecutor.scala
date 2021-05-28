/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engineconnplugin.flink.executor

import java.util.concurrent.{Future, TimeUnit}

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.engineconn.once.executor.{ManageableOnceExecutor, OnceExecutorExecutionContext}
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.deployment.{ClusterDescriptorAdapterFactory, YarnApplicationClusterDescriptorAdapter}
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration._
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.ExecutorInitException

import scala.collection.convert.WrapAsScala._
import scala.concurrent.duration.Duration

/**
  *
  */
class FlinkApplicationJobExecutor(id: Long,
                                  override protected val flinkEngineConnContext: FlinkEngineConnContext)
  extends ManageableOnceExecutor with FlinkJobExecutor {

  private var clusterDescriptor: YarnApplicationClusterDescriptorAdapter = _
  private var daemonThread: Future[_] = _

  protected def submit(onceExecutorExecutionContext: OnceExecutorExecutionContext): Unit = {
    ClusterDescriptorAdapterFactory.create(flinkEngineConnContext.getExecutionContext, null) match {
      case adapter: YarnApplicationClusterDescriptorAdapter => clusterDescriptor = adapter
      case _ => throw new ExecutorInitException("Not support ClusterDescriptorAdapter for flink application.")
    }
    val options = onceExecutorExecutionContext.getOnceExecutorContent.getJobContent.map {
      case (k, v: String) => k -> v
      case (k, v) if v != null => k -> v.toString
      case (k, _) => k -> null
    }.toMap
    val programArguments = FLINK_APPLICATION_ARGS.getValue(options).split(" ")
    val mainClass = FLINK_APPLICATION_MAIN_CLASS.getValue(options)
    clusterDescriptor.deployCluster(programArguments, mainClass)
    if (null == clusterDescriptor.getJobId || null == clusterDescriptor.getClusterID)
      throw new ExecutorInitException("The app " + mainClass + " start failed, no result was returned.")
    setJobID(clusterDescriptor.getJobId.toHexString)
    setApplicationId(clusterDescriptor.getClusterID.toString)
    setApplicationURL(clusterDescriptor.getWebInterfaceUrl)
  }

  override def getId: String = "flinkApp_"+ id

  override def close(): Unit = {
    if(daemonThread != null) daemonThread.cancel(true)
    if(clusterDescriptor != null) {
      clusterDescriptor.cancelJob()
      clusterDescriptor.close()
    }
    flinkEngineConnContext.getExecutionContext.getClusterClientFactory.close()
    super.close()
  }

  override protected def waitToRunning(): Unit = {
    Utils.waitUntil(() => clusterDescriptor.initJobId(), Duration.Inf)
    daemonThread = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        val jobStatus = clusterDescriptor.getJobStatus
        info(s"The jobStatus of $getJobID is $jobStatus.")
        if(jobStatus.isGloballyTerminalState)
          tryFailed()
        else if(jobStatus.isTerminalState) tryShutdown()
      }
    }, FLINK_APPLICATION_STATUS_FETCH_INTERVAL.getValue.toLong, FLINK_APPLICATION_STATUS_FETCH_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)
  }

  override def supportCallBackLogs(): Boolean = true
}