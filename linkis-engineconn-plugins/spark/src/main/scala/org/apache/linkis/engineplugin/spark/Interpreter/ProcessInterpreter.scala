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
 
package org.apache.linkis.engineplugin.spark.Interpreter

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.util.concurrent.TimeUnit

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineplugin.spark.common._
import org.apache.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import org.apache.commons.io.IOUtils
import org.json4s._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  *
  */
abstract class ProcessInterpreter(process: Process) extends Interpreter with Logging {

  implicit val executor: ExecutionContext = ExecutionContext.global

  protected[this] var _state: State = Starting()

  protected[this] val stdin = new PrintWriter(process.getOutputStream)
  protected[this] val stdout = new BufferedReader(new InputStreamReader(process.getInputStream()), 1)
  protected[this] val errOut = new LineBufferedStream(process.getErrorStream())

  override def state: State = _state

  override def execute(code: String): ExecuteResponse = {
    if(code == "sc.cancelAllJobs" || code == "sc.cancelAllJobs()") {
      sendExecuteRequest(code)
    }
    _state match {
      case (Dead() | ShuttingDown() | Error() | Success()) =>
        throw new IllegalStateException("interpreter is not running")
      case Idle() =>
        require(state == Idle())
        code match {
          case "SHUTDOWN" =>
            sendShutdownRequest()
            close()
            ErrorExecuteResponse("shutdown",new Exception("shutdown"))
          case _ =>
            _state = Busy()
            sendExecuteRequest(code) match {
              case Some(rep) =>
                _state = Idle()
               // ExecuteComplete(rep)
                SuccessExecuteResponse()
              case None =>
                _state = Error()
                val errorMsg = errOut.lines.mkString(", ")
                throw new Exception(errorMsg)
            }
        }
      case _ => throw new IllegalStateException(s"interpreter is in ${_state} state, cannot do query.")
    }
  }

  Future {
    val exitCode = process.waitFor()
    if (exitCode != 0) {
      errOut.lines.foreach(println)
      println(getClass.getSimpleName+" has stopped with exit code " + process.exitValue)
      _state = Error()
    } else {
      println(getClass.getSimpleName+" has finished.")
      _state = Success()
    }
  }

  protected def waitUntilReady(): Unit

  protected def sendExecuteRequest(request: String): Option[JValue]

  protected def sendShutdownRequest(): Unit = {}


  override def close(): Unit = {
    val future = Future {
      _state match {
        case (Dead() | ShuttingDown() | Success()) =>
          Future.successful()
        case _ =>
          sendShutdownRequest()
      }
    }
    _state = Dead()
    IOUtils.closeQuietly(stdin)
    IOUtils.closeQuietly(stdout)
    errOut.close

    // Give ourselves 10 seconds to tear down the process.
    Utils.tryFinally(Await.result(future, Duration(10, TimeUnit.SECONDS))){
      process.destroy()}
  }

}
