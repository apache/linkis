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
import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import org.apache.linkis.engineconn.acessible.executor.listener.event.{
  TaskLogUpdateEvent,
  TaskResponseErrorEvent,
  TaskStatusChangedEvent
}
import org.apache.linkis.engineconn.acessible.executor.utils.AccessibleExecutorUtils.currentEngineIsUnHealthy
import org.apache.linkis.engineconn.common.conf.{EngineConnConf, EngineConnConstant}
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf.SUPPORT_PARTIAL_RETRY_FOR_FAILED_TASKS_ENABLED
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask
import org.apache.linkis.engineconn.computation.executor.exception.HookExecuteException
import org.apache.linkis.engineconn.computation.executor.hook.ComputationExecutorHook
import org.apache.linkis.engineconn.computation.executor.metrics.ComputationEngineConnMetrics
import org.apache.linkis.engineconn.computation.executor.upstream.event.TaskStatusChangedForUpstreamMonitorEvent
import org.apache.linkis.engineconn.computation.executor.utlis.ComputationEngineUtils
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.executor.entity.{LabelExecutor, ResourceExecutor}
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.governance.common.paser.CodeParser
import org.apache.linkis.governance.common.protocol.task.{
  EngineConcurrentInfo,
  RequestTask,
  ResponseTaskError,
  ResponseTaskStatusWithExecuteCodeIndex
}
import org.apache.linkis.governance.common.utils.{JobUtils, LoggerUtils}
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.label.entity.engine.{EngineType, EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer._

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import java.util
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.JavaConverters._

import com.google.common.cache.{Cache, CacheBuilder}

abstract class ComputationExecutor(val outputPrintLimit: Int = 1000)
    extends AccessibleExecutor
    with ResourceExecutor
    with LabelExecutor
    with Logging {

  private val listenerBusContext = ExecutorListenerBusContext.getExecutorListenerBusContext()

  //  private val taskMap: util.Map[String, EngineConnTask] = new ConcurrentHashMap[String, EngineConnTask](8)
  private val taskCache: Cache[String, EngineConnTask] = CacheBuilder
    .newBuilder()
    .expireAfterAccess(EngineConnConf.ENGINE_TASK_EXPIRE_TIME.getValue, TimeUnit.MILLISECONDS)
    .maximumSize(EngineConnConstant.MAX_TASK_NUM)
    .build()

  private var engineInitialized: Boolean = false

  private var internalExecute: Boolean = false

  private var codeParser: Option[CodeParser] = None

  protected val runningTasks: Count = new Count

  protected val pendingTasks: Count = new Count

  protected val succeedTasks: Count = new Count

  protected val failedTasks: Count = new Count

  protected var lastTask: EngineConnTask = _

  private val MAX_TASK_EXECUTE_NUM = if (null != EngineConnObject.getEngineCreationContext) {
    ComputationExecutorConf.ENGINE_MAX_TASK_EXECUTE_NUM.getValue(
      EngineConnObject.getEngineCreationContext.getOptions
    )
  } else {
    ComputationExecutorConf.ENGINE_MAX_TASK_EXECUTE_NUM.getValue
  }

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
      listenerBusContext.getEngineConnSyncListenerBus.postToAll(
        TaskLogUpdateEvent(
          lastTask.getTaskId,
          LogUtils.generateERROR("EC exits unexpectedly and actively kills the task")
        )
      )
      killTask(lastTask.getTaskId)
    }
    else {
      killTask("By close")
    }
    super.close()
  }

  //  override def getName: String = ComputationExecutorConf.DEFAULT_COMPUTATION_NAME

  protected def ensureOp[A](f: => A): A = if (!isEngineInitialized) {
    f
  } else ensureIdle(f)

  protected def beforeExecute(engineConnTask: EngineConnTask): Unit = {}

  protected def afterExecute(
      engineConnTask: EngineConnTask,
      executeResponse: ExecuteResponse
  ): Unit = {
    Utils.tryAndWarn {
      ComputationExecutorHook.getComputationExecutorHooks.foreach { hook =>
        hook.afterExecutorExecute(engineConnTask, executeResponse)
      }
    }
    val executorNumber = getSucceedNum + getFailedNum
    if (
        MAX_TASK_EXECUTE_NUM > 0 && runningTasks
          .getCount() == 0 && executorNumber > MAX_TASK_EXECUTE_NUM
    ) {
      logger.error(s"Task has reached max execute number $MAX_TASK_EXECUTE_NUM, now  tryShutdown. ")
      ExecutorManager.getInstance.getReportExecutor.tryShutdown()
    }

    // unhealthy node should try to shutdown
    if (runningTasks.getCount() == 0 && currentEngineIsUnHealthy) {
      logger.info("no task running and ECNode is unHealthy, now to mark engine to Finished.")
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

      val engineCreationContext = EngineConnObject.getEngineCreationContext

      var hookedCode = engineConnTask.getCode
      Utils.tryCatch {
        ComputationExecutorHook.getComputationExecutorHooks.foreach(hook => {
          hookedCode =
            hook.beforeExecutorExecute(engineExecutionContext, engineCreationContext, hookedCode)
        })
      } { e =>
        e match {
          case hookExecuteException: HookExecuteException =>
            failedTasks.increase()
            logger.error("failed to do with hook", e)
            return ErrorExecuteResponse("hook execute failed task will be failed", e)
          case _ => logger.info("failed to do with hook", e)
        }
      }
      logger.info(s"ComputationExecutor code hook completed.")

      // task params log
      // spark engine: at org.apache.linkis.engineplugin.spark.executor.SparkEngineConnExecutor.executeLine log special conf
      Utils.tryAndWarn {
        val engineType = LabelUtil.getEngineType(engineCreationContext.getLabels())
        EngineType.mapStringToEngineType(engineType) match {
          case EngineType.HIVE | EngineType.TRINO => printTaskParamsLog(engineExecutionContext)
          case _ =>
        }
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

      val retryEnable: Boolean = SUPPORT_PARTIAL_RETRY_FOR_FAILED_TASKS_ENABLED

      codes.indices.foreach({ index =>
        if (ExecutionNodeStatus.Cancelled == engineConnTask.getStatus) {
          return ErrorExecuteResponse("Job is killed by user!", null)
        }
        var executeFlag = true
        val errorIndex: Int = Integer.valueOf(
          engineConnTask.getProperties
            .getOrDefault(Configuration.EXECUTE_ERROR_CODE_INDEX.key, "-1")
            .toString
        )
        engineExecutionContext.getProperties
          .put(Configuration.EXECUTE_ERROR_CODE_INDEX.key, errorIndex.toString)
        val props: util.Map[String, String] = engineCreationContext.getOptions
        val taskRetry: String =
          props.getOrDefault("linkis.task.retry.switch", "false").toString
        if (java.lang.Boolean.parseBoolean(taskRetry)) {
          // jdbc执行任务重试，如果sql有被set进sql，会导致sql的index错位，这里会将日志打印的index进行减一，保证用户看的index是正常的，然后重试的errorIndex需要加一，保证重试的位置是正确的
          var newIndex = index
          var newErrorIndex = errorIndex
          if (adjustErrorIndexForSetScenarios(engineConnTask)) {
            newIndex = index - 1
            newErrorIndex = errorIndex + 1
          }
          // 重试的时候如果执行过则跳过执行
          if (retryEnable && errorIndex > 0 && index < newErrorIndex) {
            val code = codes(index).trim.toUpperCase()
            val shouldSkip = !isContextStatement(code)

            if (shouldSkip) {
              engineExecutionContext.appendStdout(
                LogUtils.generateInfo(
                  s"task retry with errorIndex: ${errorIndex}, current sql index: ${newIndex} will skip."
                )
              )
              executeFlag = false
            } else {
              if (newIndex >= 0) {
                engineExecutionContext.appendStdout(
                  LogUtils.generateInfo(
                    s"task retry with errorIndex: ${errorIndex}, current sql index: ${newIndex} is a context statement, will execute."
                  )
                )
              }
            }
          }
        } else {
          if (retryEnable && errorIndex > 0 && index < errorIndex) {
            engineExecutionContext.appendStdout(
              LogUtils.generateInfo(
                s"aisql retry with errorIndex: ${errorIndex}, current sql index: ${index} will skip."
              )
            )
            executeFlag = false
          }
        }
        if (executeFlag) {
          val code = codes(index)
          engineExecutionContext.setCurrentParagraph(index + 1)
          response = Utils.tryCatch(if (incomplete.nonEmpty) {
            executeCompletely(engineExecutionContext, code, incomplete.toString())
          } else executeLine(engineExecutionContext, code)) { t =>
            ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(t), t)
          }
          // info(s"Finished to execute task ${engineConnTask.getTaskId}")
          incomplete ++= code
          response match {
            case e: ErrorExecuteResponse =>
              val props: util.Map[String, String] = engineCreationContext.getOptions
              val taskRetry: String =
                props.getOrDefault("linkis.task.retry.switch", "false").toString
              val retryNum: Int =
                Integer.valueOf(props.getOrDefault("linkis.task.retry.num", "0").toString)

              if (retryEnable && !props.isEmpty && "true".equals(taskRetry) && retryNum > 0) {
                logger.info(
                  s"task execute failed, with index: ${index} retryNum: ${retryNum}, and will retry",
                  e.t
                )
                engineExecutionContext.appendStdout(
                  LogUtils.generateInfo(
                    s"task execute failed, with index: ${index} retryNum: ${retryNum}, and will retry"
                  )
                )
                engineConnTask.getProperties
                  .put(Configuration.EXECUTE_ERROR_CODE_INDEX.key, index.toString)
                return ErrorRetryExecuteResponse(e.message, index, e.t)
              } else {
                failedTasks.increase()
                logger.error("execute code failed!", e.t)
                return response
              }
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
              if (StringUtils.isNotBlank(e.getOutput)) {
                engineConnTask.getProperties
                  .put(
                    Configuration.EXECUTE_RESULTSET_ALIAS_NUM.key,
                    engineExecutionContext.getAliasNum.toString
                  )
                engineExecutionContext.sendResultSet(e)
              }
            case _: IncompleteExecuteResponse =>
              incomplete ++= incompleteSplitter
          }
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
        case incompleteExecuteResponse: IncompleteExecuteResponse =>
          ErrorExecuteResponse(
            s"The task cannot be an incomplete response ${incompleteExecuteResponse.message}",
            null
          )
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
        case ErrorRetryExecuteResponse(message, index, throwable) =>
          ComputationEngineUtils.sendToEntrance(
            engineConnTask,
            ResponseTaskError(engineConnTask.getTaskId, message)
          )
          logger.warn(
            s"The task begins executing retries,jobId:${engineConnTask.getTaskId},index:${index} ,message:${message}",
            throwable
          )

          val currentAliasNum = Integer.valueOf(
            engineConnTask.getProperties
              .getOrDefault(Configuration.EXECUTE_RESULTSET_ALIAS_NUM.key, "0")
              .toString
          )

          ComputationEngineUtils.sendToEntrance(
            engineConnTask,
            new ResponseTaskStatusWithExecuteCodeIndex(
              engineConnTask.getTaskId,
              ExecutionNodeStatus.Failed,
              index,
              currentAliasNum
            )
          )
        case errorExecuteResponse: ErrorExecuteResponse =>
          listenerBusContext.getEngineConnSyncListenerBus.postToAll(
            TaskResponseErrorEvent(engineConnTask.getTaskId, errorExecuteResponse.message)
          )
          transformTaskStatus(engineConnTask, ExecutionNodeStatus.Failed)
        case _ => logger.warn(s"task get response is $executeResponse")
      }
      Utils.tryAndWarn(afterExecute(engineConnTask, executeResponse))
      executeResponse
    }
    logger.info(s"Finished to execute task ${engineConnTask.getTaskId}")
    // lastTask = null
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

  /**
   * 检测是否为需要调整错误索引的JDBC SET语句场景
   */
  protected def adjustErrorIndexForSetScenarios(engineConnTask: EngineConnTask): Boolean = {
    var result = false
    Utils.tryAndWarn {
      val executionCode = engineConnTask.getCode
      if (StringUtils.isEmpty(executionCode)) {
        return result
      }

      val engineTypeLabel = engineConnTask.getLables.collectFirst { case label: EngineTypeLabel =>
        label
      }

      result = engineTypeLabel.exists { label =>
        val engineType = label.getEngineType
        if (engineType.equals(EngineType.JDBC.toString)) {
          val upperCode = executionCode.toUpperCase().trim
          val jdbcSetPrefixes =
            ComputationExecutorConf.JDBC_SET_STATEMENT_PREFIXES.getValue.split(",")
          jdbcSetPrefixes.exists(upperCode.startsWith)
        } else {
          false
        }
      }
    }
    result
  }

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

    val errorIndex: Int = Integer.valueOf(
      engineConnTask.getProperties
        .getOrDefault(Configuration.EXECUTE_ERROR_CODE_INDEX.key, "-1")
        .toString
    )
    if (errorIndex > 0) {
      val savedAliasNum = Integer.valueOf(
        engineConnTask.getProperties
          .getOrDefault(Configuration.EXECUTE_RESULTSET_ALIAS_NUM.key, "0")
          .toString
      )
      engineExecutionContext.setResultSetNum(savedAliasNum)
      logger.info(s"Restore aliasNum to $savedAliasNum for retry task")
    }

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

  /**
   * 判断是否为上下文语句，重试时需要保留执行
   *
   * @param code
   *   SQL代码（已转换为大写并去除首尾空格）
   * @return
   *   true表示是上下文语句，false表示不是
   */
  private def isContextStatement(code: String): Boolean = {
    ComputationExecutorConf.CONTEXT_STATEMENT_PREFIXES.getValue.split(",").exists(code.startsWith)
  }

  /**
   * job task log print task params info
   *
   * @param engineExecutorContext
   * @return
   *   Unit
   */

  def printTaskParamsLog(engineExecutorContext: EngineExecutionContext): Unit = {
    val sb = new StringBuilder
    EngineConnObject.getEngineCreationContext.getOptions.asScala.foreach({ case (key, value) =>
      // skip log jobId because it corresponding jobid when the ec created
      if (
          !ComputationExecutorConf.PRINT_TASK_PARAMS_SKIP_KEYS.getValue
            .split(",")
            .exists(_.equals(key))
      ) {
        sb.append(s"${key}=${value}\n")
      }
    })

    sb.append("\n")
    engineExecutorContext.appendStdout(
      LogUtils.generateInfo(s"Your job exec with configs:\n${sb.toString()}\n")
    )
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
