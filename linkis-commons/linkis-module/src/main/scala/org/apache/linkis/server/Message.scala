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

package org.apache.linkis.server

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import org.springframework.web.context.request.{RequestContextHolder, ServletRequestAttributes}

import javax.servlet.http.HttpServletRequest

import java.util
import java.util.Locale

class Message(
    private var method: String,
    private var status: Int =
      0, // -1 no login, 0 success, 1 error, 2 validate failed, 3 auth failed, 4 warning
    private var message: String,
    private var data: util.HashMap[String, Object] = new util.HashMap[String, Object]
) {
  def this() = this(null, 0, null)

  def <<(key: String, value: Any): Message = {
    data.put(key, value.asInstanceOf[AnyRef])
    this
  }

  def <<(keyValue: (String, Any)): Message = <<(keyValue._1, keyValue._2)

  def list[T <: Any](keyValue: (String, java.util.List[T])): Message = <<(keyValue)

  def map[K <: Any, V <: Any](keyValue: (String, java.util.Map[K, V])): Message = <<(keyValue)

  def data(key: String, value: Any): Message = <<(key, value)

  def <<(method: String): Message = {
    this.method = method
    this
  }

  def setMessage(message: String): Message = {
    this.message = message
    this
  }

  def getMessage: String = message
  def setMethod(method: String): Unit = this.method = method
  def getMethod: String = method
  def setStatus(status: Int): Unit = this.status = status
  def getStatus: Int = status
  def setData(data: util.HashMap[String, Object]): Unit = this.data = data
  def getData: util.HashMap[String, Object] = data

  override def toString: String = s"Message($getMethod, $getStatus, $getData)"
}

object Message {

  def apply(
      method: String = null,
      status: Int = 0,
      message: String = null,
      data: util.HashMap[String, Object] = new util.HashMap[String, Object]
  ): Message = {
    if (StringUtils.isEmpty(method)) {
      Thread
        .currentThread()
        .getStackTrace
        .find(_.getClassName.toLowerCase(Locale.getDefault).endsWith("restfulapi"))
        .foreach { stack =>
          {
            val httpRequest: HttpServletRequest = getCurrentHttpRequest
            if (httpRequest != null) {
              val pathInfo = httpRequest.getPathInfo;
              if (pathInfo != null) {
                val method =
                  if (pathInfo.startsWith("/")) "/api" + pathInfo else "/api/" + pathInfo
                return new Message(method, status, message, data)
              } else {
                warn("get HttpServletRequest pathInfo is null,please check it!")
              }
            }
          }
        }
    }
    new Message(method, status, message, data)
  }

  implicit def ok(): Message = Message().setMessage("OK")

  implicit def ok(msg: String): Message = {
    val message = Message()
    if (StringUtils.isNotBlank(msg)) message.setMessage(msg) else message.setMessage("OK")
  }

  def error(msg: String): Message = error(msg, null)

  implicit def error(t: Throwable): Message = {
    error(ExceptionUtils.getRootCauseMessage(t), t)
  }

  implicit def error(e: (String, Throwable)): Message = error(e._1, e._2)

  implicit def error(msg: String, t: Throwable): Message = {
    error(msg, t, MessageStatus.ERROR)
  }

  implicit def error(msg: String, t: Throwable, status: Int): Message = {
    val message = Message(status = status)
    message.setMessage(msg)
    if (t != null) message << ("stack", ExceptionUtils.getStackTrace(t))
    message
  }

  implicit def warn(msg: String): Message = {
    val message = Message(status = MessageStatus.WARNING)
    message.setMessage(msg)
    message
  }

  implicit def response(message: Message): String = BDPJettyServerHelper.gson.toJson(message)

  def getCurrentHttpRequest: HttpServletRequest = {
    val requestAttributes = RequestContextHolder.getRequestAttributes
    if (requestAttributes.isInstanceOf[ServletRequestAttributes]) {
      val request = requestAttributes.asInstanceOf[ServletRequestAttributes].getRequest
      return request
    }
    null
  }

  def noLogin(msg: String, t: Throwable): Message = {
    error(msg, t, MessageStatus.NO_LOGIN)
  }

  def noLogin(msg: String): Message = noLogin(msg, null)

  def messageToHttpStatus(message: Message): Int = message.getStatus match {
    case -1 => 401
    case 0 => 200
    case 1 => 400
    case 2 => 412
    case 3 => 403
    case 4 => 206
  }

}

object MessageStatus {

  val NO_LOGIN: Int = -1
  val SUCCESS: Int = 0
  val ERROR: Int = 1
  val VALIDATE_FAILED: Int = 2
  val AUTH_FAILED: Int = 3
  val WARNING: Int = 4
}
