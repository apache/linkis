/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.orchestrator.execution.impl

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.orchestrator.conf.OrchestratorConfiguration
import org.apache.linkis.orchestrator.execution.{ExecTaskRunner, TaskConsumer}
import org.apache.linkis.orchestrator.listener.task.TaskConsumerEvent
import org.apache.linkis.orchestrator.listener.{OrchestratorAsyncEvent, OrchestratorAsyncListener}

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
    logger.error("Consumer exit, now exit process")
    System.exit(1)
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
