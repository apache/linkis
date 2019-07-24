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

package com.webank.wedatasphere.linkis.enginemanager.process

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.conf.{Configuration, DWCArgumentsParser}
import com.webank.wedatasphere.linkis.common.exception.WarnException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.enginemanager.Engine
import com.webank.wedatasphere.linkis.enginemanager.exception.EngineManagerErrorException
import com.webank.wedatasphere.linkis.protocol.engine.EngineState._
import com.webank.wedatasphere.linkis.protocol.engine.{EngineState, RequestEngineStatus, RequestKillEngine, ResponseEngineStatus}
import com.webank.wedatasphere.linkis.rpc.Sender
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Future, TimeoutException}

/**
  * Created by johnnwang on 2018/9/6.
  */
trait ProcessEngine extends Engine with Logging {
  val processBuilder: ProcessEngineBuilder
  val dwcArgumentsParser: DWCArgumentsParser
  val timeout: Long

  override val getCreateTime: Long = System.currentTimeMillis
  private var process: Process = _
  private var pid: String = _
  private var sender: Sender = _
  private var _state: EngineState = Starting
  private var initErrorMsg: String = _
  private var port: Int = _
  private var ticketId: String = _
  private var initedTime = 0l
  private implicit val executor = global

  override def getInitedTime: Long = initedTime

  def setPort(port: Int) = this.port = port
  override def getPort = port

  def setTicketId(ticketId: String) = this.ticketId = ticketId
  override def getTicketId = ticketId

  override def getState: EngineState = _state
  def getInitErrorMsg = initErrorMsg

  override def init(): Unit = {
    process = processBuilder.start(DWCArgumentsParser.formatToArray(dwcArgumentsParser))
    var exitCode: Option[Int] = None
    Future {
      val iterator = IOUtils.lineIterator(process.getInputStream, Configuration.BDP_ENCODING.getValue)
      while(!EngineState.isCompleted(_state) && iterator.hasNext) {
        dealStartupLog(iterator.next())
      }
      exitCode = Option(process.waitFor)
      warn(s"$toString has stopped with exit code " + exitCode)
      if(exitCode.exists(_ != 0)) {
        transition(Error)
      } else {
        transition(Dead)
      }
    }
    Utils.tryThrow(Utils.waitUntil(() => _state != Starting, Duration(timeout, TimeUnit.MILLISECONDS))) {
      case _: TimeoutException =>
        warn(s"wait for $toString initial timeout, now shutdown it.")
        shutdown()
        throw new EngineManagerErrorException(20018, s"wait for $toString initial timeout.")
      case t: Throwable => t
    }
    if(!EngineState.isAvailable(_state))
      throw new EngineManagerErrorException(20019, "init engine failed!" + (if(StringUtils.isNotBlank(initErrorMsg)) " Reason: " + initErrorMsg
        else " Unknown error, please ask admin for help."))
//      throw exitCode.filter(_ > 0).map(c => new EngineManagerErrorException(20019, s"init engine failed with state ${_state}, errorCode: " + c)) getOrElse
//        new EngineManagerErrorException(20019, s"init engine failed with state ${_state}. Unknown error, please ask admin for help.")
    initedTime = System.currentTimeMillis
  }

  protected def dealStartupLog(line: String): Unit = println(getPort + ": " + line)

  protected def transition(state: EngineState): Unit = this synchronized
    _state match {
    case Error | Dead | Success =>
      warn(s"$toString attempt to change state ${_state} => $state, ignore it.")
    case _ =>
      if(state == EngineState.Starting && _state != EngineState.Starting){
        warn("Can not change current state to starting for state is "+ _state)
      }else{
        info(s"$toString change state ${_state} => $state.")
        _state = state
      }

  }

  def callback(pid: String, sender: Sender): Unit = {
    if(StringUtils.isEmpty(this.pid)) this.pid = pid
    if(this.sender == null) this.sender = sender
  }

  def callback(status: Int, initErrorMsg: String): Unit = {
    val state = EngineState(status)
    info("Call back to change engine state"+status+" error msg is " + initErrorMsg)
    if(StringUtils.isNotEmpty(initErrorMsg)) {
      if(StringUtils.isEmpty(this.initErrorMsg)) this.initErrorMsg = initErrorMsg
      else this.initErrorMsg += "\n" + initErrorMsg
    }
    transition(state)
  }

  def tryHeartbeat(): Unit = if(sender != null){
    sender.ask(RequestEngineStatus(RequestEngineStatus.Status_Only)) match {
      case warn: WarnException => throw warn
      case r: ResponseEngineStatus => {
        transition(EngineState(r.state))
      }
    }
  }

  override def shutdown(): Unit = if(StringUtils.isNotEmpty(pid)) {
    info(s"try to kill $toString with pid($pid).")
    process.destroy()
    var tryNum = 1
    while(isProcessAlive && tryNum <= 3) {
      info(s"$toString still alive with pid($pid), use shell command to kill it. try $tryNum++")
      if(tryNum < 3) Utils.exec(Array(JavaProcessEngineBuilder.sudoUserScript.getValue, getUser, s"kill $pid"), 3000l)
      else Utils.exec(Array(JavaProcessEngineBuilder.sudoUserScript.getValue, getUser, s"kill -9 $pid"), 3000l)
      tryNum += 1
      Utils.tryQuietly(Thread.sleep(3000))
    }
    info(s"Stopped $toString.")
  } else {
    info(s"try to kill $toString with RPC kill command.")
    sender.send(RequestKillEngine("", Sender.getThisServiceInstance.getApplicationName, Sender.getThisServiceInstance.getInstance))
  }

  private def isProcessAlive: Boolean = {
    val r = Utils.exec(Array(JavaProcessEngineBuilder.sudoUserScript.getValue, getUser, "ps -ef | grep " +
      pid + " | grep DataWorkCloud | awk '{print \"exists_\"$2}'"), 5000l)
    r != null && r.contains("exists_" + pid)
  }
}