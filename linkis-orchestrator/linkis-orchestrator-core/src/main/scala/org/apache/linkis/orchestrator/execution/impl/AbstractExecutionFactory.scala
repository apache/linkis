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

package org.apache.linkis.orchestrator.execution.impl

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.orchestrator.conf.OrchestratorConfiguration
import org.apache.linkis.orchestrator.core.SessionState
import org.apache.linkis.orchestrator.execution.{
  Execution,
  ExecutionFactory,
  TaskManager,
  TaskScheduler
}

/**
 */
abstract class AbstractExecutionFactory extends ExecutionFactory {

  override def createExecution(sessionState: SessionState): Execution = {
    val taskScheduler = getTaskScheduler()
    val taskManager = getTaskManager()
    val execution = new ExecutionImpl(taskScheduler, taskManager, getTaskConsumer(sessionState))
    sessionState.getOrchestratorSyncListenerBus.addListener(execution)
    sessionState.getOrchestratorSyncListenerBus.addListener(taskManager)
    execution.start()
    execution
  }

  override protected def getTaskManager(): TaskManager = {
    val taskManager = new DefaultTaskManager
    // OrchestratorListenerBusContext.getListenerBusContext().getOrchestratorSyncListenerBus.addListener(taskManager)
    taskManager
  }

  override protected def getTaskScheduler(): TaskScheduler = {
    val executorService = Utils.newCachedThreadPool(
      OrchestratorConfiguration.TASK_SCHEDULER_THREAD_POOL.getValue,
      "BaseTaskScheduler-Thread-"
    )
    new BaseTaskScheduler(executorService)
  }

}
