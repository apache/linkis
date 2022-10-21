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

package org.apache.linkis

import org.apache.linkis.common.exception.{
  ErrorException,
  ExceptionManager,
  FatalException,
  WarnException
}
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.errorcode.LinkisModuleErrorCodeSummary._
import org.apache.linkis.server.exception.{BDPServerErrorException, NonLoginException}
import org.apache.linkis.server.security.SecurityFilter

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import javax.servlet.http.HttpServletRequest

import java.text.MessageFormat
import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable

import org.slf4j.Logger

package object server {

  val EXCEPTION_MSG = "errorMsg"
  type JMap[K, V] = java.util.HashMap[K, V]

  implicit def getUser(req: HttpServletRequest): String = SecurityFilter.getLoginUsername(req)

  def validateFailed(message: String): Message = Message(status = 2).setMessage(message)

  def validate[T](json: util.Map[String, T], keys: String*): Unit = {
    keys.foreach(k =>
      if (!json.contains(k) || json.get(k) == null || StringUtils.isEmpty(json.get(k).toString)) {
        throw new BDPServerErrorException(
          VERIFICATION_CANNOT_EMPTY.getErrorCode,
          MessageFormat.format(VERIFICATION_CANNOT_EMPTY.getErrorDesc, k)
        )
      }
    )
  }

  def error(message: String): Message = Message.error(message)
  implicit def ok(msg: String): Message = Message.ok(msg)
  implicit def error(t: Throwable): Message = Message.error(t)
  implicit def error(e: (String, Throwable)): Message = Message.error(e)
  implicit def error(msg: String, t: Throwable): Message = Message.error(msg -> t)

  //  def tryCatch[T](tryOp: => T)(catchOp: Throwable => T): T = Utils.tryCatch(tryOp)(catchOp)
//  def tryCatch(tryOp: => Message)(catchOp: Throwable => Message): Message = Utils.tryCatch(tryOp) {
//    case nonLogin: NonLoginException => Message.noLogin(msg = nonLogin.getMessage)
//    case t => catchOp(t)
//  }
  def catchMsg(tryOp: => Message)(msg: String)(implicit log: Logger): Message =
    Utils.tryCatch(tryOp) {
      case fatal: FatalException =>
        log.error("Fatal Error, system exit...", fatal)
        System.exit(fatal.getErrCode)
        Message.error("Fatal Error, system exit...")
      case nonLogin: NonLoginException =>
        val message = Message.noLogin(nonLogin.getMessage)
        message.data(EXCEPTION_MSG, nonLogin.toMap)
        message
      case error: ErrorException =>
        val cause = error.getCause
        val errorMsg = cause match {
          case t: ErrorException =>
            s"error code(错误码): ${t.getErrCode}, error message(错误信息): ${t.getDesc}."
          case _ =>
            s"error code(错误码): ${error.getErrCode}, error message(错误信息): ${error.getDesc}."
        }
        log.error(errorMsg, error)
        val message = Message.error(errorMsg)
        message.data(EXCEPTION_MSG, error.toMap)
        message
      case warn: WarnException =>
        val warnMsg =
          s"Warning code(警告码): ${warn.getErrCode}, Warning message(警告信息): ${warn.getDesc}."
        log.warn(warnMsg, warn)
        val message = Message.warn(warnMsg)
        message.data(EXCEPTION_MSG, warn.toMap)
        message
      case t =>
        log.error(msg, t)
        val errorMsg = ExceptionUtils.getRootCauseMessage(t)
        val message =
          if (StringUtils.isNotEmpty(errorMsg) && "operation failed(操作失败)" != msg) {
            error(msg + "！the reason(原因)：" + errorMsg)
          } else if (StringUtils.isNotEmpty(errorMsg)) error(errorMsg)
          else error(msg)
        message.data(EXCEPTION_MSG, ExceptionManager.unknownException(message.getMessage))
    }

  def catchIt(tryOp: => Message)(implicit log: Logger): Message =
    catchMsg(tryOp)("operation failed(操作失败)s")

  implicit def toScalaBuffer[T](list: util.List[T]): mutable.Buffer[T] = list.asScala
  implicit def toScalaMap[K, V](map: util.Map[K, V]): mutable.Map[K, V] = map.asScala

  implicit def toJavaList[T](list: mutable.Buffer[T]): util.List[T] = {
    val arrayList = new util.ArrayList[T]
    list.foreach(arrayList.add)
    arrayList
  }

  implicit def toJavaMap[K, V](map: mutable.Map[K, V]): JMap[K, V] = {
    val hashMap = new util.HashMap[K, V]()
    map.foreach(m => hashMap.put(m._1, m._2))
    hashMap
  }

  implicit def toJavaMap[K, V](map: Map[K, V]): JMap[K, V] = {
    val hashMap = new util.HashMap[K, V]()
    map.foreach(m => hashMap.put(m._1, m._2))
    hashMap
  }

  implicit def asString(mapWithKey: (util.Map[String, Object], String)): String =
    mapWithKey._1.get(mapWithKey._2).asInstanceOf[String]

  implicit def getString(mapWithKey: (util.Map[String, String], String)): String =
    mapWithKey._1.get(mapWithKey._2)

  implicit def asInt(map: util.Map[String, Object], key: String): Int =
    map.get(key).asInstanceOf[Int]

  implicit def asBoolean(mapWithKey: (util.Map[String, Object], String)): Boolean =
    mapWithKey._1.get(mapWithKey._2).asInstanceOf[Boolean]

}
