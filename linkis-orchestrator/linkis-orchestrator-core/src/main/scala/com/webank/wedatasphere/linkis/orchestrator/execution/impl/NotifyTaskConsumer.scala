/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.orchestrator.execution.impl

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.orchestrator.conf.OrchestratorConfiguration
import com.webank.wedatasphere.linkis.orchestrator.execution.{ExecTaskRunner, TaskConsumer}
import com.webank.wedatasphere.linkis.orchestrator.listener.task.TaskConsumerEvent
import com.webank.wedatasphere.linkis.orchestrator.listener.{OrchestratorAsyncEvent, OrchestratorAsyncListener}

/**
  *
  */
abstract class NotifyTaskConsumer extends TaskConsumer with OrchestratorAsyncListener with Logging{

  private val notifyLock = new Array[Byte](0)
  private var isStopped = false

  protected def getWaitTime: Long = OrchestratorConfiguration.TASK_CONSUMER_WAIT.getValue

  protected def beforeFetchLaunchTask(): Unit = {}

  protected def beforeLaunchTask(runnableTasks: Array[ExecTaskRunner]): Unit = {}

  protected def afterLaunchTask(runnableTasks: Array[ExecTaskRunner]): Unit = {}

  override def run(): Unit = {
    while (!isStopped)
      Utils.tryAndErrorMsg {
        beforeFetchLaunchTask()
        val runnableTasks = getExecution.taskManager.getRunnableTasks
        beforeLaunchTask(runnableTasks)
        runnableTasks.foreach(getExecution.taskScheduler.launchTask)
        afterLaunchTask(runnableTasks)
        notifyLock synchronized {
          notifyLock.wait(getWaitTime)
        }
      }("Consumer error")
  }

  override def onEvent(event: OrchestratorAsyncEvent): Unit = event match {
    case taskConsumerEvent: TaskConsumerEvent =>
      notifyLock synchronized {
        notifyLock.notify()
      }
    case _ =>
  }

  override def close(): Unit = isStopped = true
}
