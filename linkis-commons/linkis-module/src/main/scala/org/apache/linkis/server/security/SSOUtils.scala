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

package org.apache.linkis.server.security

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{Logging, RSAUtils, Utils}
import org.apache.linkis.server.conf.{ServerConfiguration, SessionHAConfiguration}
import org.apache.linkis.server.exception.{
  IllegalUserTicketException,
  LoginExpireException,
  NonLoginException
}
import org.apache.linkis.server.ticket.UserTicketService

import org.apache.commons.lang3.time.DateFormatUtils

import javax.servlet.http.Cookie

import java.util.concurrent.{ConcurrentHashMap, TimeUnit}

import scala.collection.JavaConverters._

object SSOUtils extends Logging {

  private[security] val USER_TICKET_ID_STRING =
    ServerConfiguration.LINKIS_SERVER_SESSION_TICKETID_KEY.getValue

  private val sessionTimeout = ServerConfiguration.BDP_SERVER_WEB_SESSION_TIMEOUT.getValue.toLong

  private val userTicketIdToLastAccessTime = getUserTicketIdMap

  val sslEnable: Boolean = ServerConfiguration.BDP_SERVER_SECURITY_SSL.getValue

  def getUserTicketIdMap: ConcurrentHashMap[String, Long] = {
    if (SessionHAConfiguration.SsoRedis) {
      return new UserTicketService
    }
    new ConcurrentHashMap[String, Long]
  }

  def decryptLogin(passwordString: String): String = if (sslEnable) {
    new String(RSAUtils.decrypt(passwordString), Configuration.BDP_ENCODING.getValue)
  } else passwordString

  Utils.defaultScheduler.scheduleAtFixedRate(
    new Runnable {

      override def run(): Unit = Utils.tryCatch {
        userTicketIdToLastAccessTime.asScala
          .filter(System.currentTimeMillis - _._2 > sessionTimeout)
          .foreach { case (k, v) =>
            if (userTicketIdToLastAccessTime.containsKey(k)) {
              if (
                  userTicketIdToLastAccessTime
                    .containsKey(k) && System.currentTimeMillis - userTicketIdToLastAccessTime
                    .get(k) > sessionTimeout
              ) {
                logger.info(
                  s"remove timeout userTicket $k, since the last access time is ${DateFormatUtils
                    .format(v, "yyyy-MM-dd HH:mm:ss")}."
                )
                userTicketIdToLastAccessTime.remove(k)
              }
            }
          }
      } { t =>
        logger.error("failed to do remove", t)
      }

    },
    sessionTimeout,
    sessionTimeout / 10,
    TimeUnit.MILLISECONDS
  )

  private[security] def getUserAndLoginTime(userTicketId: String): Option[(String, Long)] = {
    ServerConfiguration.getUsernameByTicket(userTicketId).map { userAndLoginTime =>
      {
        if (userAndLoginTime.indexOf(",") < 0) {
          throw new IllegalUserTicketException(s"Illegal user token information(非法的用户token信息).")
        }
        val index = userAndLoginTime.lastIndexOf(",")
        (userAndLoginTime.substring(0, index), userAndLoginTime.substring(index + 1).toLong)
      }
    }
  }

  // Determine the unique ID by username and timestamp(通过用户名和时间戳，确定唯一ID)
  private def getUserTicketId(username: String): String = {
    val timeoutUser = username + "," + System.currentTimeMillis
    ServerConfiguration.getTicketByUsername(timeoutUser)
  }

  def setLoginUser(addCookie: Cookie => Unit, username: String): Unit = {
    logger.info(s"add login userTicketCookie for user $username.")
    val userTicketId = getUserTicketId(username)
    userTicketIdToLastAccessTime.put(userTicketId, System.currentTimeMillis())
    val cookie = new Cookie(USER_TICKET_ID_STRING, userTicketId)
    cookie.setMaxAge(-1)
    if (sslEnable) cookie.setSecure(true)
    cookie.setPath("/")
    addCookie(cookie)
  }

  def setLoginUser(addUserTicketKV: (String, String) => Unit, username: String): Unit = {
    logger.info(s"add login userTicket for user $username.")
    val userTicketId = getUserTicketKV(username)
    userTicketIdToLastAccessTime.put(userTicketId._2, System.currentTimeMillis())
    addUserTicketKV(userTicketId._1, userTicketId._2)
  }

  private[linkis] def getUserTicketKV(username: String): (String, String) = {
    val userTicketId = getUserTicketId(username)
    (USER_TICKET_ID_STRING, userTicketId)
  }

  def removeLoginUser(getCookies: => Array[Cookie]): Unit = {
    val cookies = getCookies
    if (cookies != null) cookies.find(_.getName == USER_TICKET_ID_STRING).foreach { cookie =>
      if (userTicketIdToLastAccessTime.containsKey(cookie.getValue)) {
        userTicketIdToLastAccessTime.remove(cookie.getValue)
      }
      cookie.setValue(null)
      cookie.setMaxAge(0)
    }
    ProxyUserSSOUtils.removeProxyUser(cookies)
  }

  def removeLoginUserByAddCookie(addEmptyCookie: Cookie => Unit): Unit = {
    val cookie = new Cookie(USER_TICKET_ID_STRING, null)
    cookie.setMaxAge(0)
    cookie.setPath("/")
    if (sslEnable) cookie.setSecure(true)
    addEmptyCookie(cookie)
    ProxyUserSSOUtils.removeLoginUserByAddCookie(addEmptyCookie)
  }

  def removeLoginUser(removeKeyReturnValue: String => Option[String]): Unit =
    removeKeyReturnValue(USER_TICKET_ID_STRING).foreach { t =>
      if (userTicketIdToLastAccessTime.containsKey(t)) {
        userTicketIdToLastAccessTime.remove(t)
      }
    }

  def getLoginUsername(getCookies: => Array[Cookie]): String = getLoginUser(getCookies).getOrElse(
    throw new NonLoginException(s"You are not logged in, please login first(您尚未登录，请先登录!)")
  )

  def getLoginUser(getCookies: => Array[Cookie]): Option[String] = getLoginUser(_ =>
    Option(getCookies).flatMap(_.find(_.getName == USER_TICKET_ID_STRING).map(_.getValue))
  )

  def getLoginUser(getUserTicketId: String => Option[String]): Option[String] =
    getUserTicketId(USER_TICKET_ID_STRING).map { t =>
      isTimeoutOrNot(t)
      getUserAndLoginTime(t)
        .getOrElse(
          throw new IllegalUserTicketException(s"Illegal user token information(非法的用户token信息).")
        )
        ._1
    }

  def getLoginUsername(getUserTicketId: String => Option[String]): String =
    getLoginUser(getUserTicketId).getOrElse(
      throw new NonLoginException(s"You are not logged in, please login first(您尚未登录，请先登录!)")
    )

  private[security] def getLoginUserIgnoreTimeout(
      getUserTicketId: String => Option[String]
  ): Option[String] =
    getUserTicketId(USER_TICKET_ID_STRING).map(getUserAndLoginTime).flatMap(_.map(_._1))

  def updateLastAccessTime(getCookies: => Array[Cookie]): Unit = updateLastAccessTime(_ =>
    Option(getCookies).flatMap(_.find(_.getName == USER_TICKET_ID_STRING).map(_.getValue))
  )

  def updateLastAccessTime(getUserTicketId: String => Option[String]): Unit =
    getUserTicketId(USER_TICKET_ID_STRING).foreach(isTimeoutOrNot)

  @throws(classOf[LoginExpireException])
  private def isTimeoutOrNot(userTicketId: String): Unit =
    if (!userTicketIdToLastAccessTime.containsKey(userTicketId)) {
      throw new LoginExpireException("You are not logged in, please login first!(您尚未登录，请先登录!)")
    } else {
      val lastAccessTime = userTicketIdToLastAccessTime.get(userTicketId)
      if (
          System.currentTimeMillis - lastAccessTime > sessionTimeout && !Configuration.IS_TEST_MODE.getValue
      ) {
        if (
            userTicketIdToLastAccessTime.containsKey(
              userTicketId
            ) && System.currentTimeMillis - userTicketIdToLastAccessTime.get(
              userTicketId
            ) > sessionTimeout
        ) {
          userTicketIdToLastAccessTime.remove(userTicketId)
          throw new LoginExpireException("Login has expired, please log in again!(登录已过期，请重新登录！)")
        }
      } else if (System.currentTimeMillis - lastAccessTime >= sessionTimeout * 0.5) {
        userTicketIdToLastAccessTime.put(userTicketId, System.currentTimeMillis)
      }
    }

  def getSessionTimeOut(): Long = sessionTimeout

}
