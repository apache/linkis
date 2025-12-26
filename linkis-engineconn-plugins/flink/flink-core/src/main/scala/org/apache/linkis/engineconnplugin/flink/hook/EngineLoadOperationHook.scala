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

package org.apache.linkis.engineconnplugin.flink.hook

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import org.apache.linkis.engineconn.acessible.executor.hook.OperationHook
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration
import org.apache.linkis.engineconnplugin.flink.factory.FlinkManagerExecutorFactory
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.engine.{
  EngineOperateRequest,
  EngineOperateResponse
}

import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

import java.util.concurrent.atomic.AtomicInteger

@Service
class EngineLoadOperationHook extends OperationHook with Logging {

  @PostConstruct
  private def init(): Unit = {
    OperationHook.registerOperationHook(this)
    logger.info(s"${getName()} init success.")
  }

  private val taskNum = new AtomicInteger(0)
  private val lock = new Object

  override def getName(): String = getClass.getSimpleName

  override def doPreOperation(
      engineOperateRequest: EngineOperateRequest,
      engineOperateResponse: EngineOperateResponse
  ): Unit = {
    ExecutorManager.getInstance.getReportExecutor match {
      case accessibleExecutor: AccessibleExecutor =>
        accessibleExecutor.updateLastActivityTime()
      case _ =>
    }
    if (
        taskNum.incrementAndGet() >= FlinkEnvConfiguration.FLINK_MANAGER_LOAD_TASK_MAX.getHotValue()
    ) {
      lock.synchronized {
        if (
            taskNum
              .incrementAndGet() >= FlinkEnvConfiguration.FLINK_MANAGER_LOAD_TASK_MAX.getHotValue()
        ) {
          FlinkManagerExecutorFactory.getDefaultExecutor() match {
            case accessibleExecutor: AccessibleExecutor =>
              if (NodeStatus.Busy != accessibleExecutor.getStatus) {
                accessibleExecutor.transition(NodeStatus.Busy)
                logger.warn("The number of tasks exceeds the maximum limit, change status to busy.")
              }
            case _ => logger.error("FlinkManagerExecutorFactory.getDefaultExecutor() is None.")
          }
        }
      }
    }
  }

  override def doPostOperation(
      engineOperateRequest: EngineOperateRequest,
      engineOperateResponse: EngineOperateResponse
  ): Unit = {
    if (taskNum.get() - 1 < FlinkEnvConfiguration.FLINK_MANAGER_LOAD_TASK_MAX.getHotValue()) {
      lock.synchronized {
        if (
            taskNum
              .decrementAndGet() < FlinkEnvConfiguration.FLINK_MANAGER_LOAD_TASK_MAX.getHotValue()
        ) {
          FlinkManagerExecutorFactory.getDefaultExecutor() match {
            case accessibleExecutor: AccessibleExecutor =>
              if (NodeStatus.Busy == accessibleExecutor.getStatus) {
                accessibleExecutor.transition(NodeStatus.Unlock)
                logger.warn(
                  "The number of tasks is less than the maximum limit, change status to unlock."
                )
              }
            case _ => logger.error("FlinkManagerExecutorFactory.getDefaultExecutor() is None.")
          }
        }
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug(s"taskNum: ${taskNum.get()}")
    }
  }

}
