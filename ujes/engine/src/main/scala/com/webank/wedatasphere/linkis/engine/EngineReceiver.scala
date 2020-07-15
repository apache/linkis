/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.engine

import java.lang.management.ManagementFactory
import java.util.concurrent.{Future, TimeUnit}

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.conf.DWCArgumentsParser
import com.webank.wedatasphere.linkis.common.utils.{Logging, OverloadUtils, Utils}
import com.webank.wedatasphere.linkis.engine.annotation.EngineExecutorManagerBeanAnnotation.EngineExecutorManagerAutowiredAnnotation
import com.webank.wedatasphere.linkis.engine.conf.EngineConfiguration
import com.webank.wedatasphere.linkis.engine.conf.EngineConfiguration.{ENGINE_PUSH_LOG_TO_ENTRANCE, ENGINE_PUSH_PROGRESS_TO_ENTRANCE, ENGINE_SUPPORT_PARALLELISM}
import com.webank.wedatasphere.linkis.engine.exception.{EngineErrorException, JobNotExistsException}
import com.webank.wedatasphere.linkis.engine.execute.scheduler.EngineGroupFactory
import com.webank.wedatasphere.linkis.engine.execute.{CommonEngineJob, _}
import com.webank.wedatasphere.linkis.engine.log.{LogHelper, SendAppender}
import com.webank.wedatasphere.linkis.protocol.UserWithCreator
import com.webank.wedatasphere.linkis.protocol.engine._
import com.webank.wedatasphere.linkis.resourcemanager.UserResultResource
import com.webank.wedatasphere.linkis.resourcemanager.client.ResourceManagerClient
import com.webank.wedatasphere.linkis.rpc.exception.DWCRPCRetryException
import com.webank.wedatasphere.linkis.rpc.{Receiver, Sender}
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState.ExecutorState
import com.webank.wedatasphere.linkis.scheduler.executer._
import com.webank.wedatasphere.linkis.scheduler.listener.{ExecutorListener, JobListener, LogListener, ProgressListener}
import com.webank.wedatasphere.linkis.scheduler.queue.{Job, SchedulerEventState}
import com.webank.wedatasphere.linkis.server.{JMap, toJavaMap}
import javax.annotation.PostConstruct
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.{ContextClosedEvent, EventListener}
import org.springframework.stereotype.Component

import scala.concurrent.duration.Duration

/**
  * Created by enjoyyin on 2018/9/3.
  */
@Component
class EngineReceiver extends Receiver with JobListener with ProgressListener with JobProgressListener
  with LogListener with JobLogListener with ResultSetListener with ExecutorListener with Logging {

  private val engineCallback = EngineCallback.mapToEngineCallback(DWCArgumentsParser.getDWCOptionMap)
  private val resourceManagerClient = new ResourceManagerClient(ServiceInstance(engineCallback.applicationName, engineCallback.instance))
  private implicit val userWithCreator = UserWithCreator(System.getProperty("user.name"), DWCArgumentsParser.getDWCOptionMap("creator"))
  @Autowired
  private var engineServer: EngineServer = _
  @EngineExecutorManagerAutowiredAnnotation
  private var engineExecutorManager: EngineExecutorManager = _
  private var engine: EngineExecutor  = _
  private var waitForReleaseLocks: Array[String] = _
  private var parallelismEngineHeartbeat: Option[Future[_]] = None

  //Report the PID first(先上报PID)

  @PostConstruct
  def init(): Unit = {
    val group = engineServer.getEngineContext.getOrCreateScheduler.getSchedulerContext.getOrCreateGroupFactory.asInstanceOf[EngineGroupFactory].getGroup
    waitForReleaseLocks = new Array[String](group.getMaxRunningJobs)
    val pid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0)
    info(s"Starting engineServer($pid)...")
    val port = Sender.getThisInstance.split(":")(1).toInt
    val resultResource = DWCArgumentsParser.getDWCOptionMap.get("ticketId").map(UserResultResource(_, userWithCreator.user))
    if(StringUtils.isEmpty(engineCallback.applicationName) || StringUtils.isEmpty(engineCallback.instance))
      throw new EngineErrorException(40015, "Cannot find the instance information of engineManager, can not complete the callback.(找不到engineManager的instance信息，不能完成callback.)")
    val sender = Sender.getSender(ServiceInstance(engineCallback.applicationName, engineCallback.instance))
    sender.send(ResponseEnginePid(port, pid))
    //Timely report heartbeat(定时汇报心跳)
    val heartbeat = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        val state = if(engineExecutorManager.getEngineExecutor == null) ExecutorState.Starting
        else engineExecutorManager.getEngineExecutor.state
        Utils.tryAndWarn(sender.send(ResponseEngineStatusCallback(port, state.id, null)))
      }
    }, 1000, 3000, TimeUnit.MILLISECONDS)
    engineExecutorManager.setExecutorListener(this)
    engineExecutorManager.setJobLogListener(this)
    engineExecutorManager.setResultSetListener(this)
    engine = Utils.tryCatch {
      engineExecutorManager.askExecutor(null).foreach { case executor: EngineExecutor =>
        resultResource.foreach(resourceManagerClient.resourceInited(_, executor.getActualUsedResources))
        sender.send(ResponseEngineStatusCallback(port, executor.state.id, null))
      }
      heartbeat.cancel(true)
      engineExecutorManager.getEngineExecutor
    } { t =>
      error("init engine failed!", t)
      heartbeat.cancel(true)
      sender.send(ResponseEngineStatusCallback(port, ExecutorState.Error.id, ExceptionUtils.getRootCauseMessage(t)))
      System.exit(-1)
      throw t
    }


    engine.setJobProgressListener(this)
    info(s"${engineServer.getEngineName} engineServer started.")
    /*Add the LogListener to the Appender to listen to the appender. Once the append method is executed, the LogListener executes onLogUpdated.*/
    /*将LogListener 加入到Appender中，用来监听appender，一旦append方法被执行，LogListener就执行onLogUpdated*/

    //RPCAppender.setLogListener(this)
    SendAppender.setLogListener(this)
    //SendAppender.setLogCache(LogHelper.logCache)
    LogHelper.setLogListener(this)
  }
  //TODO Need to be modified to adapt to the new RPC architecture(需要改造，适应新RPC架构)
  private var broadcastSender: Sender = _

  private def initBroadcast(sender: Sender): Unit = if(broadcastSender == null) synchronized {
    if(broadcastSender == null) {
      val method = sender.getClass.getMethod("getApplicationName")
      method.setAccessible(true)
      val applicationName = method.invoke(sender).asInstanceOf[String]
      if(engineCallback.applicationName != applicationName) {
        broadcastSender = Sender.getSender(applicationName)
        info("broadcastSender => " + broadcastSender)
      }
      if(ENGINE_SUPPORT_PARALLELISM.getValue)
        parallelismEngineHeartbeat = Some(Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
          override def run(): Unit = {
            Utils.tryAndWarn(broadcastSender.send(ResponseEngineStatusChanged(Sender.getThisInstance, engine.state.id, engine.state.id, getOverloadInfo, getConcurrentInfo)))
          }
        }, 5000, 5000, TimeUnit.MILLISECONDS))  //TODO period should be set to property
    }
  }

  private def getConcurrentInfo: EngineConcurrentInfo = {
    val consumerManager = engineServer.getEngineContext.getOrCreateScheduler.getSchedulerContext.getOrCreateConsumerManager
    val runningTasks = consumerManager.listConsumers().map(_.getRunningEvents.length).sum
    val pendingTasks = consumerManager.listConsumers().map(_.getConsumeQueue.getWaitingEvents.length).sum
    if(engine != null) EngineConcurrentInfo(runningTasks, pendingTasks, engine.getSucceedNum, engine.getFailedNum)
    else EngineConcurrentInfo(runningTasks, pendingTasks, 0, 0)
  }
  private def getOverloadInfo: EngineOverloadInfo = EngineOverloadInfo(OverloadUtils.getProcessMaxMemory, OverloadUtils.getProcessUsedMemory,
    OverloadUtils.getSystemCPUUsed)
  private def getEngineInfo: ResponseEngineInfo = ResponseEngineInfo(DWCArgumentsParser.getDWCOptionMap.getOrElse(RequestEngine.REQUEST_ENTRANCE_INSTANCE, null),
    userWithCreator.creator, userWithCreator.user, DWCArgumentsParser.getDWCOptionMap)
  private def getEmptyEngineInfo: ResponseEngineInfo = ResponseEngineInfo(DWCArgumentsParser.getDWCOptionMap.getOrElse(RequestEngine.REQUEST_ENTRANCE_INSTANCE, null),
    userWithCreator.creator, userWithCreator.user, new JMap[String, String])

  override def receive(message: Any, sender: Sender): Unit = {
    message match {
      case RequestTaskPause(execId) =>
        engineServer.getEngineContext.getOrCreateScheduler.get(execId).foreach{
          case job: Job => job.pause()
          case _ =>
        }
      case RequestTaskKill(execId) =>
        engineServer.getEngineContext.getOrCreateScheduler.get(execId).foreach{
          case job: Job => job.kill()
          case _ =>
        }
      case RequestTaskResume(execId) =>
        engineServer.getEngineContext.getOrCreateScheduler.get(execId).foreach{
          case job: Job => job.resume()
          case _ =>
        }
      case RequestEngineUnlock(instance, lock) =>
        val lockManager = engineServer.getEngineContext.getOrCreateLockManager
        if(lockManager.isLockExist(lock)) {
          warn(s"try to unlock a lock $lock for instance $instance.")
          lockManager.unlock(lock)
        }
      case RequestKillEngine(_, killApplicationName, killInstance) =>
        warn(s"The ModuleInstance($killApplicationName, $killInstance) ask me to shutdown, now shutdown command is called.")
        System.exit(40080)
    }
  }

  override def receiveAndReply(message: Any, sender: Sender): Any = {
    if(broadcastSender == null) initBroadcast(sender)
    message match {
      case request: RequestTask =>
        if(ExecutorState.isCompleted(engine.state)) throw new DWCRPCRetryException(s"engine $engine completed with state ${engine.state}, please execute it in another engine.")
        if(!EngineConfiguration.CLEAR_LOG.getValue) info("received a new request " + request.getCode)
        if(!engineServer.getEngineContext.getOrCreateLockManager.isLockExist(request.getLock))
          throw new EngineErrorException(40015, "Verify lock failed, illegal engine lock!(验证锁失败，非法的引擎锁！)")
        val job = engineServer.getEngineContext.getOrCreateEngineParser.parseToJob(request)
        job.setJobListener(this)
        job.setLogListener(this)
        job.setProgressListener(this)
        engineServer.getEngineContext.getOrCreateScheduler.submit(job)
        job match {
          case senderContainer: SenderContainer => senderContainer.addSender(sender)
          case _ =>
        }
        ResponseTaskExecute(job.getId)
      case RequestEngineLock(instance, timeout) =>
        engineServer.getEngineContext.getOrCreateLockManager.tryLock(timeout + 1000).map{ lock =>
          info(s"locked a lock $lock for instance $instance.")
          ResponseEngineLock(lock)
        }.getOrElse(ResponseEngineStatus(Sender.getThisInstance, engine.state.id,
          getOverloadInfo, getConcurrentInfo, getEmptyEngineInfo))
      case RequestTaskStatus(execId) =>
        engineServer.getEngineContext.getOrCreateScheduler.get(execId).map {
          case engineJob: EngineJob =>
            if(!engineJob.isCompleted) engineJob.addSender(sender)
            ResponseTaskStatus(execId, engineJob.getState.id)
          case job: Job => ResponseTaskStatus(execId, job.getState.id)
        }.getOrElse(throw new JobNotExistsException(s"job $execId is not exists."))
      case RequestEngineStatus(messageType) =>
        import RequestEngineStatus._
        messageType match {
          case Status_Concurrent => ResponseEngineStatus(Sender.getThisInstance, engine.state.id, null,
            getConcurrentInfo, getEmptyEngineInfo)
          case Status_BasicInfo => ResponseEngineStatus(Sender.getThisInstance, engine.state.id, null, null, getEngineInfo)
          case Status_Overload => ResponseEngineStatus(Sender.getThisInstance, engine.state.id, getOverloadInfo, null, getEmptyEngineInfo)
          case Status_Overload_Concurrent => ResponseEngineStatus(Sender.getThisInstance, engine.state.id, getOverloadInfo, getConcurrentInfo, getEmptyEngineInfo)
          case ALL => ResponseEngineStatus(Sender.getThisInstance, engine.state.id, getOverloadInfo, getConcurrentInfo, getEngineInfo)
          case _ => ResponseEngineStatus(Sender.getThisInstance, engine.state.id, null, null, null)
        }
    }
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {
    if(broadcastSender == null) initBroadcast(sender)
    message match {
      case RequestTaskStatus(execId) =>
        engineServer.getEngineContext.getOrCreateScheduler.get(execId).map {
          case engineJob: EngineJob =>
            engineJob.addSender(sender)
            Utils.tryQuietly(Utils.waitUntil(() => engineJob.isCompleted, duration))
            ResponseTaskStatus(execId, engineJob.getState.id)
          case job: Job =>
            Utils.tryQuietly(Utils.waitUntil(() => job.isCompleted, duration))
            ResponseTaskStatus(execId, job.getState.id)
        }.getOrElse(throw new JobNotExistsException(s"job $execId is not exists."))
      case RequestEngineLock(_, timeout) =>
        debug("request engine lock and timeout is "+timeout.toString +" ,and ask duration is "+duration.toString)
        var lock: Option[String] = None
        Utils.tryQuietly(Utils.waitUntil(() => {
          lock = engineServer.getEngineContext.getOrCreateLockManager.tryLock(timeout + 1000)
          debug("success get a lock from lockManager;"+lock.toString)
          lock.isDefined
        }, duration))
        lock.map(ResponseEngineLock).getOrElse(ResponseEngineStatus(Sender.getThisInstance, engine.state.id,
          getOverloadInfo, getConcurrentInfo, getEmptyEngineInfo))
    }
  }

  private def send(job: Job, message: Any): Unit = job match {
    case senderContainer: SenderContainer =>
      senderContainer.getSenders.foreach(s => Utils.tryAndWarn(s.send(message)))
    case _ =>
  }

  private def ask(job: Job, message: Any): Unit = job match {
    case senderContainer: SenderContainer =>
      senderContainer.getSenders.foreach(s => Utils.tryAndWarn(s.ask(message)))
    case _ =>
  }

  override def onJobScheduled(job: Job): Unit = {
    send(job, ResponseTaskStatus(job.getId, job.getState.id))
  }


  override def onJobInited(job: Job): Unit = onJobScheduled(job)

  override def onJobRunning(job: Job): Unit = {
    waitForReleaseLocks synchronized {
      val index = waitForReleaseLocks.indexOf(null)
      if(index >= 0) waitForReleaseLocks(index) = job match {
        case j: CommonEngineJob => j.getRequestTask.getLock
        case _ => null
      }
    }
    onJobScheduled(job)
  }

  override def onJobWaitForRetry(job: Job): Unit = {
    //TODO
  }

  override def onJobCompleted(job: Job): Unit = {
    LogHelper.pushAllRemainLogs()
    if(!SchedulerEventState.isSucceed(job.getState))
      send(job, ResponseTaskError(job.getId, job.getErrorResponse.message))
    ask(job, ResponseTaskStatus(job.getId, job.getState.id))
  }

  override def onProgressUpdate(job: Job, progress: Float, progressInfo: Array[JobProgressInfo]): Unit =
    if(ENGINE_PUSH_PROGRESS_TO_ENTRANCE.getValue) send(job, ResponseTaskProgress(job.getId, progress, progressInfo))

  override def onLogUpdate(job: Job, log: String): Unit = if(ENGINE_PUSH_LOG_TO_ENTRANCE.getValue) {
    if(job != null) send(job, ResponseTaskLog(job.getId, log))
    else {
      val consumers = engineServer.getEngineContext.getOrCreateScheduler.getSchedulerContext
        .getOrCreateConsumerManager.listConsumers()
      consumers.foreach(_.getRunningEvents.foreach{
        case job: Job => Utils.tryAndWarn(send(job, ResponseTaskLog(job.getId, log)))
      })
    }
  }

  override def onLogUpdate(jobId: String, log: String): Unit = if(ENGINE_PUSH_LOG_TO_ENTRANCE.getValue) {
    val job = engineServer.getEngineContext.getOrCreateScheduler.get(jobId)
    job match {
      case Some(j: Job) => onLogUpdate(j, log)
      case None =>
    }
  }

  override def onResultSetCreated(jobId: String, output: String, alias: String): Unit = {
    val job = engineServer.getEngineContext.getOrCreateScheduler.get(jobId)
    job match {
      case Some(j: Job) => send(j, ResponseTaskResultSet(jobId, output, alias))
      case _ =>
    }
  }

  override def onResultSetCreated(jobId: String, output: String): Unit = onResultSetCreated(jobId, output, null)

  override def onResultSizeCreated(jobId: String, resultSize: Int): Unit =  {
    val job = engineServer.getEngineContext.getOrCreateScheduler.get(jobId)
    job match {
      case Some(j: Job) => ask(j, ResponseTaskResultSize(jobId, resultSize))
      case _ =>
    }
  }

  override def onExecutorCreated(executor: Executor): Unit = {
    info(s"created engine $executor.")
  }

  override def onExecutorCompleted(executor: Executor, message: String): Unit = executor match {
    case engine: EngineExecutor => info(s"engine completed with state ${engine.state}.")
  }


  override def onExecutorStateChanged(executor: Executor, fromState: ExecutorState, toState: ExecutorState): Unit = {
    toState match {
      case ExecutorState.Busy =>
        debug("begin unlock ")
        waitForReleaseLocks synchronized waitForReleaseLocks.indices.foreach { i =>
          if (waitForReleaseLocks(i) != null) {
            debug("begin unlock in  waitForReleaseLocks "+ i.toString)
            engineServer.getEngineContext.getOrCreateLockManager.unlock(waitForReleaseLocks(i))
            waitForReleaseLocks(i) = null
          }
        }
      case _ =>
    }
    if(broadcastSender != null) {  //TODO Optimization, from synchronous to asynchronous(优化，从同步改为异步)
      info(s"broadcast the state of $userWithCreator from $fromState to $toState.")
      Utils.tryAndWarn(broadcastSender.send(ResponseEngineStatusChanged(Sender.getThisInstance, fromState.id, toState.id, getOverloadInfo, getConcurrentInfo)))
    }
    engineExecutorManager.onExecutorStateChanged(fromState, toState)
  }

  override def onProgressUpdate(jobId: String, progress: Float, progressInfo: Array[JobProgressInfo]) = if(ENGINE_PUSH_PROGRESS_TO_ENTRANCE.getValue) {
    val job = engineServer.getEngineContext.getOrCreateScheduler.get(jobId)
    job match {
      case Some(j: Job) => onProgressUpdate(j, progress, progressInfo)
    }
  }

  @EventListener
  def shutdownHook(event: ContextClosedEvent): Unit = if(ExecutorState.isAvailable(engine.state)) {
    info(s"begin to shutdown ${engineServer.getEngineName} for $userWithCreator with ${Sender.getThisInstance}...")
    if(engine != null) Utils.tryAndWarnMsg(engine.tryShutdown())(s"transit engine state from ${engine.state} to ShuttingDown failed!")
    parallelismEngineHeartbeat.foreach(_.cancel(true))
    Utils.tryAndWarnMsg(engineServer.close())("stop EngineServer failed!")
    //    engineServer.getEngineContext.getOrCreateEventListenerBus  //TODO shutdown needed
    if(engine != null) {
      Utils.tryAndWarnMsg(engine.tryDead())("transit engine state from ShuttingDown to Dead failed!")
      Utils.tryQuietly(Thread.sleep(2000)) //Wait 2 seconds, wait for the status to be broadcast(等待2秒，待状态广播完毕)
    }
    warn(s"${engineServer.getEngineName} ${Sender.getThisInstance} for $userWithCreator with state ${engine.state} has stopped successful.")
  }
}
