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
 
package org.apache.linkis.gateway.security

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.gateway.http.{GatewayContext, GatewayHttpRequest}
import org.apache.linkis.server.exception.LoginExpireException
import org.apache.linkis.server.security.SecurityFilter._
import org.apache.linkis.server.security.{SSOUtils, ServerSSOUtils}
import javax.servlet.http.Cookie

import scala.collection.JavaConversions._

object GatewaySSOUtils extends Logging {
  private def getCookies(gatewayContext: GatewayContext): Array[Cookie] = gatewayContext.getRequest.getCookies.flatMap(_._2).toArray
  def getLoginUser(gatewayContext: GatewayContext): Option[String] = {
    val cookies = getCookies(gatewayContext)
    Utils.tryCatch(SSOUtils.getLoginUser(cookies)) {
      case _: LoginExpireException if Option(cookies).exists(_.exists(c => c.getName == ALLOW_ACCESS_WITHOUT_TIMEOUT && c.getValue == "true")) =>
        ServerSSOUtils.getLoginUserIgnoreTimeout(key => Option(cookies).flatMap(_.find(_.getName == key).map(_.getValue))).filter(_ != OTHER_SYSTEM_IGNORE_UM_USER)
      case t => throw t
    }
  }
  def getLoginUsername(gatewayContext: GatewayContext): String = SSOUtils.getLoginUsername(getCookies(gatewayContext))
  def setLoginUser(gatewayContext: GatewayContext, username: String): Unit = {
    val proxyUser = ProxyUserUtils.getProxyUser(username)
    SSOUtils.setLoginUser(c => gatewayContext.getResponse.addCookie(c), proxyUser)
  }
  def setLoginUser(request: GatewayHttpRequest, username: String): Unit = {
    val proxyUser = ProxyUserUtils.getProxyUser(username)
    SSOUtils.setLoginUser(c => request.addCookie(c.getName, Array(c)), proxyUser)
  }
  def removeLoginUser(gatewayContext: GatewayContext): Unit = {
    SSOUtils.removeLoginUser(gatewayContext.getRequest.getCookies.flatMap(_._2).toArray)
    SSOUtils.removeLoginUserByAddCookie(c => gatewayContext.getResponse.addCookie(c))
  }
  def updateLastAccessTime(gatewayContext: GatewayContext): Unit = SSOUtils.updateLastAccessTime(gatewayContext.getRequest.getCookies.flatMap(_._2).toArray)
}
