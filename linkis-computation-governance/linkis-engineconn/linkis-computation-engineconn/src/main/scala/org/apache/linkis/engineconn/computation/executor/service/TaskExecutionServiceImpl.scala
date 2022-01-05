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
 
package org.apache.linkis.engineconn.computation.executor.service

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent._

import com.google.common.cache.{Cache, CacheBuilder}
import org.apache.linkis.common.listener.Event
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.listener.LogListener
import org.apache.linkis.engineconn.acessible.executor.listener.event._
import org.apache.linkis.engineconn.acessible.executor.log.LogHelper
import org.apache.linkis.engineconn.acessible.executor.service.LockService
import org.apache.linkis.engineconn.common.conf.{EngineConnConf, EngineConnConstant}
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineconn.computation.executor.entity.{CommonEngineConnTask, EngineConnTask}
import org.apache.linkis.engineconn.computation.executor.execute.{ComputationExecutor, ConcurrentComputationExecutor}
import org.apache.linkis.engineconn.computation.executor.listener.{ResultSetListener, TaskProgressListener, TaskStatusListener}
import org.apache.linkis.engineconn.computation.executor.utlis.{ComputationEngineConstant, ComputationEngineUtils}
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import org.apache.linkis.engineconn.executor.listener.event.EngineConnSyncEvent
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.governance.common.exception.engineconn.{EngineConnExecutorErrorCode, EngineConnExecutorErrorException}
import org.apache.linkis.governance.common.protocol.task._
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.message.annotation.Receiver
import org.apache.linkis.message.builder.ServiceMethodContext
import org.apache.linkis.protocol.message.RequestProtocol
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.utils.RPCUtils
import org.apache.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteResponse, IncompleteExecuteResponse, SubmitResponse}
import org.apache.linkis.server.BDPJettyServerHelper
import javax.annotation.PostConstruct
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._


@Component
class TaskExecutionServiceImpl extends TaskExecutionService with Logging with ResultSetListener with LogListener with TaskProgressListener with TaskStatusListener {

  private lazy val executorManager = ExecutorManager.getInstance
  private val taskExecutedNum = new AtomicInteger(0)
  private var lastTask: EngineConnTask = _
  private var lastTaskFuture: Future[_] = _
  private var lastTaskDaemonFuture: Future[_] = _

  // for concurrent executor
  private var consumerThread: Thread = _
  private var concurrentTaskQueue: BlockingQueue[EngineConnTask] = _

  @Autowired
  private var lockService: LockService = _
  private val asyncListenerBusContext = ExecutorListenerBusContext.getExecutorListenerBusContext().getEngineConnAsyncListenerBus
  private val syncListenerBus = ExecutorListenerBusContext.getExecutorListenerBusContext().getEngineConnSyncListenerBus
  private val taskIdCache: Cache[String, ComputationExecutor] = CacheBuilder.newBuilder().expireAfterAccess(EngineConnConf.ENGINE_TASK_EXPIRE_TIME.getValue, TimeUnit.MILLISECONDS)
    .maximumSize(EngineConnConstant.MAX_TASK_NUM).build()
  lazy private val cachedThreadPool = Utils.newCachedThreadPool(ComputationExecutorConf.ENGINE_CONCURRENT_THREAD_NUM.getValue,
    "ConcurrentEngineConnThreadPool")

  @PostConstruct
  def init(): Unit = {
    LogHelper.setLogListener(this)
    syncListenerBus.addListener(this)
  }

  private def sendToEntrance(task: EngineConnTask, msg: RequestProtocol): Unit = synchronized {
    Utils.tryCatch {
      var sender: Sender = null
      if (null != task && null != task.getCallbackServiceInstance() && null != msg) {
        sender = Sender.getSender(task.getCallbackServiceInstance())
        sender.send(msg)
      } else {
        // todo
        debug("SendtoEntrance error, cannot find entrance instance.")
      }
    } {
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
    info("Received a new task, task content is " + requestTask)
    if (StringUtils.isBlank(requestTask.getLock)) {
      error(s"Invalid lock : ${requestTask.getLock} , requestTask : " + requestTask)
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
      val retry = requestTask.getProperties.getOrDefault(ComputationEngineConstant.RETRYABLE_TYPE_NAME, null)
      if (null != retry) retry.asInstanceOf[Boolean]
      else false
    }
    val task = new CommonEngineConnTask(String.valueOf(taskId), retryAble)
    task.setCode(requestTask.getCode)
    task.setProperties(requestTask.getProperties)
    task.data(ComputationEngineConstant.LOCK_TYPE_NAME, requestTask.getLock)
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
        val labelsStr = if (labels != null) labels.filter(_ != null).map(_.getStringValue).mkString(",") else ""
        val msg = "Invalid computationExecutor : " + o.getClass.getName + ", labels : " + labelsStr + ", requestTask : " + requestTask
        error(msg)
        ErrorExecuteResponse("Invalid computationExecutor(生成无效的计算引擎，请联系管理员).",
          new EngineConnExecutorErrorException(EngineConnExecutorErrorCode.INVALID_ENGINE_TYPE, msg))
    }

  }


  //  override def taskStatus(taskID: String): ResponseTaskStatus = {
  //    val task = taskIdCache.get(taskID)
  //    ResponseTaskStatus(taskID, task.getStatus.id)
  //  }

  private def submitTask(task: CommonEngineConnTask, computationExecutor: ComputationExecutor): ExecuteResponse = {
    info(s"Task ${task.getTaskId} was submited.")
    computationExecutor match {
      case concurrentComputationExecutor: ConcurrentComputationExecutor =>
        submitConcurrentTask(task, concurrentComputationExecutor)
      case _ =>
        submitSyncTask(task, computationExecutor)
    }
  }

  private def submitSyncTask(task: CommonEngineConnTask, computationExecutor: ComputationExecutor): ExecuteResponse = {
    val runTask = new Runnable {
      override def run(): Unit = Utils.tryAndWarn {
        LogHelper.dropAllRemainLogs()
        executeTask(task, computationExecutor)
      }
    }
    lastTask = task
    lastTaskFuture = Utils.defaultScheduler.submit(runTask)
    lastTaskDaemonFuture = openDaemonForTask(task, lastTaskFuture, Utils.defaultScheduler)
    SubmitResponse(task.getTaskId)
  }

  private def submitConcurrentTask(task: CommonEngineConnTask, executor: ConcurrentComputationExecutor): ExecuteResponse = {
    if (null == concurrentTaskQueue) synchronized {
      if (null == concurrentTaskQueue) {
        concurrentTaskQueue = new LinkedBlockingDeque[EngineConnTask]()
      }
    }
    concurrentTaskQueue.put(task)
    if (null == consumerThread) synchronized {
      if (null == consumerThread) {
        consumerThread = new Thread(createConsumerRunnable(executor))
        consumerThread.setDaemon(true)
        consumerThread.setName("ConcurrentTaskQueueFifoConsumerThread")
        consumerThread.start()
      }
    }
    SubmitResponse(task.getTaskId)
  }

  private def createConsumerRunnable(executor: ComputationExecutor): Thread = {
    val consumerRunnable = new Runnable {
      override def run(): Unit = {
        var errCount = 0
        val ERR_COUNT_MAX = 20
        while (true) {
          Utils.tryCatch {
            if (!executor.isBusy && !executor.isClosed) {
              val task = concurrentTaskQueue.take()
              val concurrentJob = new Runnable {
                override def run(): Unit = {
                  lastTask = task
                  Utils.tryCatch {
                    logger.info(s"Start to run task ${task.getTaskId}")
                    executeTask(task, executor)
                  } {
                    case t: Throwable => {
                      errCount += 1
                      logger.error(s"Execute task ${task.getTaskId} failed  :", t)
                      if (errCount > ERR_COUNT_MAX) {
                        logger.error(s"Executor run failed for ${errCount} times over ERROR_COUNT_MAX : ${ERR_COUNT_MAX}, will shutdown.")
                        executor.transition(NodeStatus.ShuttingDown)
                      }
                    }
                  }
                }
              }
              cachedThreadPool.submit(concurrentJob)
            }
            Thread.sleep(20)
          } { case t: Throwable =>
            logger.error(s"consumerThread failed  :", t)
          }
        }
      }
    }
    new Thread(consumerRunnable)
  }

  private def executeTask(task: EngineConnTask, executor: ComputationExecutor): Unit = {
    val response = executor.execute(task)
    response match {
      case ErrorExecuteResponse(message, throwable) =>
        sendToEntrance(task, ResponseTaskError(task.getTaskId, message))
        error(message, throwable)
        LogHelper.pushAllRemainLogs()
        executor.transformTaskStatus(task, ExecutionNodeStatus.Failed)
      case _ => logger.warn(s"task get response is $response")
    }
    clearCache(task.getTaskId)
  }

  /**
   * Open daemon thread
   *
   * @param task      engine conn task
   * @param scheduler scheduler
   * @return
   */
  private def openDaemonForTask(task: EngineConnTask, taskFuture: Future[_], scheduler: ExecutorService): Future[_] = {
    val sleepInterval = ComputationExecutorConf.ENGINE_PROGRESS_FETCH_INTERVAL.getValue
    scheduler.submit(new Runnable {
      override def run(): Unit = Utils.tryAndWarn {
        Utils.tryQuietly(Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS)))
        while (null != taskFuture && !taskFuture.isDone) {
          if (ExecutionNodeStatus.isCompleted(task.getStatus) || ExecutionNodeStatus.isRunning(task.getStatus)) {
            sendToEntrance(task, taskProgress(task.getTaskId))
            Thread.sleep(TimeUnit.MILLISECONDS.convert(sleepInterval, TimeUnit.SECONDS))
          }
        }
      }
    })
  }

  override def taskProgress(taskID: String): ResponseTaskProgress = {
    var response = ResponseTaskProgress(taskID, 0, null)
    if (StringUtils.isBlank(taskID)) return response
    val executor = taskIdCache.getIfPresent(taskID)
    if (null != executor) {
      val task = getTaskByTaskId(taskID)
      if (null != task) {
        if (ExecutionNodeStatus.isCompleted(task.getStatus)) {
          response = ResponseTaskProgress(taskID, 1.0f, null)
        } else {
          response = Utils.tryQuietly(ResponseTaskProgress(taskID, executor.progress(taskID), executor.getProgressInfo(taskID)))
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
    Utils.tryAndWarn(Thread.sleep(50))
    if (null != lastTask && lastTask.getTaskId.equalsIgnoreCase(taskID)) {
      if (null != lastTaskFuture && !lastTaskFuture.isDone) {
        Utils.tryAndWarn {
          lastTaskFuture.cancel(true)
          //Close the daemon also
          lastTaskDaemonFuture.cancel(true)
        }
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
    info(s"Pause is Not supported for task : " + requestTaskPause.execId)
  }

  @Receiver
  override def dealRequestTaskKill(requestTaskKill: RequestTaskKill): Unit = {
    warn(s"Requested to kill task : ${requestTaskKill.execId}")
    val executor = taskIdCache.getIfPresent(requestTaskKill.execId)
    if (null != executor) {
      executor.killTask(requestTaskKill.execId)
      info(s"TaskId : ${requestTaskKill.execId} was killed by user.")
    } else {
      error(s"Kill failed, got invalid executor : null for taskId : ${requestTaskKill.execId}")
    }
  }

  @Receiver
  override def dealRequestTaskResume(requestTaskResume: RequestTaskResume): Unit = {
    info(s"Resume is Not support for task : " + requestTaskResume.execId)
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
          error("Task cannot null! logupdateEvent: " + logUpdateEvent.taskId)
        }
      } else if (null != lastTask) {
        val executor = executorManager.getReportExecutor
        executor match {
          case computationExecutor: ComputationExecutor =>
            if (computationExecutor.isBusy) {
              sendToEntrance(lastTask, ResponseTaskLog(lastTask.getTaskId, logUpdateEvent.log))
            }
          case _ =>
            error("OnLogUpdate error. Invalid ComputationExecutor : " + ComputationEngineUtils.GSON.toJson(executor))
        }
      } else {
        info(s"Task not ready, log will be dropped : ${logUpdateEvent.taskId}")
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
      val toStatus = taskStatusChangedEvent.toStatus
      logger.info(s"send task ${task.getTaskId} status $toStatus to entrance")
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
      error(s"Task cannot null! taskResultCreateEvent: ${taskResultCreateEvent.taskId}")
    }
    info(s"Finished  to deal result event ${taskResultCreateEvent.taskId}")
  }

  private def getTaskByTaskId(taskId: String): EngineConnTask = {
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

  override def clearCache(taskId: String): Unit = {
    if (StringUtils.isBlank(taskId)) return
    Utils.tryAndError {
      val executor = taskIdCache.getIfPresent(taskId)
      if (null != executor) {
        executor.clearTaskCache(taskId)
        taskIdCache.invalidate(taskId)
      }
    }
  }
}
