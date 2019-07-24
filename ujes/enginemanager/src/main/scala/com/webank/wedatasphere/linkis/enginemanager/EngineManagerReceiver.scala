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

package com.webank.wedatasphere.linkis.enginemanager

import java.util
import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.enginemanager.conf.EngineManagerConfiguration.ENGINE_SPRING_APPLICATION_NAME
import com.webank.wedatasphere.linkis.enginemanager.exception.EngineManagerErrorException
import com.webank.wedatasphere.linkis.enginemanager.process.ProcessEngine
import com.webank.wedatasphere.linkis.protocol.engine.EngineState._
import com.webank.wedatasphere.linkis.protocol.engine._
import com.webank.wedatasphere.linkis.resourcemanager.client.ResourceManagerClient
import com.webank.wedatasphere.linkis.rpc.{Receiver, Sender}
import javax.annotation.PostConstruct
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.{ContextClosedEvent, EventListener}
import org.springframework.context.{ApplicationContext, ApplicationContextAware}
import org.springframework.stereotype.Component

import scala.concurrent.duration.Duration

/**
  * Created by johnnwang on 2018/9/6.
  */
@Component
class EngineManagerReceiver extends Receiver with EngineListener with Logging with ApplicationContextAware {

  @Autowired
  private var engineManager: EngineManager = _
  @Autowired
  private var rmClient: ResourceManagerClient = _
  private val portToSenders = new util.HashMap[Int, Sender]

  @PostConstruct
  def init(): Unit = engineManager match {
    case em: AbstractEngineManager => em.setEngineListener(this)
    case _ =>
  }

  private def findEngine(port: Int): Option[ProcessEngine] =
    engineManager.getEngineManagerContext.getOrCreateEngineFactory.list().find(_.getPort == port).map(_.asInstanceOf[ProcessEngine])

  private def getMsg(state: EngineState, responseEngineStatusCallback: ResponseEngineStatusCallback): Any = {
    val instance = EngineManagerReceiver.getInstanceByPort(responseEngineStatusCallback.port)
    state match {
      case Idle | Busy =>
        ResponseNewEngine(ENGINE_SPRING_APPLICATION_NAME.getValue, instance)
      case _ =>
        ResponseNewEngineStatus(instance, responseEngineStatusCallback)
    }
  }

  override def receive(message: Any, sender: Sender): Unit = message match {
    case ResponseEnginePid(port, pid) =>
      findEngine(port).foreach(_.callback(pid, sender))
    case ResponseEngineStatusCallback(port, status, initErrorMsg) =>
      findEngine(port).foreach(_.callback(status, initErrorMsg))
      val obj = getMsg(EngineState(status), ResponseEngineStatusCallback(port, status, initErrorMsg))
      val _sender = portToSenders.get(port)
      if(_sender != null) {
        //If the entity sent to the application fails, but the engine has started successfully, it will be broadcast directly later, so ignore the exception.
        //If the entity sent to the application fails, but the engine is still starting, wait until the result of the startup is processed again, so ignore the exception.
        //If the entity sent to the application fails, but the engine fails to start, then it is no longer necessary to return the information, ignore it.
        //如果发送给申请的entrance失败，但是engine已经启动成功，后面会直接进行广播，所以忽略该异常
        //如果发送给申请的entrance失败，但是engine还在启动之中，等启动的结果再统一进行处理，所以忽略该异常
        //如果发送给申请的entrance失败，但是engine启动失败，这时已不必回传信息，忽略即可
        Utils.tryAndWarnMsg(_sender.send(obj))(s"response the engine starting information $obj to entrance ${_sender} failed, ignore it.")
        obj match {
          case r: ResponseNewEngine =>
            engineManager.getEngineManagerContext.getOrCreateEngineFactory.get(port).foreach { engine =>
              warn(s"$engine start successfully, now try to broadcast it to all related entrances(${_sender}).")
              _sender.send(BroadcastNewEngine(r, ResponseEngineStatus(r.instance, status, null, null, ResponseEngineInfo(_sender.toString, engine.getCreator, engine.getUser, new util.HashMap[String, String]()))))
            }
          case _ =>
        }
      }
  }

  override def receiveAndReply(message: Any, sender: Sender): Any = receiveAndReply(message, null, sender)

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = message match {
    case request: RequestEngine =>
      if(StringUtils.isBlank(request.creator)) throw new EngineManagerErrorException(20050, "creator cannot be empty.")
      else if(StringUtils.isBlank(request.user)) throw new EngineManagerErrorException(20050, "user cannot be empty.")
      val engine = if(duration != null) engineManager.requestEngine(request, duration.toMillis)
      else engineManager.requestEngine(request)
      engine.map {
        case p: ProcessEngine =>
          portToSenders.put(p.getPort, sender)
          getMsg(p.getState, ResponseEngineStatusCallback(p.getPort, p.getState.id, p.getInitErrorMsg))
      }.get
    case request: RequestUserEngineKill =>
      engineManager.getEngineManagerContext.getOrCreateEngineFactory.list().find(_.getTicketId == request.ticketId)
        .foreach(engineManager.getEngineManagerContext.getOrCreateEngineFactory.delete)
      ResponseUserEngineKill(request.ticketId, ResponseUserEngineKill.Success, "")
    case _ => warn(s"cannot recognize the message $message.")
  }

  override def setApplicationContext(applicationContext: ApplicationContext): Unit =
    EngineManagerReceiver.applicationContext = applicationContext

  private val initEngines = new util.LinkedList[TimeoutEngine]
  case class TimeoutEngine(engine: Engine, timeout: Long) {
    override def equals(obj: scala.Any): Boolean = if(obj == null) false
    else if(!obj.isInstanceOf[TimeoutEngine]) false
    else engine == obj.asInstanceOf[TimeoutEngine].engine
  }
  import scala.collection.JavaConversions.asScalaIterator
  Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = initEngines.iterator().foreach {
      case TimeoutEngine(engine, timeout) =>
        if(timeout > 0 && System.currentTimeMillis - engine.getCreateTime > timeout) {
          engine match {
            case p: ProcessEngine =>
              p.callback(EngineState.ShuttingDown.id, s"inited timeout for more than $timeout.")
            case _ =>
          }
        }
    }
  }, 2, 2, TimeUnit.MINUTES)
  override def onEngineCreated(request: RequestEngine, engine: Engine): Unit = {
    info("User " + request.user + "created a new engine " + engine + ", creator=" + request.creator)
    request match {
      case timeoutRequest: TimeoutRequestEngine =>
        initEngines.add(TimeoutEngine(engine, timeoutRequest.timeout))
      case _ =>
        initEngines.add(TimeoutEngine(engine, -1))
    }
  }

  override def onEngineInited(engine: Engine): Unit = {
    initEngines.remove(TimeoutEngine(engine, -1))
    portToSenders.remove(engine.getPort)
  }

  override def onEngineInitFailed(engine: Engine, t: Throwable): Unit = {
    initEngines.remove(TimeoutEngine(engine, -1))
    engine match {
      case p: ProcessEngine if !EngineState.isCompleted(p.getState) =>
        p.callback(EngineState.Error.id, ExceptionUtils.getRootCauseMessage(t))
      case _ =>
    }
    val obj = getMsg(EngineState.Error, ResponseEngineStatusCallback(engine.getPort,
      EngineState.Error.id, ExceptionUtils.getRootCauseMessage(t)))
    val _sender = portToSenders.get(engine.getPort)
    if(_sender != null) {
      //Because obj has a retry mechanism, if it is not retried several times, it means that the entrance has been hanged, ignoring the current engine startup failure information.
      //因为obj存在重试机制，如果重试几次都不行，那表明entrance已经挂掉了，忽略本次的引擎启动失败信息
      Utils.tryAndWarnMsg(_sender.send(obj))(s"response $engine status(Error) to entrance ${_sender} failed, ignore it.")
      portToSenders.remove(engine.getPort)
    }
  }

  override def onEngineCompleted(engine: Engine): Unit = {
  }

  @EventListener
  def shutdownHook(event: ContextClosedEvent): Unit = {
    warn("shutdown engineManager " + Sender.getThisServiceInstance.getApplicationName)
    Utils.tryAndWarnMsg(engineManager.getEngineManagerContext.getOrCreateEngineFactory.shutdown(true))(
      "delete all engines failed.")
    Utils.tryAndWarnMsg{
      rmClient.unregister()
      warn("EngineManager has released resources from ResourceManager.")
    }("EngineManager release resources from ResourceManager failed.")
  }
}
object EngineManagerReceiver {
  private val address = Sender.getThisInstance.substring(0, Sender.getThisInstance.lastIndexOf(":"))
  def getInstanceByPort(port: Int): String = address + ":" + port

  def isEngineBelongTo(instance: String): Boolean = instance.startsWith(address)

  private var applicationContext: ApplicationContext = _
  def getSpringConf(key: String): String = applicationContext.getEnvironment.getProperty(key)
}