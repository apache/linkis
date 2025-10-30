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

package org.apache.linkis.gateway.security.token

import org.apache.linkis.common.utils.{Logging, MD5Utils, Utils}
import org.apache.linkis.gateway.authentication.service.TokenService
import org.apache.linkis.gateway.config.GatewayConfiguration
import org.apache.linkis.gateway.config.GatewayConfiguration._
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.security.{GatewaySSOUtils, SecurityFilter}
import org.apache.linkis.server.Message
import org.apache.linkis.server.utils.ModuleUserUtils

import org.apache.commons.lang3.StringUtils

import scala.util.Try

object TokenAuthentication extends Logging {

  private var tokenService: TokenService = _

  def setTokenService(tokenService: TokenService): Unit = {
    this.tokenService = tokenService
  }

  def isTokenRequest(gatewayContext: GatewayContext): Boolean = {
    (gatewayContext.getRequest.getHeaders.containsKey(TOKEN_KEY) &&
      gatewayContext.getRequest.getHeaders.containsKey(
        TOKEN_USER_KEY
      )) || (gatewayContext.getRequest.getCookies.containsKey(TOKEN_KEY) &&
      gatewayContext.getRequest.getCookies.containsKey(TOKEN_USER_KEY))
  }

  def tokenAuth(gatewayContext: GatewayContext, login: Boolean = false): Boolean = {
    if (!ENABLE_TOKEN_AUTHENTICATION.getValue) {
      val message =
        Message.noLogin(s"Gateway未启用token认证，请采用其他认证方式!") << gatewayContext.getRequest.getRequestURI
      SecurityFilter.filterResponse(gatewayContext, message)
      return false
    }
    val tokenOpt = Option(gatewayContext.getRequest.getHeaders.get(TOKEN_KEY)).map(_.head)
    val tokenUserOpt = Option(gatewayContext.getRequest.getHeaders.get(TOKEN_USER_KEY)).map(_.head)
    var token = tokenOpt.getOrElse("")
    var tokenUser = tokenUserOpt.getOrElse("")

    var host = gatewayContext.getRequest.getRequestRealIpAddr()
    logger.info(
      String
        .format("Use Linkis Auth : %s,User : %s,Ip : %s", encryptToken(token), tokenUser, host)
    )
    if (StringUtils.isBlank(token) || StringUtils.isBlank(tokenUser)) {
      val cookieTokenOpt = Option(gatewayContext.getRequest.getCookies.get(TOKEN_KEY)).map(_.head)
      val cookieTokenUserOpt =
        Option(gatewayContext.getRequest.getCookies.get(TOKEN_USER_KEY)).map(_.head)
      val isValid = cookieTokenOpt.nonEmpty && StringUtils.isNotBlank(
        cookieTokenOpt.get.getValue
      ) && cookieTokenUserOpt.nonEmpty && StringUtils.isNotBlank(cookieTokenUserOpt.get.getValue)
      if (!isValid) {
        val message = Message.noLogin(
          s"请在Header或Cookie中同时指定$TOKEN_KEY 和 $TOKEN_USER_KEY，以便完成token认证！"
        ) << gatewayContext.getRequest.getRequestURI
        SecurityFilter.filterResponse(gatewayContext, message)
        return false
      }
      token = cookieTokenOpt.get.getValue
      tokenUser = cookieTokenUserOpt.get.getValue
    }
    var tokenAlive = false
    val tokenAliveArr = gatewayContext.getRequest.getHeaders.get(TOKEN_ALIVE_KEY)
    var tokenAliveStr = ""
    if (null != tokenAliveArr && !tokenAliveArr.isEmpty) {
      tokenAliveStr = gatewayContext.getRequest.getHeaders.get(TOKEN_ALIVE_KEY)(0)
    } else {
      val tokenAliveCookieArr = gatewayContext.getRequest.getCookies.get(TOKEN_ALIVE_KEY)
      if (null != tokenAliveCookieArr && !tokenAliveCookieArr.isEmpty) {
        tokenAliveStr = tokenAliveCookieArr(0).getValue
      }
    }
    if (StringUtils.isNotBlank(tokenAliveStr)) {
      if (tokenAliveStr.toLowerCase().equals(GatewayConfiguration.TOKEN_ALIVE_TRUE)) {
        tokenAlive = true
      }
    }
    var authMsg: Message = Message.noLogin(
      s"未授权的token$token，无法将请求绑定给tokenUser$tokenUser!"
    ) << gatewayContext.getRequest.getRequestURI
    val ok: Boolean = Utils.tryCatch(tokenService.doAuth(token, tokenUser, host))(t => {
      authMsg = Message.noLogin(
        s"Token Authentication Failed, token: $token，tokenUser: $tokenUser, reason: ${t.getMessage}"
      ) << gatewayContext.getRequest.getRequestURI
      false
    })
    if (ok) {
      logger.info(
        s"Token authentication succeed, uri: ${gatewayContext.getRequest.getRequestURI}, token: $token, tokenUser: $tokenUser, host: $host."
      )
      if (login) {
        logger.info(
          s"Token authentication succeed, uri: ${gatewayContext.getRequest.getRequestURI}, token: $token, tokenUser: $tokenUser."
        )
        GatewaySSOUtils.setLoginUser(gatewayContext, tokenUser)
        val msg =
          Message.ok("login successful(登录成功)！").data("userName", tokenUser).data("isAdmin", false)
        SecurityFilter.filterResponse(gatewayContext, msg)
        return false
      }
      if (GatewayConfiguration.ENABLE_TOEKN_AUTHENTICATION_ALIVE.getValue || tokenAlive) {
        if (logger.isDebugEnabled()) {
          logger.debug(s"Token auth of user : ${tokenUser} has param : tokenAlive : true.")
        }
        GatewaySSOUtils.setLoginUser(gatewayContext.getRequest, tokenUser, true)
      } else {
        GatewaySSOUtils.setLoginUser(gatewayContext.getRequest, tokenUser, false)
      }
      true
    } else {
      logger.info(
        s"Token authentication fail, uri: ${gatewayContext.getRequest.getRequestURI}, token: $token, tokenUser: $tokenUser, host: $host."
      )
      SecurityFilter.filterResponse(gatewayContext, authMsg)
      false
    }
  }

  def encryptToken(token: String): String = {
    if (StringUtils.isBlank(token)) ""
    else MD5Utils.encrypt(token)
  }

}
