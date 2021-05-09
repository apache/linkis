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

package com.webank.wedatasphere.linkis.engineconn.computation.executor.execute

import java.util.concurrent.TimeUnit

import com.google.common.cache.{Cache, CacheBuilder}
import com.webank.wedatasphere.linkis.DataWorkCloudApplication
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.listener.event.TaskStatusChangedEvent
import com.webank.wedatasphere.linkis.engineconn.common.conf.{EngineConnConf, EngineConnConstant}
import com.webank.wedatasphere.linkis.engineconn.computation.executor.entity.EngineConnTask
import com.webank.wedatasphere.linkis.engineconn.computation.executor.hook.ComputationExecutorHook
import com.webank.wedatasphere.linkis.engineconn.computation.executor.parser.CodeParser
import com.webank.wedatasphere.linkis.engineconn.core.engineconn.EngineConnManager
import com.webank.wedatasphere.linkis.engineconn.executor.entity.{LabelExecutor, ResourceExecutor}
import com.webank.wedatasphere.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.governance.common.protocol.task.{EngineConcurrentInfo, RequestTask}
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.ExecutorFactory
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.scheduler.executer._
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils


abstract class ComputationExecutor(val outputPrintLimit: Int = 1000) extends AccessibleExecutor with ResourceExecutor with LabelExecutor with Logging {

  private val listenerBusContext = ExecutorListenerBusContext.getExecutorListenerBusContext

  //  private val taskMap: util.Map[String, EngineConnTask] = new ConcurrentHashMap[String, EngineConnTask](8)
  protected val taskCache: Cache[String, EngineConnTask] = CacheBuilder.newBuilder().expireAfterAccess(EngineConnConf.ENGINE_TASK_EXPIRE_TIME.getValue, TimeUnit.MILLISECONDS)
    .maximumSize(EngineConnConstant.MAX_TASK_NUM).build()

  private var engineInitialized: Boolean = false

  private var codeParser: Option[CodeParser] = None

  private var runningTasks: Int = 0

  private var pendingTasks: Int = 0

  private var succeedTasks: Int = 0

  private var failedTasks: Int = 0

  private var lastTask: EngineConnTask = null

  final override def tryReady: Boolean = {
    transition(NodeStatus.Unlock)
    if (!engineInitialized) {
      engineInitialized = true
    }
    true
  }


  override def init: Unit = {
    info(s"executor($getId()) is ready ")
    tryReady
  }

  def tryShutdown(): Boolean = {
    this.ensureAvailable(transition(NodeStatus.ShuttingDown))
    close()
    true
  }

  def tryFailed(): Boolean = {
    this.whenStatus(NodeStatus.ShuttingDown, transition(NodeStatus.Failed))
    true
  }


  def getSucceedNum = succeedTasks

  def getFailedNum = failedTasks

  def getRunningTask: Int = runningTasks

  private def getExecutorConcurrentInfo: EngineConcurrentInfo = EngineConcurrentInfo(runningTasks, pendingTasks, succeedTasks, failedTasks)

  def isEngineInitialized = engineInitialized

  protected def callback(): Unit = {}

  override def close(): Unit = {
    if (null != lastTask) synchronized {
      killTask(lastTask.getTaskId)
    } else {
      killTask("By close")
    }
    super.close()
  }

  //  override def getName: String = ComputationExecutorConf.DEFAULT_COMPUTATION_NAME

  protected def ensureOp[A](f: => A): A = if (!this.engineInitialized)
    f
  else ensureIdle(f)

  def execute(engineConnTask: EngineConnTask): ExecuteResponse = {

    runningTasks += 1

    taskCache.put(engineConnTask.getTaskId, engineConnTask)
    lastTask = engineConnTask

    transformTaskStatus(engineConnTask, ExecutionNodeStatus.Running)

    ensureOp {
      val engineExecutionContext = createEngineExecutionContext(engineConnTask)
      var hookedCode = engineConnTask.getCode
      Utils.tryCatch {
        val engineCreationContext = EngineConnManager.getEngineConnManager.getEngineConn().getEngineCreationContext
        ComputationExecutorHook.getComputationExecutorHooks.foreach(hook => {
          hookedCode = hook.beforeExecutorExecute(engineExecutionContext, engineCreationContext, hookedCode)
        })
      } {
        case e: Throwable => logger.info("failed to do with hook", e)
      }
      if (hookedCode.length > 100) {
        info(s"hooked after code: ${hookedCode.substring(0, 100)} ....")
      } else {
        info(s"hooked after code: $hookedCode ")
      }
      val localPath = System.getenv(EngineConnConf.ENGINE_CONN_LOCAL_LOG_DIRS_KEY.getValue)
      engineExecutionContext.appendStdout(s"EngineConn local log path : ${DataWorkCloudApplication.getServiceInstance.toString}  ${localPath}")
      var response: ExecuteResponse = null
      val incomplete = new StringBuilder
      val codes = Utils.tryCatch(getCodeParser().map(_.parse(hookedCode, engineExecutionContext)).getOrElse(Array(hookedCode))) { e =>
        info("Your code will be submitted in overall mode")
        Array(hookedCode)
      }
      engineExecutionContext.setTotalParagraph(codes.length)
      codes.indices.foreach({
        index =>
          if (ExecutionNodeStatus.Cancelled == engineConnTask.getStatus) return ErrorExecuteResponse("Job is killed by user!", null)
          val code = codes(index)
          engineExecutionContext.setCurrentParagraph(index + 1)
          response = Utils.tryCatch(if (incomplete.nonEmpty) executeCompletely(engineExecutionContext, code, incomplete.toString())
          else executeLine(engineExecutionContext, code)
          ) {
            t => ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(t), t)
          }
          info(s"Finished to execute task ${engineConnTask.getTaskId}")
          incomplete ++= code
          response match {
            case e: ErrorExecuteResponse =>
              failedTasks += 1
              error("execute code failed!", e.t)
              return response
            case SuccessExecuteResponse() =>
              engineExecutionContext.appendStdout("\n")
              incomplete.setLength(0)
            case e: OutputExecuteResponse =>
              incomplete.setLength(0)
              val output = if (StringUtils.isNotEmpty(e.getOutput) && e.getOutput.length > outputPrintLimit)
                e.getOutput.substring(0, outputPrintLimit) else e.getOutput
              engineExecutionContext.appendStdout(output)
              if (StringUtils.isNotBlank(e.getOutput)) engineExecutionContext.sendResultSet(e)
            case _: IncompleteExecuteResponse =>
              incomplete ++= incompleteSplitter
          }
      })
      Utils.tryCatch(engineExecutionContext.close()) {
        t =>
          response = ErrorExecuteResponse("send resultSet to entrance failed!", t)
          failedTasks += 1
      }
      runningTasks -= 1
      lastTask = null
      response match {
        case _: OutputExecuteResponse =>
          succeedTasks += 1
          transformTaskStatus(engineConnTask, ExecutionNodeStatus.Succeed)
          SuccessExecuteResponse()
        case s: SuccessExecuteResponse =>
          succeedTasks += 1
          transformTaskStatus(engineConnTask, ExecutionNodeStatus.Succeed)
          s
        case _ => response
      }
    }


  }

  def setCodeParser(codeParser: CodeParser) = this.codeParser = Some(codeParser)

  def getCodeParser(): Option[CodeParser] = this.codeParser

  def executeLine(engineExecutorContext: EngineExecutionContext, code: String): ExecuteResponse

  protected def incompleteSplitter = "\n"

  def executeCompletely(engineExecutorContext: EngineExecutionContext, code: String, completedLine: String): ExecuteResponse

  def progress(): Float

  def getProgressInfo: Array[JobProgressInfo]

  protected def createEngineExecutionContext(engineConnTask: EngineConnTask): EngineExecutionContext = {
    val userCreator = ExecutorFactory.parseUserWithCreator(engineConnTask.getLables)

    val engineExecutionContext = if (null != userCreator && StringUtils.isNotBlank(userCreator.user)) {
      new EngineExecutionContext(this, userCreator.user)
    } else {
      new EngineExecutionContext(this)
    }
    if (engineConnTask.getProperties.containsKey(RequestTask.RESULT_SET_STORE_PATH)) {
      engineExecutionContext.setStorePath(engineConnTask.getProperties.get(RequestTask.RESULT_SET_STORE_PATH).toString)
    }
    engineExecutionContext.setJobId(engineConnTask.getTaskId)
    engineExecutionContext.getProperties.putAll(engineConnTask.getProperties)
    engineExecutionContext.setLabels(engineConnTask.getLables)
    engineExecutionContext
  }

  def killTask(taskId: String): Unit = {
    Utils.tryAndWarn {
      val task = taskCache.getIfPresent(taskId)
      if (null != task) {
        task.setStatus(ExecutionNodeStatus.Cancelled)
        transformTaskStatus(task, ExecutionNodeStatus.Cancelled)
      }
    }
  }

  def transformTaskStatus(task: EngineConnTask, newStatus: ExecutionNodeStatus) = {
    val oriStatus = task.getStatus
    oriStatus match {
      case ExecutionNodeStatus.Scheduled =>
        if (task.getStatus != newStatus) {
          task.setStatus(newStatus)
        }
      case ExecutionNodeStatus.Running =>
        if (newStatus == ExecutionNodeStatus.Succeed || newStatus == ExecutionNodeStatus.Failed || newStatus == ExecutionNodeStatus.Cancelled) {
          task.setStatus(newStatus)
        } else {
          error(s"Task status change error. task: ${task}, newStatus : ${newStatus}")
        }
      case _ =>
        error(s"Task status change error. task: ${task}, newStatus : ${newStatus}")
    }
    if (oriStatus != newStatus) {
      listenerBusContext.getEngineConnSyncListenerBus.postToAll(TaskStatusChangedEvent(task.getTaskId, oriStatus, newStatus))
    }
  }

  def getTaskById(taskId: String): EngineConnTask = {
    taskCache.getIfPresent(taskId)
  }

}
