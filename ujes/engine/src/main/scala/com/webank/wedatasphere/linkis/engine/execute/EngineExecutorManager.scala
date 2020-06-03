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

package com.webank.wedatasphere.linkis.engine.execute

import com.webank.wedatasphere.linkis.common.conf.DWCArgumentsParser
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engine.exception.EngineErrorException
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState.{Busy, ExecutorState, Idle}
import com.webank.wedatasphere.linkis.scheduler.executer._
import com.webank.wedatasphere.linkis.scheduler.listener.ExecutorListener
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEvent
import com.webank.wedatasphere.linkis.server.{JMap, toJavaMap}
import org.apache.commons.io.IOUtils

import scala.concurrent.duration.Duration

/**
  * Created by enjoyyin on 2018/9/18.
  */
abstract class EngineExecutorManager extends ExecutorManager with Logging {

  private var executorListener: Option[ExecutorListener] = None
  private var resultSetListener: ResultSetListener = _
  private var jobLogListener: JobLogListener = _
  protected var executor: EngineExecutor = _

  override def setExecutorListener(executorListener: ExecutorListener): Unit = this.executorListener = Some(executorListener)
  def setResultSetListener(resultSetListener: ResultSetListener) = this.resultSetListener = resultSetListener
  def setJobLogListener(jobLogListener: JobLogListener) = this.jobLogListener = jobLogListener

  protected def isSupportParallelism: Boolean

  protected def getOrCreateCodeParser(): CodeParser

  protected def getOrCreateEngineExecutorFactory(): EngineExecutorFactory

  protected def getEngineHooks: Array[EngineHook]

  def getEngineExecutor = executor

  override protected def createExecutor(event: SchedulerEvent): Executor = null

  override def askExecutor(schedulerEvent: SchedulerEvent): Option[Executor] = {
    if(executor == null) synchronized {
      if(executor == null) {
        var options: JMap[String, String] = DWCArgumentsParser.getDWCOptionMap
        //TODO getUDF peaceWong
        getEngineHooks.foreach(hook => options = hook.beforeCreateEngine(options))
        executor = getOrCreateEngineExecutorFactory().createExecutor(options)
        executor.setCodeParser(getOrCreateCodeParser())
        executor.init()
        executor.setLogListener(jobLogListener)
        executor.setResultSetListener(resultSetListener)
        //TODO Consider adding timeout（考虑加上超时时间）
        getEngineHooks.foreach(_.afterCreatedEngine(executor))
        executorListener.foreach(executor.setExecutorListener)
        executorListener.foreach(_.onExecutorCreated(executor))
        executor.ready()
      }
    }
    executor match {
      case engine: EngineExecutor =>
        if(!ExecutorState.isAvailable(engine.state)) throw new EngineErrorException(40000, s"engine不可用，状态为${engine.state}（engine不可用，状态为${engine.state}).")
        else if(isSupportParallelism) Some(engine)
        else if(engine.state == Busy) None else synchronized {
        if(engine.state == Idle) Some(engine) else None
      }
    }
  }

  override def askExecutor(schedulerEvent: SchedulerEvent, wait: Duration): Option[Executor] = {
    val startTime = System.currentTimeMillis()
    askExecutor(schedulerEvent).orElse {
      var executor: Option[Executor] = None
      while(System.currentTimeMillis - startTime < wait.toMillis && executor.isEmpty) {
        this.executor synchronized this.executor.wait(wait.toMillis)
        executor = askExecutor(schedulerEvent)
      }
      executor
    }
  }

  def onExecutorStateChanged(fromState: ExecutorState, toState: ExecutorState): Unit = toState match {
    case Idle => this.executor synchronized this.executor.notify()
    case _ =>
  }

  override def getById(id: Long): Option[Executor] = if(executor == null) None else if(executor.getId == id) Some(executor) else None

  override def getByGroup(groupName: String): Array[Executor] = Array(executor)

  override protected def delete(executor: Executor): Unit = throw new EngineErrorException(40001, s"Unsupported method delete(不支持的方法delete).")

  override def shutdown(): Unit = {
    executor match {
      case s: SingleTaskOperateSupport =>
        Utils.tryAndWarn(s.kill())
      case c: ConcurrentTaskOperateSupport =>
        Utils.tryAndWarn(c.killAll())
    }
    IOUtils.closeQuietly(executor)
  }
}
