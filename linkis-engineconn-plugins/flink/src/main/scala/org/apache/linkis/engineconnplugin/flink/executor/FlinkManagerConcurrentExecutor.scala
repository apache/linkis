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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.acessible.executor.conf.AccessibleExecutorConfiguration
import org.apache.linkis.engineconn.acessible.executor.service.EngineConnConcurrentLockService
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.computation.executor.execute.{
  ComputationExecutor,
  ConcurrentComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.once.executor.OnceExecutorExecutionContext
import org.apache.linkis.engineconnplugin.flink.client.deployment.ClusterDescriptorAdapter
import org.apache.linkis.engineconnplugin.flink.config.FlinkLockerServiceHolder
import org.apache.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import org.apache.linkis.engineconnplugin.flink.errorcode.FlinkErrorCodeSummary
import org.apache.linkis.engineconnplugin.flink.exception.JobExecutionException
import org.apache.linkis.engineconnplugin.flink.util.ManagerUtil
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.{
  AsynReturnExecuteResponse,
  ErrorExecuteResponse,
  ExecuteResponse
}

class FlinkManagerConcurrentExecutor(
    val id: Long,
    maxRunningNumber: Int,
    val flinkEngineConnContext: FlinkEngineConnContext
) extends FlinkOnceExecutor[ClusterDescriptorAdapter]
    with FlinkExecutor
    with Logging {

  override protected def submit(
      onceExecutorExecutionContext: OnceExecutorExecutionContext
  ): Unit = {
    logger.info("Succeed to init FlinkManagerExecutor.")
  }

  override def execute(
      onceExecutorExecutionContext: OnceExecutorExecutionContext
  ): ExecuteResponse = {
    val isManager = ManagerUtil.isManager
    val lockService = FlinkLockerServiceHolder.getDefaultLockService()
    if (
        isManager && null != lockService && lockService
          .isInstanceOf[EngineConnConcurrentLockService]
    ) {
      val msg = "Succeed to init FlinkManagerExecutor."
      logger.info(msg)
      new AsynReturnExecuteResponse {
        override def notify(rs: ExecuteResponse => Unit): Unit = {
          logger.info(s"FlinkManagerExecutor will skip listener : ${rs}")
        }
      }
    } else {
      ErrorExecuteResponse(
        "FlinkManagerExecutor got default lockService is not instance of EngineConnConcurrentLockService, will shutdown.",
        null
      )
    }
  }

  override def getId: String = id.toString

  override def close(): Unit = {
    logger.info(s"FlinkManagerExecutor : ${getId} will close.")
    super.close()
  }

  def getMaxRunningNumber: Int = maxRunningNumber

  def getFlinkContext(): FlinkEngineConnContext = flinkEngineConnContext

  override def doSubmit(
      onceExecutorExecutionContext: OnceExecutorExecutionContext,
      options: Map[String, String]
  ): Unit = submit(onceExecutorExecutionContext)

  override protected def initOnceExecutorExecutionContext(
      onceExecutorExecutionContext: OnceExecutorExecutionContext
  ): Unit = {}

  override protected def createOnceExecutorExecutionContext(
      engineCreationContext: EngineCreationContext
  ): OnceExecutorExecutionContext = new OnceExecutorExecutionContext(null, null)

  override def tryReady(): Boolean = {
    // set default status to Unlock
    transition(NodeStatus.Unlock)
    true
  }

}
