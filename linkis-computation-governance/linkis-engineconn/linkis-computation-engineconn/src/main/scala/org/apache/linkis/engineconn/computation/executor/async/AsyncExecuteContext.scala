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

import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.scheduler.{Scheduler, SchedulerContext}
import org.apache.linkis.scheduler.queue.fifoqueue.FIFOGroupFactory
import org.apache.linkis.scheduler.queue.parallelqueue.{
  ParallelScheduler,
  ParallelSchedulerContextImpl
}

trait AsyncExecuteContext {

  def getOrCreateScheduler(executor: AsyncConcurrentComputationExecutor): Scheduler

}

class AsyncExecuteContextImpl extends AsyncExecuteContext {

  private var scheduler: Scheduler = _

  override def getOrCreateScheduler(executor: AsyncConcurrentComputationExecutor): Scheduler = {
    if (null == scheduler) synchronized {
      if (null == scheduler) {
        scheduler = new ParallelScheduler(createSchedulerContext(executor))
        scheduler.init()
      }
    }
    scheduler
  }

  private def createSchedulerContext(
      executor: AsyncConcurrentComputationExecutor
  ): SchedulerContext = {
    val parallelSchedulerContext = new ParallelSchedulerContextImpl(
      ComputationExecutorConf.ASYNC_EXECUTE_MAX_PARALLELISM.getValue
    )
    parallelSchedulerContext.setExecutorManager(new AsyncExecutorManager(executor))
    parallelSchedulerContext.getOrCreateGroupFactory match {
      case groupFactory: FIFOGroupFactory =>
        groupFactory.setDefaultMaxRunningJobs(
          ComputationExecutorConf.ASYNC_SCHEDULER_MAX_RUNNING_JOBS
        )
      case _ =>
    }
    parallelSchedulerContext
  }

}

object AsyncExecuteContext {

  private val CONTEXT: AsyncExecuteContext = new AsyncExecuteContextImpl()

  def getAsyncExecuteContext: AsyncExecuteContext = CONTEXT

}
