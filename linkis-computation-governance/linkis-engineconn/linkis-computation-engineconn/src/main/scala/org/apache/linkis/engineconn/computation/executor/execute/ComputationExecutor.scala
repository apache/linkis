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

package org.apache.linkis.engineconn.computation.executor.execute

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import org.apache.linkis.engineconn.acessible.executor.listener.event.{
  TaskResponseErrorEvent,
  TaskStatusChangedEvent
}
import org.apache.linkis.engineconn.common.conf.{EngineConnConf, EngineConnConstant}
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask
import org.apache.linkis.engineconn.computation.executor.hook.ComputationExecutorHook
import org.apache.linkis.engineconn.computation.executor.metrics.ComputationEngineConnMetrics
import org.apache.linkis.engineconn.computation.executor.upstream.event.TaskStatusChangedForUpstreamMonitorEvent
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.executor.entity.{LabelExecutor, ResourceExecutor}
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.governance.common.paser.CodeParser
import org.apache.linkis.governance.common.protocol.task.{EngineConcurrentInfo, RequestTask}
import org.apache.linkis.governance.common.utils.{JobUtils, LoggerUtils}
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer._

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import com.google.common.cache.{Cache, CacheBuilder}

abstract class ComputationExecutor(val outputPrintLimit: Int = 1000)
    extends AccessibleExecutor
    with ResourceExecutor
    with LabelExecutor
    with Logging {

  private val listenerBusContext = ExecutorListenerBusContext.getExecutorListenerBusContext()

  private val taskCache: Cache[String, EngineConnTask] = CacheBuilder
    .newBuilder()
    .expireAfterAccess(EngineConnConf.ENGINE_TASK_EXPIRE_TIME.getValue, TimeUnit.MILLISECONDS)
    .maximumSize(EngineConnConstant.MAX_TASK_NUM)
    .build()

  private var engineInitialized: Boolean = false

  private var internalExecute: Boolean = false

  private var codeParser: Option[CodeParser] = None

  protected val runningTasks: Count = new Count

  protected val succeedTasks: Count = new Count

  protected val failedTasks: Count = new Count

  private var lastTask: EngineConnTask = _

  private val MAX_TASK_EXECUTE_NUM = ComputationExecutorConf.ENGINE_MAX_TASK_EXECUTE_NUM.getValue

  private val CLOSE_LOCKER = new Object

  protected def setInitialized(inited: Boolean = true): Unit = this.engineInitialized = inited

  final override def tryReady(): Boolean = {
    transition(NodeStatus.Unlock)
    if (!engineInitialized) {
      engineInitialized = true
    }
    logger.info(s"Executor($getId) is ready.")
    true
  }

  override def init(): Unit = {
    setInitialized()
    logger.info(s"Executor($getId) inited : ${isEngineInitialized}")
  }

  def tryShutdown(): Boolean = {
    transition(NodeStatus.ShuttingDown)
    true
  }

  def tryFailed(): Boolean = {
    this.whenStatus(NodeStatus.ShuttingDown, transition(NodeStatus.Failed))
    true
  }

  override def trySucceed(): Boolean = {
    transition(NodeStatus.Success)
    true
  }

  def getSucceedNum: Int = succeedTasks.getCount()

  def getFailedNum: Int = failedTasks.getCount()

  def getRunningTask: Int = runningTasks.getCount()

  protected def getExecutorConcurrentInfo: EngineConcurrentInfo =
    EngineConcurrentInfo(getRunningTask, 0, getSucceedNum, getFailedNum)

  def isEngineInitialized: Boolean = engineInitialized

  def isInternalExecute: Boolean = internalExecute

  protected def callback(): Unit = {}

  override def close(): Unit = {
    if (null != lastTask) CLOSE_LOCKER.synchronized {
      killTask(lastTask.getTaskId)
    }
    else {
      killTask("By close")
    }
    super.close()
  }

  protected def ensureOp[A](f: => A): A = if (!isEngineInitialized) {
    f
  } else ensureIdle(f)

  protected def beforeExecute(engineConnTask: EngineConnTask): Unit = {}

  protected def afterExecute(
      engineConnTask: EngineConnTask,
      executeResponse: ExecuteResponse
  ): Unit = {
    val executorNumber = getSucceedNum + getFailedNum
    if (
        MAX_TASK_EXECUTE_NUM > 0 && runningTasks
          .getCount() == 0 && executorNumber > MAX_TASK_EXECUTE_NUM
    ) {
      logger.error(s"Task has reached max execute number $MAX_TASK_EXECUTE_NUM, now  tryShutdown. ")
      ExecutorManager.getInstance.getReportExecutor.tryShutdown()
    }
  }

  def toExecuteTask(
      engineConnTask: EngineConnTask,
      internalExecute: Boolean = false
  ): ExecuteResponse = {
    runningTasks.increase()
    this.internalExecute = internalExecute
    Utils.tryFinally {
      transformTaskStatus(engineConnTask, ExecutionNodeStatus.Running)
      val engineExecutionContext = createEngineExecutionContext(engineConnTask)
      var hookedCode = engineConnTask.getCode
      Utils.tryCatch {
        val engineCreationContext = EngineConnObject.getEngineCreationContext
        ComputationExecutorHook.getComputationExecutorHooks.foreach(hook => {
          hookedCode =
            hook.beforeExecutorExecute(engineExecutionContext, engineCreationContext, hookedCode)
        })
      }(e => logger.info("failed to do with hook", e))
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
      var response: ExecuteResponse = null
      val incomplete = new StringBuilder
      val codes =
        Utils.tryCatch(getCodeParser.map(_.parse(hookedCode)).getOrElse(Array(hookedCode))) { e =>
          logger.info("Your code will be submitted in overall mode.", e)
          Array(hookedCode)
        }
      engineExecutionContext.setTotalParagraph(codes.length)
      codes.indices.foreach({ index =>
        if (ExecutionNodeStatus.Cancelled == engineConnTask.getStatus) {
          return ErrorExecuteResponse("Job is killed by user!", null)
        }
        val code = codes(index)
        engineExecutionContext.setCurrentParagraph(index + 1)
        response = Utils.tryCatch(if (incomplete.nonEmpty) {
          executeCompletely(engineExecutionContext, code, incomplete.toString())
        } else executeLine(engineExecutionContext, code)) { t =>
          ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(t), t)
        }
        incomplete ++= code
        response match {
          case e: ErrorExecuteResponse =>
            failedTasks.increase()
            logger.error("execute code failed!", e.t)
            return response
          case SuccessExecuteResponse() =>
            engineExecutionContext.appendStdout("\n")
            incomplete.setLength(0)
          case e: OutputExecuteResponse =>
            incomplete.setLength(0)
            val output =
              if (StringUtils.isNotEmpty(e.getOutput) && e.getOutput.length > outputPrintLimit) {
                e.getOutput.substring(0, outputPrintLimit)
              } else e.getOutput
            engineExecutionContext.appendStdout(output)
            if (StringUtils.isNotBlank(e.getOutput)) engineExecutionContext.sendResultSet(e)
          case _: IncompleteExecuteResponse =>
            incomplete ++= incompleteSplitter
        }
      })
      Utils.tryCatch(engineExecutionContext.close()) { t =>
        response = ErrorExecuteResponse("send resultSet to entrance failed!", t)
        failedTasks.increase()
      }

      if (null == response && codes.isEmpty) {
        logger.warn("This code is empty, the task will be directly marked as successful")
        response = SuccessExecuteResponse()
      }
      response = response match {
        case _: OutputExecuteResponse =>
          succeedTasks.increase()
          SuccessExecuteResponse()
        case s: SuccessExecuteResponse =>
          succeedTasks.increase()
          s
        case _ => response
      }
      response
    } {
      runningTasks.decrease()
      this.internalExecute = false
    }
  }

  def execute(engineConnTask: EngineConnTask): ExecuteResponse = Utils.tryFinally {
    val jobId = JobUtils.getJobIdFromMap(engineConnTask.getProperties)
    LoggerUtils.setJobIdMDC(jobId)
    logger.info(s"start to execute task ${engineConnTask.getTaskId}")
    updateLastActivityTime()
    beforeExecute(engineConnTask)
    taskCache.put(engineConnTask.getTaskId, engineConnTask)
    lastTask = engineConnTask
    val response = ensureOp {
      val executeResponse = toExecuteTask(engineConnTask)
      executeResponse match {
        case successExecuteResponse: SuccessExecuteResponse =>
          transformTaskStatus(engineConnTask, ExecutionNodeStatus.Succeed)
        case errorExecuteResponse: ErrorExecuteResponse =>
          listenerBusContext.getEngineConnSyncListenerBus.postToAll(
            TaskResponseErrorEvent(engineConnTask.getTaskId, errorExecuteResponse.message)
          )
          transformTaskStatus(engineConnTask, ExecutionNodeStatus.Failed)
      }
      executeResponse
    }

    Utils.tryAndWarn(afterExecute(engineConnTask, response))
    logger.info(s"Finished to execute task ${engineConnTask.getTaskId}")
    response
  } {
    LoggerUtils.removeJobIdMDC()
  }

  def setCodeParser(codeParser: CodeParser): Unit = this.codeParser = Some(codeParser)

  def getCodeParser: Option[CodeParser] = this.codeParser

  def executeLine(engineExecutorContext: EngineExecutionContext, code: String): ExecuteResponse

  protected def incompleteSplitter = "\n"

  def executeCompletely(
      engineExecutorContext: EngineExecutionContext,
      code: String,
      completedLine: String
  ): ExecuteResponse

  def progress(taskID: String): Float

  def getProgressInfo(taskID: String): Array[JobProgressInfo]

  protected def createEngineExecutionContext(
      engineConnTask: EngineConnTask
  ): EngineExecutionContext = {
    val userCreator = engineConnTask.getLables
      .find(_.isInstanceOf[UserCreatorLabel])
      .map { case label: UserCreatorLabel => label }
      .orNull

    val engineExecutionContext =
      if (null != userCreator && StringUtils.isNotBlank(userCreator.getUser)) {
        new EngineExecutionContext(this, userCreator.getUser)
      } else {
        new EngineExecutionContext(this)
      }
    if (engineConnTask.getProperties.containsKey(RequestTask.RESULT_SET_STORE_PATH)) {
      engineExecutionContext.setStorePath(
        engineConnTask.getProperties.get(RequestTask.RESULT_SET_STORE_PATH).toString
      )
    }
    logger.info(s"StorePath : ${engineExecutionContext.getStorePath.orNull}.")
    engineExecutionContext.setJobId(engineConnTask.getTaskId)
    engineExecutionContext.getProperties.putAll(engineConnTask.getProperties)
    engineExecutionContext.setLabels(engineConnTask.getLables)
    engineExecutionContext
  }

  def killTask(taskId: String): Unit = {
    Utils.tryAndWarn {
      val task = getTaskById(taskId)
      if (null != task) {
        transformTaskStatus(task, ExecutionNodeStatus.Cancelled)
      }
    }
  }

  def transformTaskStatus(task: EngineConnTask, newStatus: ExecutionNodeStatus): Unit = {
    val oriStatus = task.getStatus
    logger.info(s"task ${task.getTaskId} from status $oriStatus to new status $newStatus")
    oriStatus match {
      case ExecutionNodeStatus.Scheduled =>
        if (task.getStatus != newStatus) {
          task.setStatus(newStatus)
        }
      case ExecutionNodeStatus.Running =>
        if (
            newStatus == ExecutionNodeStatus.Succeed || newStatus == ExecutionNodeStatus.Failed || newStatus == ExecutionNodeStatus.Cancelled
        ) {
          task.setStatus(newStatus)
        } else {
          logger.error(s"Task status change error. task: $task, newStatus : $newStatus.")
        }
      case _ =>
        logger.error(s"Task status change error. task: $task, newStatus : $newStatus.")
    }
    if (oriStatus != newStatus && !isInternalExecute) {
      listenerBusContext.getEngineConnSyncListenerBus.postToAll(
        TaskStatusChangedEvent(task.getTaskId, oriStatus, newStatus)
      )
      listenerBusContext.getEngineConnSyncListenerBus.postToAll(
        TaskStatusChangedForUpstreamMonitorEvent(task.getTaskId, oriStatus, newStatus, task, this)
      )
    }
  }

  def getTaskById(taskId: String): EngineConnTask = {
    taskCache.getIfPresent(taskId)
  }

  def clearTaskCache(taskId: String): Unit = {
    taskCache.invalidate(taskId)
  }

  override protected def onStatusChanged(fromStatus: NodeStatus, toStatus: NodeStatus): Unit = {
    ComputationEngineConnMetrics.updateMetrics(fromStatus, toStatus)
    super.onStatusChanged(fromStatus, toStatus)
  }

}

class Count {

  val count = new AtomicInteger(0)

  def getCount(): Int = {
    count.get()
  }

  def increase(): Unit = {
    count.incrementAndGet()
  }

  def decrease(): Unit = {
    count.decrementAndGet()
  }

}
