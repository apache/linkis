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

import java.util

import com.webank.wedatasphere.linkis.common.exception.WarnException
import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration.ENGINE_MANAGER_SPRING_APPLICATION_NAME
import com.webank.wedatasphere.linkis.entrance.exception.EntranceErrorException
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob
import com.webank.wedatasphere.linkis.protocol.engine._
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState._
import com.webank.wedatasphere.linkis.scheduler.listener.ExecutorListener
import com.webank.wedatasphere.linkis.scheduler.queue.Job


/**
  * Created by enjoyyin on 2018/9/10.
  */
abstract class EngineRequester extends Logging {

  private val engineInitThreads = new util.HashMap[String, EngineInitThread](1000)
  private var executorListener: Option[ExecutorListener] = None
  protected var engineBuilder: EngineBuilder = _

  def setEngineBuilder(engineBuilder: EngineBuilder) = this.engineBuilder = engineBuilder

  def setExecutorListener(executorListener: ExecutorListener): Unit =
    this.executorListener = Some(executorListener)

  def request(job: Job): Option[EntranceEngine] = {
    val requestEngine = createRequestEngine(job)
    val engineInitThread = getSender.ask(requestEngine) match {
      case ResponseNewEngineStatus(instance, responseEngineStatus) =>
        new EngineInitThread(instance, requestEngine).notifyThread(responseEngineStatus)
      case r: ResponseNewEngine =>
        new EngineInitThread(r.instance, requestEngine).notifyThread(r)
      case warn: WarnException => throw warn //Reporting alarm information(报出告警信息)
    }
    info(s"request a new engine for ${requestEngine.user} which requested by ${requestEngine.creator}, wait for it initial.")
    pushLog(job, LogUtils.generateInfo("Background is starting a new engine for you, it may take several seconds, please wait"))
    engineInitThreads.put(engineInitThread.instance, engineInitThread)
    Utils.tryFinally(engineInitThread.waitUntilInited())(engineInitThreads.remove(engineInitThread.instance))
    info(s"the engine for user ${requestEngine.user} which requested by ${requestEngine.creator} has inited.")
    pushLog(job, LogUtils.generateInfo("Congratulations! Your new engine has started successfully"))
    engineInitThread.getEngine
  }

  /**
    * job to send and write log
    * @param job Job
    * @param log String
    */
  private def pushLog(job:Job, log:String):Unit = {
    job match{
      case entranceJob:EntranceExecutionJob =>
        entranceJob.getLogWriter.foreach(writer => writer.write(log))
        entranceJob.getWebSocketLogWriter.foreach(writer => writer.write(log))
      case _ =>
    }
  }

  def reportNewEngineStatus(responseNewEngineStatus: ResponseNewEngineStatus): Unit =
    if(engineInitThreads.containsKey(responseNewEngineStatus.instance))
      engineInitThreads.get(responseNewEngineStatus.instance).notifyThread(responseNewEngineStatus)

  def reportNewEngine(responseNewEngine: ResponseNewEngine): Unit =
    if(engineInitThreads.containsKey(responseNewEngine.instance))
      engineInitThreads.get(responseNewEngine.instance).notifyThread(responseNewEngine)
    else {
      info(s"receive a new engine ${responseNewEngine.instance}, which is created by this entrance, but has been ignored, now add it.")
      val engine = engineBuilder.buildEngine(responseNewEngine.instance)
      executorListener.foreach(_.onExecutorCreated(engine))
    }


  protected def createRequestEngine(job: Job): RequestEngine
  protected def getSender: Sender = Sender.getSender(ENGINE_MANAGER_SPRING_APPLICATION_NAME.getValue)

  private class EngineInitThread(val instance: String, requestEngine: RequestEngine) {
    private var initError: Option[Exception] = None
    private var engine: EntranceEngine = _
    def getEngine: Option[EntranceEngine] = initError match {
      case Some(exception) => throw exception
      case None => Some(engine)
    }
    def notifyThread(responseEngineStatus: ResponseEngineStatusCallback): this.type = {
      ExecutorState.apply(responseEngineStatus.status) match {
        case Error | Dead | ShuttingDown | Success =>
          initError = Some(new EntranceErrorException(20010, responseEngineStatus.initErrorMsg))
          warn(s"request a new engine for (creator: ${requestEngine.creator}, user: ${requestEngine.user}) failed! reason: " + responseEngineStatus.initErrorMsg)
          this synchronized notify
        case _ =>
      }
      this
    }
    def notifyThread(responseNewEngineStatus: ResponseNewEngineStatus): Unit =
      notifyThread(responseNewEngineStatus.responseEngineStatus)

    def notifyThread(responseNewEngine: ResponseNewEngine): this.type = responseNewEngine match {
      case ResponseNewEngine(applicationName, _instance) =>
        if(_instance != instance) throw new EntranceErrorException(20020, s"instance不一致！$instance != ${_instance}")
        engine = engineBuilder.buildEngine(instance, requestEngine)
        info(s"request a new engine for (creator: ${requestEngine.creator}, user: ${requestEngine.user}) succeed, $engine with state ${engine.state}.")
        this synchronized notify
        this
    }
    def waitUntilInited(): Unit = {
      if(isCompleted) return
      val timeout = requestEngine match {
        case t: TimeoutRequestEngine => t.timeout
        case _ => 0
      }
      val startTime = System.currentTimeMillis
      this synchronized {
        while(timeout <= 0 || System.currentTimeMillis - startTime < timeout) {
          if(timeout > 0) wait(timeout) else wait()
          if(isCompleted) return
        }
      }
      initError = Some(new EntranceErrorException(20011, s"Request new engine timeout for user ${requestEngine.user}, ${requestEngine.creator}!(为用户${requestEngine.user},${requestEngine.creator}请求新引擎超时!)"))
    }
    def isCompleted = engine != null || initError.isDefined
  }

}