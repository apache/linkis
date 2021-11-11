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
 
package org.apache.linkis.gateway.security.token

import java.io.File
import java.util.Properties
import java.util.concurrent.TimeUnit

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.gateway.config.GatewayConfiguration._
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.security.{GatewaySSOUtils, SecurityFilter}
import org.apache.linkis.server.Message
import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.commons.lang.StringUtils

object TokenAuthentication extends Logging {

  private val (props, file) = if(ENABLE_TOKEN_AUTHENTICATION.getValue)
    (new Properties, new File(this.getClass.getClassLoader.getResource(TOKEN_AUTHENTICATION_CONFIG.getValue).toURI.getPath))
  else (null, null)
  private var lastModified = 0l

  if(ENABLE_TOKEN_AUTHENTICATION.getValue) {
    Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = Utils.tryAndError(init())
    }, TOKEN_AUTHENTICATION_SCAN_INTERVAL.getValue, TOKEN_AUTHENTICATION_SCAN_INTERVAL.getValue, TimeUnit.MILLISECONDS)
    init()
  }

  private def init(): Unit = if(file.lastModified() > lastModified) {
    lastModified = file.lastModified()
    info(s"loading token authentication file $file.")
    val newProps = new Properties
    val input = FileUtils.openInputStream(file)
    Utils.tryFinally(newProps.load(input))(IOUtils.closeQuietly(input))
    props.putAll(newProps)
  }

  private def validateTokenUser(token: String, tokenUser: String): Boolean = {
    val tokenUsers = props.getProperty(token)
    if(tokenUsers == "*" || (StringUtils.isNotBlank(tokenUsers) && tokenUsers.contains(tokenUser))) true
    else false
  }

  def isTokenRequest(gatewayContext: GatewayContext) : Boolean = {
    (gatewayContext.getRequest.getHeaders.containsKey(TOKEN_KEY) &&
      gatewayContext.getRequest.getHeaders.containsKey(TOKEN_USER_KEY)) || (
      gatewayContext.getRequest.getCookies.containsKey(TOKEN_KEY) &&
        gatewayContext.getRequest.getCookies.containsKey(TOKEN_USER_KEY))
  }

  def tokenAuth(gatewayContext: GatewayContext): Boolean = {
    if(!ENABLE_TOKEN_AUTHENTICATION.getValue) {
      val message = Message.noLogin(s"Gateway未启用token认证，请采用其他认证方式!") << gatewayContext.getRequest.getRequestURI
      SecurityFilter.filterResponse(gatewayContext, message)
      return false
    }
    var token = gatewayContext.getRequest.getHeaders.get(TOKEN_KEY)(0)
    var tokenUser = gatewayContext.getRequest.getHeaders.get(TOKEN_USER_KEY)(0)
    if(StringUtils.isBlank(token) || StringUtils.isBlank(tokenUser)) {
      token = gatewayContext.getRequest.getCookies.get(TOKEN_KEY)(0).getValue
      tokenUser = gatewayContext.getRequest.getCookies.get(TOKEN_USER_KEY)(0).getValue
      if(StringUtils.isBlank(token) || StringUtils.isBlank(tokenUser)) {
        val message = Message.noLogin(s"请在Header或Cookie中同时指定$TOKEN_KEY 和 $TOKEN_USER_KEY，以便完成token认证！") << gatewayContext.getRequest.getRequestURI
        SecurityFilter.filterResponse(gatewayContext, message)
        return false
      }
    }
    if(validateTokenUser(token, tokenUser)){
      info(s"Token authentication succeed, uri: ${gatewayContext.getRequest.getRequestURI}, token: $token, tokenUser: $tokenUser.")
      GatewaySSOUtils.setLoginUser(gatewayContext.getRequest, tokenUser)
      true
    } else {
      val message = Message.noLogin(s"未授权的token$token，无法将请求绑定给tokenUser$tokenUser!") << gatewayContext.getRequest.getRequestURI
      SecurityFilter.filterResponse(gatewayContext, message)
      false
    }
  }

}