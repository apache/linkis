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

package org.apache.linkis.engineconn.computation.executor.async

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.scheduler.executer.{Executor, ExecutorManager}
import org.apache.linkis.scheduler.listener.ExecutorListener
import org.apache.linkis.scheduler.queue.SchedulerEvent

import scala.concurrent.duration.Duration

class AsyncExecutorManager(executor: AsyncConcurrentComputationExecutor)
    extends ExecutorManager
    with Logging {

  private val asyncExecuteExecutor: AsyncExecuteExecutor = new AsyncExecuteExecutor(executor)

  override def setExecutorListener(engineListener: ExecutorListener): Unit = {}

  override protected def createExecutor(event: SchedulerEvent): Executor = {
    asyncExecuteExecutor
  }

  override def askExecutor(event: SchedulerEvent): Option[Executor] = {
    Some(createExecutor(event))
  }

  override def askExecutor(event: SchedulerEvent, wait: Duration): Option[Executor] = {
    Some(createExecutor(event))
  }

  override def getById(id: Long): Option[Executor] = {
    None
  }

  override def getByGroup(groupName: String): Array[Executor] = {
    Array.empty[Executor]
  }

  override def delete(executor: Executor): Unit = {}

  override def shutdown(): Unit = {}
}
