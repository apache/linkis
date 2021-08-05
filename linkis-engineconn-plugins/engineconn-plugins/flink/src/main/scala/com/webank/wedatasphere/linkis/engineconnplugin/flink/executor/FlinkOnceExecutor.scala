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
import com.webank.wedatasphere.linkis.engineconn.core.hook.ShutdownHook
import com.webank.wedatasphere.linkis.engineconn.once.executor.{ManageableOnceExecutor, OnceExecutorExecutionContext}
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.deployment.{ClusterDescriptorAdapter, ClusterDescriptorAdapterFactory}
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration.FLINK_ONCE_APP_STATUS_FETCH_INTERVAL
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.ExecutorInitException
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.flink.api.common.JobStatus

import scala.collection.convert.WrapAsScala._


trait FlinkOnceExecutor[T <: ClusterDescriptorAdapter] extends ManageableOnceExecutor with FlinkExecutor {

  protected var clusterDescriptor: T = _
  private var daemonThread: Future[_] = _

  protected def submit(onceExecutorExecutionContext: OnceExecutorExecutionContext): Unit = {
    ClusterDescriptorAdapterFactory.create(flinkEngineConnContext.getExecutionContext) match {
      case adapter: T => clusterDescriptor = adapter
      case _ => throw new ExecutorInitException("Not support ClusterDescriptorAdapter for flink application.")
    }
    val options = onceExecutorExecutionContext.getOnceExecutorContent.getJobContent.map {
      case (k, v: String) => k -> v
      case (k, v) if v != null => k -> v.toString
      case (k, _) => k -> null
    }.toMap
    doSubmit(onceExecutorExecutionContext, options)
    if(isCompleted) return
    if (null == clusterDescriptor.getClusterID)
      throw new ExecutorInitException("The application start failed, since yarn applicationId is null.")
    if(clusterDescriptor.getJobId != null) setJobID(clusterDescriptor.getJobId.toHexString)
    setApplicationId(clusterDescriptor.getClusterID.toString)
    setApplicationURL(clusterDescriptor.getWebInterfaceUrl)
    info(s"Application is started, applicationId: $getApplicationId, applicationURL: $getApplicationURL.")
    info(s"Application is started, applicationId: ${clusterDescriptor.getClusterID}, webUrl: ${clusterDescriptor.getWebInterfaceUrl}.")
  }

  protected def isCompleted: Boolean = isClosed || NodeStatus.isCompleted(getStatus)

  def doSubmit(onceExecutorExecutionContext: OnceExecutorExecutionContext, options: Map[String, String]): Unit

  val id: Long

  override def getId: String = "FlinkOnceApp_"+ id

  protected def closeDaemon(): Unit = {
    if(daemonThread != null) daemonThread.cancel(true)
  }

  override def close(): Unit = {
    closeDaemon()
    if(clusterDescriptor != null) {
      clusterDescriptor.cancelJob()
      clusterDescriptor.close()
    }
    flinkEngineConnContext.getExecutionContext.getClusterClientFactory.close()
    super.close()
  }

  override protected def waitToRunning(): Unit = {
    if(!isCompleted) daemonThread = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        val jobStatus = clusterDescriptor.getJobStatus
        info(s"The jobStatus of $getJobID is $jobStatus.")
        jobStatus match {
          case JobStatus.FAILED | JobStatus.CANCELED =>
            tryFailed()
          case JobStatus.FINISHED =>
            trySucceed()
          case _ =>
        }
      }
    }, FLINK_ONCE_APP_STATUS_FETCH_INTERVAL.getValue.toLong, FLINK_ONCE_APP_STATUS_FETCH_INTERVAL.getValue.toLong, TimeUnit.MILLISECONDS)
  }


  override def trySucceed(): Boolean = {
    super.trySucceed()
    warn(s"$getId has finished with status $getStatus, now stop it.")
    ShutdownHook.getShutdownHook.notifyStop()
    true
  }

  override def tryFailed(): Boolean = {
    super.tryFailed()
    error(s"$getId has failed with status $getStatus, now stop it.")
    ShutdownHook.getShutdownHook.notifyStop()
    true
  }

  override def supportCallBackLogs(): Boolean = true

}
