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

package org.apache.linkis.entrance.execute

import org.apache.linkis.common.exception.WarnException
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary._
import org.apache.linkis.entrance.exception.EntranceErrorException
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.scheduler.executer.{Executor, ExecutorManager}
import org.apache.linkis.scheduler.queue.{GroupFactory, Job, SchedulerEvent}

import java.util.Date
import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.duration.Duration

abstract class EntranceExecutorManager(groupFactory: GroupFactory)
    extends ExecutorManager
    with Logging {

  private val idGenerator = new AtomicLong(0)

  def getOrCreateInterceptors(): Array[ExecuteRequestInterceptor]

  override def delete(executor: Executor): Unit = {
    if (null != executor) {
      executor.close()
    }
  }

  override def askExecutor(schedulerEvent: SchedulerEvent): Option[Executor] =
    schedulerEvent match {
      case job: Job =>
        Option(createExecutor(job))
    }

  // Update the overall job running status after no subtask runs
  // Until all the tasks are completed, or the task exits abnormally, the overall job ends
  override def askExecutor(schedulerEvent: SchedulerEvent, wait: Duration): Option[Executor] =
    schedulerEvent match {
      case job: Job =>
        val startTime = System.currentTimeMillis()
        var warnException: WarnException = null
        var executor: Option[Executor] = None
        while (System.currentTimeMillis - startTime < wait.toMillis && executor.isEmpty) {
          Utils.tryCatch(executor = askExecutor(job)) {
            case warn: WarnException =>
              logger.warn("request engine failed!", warn)
              warnException = warn
              None
            case t: Throwable => throw t
          }
        }
        // todo check
        if (warnException != null && executor.isEmpty) throw warnException
        executor
    }

  override def getById(id: Long): Option[Executor] = {
//    Option(idToEngines.get(id))o
    null
  }

  override def getByGroup(groupName: String): Array[Executor] = {
    // TODO
    null
  }

  // todo  获取Orchestrator session； 切分job； 提交jobGroup；
  override protected def createExecutor(schedulerEvent: SchedulerEvent): EntranceExecutor =
    schedulerEvent match {
      case job: EntranceJob =>
        job.getJobRequest match {
          case jobReq: JobRequest =>
            val entranceEntranceExecutor =
              new DefaultEntranceExecutor(idGenerator.incrementAndGet())
            // getEngineConn Executor
            job.getLogListener.foreach(
              _.onLogUpdate(
                job,
                LogUtils.generateInfo("Your job is being scheduled by orchestrator.")
              )
            )
            jobReq.setUpdatedTime(new Date(System.currentTimeMillis()))

            /**
             * // val engineConnExecutor = engineConnManager.getAvailableEngineConnExecutor(mark)
             * idToEngines.put(entranceEntranceExecutor.getId, entranceEntranceExecutor)
             */
//          instanceToEngines.put(engineConnExecutor.getServiceInstance.getInstance, entranceEntranceExecutor) // todo
//          entranceEntranceExecutor.setInterceptors(getOrCreateInterceptors()) // todo
            entranceEntranceExecutor
          case _ =>
            throw new EntranceErrorException(
              NOT_CREATE_EXECUTOR.getErrorCode,
              NOT_CREATE_EXECUTOR.getErrorDesc
            )
        }
      case _ =>
        throw new EntranceErrorException(
          ENTRA_NOT_CREATE_EXECUTOR.getErrorCode,
          ENTRA_NOT_CREATE_EXECUTOR.getErrorDesc
        )
    }

  override def shutdown(): Unit = {}

  def getEntranceExecutorByInstance(instance: String): Option[EntranceExecutor] = {
//    Option(instanceToEngines.get(instance))
    null
  }

}
