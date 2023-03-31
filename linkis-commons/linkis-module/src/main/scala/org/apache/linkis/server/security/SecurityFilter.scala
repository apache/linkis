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
import org.apache.linkis.errorcode.LinkisModuleErrorCodeSummary.ILLEGAL_USER_TOKEN
import org.apache.linkis.server.{Message, _}
import org.apache.linkis.server.conf.ServerConfiguration
import org.apache.linkis.server.exception.{
  IllegalUserTicketException,
  LoginExpireException,
  NonLoginException
}
import org.apache.linkis.server.security.SecurityFilter.logger
import org.apache.linkis.server.security.SSOUtils.sslEnable

import org.apache.commons.lang3.StringUtils

import javax.servlet._
import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}

import java.text.DateFormat
import java.util.{Date, Locale}

import org.slf4j.{Logger, LoggerFactory}

class SecurityFilter extends Filter {

  private val refererValidate = ServerConfiguration.BDP_SERVER_SECURITY_REFERER_VALIDATE.getValue
  private val localAddress = ServerConfiguration.BDP_SERVER_ADDRESS.getValue
  protected val testUser = ServerConfiguration.BDP_TEST_USER.getValue

  override def init(filterConfig: FilterConfig): Unit = {}

  private def filterResponse(message: Message)(implicit response: HttpServletResponse): Unit = {
    response.setHeader("Content-Type", "application/json;charset=UTF-8")
    response.setStatus(Message.messageToHttpStatus(message))
    response.getOutputStream.print(message)
    response.getOutputStream.flush()
  }

  def doFilter(request: HttpServletRequest)(implicit response: HttpServletResponse): Boolean = {
    addAccessHeaders(response)
    if (refererValidate) {
      // Security certification support, referer limited(安全认证支持，referer限定)
      val referer = request.getHeader("Referer")
      if (StringUtils.isNotEmpty(referer) && !referer.trim.contains(localAddress)) {
        filterResponse(validateFailed("不允许的跨站请求！"))
        return false
      }
      // Security certification support, solving verb tampering(安全认证支持，解决动词篡改)
      request.getMethod.toUpperCase(Locale.getDefault) match {
        case "GET" | "POST" | "PUT" | "DELETE" | "HEAD" | "TRACE" | "CONNECT" | "OPTIONS" =>
        case _ =>
          filterResponse(validateFailed("Do not use HTTP verbs to tamper with!(不可使用HTTP动词篡改！)"))
          return false
      }
    }
    if (request.getRequestURI == ServerConfiguration.BDP_SERVER_SECURITY_SSL_URI.getValue) {
      val message = Message.ok("Get success!(获取成功！)").data("enable", SSOUtils.sslEnable)
      if (SSOUtils.sslEnable) message.data("publicKey", RSAUtils.getDefaultPublicKey())
      filterResponse(message)
      false
    } else if (request.getRequestURI == ServerConfiguration.BDP_SERVER_RESTFUL_LOGIN_URI.getValue) {
      true
    } else if (
        ServerConfiguration.BDP_SERVER_RESTFUL_PASS_AUTH_REQUEST_URI
          .exists(r => !r.equals("") && request.getRequestURI.startsWith(r))
    ) {
      logger.info("pass auth uri: " + request.getRequestURI)
      true
    } else {
      val userName = Utils.tryCatch(SecurityFilter.getLoginUser(request)) {
        case n: NonLoginException =>
          if (Configuration.IS_TEST_MODE.getValue) None
          else {
            filterResponse(Message.noLogin(n.getMessage) << request.getRequestURI)
            return false
          }
        case t: Throwable =>
          logger.warn("", t)
          throw t
      }
      if (userName.isDefined) {
        true
      } else if (Configuration.IS_TEST_MODE.getValue) {
        logger.info("test mode! login for uri: " + request.getRequestURI)
        SecurityFilter.setLoginUser(response, testUser)
        true
      } else {
        filterResponse(
          Message.noLogin(
            "You are not logged in, please login first!(您尚未登录，请先登录!)"
          ) << request.getRequestURI
        )
        false
      }
    }
  }

  override def doFilter(
      servletRequest: ServletRequest,
      servletResponse: ServletResponse,
      filterChain: FilterChain
  ): Unit = {
    val request = servletRequest.asInstanceOf[HttpServletRequest]
    implicit val response = servletResponse.asInstanceOf[HttpServletResponse]
    if (doFilter(request)) filterChain.doFilter(servletRequest, servletResponse)
    if (SecurityFilter.isRequestIgnoreTimeout(request)) {
      SecurityFilter.removeIgnoreTimeoutSignal(response)
    }
  }

  protected def addAccessHeaders(response: HttpServletResponse) {
    response.setHeader(
      "Access-Control-Allow-Origin",
      ServerConfiguration.BDP_SERVER_WEB_ALLOW_ORIGIN.getValue
    )
    response.setHeader("Access-Control-Allow-Credentials", "true")
    response.setHeader("Access-Control-Allow-Headers", "authorization,Content-Type")
    response.setHeader(
      "Access-Control-Allow-Methods",
      ServerConfiguration.BDP_SERVER_WEB_ALLOW_METHOD.getValue
    )
    val fullDateFormatEN =
      DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, new Locale("EN", "en"))
    response.setHeader("Date", fullDateFormatEN.format(new Date))
  }

  override def destroy(): Unit = {}
}

object SecurityFilter {
  private val logger: Logger = LoggerFactory.getLogger(classOf[SecurityFilter])
  private[linkis] val OTHER_SYSTEM_IGNORE_UM_USER = "dataworkcloud_rpc_user"
  private[linkis] val ALLOW_ACCESS_WITHOUT_TIMEOUT = "dataworkcloud_inner_request"

  def getLoginUserThrowsExceptionWhenTimeout(req: HttpServletRequest): Option[String] =
    Option(req.getCookies)
      .flatMap(cs => SSOUtils.getLoginUser(cs))
      .orElse(
        SSOUtils
          .getLoginUserIgnoreTimeout(key => Option(req.getHeader(key)))
          .filter(_ == OTHER_SYSTEM_IGNORE_UM_USER)
      )

  def getLoginUser(req: HttpServletRequest): Option[String] =
    Utils.tryCatch(getLoginUserThrowsExceptionWhenTimeout(req)) {
      case _: LoginExpireException =>
        SSOUtils
          .getLoginUserIgnoreTimeout(key =>
            Option(req.getCookies).flatMap(_.find(_.getName == key).map(_.getValue))
          )
          .filter(user =>
            user != OTHER_SYSTEM_IGNORE_UM_USER &&
              isRequestIgnoreTimeout(req)
          )
      case t => throw t
    }

  def isRequestIgnoreTimeout(req: HttpServletRequest): Boolean = Option(req.getCookies).exists(
    _.exists(c => c.getName == ALLOW_ACCESS_WITHOUT_TIMEOUT && c.getValue == "true")
  )

  def addIgnoreTimeoutSignal(response: HttpServletResponse): Unit =
    response.addCookie(ignoreTimeoutSignal())

  def ignoreTimeoutSignal(): Cookie = {
    val cookie = new Cookie(ALLOW_ACCESS_WITHOUT_TIMEOUT, "true")
    cookie.setMaxAge(-1)
    cookie.setPath("/")
    if (sslEnable) cookie.setSecure(true)
    cookie
  }

  def removeIgnoreTimeoutSignal(response: HttpServletResponse): Unit = {
    val cookie = new Cookie(ALLOW_ACCESS_WITHOUT_TIMEOUT, "false")
    cookie.setMaxAge(0)
    cookie.setPath("/")
    if (sslEnable) cookie.setSecure(true)
    response.addCookie(cookie)
  }

  def getLoginUsername(req: HttpServletRequest): String = {
    if (Configuration.IS_TEST_MODE.getValue) {
      val testUser = ServerConfiguration.BDP_TEST_USER.getValue
      if (StringUtils.isBlank(testUser)) {
        throw new IllegalUserTicketException("Need to set test user when enable test module")
      }
      testUser
    } else {
      getLoginUser(req).getOrElse(
        throw new IllegalUserTicketException(ILLEGAL_USER_TOKEN.getErrorDesc)
      )
    }

  }

  def setLoginUser(resp: HttpServletResponse, username: String): Unit =
    SSOUtils.setLoginUser(c => resp.addCookie(c), username)

  def removeLoginUser(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    SSOUtils.removeLoginUser(req.getCookies)
    SSOUtils.removeLoginUserByAddCookie(s => resp.addCookie(s))
  }

}
