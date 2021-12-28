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

package org.apache.linkis.server

import java.util

import javax.servlet.http.HttpServletRequest
import javax.xml.bind.annotation.XmlRootElement
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.web.context.request.{RequestContextHolder, ServletRequestAttributes}


@XmlRootElement(name = "message")
class Message(private var method: String,
              private var status: Int = 0,          //-1 no login, 0 success, 1 error, 2 validate failed, 3 auth failed, 4 warning
              private var message: String,
              private var data: util.HashMap[String, Object] = new util.HashMap[String, Object]) {
  def this() = this(null, 0, null)
  def << (key: String, value: Any): Message = {
    data.put(key, value.asInstanceOf[AnyRef])
    this
  }
  def << (keyValue: (String, Any)): Message = <<(keyValue._1, keyValue._2)

  def list[T <: Any](keyValue: (String, java.util.List[T])): Message = << (keyValue)

  def map[K <: Any, V <: Any](keyValue: (String, java.util.Map[K, V])): Message = << (keyValue)

  def data(key: String, value: Any): Message = <<(key, value)

  def << (method: String): Message = {
    this.method = method
    this
  }
  def setMessage(message: String) = {
    this.message = message
    this
  }
  def getMessage = message
  def setMethod(method: String): Unit = this.method = method
  def getMethod = method
  def setStatus(status: Int): Unit = this.status = status
  def getStatus = status
  def setData(data: util.HashMap[String, Object]): Unit = this.data = data
  def getData = data

  //  def isSuccess = status == 0
  //  def isError = status != 0

  override def toString = s"Message($getMethod, $getStatus, $getData)"
}

object Message {
  private val logger = LoggerFactory.getLogger(getClass)

  def apply(method: String = null, status: Int = 0, message: String = null,
            data: util.HashMap[String, Object] = new util.HashMap[String, Object]): Message = {
    if (StringUtils.isEmpty(method)) {
      Thread.currentThread().getStackTrace.find(_.getClassName.toLowerCase.endsWith("restfulapi")).foreach {
        stack => {
            val httpRequest:HttpServletRequest=getCurrentHttpRequest
            if(httpRequest!=null){
              val pathInfo=httpRequest.getPathInfo;
              if(pathInfo!=null) {
                  val method = if (pathInfo.startsWith("/")) "/api"+ pathInfo else "/api" + "/" + pathInfo
                  return new Message(method, status, message, data)
              }else{
                logger.warn("get HttpServletRequest pathInfo is null,please check it!")
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
    if(StringUtils.isNotBlank(msg)) message.setMessage(msg) else message.setMessage("OK")
  }
  def error(msg: String): Message = error(msg, null)
  implicit def error(t: Throwable): Message = {
    Message(status =  1).setMessage(ExceptionUtils.getRootCauseMessage(t)) << ("stack", ExceptionUtils.getFullStackTrace(t))
  }
  implicit def error(e: (String, Throwable)): Message = error(e._1, e._2)
  implicit def error(msg: String, t: Throwable): Message = {
    val message = Message(status =  1)
    message.setMessage(msg)
    if(t != null) message << ("stack", ExceptionUtils.getFullStackTrace(t))
    message
  }
  implicit def warn(msg: String): Message = {
    val message = Message(status = 4)
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
    val message = Message(status = -1)
    message.setMessage(msg)
    if(t != null) message << ("stack", ExceptionUtils.getFullStackTrace(t))
    message
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