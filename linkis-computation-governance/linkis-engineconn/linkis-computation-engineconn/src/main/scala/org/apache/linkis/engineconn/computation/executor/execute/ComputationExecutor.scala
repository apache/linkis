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
 
package org.apache.linkis.engineconn.computation.executor.execute

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import com.google.common.cache.{Cache, CacheBuilder}
import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import org.apache.linkis.engineconn.acessible.executor.listener.event.TaskStatusChangedEvent
import org.apache.linkis.engineconn.common.conf.{EngineConnConf, EngineConnConstant}
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineconn.computation.executor.creation.ComputationExecutorManager
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask
import org.apache.linkis.engineconn.computation.executor.hook.ComputationExecutorHook
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.core.engineconn.EngineConnManager
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.executor.entity.{LabelExecutor, ResourceExecutor}
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.governance.common.paser.CodeParser
import org.apache.linkis.governance.common.protocol.task.{EngineConcurrentInfo, RequestTask}
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.engineplugin.common.creation.ExecutorFactory
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel
import org.apache.linkis.manager.label.entity.entrance.ExecuteOnceLabel
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer._
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils


abstract class ComputationExecutor(val outputPrintLimit: Int = 1000) extends AccessibleExecutor with ResourceExecutor with LabelExecutor with Logging {

  private val listenerBusContext = ExecutorListenerBusContext.getExecutorListenerBusContext()

  //  private val taskMap: util.Map[String, EngineConnTask] = new ConcurrentHashMap[String, EngineConnTask](8)
  private val taskCache: Cache[String, EngineConnTask] = CacheBuilder.newBuilder().expireAfterAccess(EngineConnConf.ENGINE_TASK_EXPIRE_TIME.getValue, TimeUnit.MILLISECONDS)
    .maximumSize(EngineConnConstant.MAX_TASK_NUM).build()

  private var engineInitialized: Boolean = false

  private var internalExecute: Boolean = false

  private var codeParser: Option[CodeParser] = None

  private var runningTasks: Count = new Count

  private var pendingTasks: Count = new Count

  private var succeedTasks: Count = new Count

  private var failedTasks: Count = new Count

  private var lastTask: EngineConnTask = _

  private val MAX_TASK_EXECUTE_NUM = ComputationExecutorConf.ENGINE_MAX_TASK_EXECUTE_NUM.getValue

  protected def setInitialized(inited: Boolean = true): Unit = this.engineInitialized = inited

  final override def tryReady(): Boolean = {
    transition(NodeStatus.Unlock)
    if (!engineInitialized) {
      engineInitialized = true
    }
    info(s"Executor($getId) is ready.")
    true
  }


  override def init(): Unit = {
    setInitialized()
    info(s"Executor($getId) inited : ${isEngineInitialized}")
  }

  def tryShutdown(): Boolean = {
    transition(NodeStatus.ShuttingDown)
    true
  }

  def tryFailed(): Boolean = {
    this.whenStatus(NodeStatus.ShuttingDown, transition(NodeStatus.Failed))
    true
  }

  override def trySucceed(): Boolean = false

  def getSucceedNum: Int = succeedTasks.getCount()

  def getFailedNum: Int = failedTasks.getCount()

  def getRunningTask: Int = runningTasks.getCount()

  protected def getExecutorConcurrentInfo: EngineConcurrentInfo = EngineConcurrentInfo(getRunningTask, 0, getSucceedNum, getFailedNum)

  def isEngineInitialized: Boolean = engineInitialized

  def isInternalExecute: Boolean = internalExecute

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

  protected def ensureOp[A](f: => A): A = if (!isEngineInitialized)
    f
  else ensureIdle(f)

  protected def beforeExecute(engineConnTask: EngineConnTask): Unit = {}

  protected def afterExecute(engineConnTask: EngineConnTask, executeResponse: ExecuteResponse): Unit = {
    val executorNumber = getSucceedNum + getFailedNum
    if (MAX_TASK_EXECUTE_NUM > 0 && runningTasks.getCount() == 0 && executorNumber > MAX_TASK_EXECUTE_NUM) {
      error(s"Task has reached max execute number $MAX_TASK_EXECUTE_NUM, now  tryShutdown. ")
      ExecutorManager.getInstance.getReportExecutor.tryShutdown()
    }
  }


  def toExecuteTask(engineConnTask: EngineConnTask, internalExecute: Boolean = false): ExecuteResponse = {
    runningTasks.increase()
    this.internalExecute = internalExecute
    Utils.tryFinally{
    transformTaskStatus(engineConnTask, ExecutionNodeStatus.Running)
      val engineExecutionContext = createEngineExecutionContext(engineConnTask)
      var hookedCode = engineConnTask.getCode
      Utils.tryCatch {
        val engineCreationContext = EngineConnObject.getEngineCreationContext
        ComputationExecutorHook.getComputationExecutorHooks.foreach(hook => {
          hookedCode = hook.beforeExecutorExecute(engineExecutionContext, engineCreationContext, hookedCode)
        })
      } ( e => info("failed to do with hook", e))
      if (hookedCode.length > 100) {
        info(s"hooked after code: ${hookedCode.substring(0, 100)} ....")
      } else {
        info(s"hooked after code: $hookedCode ")
      }
      val localPath = EngineConnConf.getLogDir
      engineExecutionContext.appendStdout(LogUtils.generateInfo(s"EngineConn local log path: ${DataWorkCloudApplication.getServiceInstance.toString} $localPath"))
      var response: ExecuteResponse = null
      val incomplete = new StringBuilder
      val codes = Utils.tryCatch(getCodeParser.map(_.parse(hookedCode)).getOrElse(Array(hookedCode))) { e =>
        info("Your code will be submitted in overall mode.", e)
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
          //info(s"Finished to execute task ${engineConnTask.getTaskId}")
          incomplete ++= code
          response match {
            case e: ErrorExecuteResponse =>
              failedTasks.increase()
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
          failedTasks.increase()
      }

      if (null == response && codes.isEmpty) {
        error("This code is empty, and the task will be directly marked as successful.")
        response = SuccessExecuteResponse()
      }

      response = response match {
        case _: OutputExecuteResponse =>
          succeedTasks.increase()
          transformTaskStatus(engineConnTask, ExecutionNodeStatus.Succeed)
          SuccessExecuteResponse()
        case s: SuccessExecuteResponse =>
          succeedTasks.increase()
          transformTaskStatus(engineConnTask, ExecutionNodeStatus.Succeed)
          s
        case _ => response
      }
      response
    }{
      runningTasks.decrease()
      this.internalExecute = false
    }
  }



  def execute(engineConnTask: EngineConnTask): ExecuteResponse = {
    info(s"start to execute task ${engineConnTask.getTaskId}")
    updateLastActivityTime()
    beforeExecute(engineConnTask)
    taskCache.put(engineConnTask.getTaskId, engineConnTask)
    lastTask = engineConnTask
    val response = ensureOp {
      toExecuteTask(engineConnTask)
    }

      Utils.tryAndWarn(afterExecute(engineConnTask, response))
    info(s"Finished to execute task ${engineConnTask.getTaskId}")
    lastTask = null
      response
    }

  def setCodeParser(codeParser: CodeParser): Unit = this.codeParser = Some(codeParser)

  def getCodeParser: Option[CodeParser] = this.codeParser

  def executeLine(engineExecutorContext: EngineExecutionContext, code: String): ExecuteResponse

  protected def incompleteSplitter = "\n"

  def executeCompletely(engineExecutorContext: EngineExecutionContext, code: String, completedLine: String): ExecuteResponse

  def progress(taskID: String): Float

  def getProgressInfo(taskID: String): Array[JobProgressInfo]

  protected def createEngineExecutionContext(engineConnTask: EngineConnTask): EngineExecutionContext = {
    val userCreator = engineConnTask.getLables.find(_.isInstanceOf[UserCreatorLabel])
      .map{case label: UserCreatorLabel => label}.orNull

    val engineExecutionContext = if (null != userCreator && StringUtils.isNotBlank(userCreator.getUser)) {
      new EngineExecutionContext(this, userCreator.getUser)
    } else {
      new EngineExecutionContext(this)
    }
    if (engineConnTask.getProperties.containsKey(RequestTask.RESULT_SET_STORE_PATH)) {
      engineExecutionContext.setStorePath(engineConnTask.getProperties.get(RequestTask.RESULT_SET_STORE_PATH).toString)
    }
    info(s"StorePath : ${engineExecutionContext.getStorePath.orNull}.")
    engineExecutionContext.setJobId(engineConnTask.getTaskId)
    engineExecutionContext.getProperties.putAll(engineConnTask.getProperties)
    engineExecutionContext.setLabels(engineConnTask.getLables)
    engineExecutionContext
  }

  def killTask(taskId: String): Unit = {
    Utils.tryAndWarn {
      val task = getTaskById(taskId)
      if (null != task) {
        task.setStatus(ExecutionNodeStatus.Cancelled)
        transformTaskStatus(task, ExecutionNodeStatus.Cancelled)
      }
    }
  }

  def transformTaskStatus(task: EngineConnTask, newStatus: ExecutionNodeStatus): Unit = {
    val oriStatus = task.getStatus
    info(s"task ${task.getTaskId} from status $oriStatus to new status $newStatus")
    oriStatus match {
      case ExecutionNodeStatus.Scheduled =>
        if (task.getStatus != newStatus) {
          task.setStatus(newStatus)
        }
      case ExecutionNodeStatus.Running =>
        if (newStatus == ExecutionNodeStatus.Succeed || newStatus == ExecutionNodeStatus.Failed || newStatus == ExecutionNodeStatus.Cancelled) {
          task.setStatus(newStatus)
        } else {
          error(s"Task status change error. task: $task, newStatus : $newStatus.")
        }
      case _ =>
        error(s"Task status change error. task: $task, newStatus : $newStatus.")
    }
    if (oriStatus != newStatus && !isInternalExecute) {
      listenerBusContext.getEngineConnSyncListenerBus.postToAll(TaskStatusChangedEvent(task.getTaskId, oriStatus, newStatus))
    }
  }

  def getTaskById(taskId: String): EngineConnTask = {
    taskCache.getIfPresent(taskId)
  }

  def clearTaskCache(taskId: String): Unit = {
    taskCache.invalidate(taskId)
  }
}

class Count{

  val  count = new AtomicInteger(0)

  def  getCount(): Int =  {
    count.get()
  }

  def increase() : Unit = {
    count.incrementAndGet()
  }
  def decrease(): Unit = {
    count.decrementAndGet()
  }
}
