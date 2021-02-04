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

package com.webank.wedatasphere.linkis.entrance.execute

import java.util.Date

import com.webank.wedatasphere.linkis.common.exception.WarnException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState.ExecutorState
import com.webank.wedatasphere.linkis.scheduler.executer.{Executor, ExecutorManager}
import com.webank.wedatasphere.linkis.scheduler.listener.ExecutorListener
import com.webank.wedatasphere.linkis.scheduler.queue.{GroupFactory, Job, LockJob, SchedulerEvent}

import scala.concurrent.duration.Duration

/**
  * Created by enjoyyin on 2018/9/10.
  */
abstract class EntranceExecutorManager(groupFactory: GroupFactory) extends ExecutorManager with Logging {
  @volatile private var executorListener: Option[ExecutorListener] = None

  def getOrCreateEngineBuilder(): EngineBuilder

  def getOrCreateEngineManager(): EngineManager

  def getOrCreateEngineRequester(): EngineRequester

  def getOrCreateEngineSelector(): EngineSelector

  def getOrCreateEntranceExecutorRulers(): Array[EntranceExecutorRuler]

  def getOrCreateInterceptors(): Array[ExecuteRequestInterceptor]

  private def getExecutorListeners: Array[ExecutorListener] =
    executorListener.map(l => Array(getOrCreateEngineManager(), l)).getOrElse(Array(getOrCreateEngineManager()))

  override def setExecutorListener(executorListener: ExecutorListener): Unit =
    this.executorListener = Option(executorListener)

  def initialEntranceEngine(engine: EntranceEngine): Unit = {
    executorListener.map(_ => new ExecutorListener {
      override def onExecutorCreated(executor: Executor): Unit = getExecutorListeners.foreach(_.onExecutorCreated(executor))
      override def onExecutorCompleted(executor: Executor, message: String): Unit = getExecutorListeners.foreach(_.onExecutorCompleted(executor, message))
      override def onExecutorStateChanged(executor: Executor, fromState: ExecutorState, toState: ExecutorState): Unit =
        getExecutorListeners.foreach(_.onExecutorStateChanged(executor, fromState, toState))
    }).orElse(Some(getOrCreateEngineManager())).foreach(engine.setExecutorListener)
    engine.setInterceptors(getOrCreateInterceptors())
    engine.setEngineLockListener(getOrCreateEngineSelector())
    getExecutorListeners.foreach(_.onExecutorCreated(engine))
  }

  override protected def createExecutor(schedulerEvent: SchedulerEvent): EntranceEngine = schedulerEvent match {
    case job: Job =>
      val newEngine = getOrCreateEngineRequester().request(job)
      newEngine.foreach(initialEntranceEngine)
      //There may be a situation where the broadcast is faster than the return. Here, you need to get the EntranceEngine that is actually stored in the EngineManager.
      //可能存在广播比返回快的情况，这里需拿到实际存入EngineManager的EntranceEngine
      newEngine.flatMap(engine => getOrCreateEngineManager().get(engine.getModuleInstance.getInstance)).orNull
  }

  private def setLock(lock: Option[String], job: Job): Unit = lock.foreach(l => job match {
    case lj: LockJob => lj.setLock(l)
    case _ =>
  })

  protected def findExecutors(job: Job): Array[EntranceEngine] = {
    val groupName = groupFactory.getGroupNameByEvent(job)
    var engines = getOrCreateEngineManager().listEngines(_.getGroup.getGroupName == groupName)
    getOrCreateEntranceExecutorRulers().foreach(ruler => engines = ruler.rule(engines, job))
    engines
  }

  private def findUsefulExecutor(job: Job): Option[Executor] = {
    val engines = findExecutors(job).toBuffer
    if(engines.isEmpty) {
      return None
    }
    var engine: Option[EntranceEngine] = None
    var lock: Option[String] = None
    while(lock.isEmpty && engines.nonEmpty) {
      engine = getOrCreateEngineSelector().chooseEngine(engines.toArray)
      var ruleEngines = engine.map(Array(_)).getOrElse(Array.empty)
      getOrCreateEntranceExecutorRulers().foreach(ruler => ruleEngines = ruler.rule(ruleEngines, job))
      if(engine.isEmpty) {
        return None
      }
      ruleEngines.foreach(e => lock = getOrCreateEngineSelector().lockEngine(e))
      engine.foreach(engines -= _)
    }
    setLock(lock, job)
    lock.flatMap(_ => engine)
  }

  override def askExecutor(schedulerEvent: SchedulerEvent): Option[Executor] = schedulerEvent match {
    case job: Job =>
      findUsefulExecutor(job).orElse {
        val executor = createExecutor(job)
        if(executor != null) {
          job match{
            case entranceExecutionJob: EntranceExecutionJob => val task = entranceExecutionJob.getTask
              task.asInstanceOf[RequestPersistTask].setEngineStartTime(new Date())
            case _ =>
          }
          if(!job.isCompleted){
            val lock = getOrCreateEngineSelector().lockEngine(executor)
            setLock(lock, job)
            lock.map(_ => executor)
          }else Some(executor)
        } else None
      }
  }

  override def askExecutor(schedulerEvent: SchedulerEvent, wait: Duration): Option[Executor] = schedulerEvent match {
    case job: Job =>
      val startTime = System.currentTimeMillis()
      var warnException: WarnException = null
      var executor: Option[Executor] = None
      while(System.currentTimeMillis - startTime < wait.toMillis && executor.isEmpty)
        Utils.tryCatch(askExecutor(job)) {
          case warn: WarnException =>
            this.warn("request engine failed!", warn)
            warnException = warn
            None
          case t: Throwable => throw t
        } match {
          case Some(e) => executor = Option(e)
          case _ =>
            if(System.currentTimeMillis - startTime < wait.toMillis) {
              val interval = math.min(3000, wait.toMillis - System.currentTimeMillis + startTime)
              getOrCreateEngineManager().waitForIdle(interval)
            }
        }
      if(warnException != null && executor.isEmpty) throw warnException
      executor
  }

  override def getById(id: Long): Option[Executor] = Option(getOrCreateEngineManager().get(id))

  override def getByGroup(groupName: String): Array[Executor] =
    getOrCreateEngineManager().listEngines(_.getGroup.getGroupName == groupName).map(_.asInstanceOf)

  override protected def delete(executor: Executor): Unit = {
    getOrCreateEngineManager().delete(executor.getId)
    getExecutorListeners.foreach(_.onExecutorCompleted(executor, "deleted by ExecutorManager."))
  }

  override def shutdown(): Unit = {}
}
