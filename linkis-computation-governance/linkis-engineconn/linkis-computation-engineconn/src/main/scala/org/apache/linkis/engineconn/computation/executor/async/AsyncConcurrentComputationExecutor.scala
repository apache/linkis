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

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.acessible.executor.listener.event.TaskResponseErrorEvent
import org.apache.linkis.engineconn.common.conf.EngineConnConf
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask
import org.apache.linkis.engineconn.computation.executor.execute.{
  ComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.computation.executor.hook.ComputationExecutorHook
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.executor.entity.ConcurrentExecutor
import org.apache.linkis.engineconn.executor.listener.{
  EngineConnSyncListenerBus,
  ExecutorListenerBusContext
}
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer._
import org.apache.linkis.scheduler.listener.JobListener
import org.apache.linkis.scheduler.queue.{Job, SchedulerEventState}
import org.apache.linkis.scheduler.queue.SchedulerEventState._

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import java.util
import java.util.concurrent.ConcurrentHashMap

abstract class AsyncConcurrentComputationExecutor(override val outputPrintLimit: Int = 1000)
    extends ComputationExecutor(outputPrintLimit)
    with ConcurrentExecutor
    with JobListener {

  private lazy val scheduler =
    AsyncExecuteContext.getAsyncExecuteContext.getOrCreateScheduler(this)

  private val taskIdToJobMap: util.Map[String, Job] = new ConcurrentHashMap[String, Job](8)

  override def toExecuteTask(
      engineConnTask: EngineConnTask,
      internalExecute: Boolean = false
  ): ExecuteResponse = {

    transformTaskStatus(engineConnTask, ExecutionNodeStatus.Running)

    val job = parserTaskToJob(engineConnTask)
    job.setJobListener(this)
    job.setId(engineConnTask.getTaskId)
    taskIdToJobMap.put(engineConnTask.getTaskId, job)
    Utils.tryCatch(scheduler.submit(job)) { t =>
      val msg = s" submit task(${engineConnTask.getTaskId}) failed"
      getEngineSyncListenerBus.postToAll(TaskResponseErrorEvent(engineConnTask.getTaskId, msg))
      logger.error(msg, t)
      onJobCompleted(job)
    }
    null
  }

  protected def parserTaskToJob(engineConnTask: EngineConnTask): Job = {
    val engineExecutionContext = createEngineExecutionContext(engineConnTask)
    new AsyncEngineConnJob(engineConnTask, engineExecutionContext)
  }

  def asyncExecuteTask(
      engineConnTask: EngineConnTask,
      engineExecutionContext: EngineExecutionContext
  ): ExecuteResponse = {

    var hookedCode = engineConnTask.getCode
    Utils.tryCatch {
      val engineCreationContext = EngineConnObject.getEngineCreationContext
      ComputationExecutorHook.getComputationExecutorHooks.foreach(hook => {
        hookedCode =
          hook.beforeExecutorExecute(engineExecutionContext, engineCreationContext, hookedCode)
      })
    } { e =>
      logger.info("failed to do with hook", e)
      engineExecutionContext.appendStdout(
        LogUtils.generateWarn(s"failed execute hook: ${ExceptionUtils.getStackTrace(e)}")
      )
    }
    if (hookedCode.length > 100) {
      logger.info(s"hooked after code: ${hookedCode.substring(0, 100)} ....")
    } else {
      logger.info(s"hooked after code: $hookedCode ")
    }
    val localPath = EngineConnConf.getLogDir
    engineExecutionContext.appendStdout(
      LogUtils.generateInfo(
        s"EngineConn local log path: ${DataWorkCloudApplication.getServiceInstance.toString} $localPath"
      )
    )

    var response: ExecuteResponse =
      Utils.tryCatch(executeLine(engineExecutionContext, hookedCode)) { t =>
        ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(t), t)
      }

    response match {
      case e: ErrorExecuteResponse =>
        logger.error("execute code failed!", e.t)
        val errorStr = if (e.t != null) {
          ExceptionUtils.getStackTrace(e.t)
        } else StringUtils.EMPTY
        engineExecutionContext.appendStdout(
          LogUtils.generateERROR(s"execute code failed!: $errorStr")
        )
      case SuccessExecuteResponse() =>
        logger.info(s"task{${engineConnTask.getTaskId} execute success")
      case e: OutputExecuteResponse =>
        val output =
          if (StringUtils.isNotEmpty(e.getOutput) && e.getOutput.length > outputPrintLimit) {
            e.getOutput.substring(0, outputPrintLimit)
          } else e.getOutput
        engineExecutionContext.appendStdout(output)
        if (StringUtils.isNotBlank(e.getOutput)) engineExecutionContext.sendResultSet(e)
        response = SuccessExecuteResponse()
      case r: AsynReturnExecuteResponse =>
        val msg = s"task${engineConnTask.getTaskId}  received asyncReturnExecuteResponse"
        logger.info(msg)
        engineExecutionContext.appendStdout(msg)
      case _ =>
        logger.error(s"execute code failed! ${response}")
        response = ErrorExecuteResponse("Unexpected response", null)
    }
    response
  }

  private def getJobByTaskId(taskId: String): Job = taskIdToJobMap.get(taskId)

  private def removeJob(taskId: String): Job = taskIdToJobMap.remove(taskId)

  override def progress(taskID: String): Float = 0f

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] = null

  override def killTask(taskId: String): Unit = {
    Utils.tryAndWarn {
      val task = getTaskById(taskId)
      if (null != task) {
        val job = getJobByTaskId(task.getTaskId)
        if (null != job) {
          logger.info(s"kill job${job.getId()} task is ${task.getTaskId}")
          job.kill()
        }
        /* task.setStatus(ExecutionNodeStatus.Cancelled)
        transformTaskStatus(task, ExecutionNodeStatus.Cancelled) */
      }
    }
  }

  override def killAll(): Unit = {
    val jobs = taskIdToJobMap.values().iterator()
    while (jobs.hasNext) {
      val job = jobs.next()
      if (null != job) {
        logger.info(s"kill job${job.getId()} by killAll")
        job.kill()
      }
    }
  }

  protected override def ensureOp[A](f: => A): A = f

  override def afterExecute(
      engineConnTask: EngineConnTask,
      executeResponse: ExecuteResponse
  ): Unit = {}

  private def getEngineSyncListenerBus: EngineConnSyncListenerBus = {
    ExecutorListenerBusContext.getExecutorListenerBusContext().getEngineConnSyncListenerBus
  }

  // JobListener

  override def onJobScheduled(job: Job): Unit = {}

  override def onJobInited(job: Job): Unit = {}

  override def onJobWaitForRetry(job: Job): Unit = {}

  override def onJobRunning(job: Job): Unit = {
    if (isBusy) {
      logger.error(
        s"Executor is busy but still got new task ! Running task num : ${getRunningTask}"
      )
    }
    if (getRunningTask >= getConcurrentLimit) synchronized {
      if (getRunningTask >= getConcurrentLimit && NodeStatus.isIdle(getStatus)) {
        logger.info(
          s"running task($getRunningTask) > concurrent limit $getConcurrentLimit, now to mark engine to busy "
        )
        transition(NodeStatus.Busy)
      }
    }
    runningTasks.increase()
  }

  override def onJobCompleted(job: Job): Unit = {
    runningTasks.decrease()
    job match {
      case asyncEngineConnJob: AsyncEngineConnJob =>
        job.getState match {
          case Succeed =>
            succeedTasks.increase()
            transformTaskStatus(asyncEngineConnJob.getEngineConnTask, ExecutionNodeStatus.Succeed)
          case Failed =>
            failedTasks.increase()
            transformTaskStatus(asyncEngineConnJob.getEngineConnTask, ExecutionNodeStatus.Failed)
          case _ =>
        }
        if (!SchedulerEventState.isSucceed(job.getState)) {
          getEngineSyncListenerBus.postToAll(
            TaskResponseErrorEvent(
              asyncEngineConnJob.getEngineConnTask.getTaskId,
              job.getErrorResponse.message
            )
          )
        }
        removeJob(asyncEngineConnJob.getEngineConnTask.getTaskId)
        clearTaskCache(asyncEngineConnJob.getEngineConnTask.getTaskId)

      case _ =>
    }

    if (getStatus == NodeStatus.Busy && getConcurrentLimit > getRunningTask) synchronized {
      if (getStatus == NodeStatus.Busy && getConcurrentLimit > getRunningTask) {
        logger.info(
          s"running task($getRunningTask) < concurrent limit $getConcurrentLimit, now to mark engine to Unlock "
        )
        transition(NodeStatus.Unlock)
      }
    }
  }

  override def hasTaskRunning(): Boolean = {
    getRunningTask > 0
  }

}
