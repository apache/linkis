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

package com.webank.wedatasphere.linkis.gateway.security

import java.text.DateFormat
import java.util.{Date, Locale}

import com.webank.wedatasphere.linkis.common.conf.Configuration
import com.webank.wedatasphere.linkis.common.exception.DWCException
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.gateway.config.GatewayConfiguration
import com.webank.wedatasphere.linkis.gateway.http.GatewayContext
import com.webank.wedatasphere.linkis.gateway.security.sso.SSOInterceptor
import com.webank.wedatasphere.linkis.gateway.security.token.TokenAuthentication
import com.webank.wedatasphere.linkis.server.conf.ServerConfiguration
import com.webank.wedatasphere.linkis.server.exception.{LoginExpireException, NonLoginException}
import com.webank.wedatasphere.linkis.server.{Message, validateFailed}
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils

/**
  * created by cooperyang on 2019/1/9.
  */
object SecurityFilter {

  private val refererValidate = ServerConfiguration.BDP_SERVER_SECURITY_REFERER_VALIDATE.getValue
  private val localAddress = ServerConfiguration.BDP_SERVER_ADDRESS.getValue
  protected val testUser: String = ServerConfiguration.BDP_TEST_USER.getValue

  private var userRestful: UserRestful = _
  def setUserRestful(userRestful: UserRestful): Unit = this.userRestful = userRestful

  def filterResponse(gatewayContext: GatewayContext, message: Message): Unit = {
    gatewayContext.getResponse.setStatus(Message.messageToHttpStatus(message))
    gatewayContext.getResponse.write(message)
    gatewayContext.getResponse.sendResponse()
  }

  def doFilter(gatewayContext: GatewayContext): Boolean = {
    addAccessHeaders(gatewayContext)
    if(refererValidate) {
      //Security certification support, referer limited(安全认证支持，referer限定)
      val referer = gatewayContext.getRequest.getHeaders.get("Referer")
      if(referer != null && referer.nonEmpty && StringUtils.isNotEmpty(referer.head) && !referer.head.trim.contains(localAddress)) {
        filterResponse(gatewayContext, validateFailed("Unallowed cross-site request(不允许的跨站请求)！"))
        return false
      }
      //Security certification support, solving verb tampering(安全认证支持，解决动词篡改)
      gatewayContext.getRequest.getMethod.toUpperCase match {
        case "GET" | "POST" | "PUT" | "DELETE" | "HEAD" | "TRACE" | "CONNECT" | "OPTIONS" =>
        case _ =>
          filterResponse(gatewayContext, validateFailed("Do not use HTTP verbs to tamper with(不可使用HTTP动词篡改)！"))
          return false
      }
    }
    val isPassAuthRequest = GatewayConfiguration.PASS_AUTH_REQUEST_URI.exists(gatewayContext.getRequest.getRequestURI.startsWith)
    if(gatewayContext.getRequest.getRequestURI.startsWith(ServerConfiguration.BDP_SERVER_USER_URI.getValue)) {
      Utils.tryCatch(userRestful.doUserRequest(gatewayContext)){ t =>
        val message = t match {
          case dwc: DWCException => dwc.getMessage
          case _ => "login failed! reason: " + ExceptionUtils.getRootCauseMessage(t)
        }
        GatewaySSOUtils.error("login failed!", t)
        filterResponse(gatewayContext, Message.error(message).<<(gatewayContext.getRequest.getRequestURI))
      }
      false
    } else if(isPassAuthRequest && !GatewayConfiguration.ENABLE_SSO_LOGIN.getValue) {
      GatewaySSOUtils.info("No login needed for proxy uri: " + gatewayContext.getRequest.getRequestURI)
      true
    } else if(TokenAuthentication.isTokenRequest(gatewayContext)) {
      TokenAuthentication.tokenAuth(gatewayContext)
    } else {
      val userName = Utils.tryCatch(GatewaySSOUtils.getLoginUser(gatewayContext)){
        case n @ (_: NonLoginException | _: LoginExpireException )=>
          if(Configuration.IS_TEST_MODE.getValue) None else {
            filterResponse(gatewayContext, Message.noLogin(n.getMessage) << gatewayContext.getRequest.getRequestURI)
            return false
          }
        case t: Throwable =>
          GatewaySSOUtils.warn("", t)
          throw t
      }
      if(userName.isDefined) {
        true
      } else if(Configuration.IS_TEST_MODE.getValue) {
        GatewaySSOUtils.info("test mode! login for uri: " + gatewayContext.getRequest.getRequestURI)
        GatewaySSOUtils.setLoginUser(gatewayContext, testUser)
        true
      } else if(GatewayConfiguration.ENABLE_SSO_LOGIN.getValue) {
        val user = SSOInterceptor.getSSOInterceptor.getUser(gatewayContext)
        if(StringUtils.isNotBlank(user)) {
          GatewaySSOUtils.setLoginUser(gatewayContext.getRequest, user)
          true
        } else if(isPassAuthRequest) {
          gatewayContext.getResponse.redirectTo(SSOInterceptor.getSSOInterceptor.redirectTo(gatewayContext.getRequest.getURI))
          gatewayContext.getResponse.sendResponse()
          false
        } else {
          filterResponse(gatewayContext, Message.noLogin("You are not logged in, please login first(您尚未登录，请先登录)!")
            .data("enableSSO", true).data("SSOURL", SSOInterceptor.getSSOInterceptor.redirectTo(gatewayContext.getRequest.getURI)) << gatewayContext.getRequest.getRequestURI)
          false
        }
      } else {
        filterResponse(gatewayContext, Message.noLogin("You are not logged in, please login first(您尚未登录，请先登录)!") << gatewayContext.getRequest.getRequestURI)
        false
      }
    }
  }

  protected def addAccessHeaders(gatewayContext: GatewayContext) {
    val response = gatewayContext.getResponse
    response.setHeader("Access-Control-Allow-Origin", "*")
    response.setHeader("Access-Control-Allow-Credentials", "true")
    response.setHeader("Access-Control-Allow-Headers", "authorization,Content-Type")
    response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, HEAD, DELETE")
    val fullDateFormatEN = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, new Locale("EN", "en"))
    response.setHeader("Date", fullDateFormatEN.format(new Date))
  }

}
