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

package com.webank.wedatasphere.linkis.engineconn.computation.executor.service

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, Executors, Future, TimeUnit}

import com.google.common.cache.{Cache, CacheBuilder}
import com.webank.wedatasphere.linkis.common.listener.Event
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.listener.LogListener
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.listener.event._
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.log.LogHelper
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.service.LockService
import com.webank.wedatasphere.linkis.engineconn.common.conf.{EngineConnConf, EngineConnConstant}
import com.webank.wedatasphere.linkis.engineconn.computation.executor.entity.{CommonEngineConnTask, EngineConnTask}
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.ComputationExecutor
import com.webank.wedatasphere.linkis.engineconn.computation.executor.listener.{ResultSetListener, TaskProgressListener, TaskStatusListener}
import com.webank.wedatasphere.linkis.engineconn.computation.executor.utlis.{ComputationEngineUtils, ComputaionEngineContant}
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import com.webank.wedatasphere.linkis.engineconn.executor.listener.event.EngineConnSyncEvent
import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.governance.common.exception.engineconn.{EngineConnExecutorErrorCode, EngineConnExecutorErrorException}
import com.webank.wedatasphere.linkis.governance.common.protocol.task._
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.rpc.utils.RPCUtils
import com.webank.wedatasphere.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteResponse, IncompleteExecuteResponse, SubmitResponse}
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper
import javax.annotation.PostConstruct
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._


@Component
class TaskExecutionServiceImpl extends TaskExecutionService with Logging with ResultSetListener with LogListener with TaskProgressListener with TaskStatusListener {

  private val executorManager = ExecutorManager.getInstance()
  private val taskExecutedNum = new AtomicInteger(0)
  private var lastTask: EngineConnTask = _
  private var executorService: ExecutorService = _
  private var lastTaskFuture: Future[_] = _

  @Autowired
  private var lockService: LockService = _
  private val asyncListenerBusContext = ExecutorListenerBusContext.getExecutorListenerBusContext.getEngineConnAsyncListenerBus
  private val syncListenerBus = ExecutorListenerBusContext.getExecutorListenerBusContext.getEngineConnSyncListenerBus
  private val taskIdCache: Cache[String, ComputationExecutor] = CacheBuilder.newBuilder().expireAfterAccess(EngineConnConf.ENGINE_TASK_EXPIRE_TIME.getValue, TimeUnit.MILLISECONDS)
    .maximumSize(EngineConnConstant.MAX_TASK_NUM).build()

  @PostConstruct
  def init(): Unit = {
    LogHelper.setLogListener(this)
    syncListenerBus.addListener(this)
    executorService = Executors.newSingleThreadExecutor()
  }

  private def sendToEntrance(task: EngineConnTask, msg: RequestProtocol): Unit = synchronized {
    Utils.tryCatch {
      var sender : Sender = null
      if (null != task && null != task.getCallbackServiceInstance()) {
        sender = Sender.getSender(task.getCallbackServiceInstance())
        sender.send(msg)
      } else {
        // todo
        debug("SendtoEntrance error, cannot find entrance instance.")
      }
    }{
      t =>
        val errorMsg = s"SendToEntrance error. $msg" + t.getCause
        error(errorMsg, t)
        throw new EngineConnExecutorErrorException(EngineConnExecutorErrorCode.SEND_TO_ENTRANCE_ERROR, errorMsg)
    }
  }

  @Receiver
  override def execute(requestTask: RequestTask, smc: ServiceMethodContext): ExecuteResponse = {
    // smc    // todo get sender
    // check lock
    if (StringUtils.isBlank(requestTask.getLock)) {
      error(s"Invalid lock : ${requestTask.getLock} , requestTask : " + BDPJettyServerHelper.gson.toJson(requestTask))
      return ErrorExecuteResponse(s"Invalid lock : ${requestTask.getLock}.", new EngineConnExecutorErrorException(EngineConnExecutorErrorCode.INVALID_PARAMS, "Invalid lock or code(请获取到锁后再提交任务.)"))
    }
    if (!lockService.isLockExist(requestTask.getLock)) {
      error(s"Lock ${requestTask.getLock} not exist, cannot execute.")
      return ErrorExecuteResponse("Lock not exixt", new EngineConnExecutorErrorException(EngineConnExecutorErrorCode.INVALID_LOCK, "Lock : " + requestTask.getLock + " not exist(您的锁无效，请重新获取后再提交)."))
    }

    if (StringUtils.isBlank(requestTask.getCode)) {
      return IncompleteExecuteResponse("Your code is incomplete, it may be that only comments are selected for execution(您的代码不完整，可能是仅仅选中了注释进行执行)")
    }

    // 获取任务类型和任务代码，运行任务
    val taskId: Int = taskExecutedNum.incrementAndGet()
    val retryAble: Boolean = {
      val retry = requestTask.getProperties.getOrDefault(ComputaionEngineContant.RETRYABLE_TYPE_NAME, null)
      if (null != retry) retry.asInstanceOf[Boolean]
      else false
    }
    val task = new CommonEngineConnTask(String.valueOf(taskId), retryAble)
    task.setCode(requestTask.getCode)
    task.setProperties(requestTask.getProperties)
    task.data(ComputaionEngineContant.LOCK_TYPE_NAME, requestTask.getLock)
    task.setStatus(ExecutionNodeStatus.Scheduled)
    val labels = requestTask.getLabels.asScala.toArray
    task.setLabels(labels)
    val entranceServerInstance = RPCUtils.getServiceInstanceFromSender(smc.getSender)
    task.setCallbackServiceInstance(entranceServerInstance)
    val executor = executorManager.getExecutorByLabels(labels)
    executor match {
      case computationExecutor: ComputationExecutor =>
        taskIdCache.put(task.getTaskId, computationExecutor)
        submitTask(task, computationExecutor)
      case o =>
        val msg = "Invalid compuatitionExecutor : " + ComputationEngineUtils.GSON.toJson(o) + ", labels : " + ComputationEngineUtils.GSON.toJson(labels) + ", requestTask : " + ComputationEngineUtils.GSON.toJson(requestTask)
        error(msg)
        ErrorExecuteResponse("Invalid computationExecutor(生成无效的计算引擎，请联系管理员).",
          new EngineConnExecutorErrorException(EngineConnExecutorErrorCode.INVALID_ENGINE_TYPE, msg))
    }

  }


//  override def taskStatus(taskID: String): ResponseTaskStatus = {
//    val task = taskIdCache.get(taskID)
//    ResponseTaskStatus(taskID, task.getStatus.id)
//  }

  private def submitTask(task: CommonEngineConnTask, computationExecutor: ComputationExecutor) = {
    val runTask = new Runnable {
      override def run(): Unit = Utils.tryAndWarn {
        val response = computationExecutor.execute(task)
        response match {
          case ErrorExecuteResponse(message, throwable) =>
            sendToEntrance(task, ResponseTaskError(task.getTaskId, message))
            error(message, throwable)
            computationExecutor.transformTaskStatus(task, ExecutionNodeStatus.Failed)
          case _ =>
        }
      }
    }
    lastTask = task
    lastTaskFuture = executorService.submit(runTask)
    info(s"Task ${task.getTaskId} was submited.")
    SubmitResponse(task.getTaskId)
  }

  override def taskProgress(taskID: String): ResponseTaskProgress = {
    var response = ResponseTaskProgress(taskID, -1, null)
    if (StringUtils.isBlank(taskID)) response
    val executor = taskIdCache.getIfPresent(taskID)
    if (null != executor) {
      val task = executor.getTaskById(taskID)
      if (null != task) {
        if (ExecutionNodeStatus.isCompleted(task.getStatus)) {
          response = ResponseTaskProgress(taskID, 1.0f, null)
        } else {
          response = ResponseTaskProgress(taskID, executor.progress(), executor.getProgressInfo)
        }
      } else {
        response = ResponseTaskProgress(taskID, -1, null)
      }
    } else {
      error(s"Executor of taskId : $taskID is not cached.")
    }
    response
  }

  override def taskLog(taskID: String): ResponseTaskLog = {
    // todo check
    null
  }

//  override def pauseTask(taskID: String): Unit = {
//    val task = taskIdCache.get(taskID)
//    // todo
//  }

  override def killTask(taskID: String): Unit = {
    val executor = taskIdCache.getIfPresent(taskID)
    if (null != executor) {
      executor.killTask(taskID)
    } else {
      error(s"Executor of taskId : $taskID is not cached.")
    }
    Utils.tryAndWarn (Thread.sleep(50))
    if (null != lastTask && lastTask.getTaskId.equalsIgnoreCase(taskID)) {
      if (null != lastTaskFuture && !lastTaskFuture.isDone) {
        Utils.tryAndWarn(lastTaskFuture.cancel(true))
      }
    }
  }

  /*override def resumeTask(taskID: String): Unit = {
    // todo
  }*/

  @Receiver
  override def dealRequestTaskStatus(requestTaskStatus: RequestTaskStatus): ResponseTaskStatus = {
    val task = getTaskByTaskId(requestTaskStatus.execId)
    if (null != task) {
      ResponseTaskStatus(task.getTaskId, task.getStatus)
    } else {
      val msg = "Task null! requestTaskStatus: " + ComputationEngineUtils.GSON.toJson(requestTaskStatus)
      error(msg)
      ResponseTaskStatus(requestTaskStatus.execId, ExecutionNodeStatus.Cancelled)
    }
  }

  @Receiver
  override def dealRequestTaskPause(requestTaskPause: RequestTaskPause): Unit = {
    info(s"Pause is Not supported for task : " + requestTaskPause.execId )
  }

  @Receiver
  override def dealRequestTaskKill(requestTaskKill: RequestTaskKill): Unit = {
    val executor = taskIdCache.getIfPresent(requestTaskKill.execId)
    if (null != executor) {
      executor.killTask(requestTaskKill.execId)
      info(s"TaskId : ${requestTaskKill.execId} was killed by user.")
    } else {
      error(s"Invalid executor : null for taskId : ${requestTaskKill.execId}")
    }
  }

  @Receiver
  override def dealRequestTaskResume(requestTaskResume: RequestTaskResume): Unit = {
    info(s"Resume is Not support for task : " + requestTaskResume.execId )
  }

  override def onEvent(event: EngineConnSyncEvent): Unit = event match {
    case taskStatusChangedEvent: TaskStatusChangedEvent => onTaskStatusChanged(taskStatusChangedEvent)
    case taskProgressUpdateEvent: TaskProgressUpdateEvent => onProgressUpdate(taskProgressUpdateEvent)
    case logUpdateEvent: TaskLogUpdateEvent => onLogUpdate(logUpdateEvent)
    case taskResultCreateEvent: TaskResultCreateEvent => onResultSetCreated(taskResultCreateEvent)
    case taskResultSizeCreatedEvent: TaskResultSizeCreatedEvent => onResultSizeCreated(taskResultSizeCreatedEvent)
    case _ =>
      warn("Unknown event : " + BDPJettyServerHelper.gson.toJson(event))
  }

  override def onLogUpdate(logUpdateEvent: TaskLogUpdateEvent): Unit = {
    if (EngineConnConf.ENGINE_PUSH_LOG_TO_ENTRANCE.getValue) {
      if (null != logUpdateEvent && StringUtils.isNotBlank(logUpdateEvent.taskId)) {
        val task = getTaskByTaskId(logUpdateEvent.taskId)
        if (null != task) {
          sendToEntrance(task, ResponseTaskLog(logUpdateEvent.taskId, logUpdateEvent.log))
        } else {
          error("Task cannot null! logupdateEvent: " + ComputationEngineUtils.GSON.toJson(logUpdateEvent))
        }
      } else {
        val executor = ExecutorManager.getInstance().getDefaultExecutor
        executor match {
          case computationExecutor: ComputationExecutor =>
            sendToEntrance(lastTask, ResponseTaskLog(lastTask.getTaskId, logUpdateEvent.log))
          case _ =>
            error("OnLogUpdate error. Invalid ComputationExecutor : " + ComputationEngineUtils.GSON.toJson(executor))
        }
      }

    }
  }

  override def onTaskStatusChanged(taskStatusChangedEvent: TaskStatusChangedEvent): Unit = {
    val task = getTaskByTaskId(taskStatusChangedEvent.taskId)
    if (null != task) {
      if (ExecutionNodeStatus.isCompleted(taskStatusChangedEvent.toStatus)) {
        lastTask = task
        LogHelper.pushAllRemainLogs()
      }
      sendToEntrance(task, ResponseTaskStatus(taskStatusChangedEvent.taskId, taskStatusChangedEvent.toStatus))
    } else {
      error("Task cannot null! taskStatusChangedEvent: " + ComputationEngineUtils.GSON.toJson(taskStatusChangedEvent))
    }
  }

  override def onProgressUpdate(taskProgressUpdateEvent: TaskProgressUpdateEvent): Unit = {
    if (EngineConnConf.ENGINE_PUSH_LOG_TO_ENTRANCE.getValue) {
      val task = getTaskByTaskId(taskProgressUpdateEvent.taskId)
      if (null != task) {
        sendToEntrance(task, ResponseTaskProgress(taskProgressUpdateEvent.taskId, taskProgressUpdateEvent.progress, taskProgressUpdateEvent.progressInfo))
      } else {
        error("Task cannot null! taskProgressUpdateEvent : " + ComputationEngineUtils.GSON.toJson(taskProgressUpdateEvent))
      }
    }
  }

  override def onResultSetCreated(taskResultCreateEvent: TaskResultCreateEvent): Unit = {
    info(s"start to deal result event ${taskResultCreateEvent.taskId}")
    val task = getTaskByTaskId(taskResultCreateEvent.taskId)
    if (null != task) {
      sendToEntrance(task, ResponseTaskResultSet(
        taskResultCreateEvent.taskId,
        taskResultCreateEvent.resStr,
        taskResultCreateEvent.alias
      ))
    } else {
      error("Task cannot null! taskResultCreateEvent: " + ComputationEngineUtils.GSON.toJson(taskResultCreateEvent))
    }
    info(s"Finished  to deal result event ${taskResultCreateEvent.taskId}")
  }

  def getTaskByTaskId(taskId: String): EngineConnTask = {
    if (StringUtils.isBlank(taskId)) return null
    val executor = taskIdCache.getIfPresent(taskId)
    if (null != executor) {
      executor.getTaskById(taskId)
    } else {
      error(s"Executor of taskId : $taskId is not cached.")
      null
    }
  }

  override def onResultSizeCreated(taskResultSizeCreatedEvent: TaskResultSizeCreatedEvent): Unit = {
    val task = getTaskByTaskId(taskResultSizeCreatedEvent.taskId)
    if (null != task) {
      sendToEntrance(task, ResponseTaskResultSize(
        taskResultSizeCreatedEvent.taskId,
        taskResultSizeCreatedEvent.resultSize
      ))
    } else {
      error("Task cannot null! taskResultSizeCreatedEvent: " + ComputationEngineUtils.GSON.toJson(taskResultSizeCreatedEvent))
    }
  }

  override def onEventError(event: Event, t: Throwable): Unit = {

  }

}
