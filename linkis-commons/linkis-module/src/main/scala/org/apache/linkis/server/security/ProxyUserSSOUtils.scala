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

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{DESUtil, Logging}
import org.apache.linkis.server.conf.ServerConfiguration

import org.apache.commons.lang3.StringUtils

import javax.servlet.http.{Cookie, HttpServletRequest}

object ProxyUserSSOUtils extends Logging {

  private val PROXY_TICKET_HEADER_CRYPT_KEY =
    CommonVars("wds.linkis.proxy.ticket.header.crypt.key", "linkis-trust-key").getValue

  private val PROXY_TICKET_HEADER_CONTENT =
    CommonVars("wds.linkis.proxy.ticket.header.crypt.key", "bfs_").getValue

  private val linkisTrustCode =
    DESUtil.encrypt(PROXY_TICKET_HEADER_CONTENT, PROXY_TICKET_HEADER_CRYPT_KEY)

  private[security] val PROXY_USER_TICKET_ID_STRING =
    ServerConfiguration.LINKIS_SERVER_SESSION_PROXY_TICKETID_KEY.getValue

  private val sslEnable: Boolean = ServerConfiguration.BDP_SERVER_SECURITY_SSL.getValue

  private def getProxyUsernameByTicket(ticketId: String): Option[String] =
    if (StringUtils.isBlank(ticketId)) None
    else {
      val userName = DESUtil.decrypt(ticketId, ServerConfiguration.cryptKey)
      if (userName.startsWith(linkisTrustCode)) Some(userName.substring(linkisTrustCode.length))
      else None
    }

  private def getTicketByUsernameAndTrustCode(userName: String, trustCode: String): String = {
    if (!trustCode.equals(linkisTrustCode)) {
      logger.info(s"$trustCode error,will be use default username")
      userName
    } else {
      DESUtil.encrypt(trustCode + userName, ServerConfiguration.cryptKey)
    }
  }

  def getProxyUserTicketKV(proxyUsername: String, trustCode: String): (String, String) = {
    val userTicketId = getTicketByUsernameAndTrustCode(proxyUsername, trustCode)
    (PROXY_USER_TICKET_ID_STRING, userTicketId)
  }

  def setProxyUserCookie(addCookie: Cookie => Unit, username: String, trustCode: String): Unit = {
    logger.info(s"add login userTicketCookie for user $username.")
    val userTicketIdKv = getProxyUserTicketKV(username, trustCode)
    val cookie = new Cookie(userTicketIdKv._1, userTicketIdKv._2)
    cookie.setMaxAge(-1)
    if (sslEnable) cookie.setSecure(true)
    cookie.setPath("/")
    addCookie(cookie)
  }

  def removeProxyUser(getCookies: => Array[Cookie]): Unit = {
    val cookies = getCookies
    if (cookies != null) {
      cookies.find(_.getName == PROXY_USER_TICKET_ID_STRING).foreach { cookie =>
        cookie.setValue(null)
        cookie.setMaxAge(0)
      }
    }
  }

  def removeLoginUserByAddCookie(addEmptyCookie: Cookie => Unit): Unit = {
    val cookie = new Cookie(PROXY_USER_TICKET_ID_STRING, null)
    cookie.setMaxAge(0)
    cookie.setPath("/")
    addEmptyCookie(cookie)
  }

  def getProxyUserUsername(req: HttpServletRequest): Option[String] = {
    val ticketOption = Option(req.getCookies).flatMap(
      _.find(_.getName == PROXY_USER_TICKET_ID_STRING).map(_.getValue)
    )
    if (ticketOption.isDefined) {
      logger.info(s"PROXY_USER_TICKET_ID_STRING: ${ticketOption.get}")
      getProxyUsernameByTicket(ticketOption.get)
    } else {
      None
    }
  }

}
