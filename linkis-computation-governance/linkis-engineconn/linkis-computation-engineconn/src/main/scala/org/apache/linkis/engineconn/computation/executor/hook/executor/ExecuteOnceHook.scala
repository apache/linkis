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

package org.apache.linkis.engineconn.computation.executor.hook.executor

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.acessible.executor.listener.ExecutorLockListener
import org.apache.linkis.engineconn.acessible.executor.listener.event.{
  ExecutorLockEvent,
  ExecutorUnLockEvent
}
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineconn.computation.executor.hook.ComputationExecutorHook
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import org.apache.linkis.manager.label.entity.entrance.ExecuteOnceLabel

class ExecuteOnceHook extends ComputationExecutorHook with ExecutorLockListener with Logging {

  private var executeOnce = false

  private var isRegister = false

  private val asyncListenerBusContext =
    ExecutorListenerBusContext.getExecutorListenerBusContext().getEngineConnAsyncListenerBus

  override def getHookName(): String = "ExecuteOnceHook"

  override def beforeExecutorExecute(
      engineExecutionContext: EngineExecutionContext,
      engineCreationContext: EngineCreationContext,
      codeBeforeHook: String
  ): String = {
    executeOnce = engineExecutionContext.getLabels.exists(_.isInstanceOf[ExecuteOnceLabel])
    if (executeOnce && !isRegister) {
      isRegister = true
      asyncListenerBusContext.addListener(this)
      logger.warn("execute once become effective, register lock listener")
    }
    codeBeforeHook
  }

  override def onAddLock(addLockEvent: ExecutorLockEvent): Unit = {}

  override def onReleaseLock(releaseLockEvent: ExecutorUnLockEvent): Unit = {
    if (executeOnce) {
      logger.warn("engine unlock trigger execute once label to shutdown engineConn")
      ExecutorManager.getInstance.getReportExecutor.tryShutdown()
    }
  }

}
