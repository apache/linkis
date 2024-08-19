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

package org.apache.linkis.gateway.security

import org.apache.linkis.common.conf.{CommonVars, Configuration}
import org.apache.linkis.common.exception.LinkisException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.gateway.config.GatewayConfiguration
import org.apache.linkis.gateway.config.GatewayConfiguration._
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.security.sso.SSOInterceptor
import org.apache.linkis.gateway.security.token.TokenAuthentication
import org.apache.linkis.server.{validateFailed, Message}
import org.apache.linkis.server.conf.ServerConfiguration
import org.apache.linkis.server.exception.{LoginExpireException, NonLoginException}

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import java.io.File
import java.text.DateFormat
import java.util
import java.util.{Date, Locale}
import java.util.concurrent.TimeUnit

object SecurityFilter extends Logging {

  private val refererValidate = ServerConfiguration.BDP_SERVER_SECURITY_REFERER_VALIDATE.getValue
  private val referers = ServerConfiguration.BDP_SERVER_ADDRESS.getValue
  protected val testUser: String = ServerConfiguration.BDP_TEST_USER.getValue
  private val ACCESS_CONTROL_USER_PREFIX = "linkis.client.access.control.user."

  private val ipSet = new util.HashSet[String]()

  if (ENABLE_GATEWAY_AUTH.getValue) {
    Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = Utils.tryAndError(init())
      },
      0,
      2,
      TimeUnit.MINUTES
    )
  }

  def doFilter(gatewayContext: GatewayContext): Boolean = {
    addAccessHeaders(gatewayContext)
    if (ENABLE_GATEWAY_AUTH.getValue) {
      val host =
        gatewayContext.getRequest.getRemoteAddress.getAddress.toString.replaceAll("/", "")
      val port = gatewayContext.getRequest.getRemoteAddress.getPort
      if (!ipSet.contains(host)) {
        logger.error(s"${host} and ${port} is not in whitelist, it is dangerous")
        filterResponse(gatewayContext, Message.error(s"$host is not in whitelist"))
        return false
      }
    }
    if (refererValidate) {
      // Security certification support, referer limited(安全认证支持，referer限定)
      val referer = gatewayContext.getRequest.getHeaders.get("Referer")
      val refList = if (StringUtils.isNotEmpty(referers)) referers.split(",") else Array.empty
      val flag =
        refList.exists(ref =>
          referer != null && referer.nonEmpty && StringUtils.isNotEmpty(
            referer.head
          ) && referer.head.trim().contains(ref)
        )
      if (referer != null && referer.nonEmpty && StringUtils.isNotEmpty(referer.head) && !flag) {
        filterResponse(gatewayContext, validateFailed("Unallowed cross-site request(不允许的跨站请求)！"))
        return false
      }
      if (
          !gatewayContext.isWebSocketRequest && (referer == null || referer.isEmpty || StringUtils
            .isEmpty(referer.head))
      ) {
        filterResponse(gatewayContext, validateFailed("referer为空,不能继续访问"))
        return false
      }
      // Security certification support, solving verb tampering(安全认证支持，解决动词篡改)
      gatewayContext.getRequest.getMethod.toUpperCase(Locale.getDefault()) match {
        case "GET" | "POST" | "PUT" | "DELETE" | "HEAD" | "TRACE" | "CONNECT" | "OPTIONS" =>
        case _ =>
          filterResponse(
            gatewayContext,
            validateFailed("Do not use HTTP verbs to tamper with(不可使用HTTP动词篡改)！")
          )
          return false
      }
    }
    val isPassAuthRequest = GatewayConfiguration.PASS_AUTH_REQUEST_URI.exists(r =>
      !r.equals("") && gatewayContext.getRequest.getRequestURI.startsWith(r)
    )

    val isUserRestful = gatewayContext.getRequest.getRequestURI.startsWith(
      ServerConfiguration.BDP_SERVER_USER_URI.getValue
    )

    if (isUserRestful) {
      Utils.tryCatch(userRestful.doUserRequest(gatewayContext)) { t =>
        val message = t match {
          case dwc: LinkisException => dwc.getMessage
          case _ => "login failed! reason: " + ExceptionUtils.getRootCauseMessage(t)
        }
        logger.error("login failed! Reason: " + message, t)
        filterResponse(
          gatewayContext,
          Message.error(message).<<(gatewayContext.getRequest.getRequestURI)
        )
      }
      return false
    } else if (isPassAuthRequest && !GatewayConfiguration.ENABLE_SSO_LOGIN.getValue) {
      logger.info("No login needed for proxy uri: " + gatewayContext.getRequest.getRequestURI)
    } else if (TokenAuthentication.isTokenRequest(gatewayContext)) {
      TokenAuthentication.tokenAuth(gatewayContext)
    } else {
      val userName = Utils.tryCatch(GatewaySSOUtils.getLoginUser(gatewayContext)) {
        case n @ (_: NonLoginException | _: LoginExpireException) =>
          if (Configuration.IS_TEST_MODE.getValue) None
          else {
            filterResponse(
              gatewayContext,
              Message.noLogin(n.getMessage) << gatewayContext.getRequest.getRequestURI
            )
            return false
          }
        case t: Throwable =>
          logger.warn("", t)
          throw t
      }
      if (userName.isDefined) {
        logger.info(s"User $userName has logged in.")
      } else if (Configuration.IS_TEST_MODE.getValue) {
        logger.info("test mode! login for uri: " + gatewayContext.getRequest.getRequestURI)
        GatewaySSOUtils.setLoginUser(gatewayContext, testUser)
      } else if (GatewayConfiguration.ENABLE_SSO_LOGIN.getValue) {
        val user = SSOInterceptor.getSSOInterceptor.getUser(gatewayContext)
        if (StringUtils.isNotBlank(user)) {
          GatewaySSOUtils.setLoginUser(gatewayContext.getRequest, user)
        } else if (isPassAuthRequest) {
          gatewayContext.getResponse.redirectTo(
            SSOInterceptor.getSSOInterceptor.redirectTo(gatewayContext.getRequest.getURI)
          )
          gatewayContext.getResponse.sendResponse()
          return false
        } else {
          filterResponse(
            gatewayContext,
            Message
              .noLogin("You are not logged in, please login first(您尚未登录，请先登录)!")
              .data("enableSSO", true)
              .data(
                "SSOURL",
                SSOInterceptor.getSSOInterceptor.redirectTo(gatewayContext.getRequest.getURI)
              ) << gatewayContext.getRequest.getRequestURI
          )
          return false
        }
      } else if (
          gatewayContext.getRequest.getRequestURI.matches(
            GatewayConfiguration.GATEWAY_NO_AUTH_URL_REGEX.getValue
          )
      ) {
        logger.info(
          "Not logged in, still let it pass (GATEWAY_NO_AUTH_URL): " + gatewayContext.getRequest.getRequestURI
        )
      } else {
        filterResponse(
          gatewayContext,
          Message.noLogin(
            "You are not logged in, please login first(您尚未登录，请先登录)!"
          ) << gatewayContext.getRequest.getRequestURI
        )
        return false
      }
    }

    // 访问控制, 先判断当前用户是否可以在当前IP执行，再判断当前IP是否有权限调用当前接口
    // Access control
    // first determine whether the current user can perform operations from the current IP address,
    // and then determine whether the current IP address has permission to call the current interface.
    if (
        GatewayConfiguration.ACCESS_CONTROL_USER_ENABLED.getValue && !isPassAuthRequest && !isUserRestful
    ) {
      val userName = GatewaySSOUtils.getLoginUsername(gatewayContext)
      val userIps =
        CommonVars.apply(ACCESS_CONTROL_USER_PREFIX + userName, "").getValue
      val host =
        gatewayContext.getRequest.getRemoteAddress.getAddress.toString.replaceAll("/", "")
      if (StringUtils.isNotEmpty(userIps)) {
        if (!userIps.contains(host)) {
          val message =
            Message.error(
              s"Unauthorized access! User $userName is prohibited from accessing from the current IP $host. (未授权的访问！用户${userName}禁止在当前IP${host}访问。)"
            )
          filterResponse(gatewayContext, message)
          return false
        }
      }
    }
    if (
        GatewayConfiguration.ACCESS_CONTROL_ENABLED.getValue && !isPassAuthRequest && !isUserRestful
    ) {
      if (
          StringUtils.isNotEmpty(GatewayConfiguration.ACCESS_CONTROL_IP.getValue) && StringUtils
            .isNotEmpty(GatewayConfiguration.ACCESS_CONTROL_URL.getValue)
      ) {
        val host =
          gatewayContext.getRequest.getRemoteAddress.getAddress.toString.replaceAll("/", "")
        if (GatewayConfiguration.ACCESS_CONTROL_IP.getValue.contains(host)) {
          val requestUrl = gatewayContext.getRequest.getRequestURI
          if (!GatewayConfiguration.ACCESS_CONTROL_URL.getValue.contains(requestUrl)) {
            val message =
              Message.error(
                s"Unauthorized access! IP $host is prohibited from accessing this URL. (未授权的访问！当前IP${host}禁止访问此URL。)"
              )
            filterResponse(gatewayContext, message)
            return false
          }
        }
      }
    }
    true
  }

  private var userRestful: UserRestful = _

  def setUserRestful(userRestful: UserRestful): Unit = this.userRestful = userRestful

  def filterResponse(gatewayContext: GatewayContext, message: Message): Unit = {
    gatewayContext.getResponse.setStatus(Message.messageToHttpStatus(message))
    gatewayContext.getResponse.write(message)
    gatewayContext.getResponse.sendResponse()
  }

  private def init(): Unit = {
    Utils.tryAndError {
      val authFile =
        new File(this.getClass.getClassLoader.getResource(AUTH_IP_FILE.getValue).toURI.getPath)
      import scala.io.Source
      val source = Source.fromFile(authFile, "UTF-8")
      val lines = Utils.tryFinally(source.getLines().toArray)(source.close())
      lines.foreach(ipSet.add)
    }
  }

  protected def addAccessHeaders(gatewayContext: GatewayContext) {
    val response = gatewayContext.getResponse
    response.setHeader(
      "Access-Control-Allow-Origin",
      GatewayConfiguration.GATEWAY_HEADER_ALLOW_ORIGIN.getValue
    )
    response.setHeader("Access-Control-Allow-Credentials", "true")
    response.setHeader("Access-Control-Allow-Headers", "authorization,Content-Type")
    response.setHeader(
      "Access-Control-Allow-Methods",
      GatewayConfiguration.GATEWAY_HEADER_ALLOW_METHOD.getValue
    )
    val fullDateFormatEN =
      DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, new Locale("EN", "en"))
    response.setHeader("Date", fullDateFormatEN.format(new Date))
  }

}
