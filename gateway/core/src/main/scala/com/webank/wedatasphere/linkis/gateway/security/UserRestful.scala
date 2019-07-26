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

import com.webank.wedatasphere.linkis.common.utils.{RSAUtils, Utils}
import com.webank.wedatasphere.linkis.gateway.config.GatewayConfiguration
import com.webank.wedatasphere.linkis.gateway.http.GatewayContext
import com.webank.wedatasphere.linkis.server.conf.ServerConfiguration
import com.webank.wedatasphere.linkis.server.security.SSOUtils
import com.webank.wedatasphere.linkis.server.{Message, _}
import org.apache.commons.lang.StringUtils

/**
  * created by cooperyang on 2019/1/9.
  */
trait UserRestful {

  def doUserRequest(gatewayContext: GatewayContext): Unit

}
abstract class AbstractUserRestful extends UserRestful {

  private var resourceReleases: Array[ResourceRelease] = Array.empty

  def setResourceReleases(resourceReleases: Array[ResourceRelease]): Unit = this.resourceReleases = resourceReleases

  private val userRegex = {
    var userURI = ServerConfiguration.BDP_SERVER_USER_URI.getValue
    if(!userURI.endsWith("/")) userURI += "/"
    userURI
  }

  override def doUserRequest(gatewayContext: GatewayContext): Unit = {
    val path = gatewayContext.getRequest.getRequestURI.replace(userRegex, "")
    val message = path match {
      case "login" =>
        Utils.tryCatch {
          val loginUser = GatewaySSOUtils.getLoginUsername(gatewayContext)
          Message.error(loginUser + "Already logged in, please log out before signing in(已经登录，请先退出再进行登录)！").data("redirectToIndex", true)
        }(_ => login(gatewayContext))
      case "logout" => logout(gatewayContext)
      case "userInfo" => userInfo(gatewayContext)
      case "publicKey" => publicKey(gatewayContext)
      case "heartbeat" => heartbeat(gatewayContext)
      case _ =>
        Message.error("unknown request URI " + path)
    }
    gatewayContext.getResponse.write(message)
    gatewayContext.getResponse.setStatus(Message.messageToHttpStatus(message))
    gatewayContext.getResponse.sendResponse()
  }

  def login(gatewayContext: GatewayContext): Message

  def logout(gatewayContext: GatewayContext): Message = {
    GatewaySSOUtils.removeLoginUser(gatewayContext)
    if(resourceReleases != null) resourceReleases.foreach(_.release(gatewayContext))
    "Logout successful(退出登录成功)！"
  }

  def userInfo(gatewayContext: GatewayContext): Message

  def publicKey(gatewayContext: GatewayContext): Message = {
    val message = Message.ok("Gain success(获取成功)！").data("enable", SSOUtils.sslEnable)
    if(SSOUtils.sslEnable) message.data("publicKey", RSAUtils.getDefaultPublicKey())
    message
  }

  def heartbeat(gatewayContext: GatewayContext): Message = Utils.tryCatch {
    GatewaySSOUtils.getLoginUsername(gatewayContext)
    "Maintain heartbeat success(维系心跳成功)！"
  }(t => Message.noLogin(t.getMessage))
}
abstract class UserPwdAbstractUserRestful extends AbstractUserRestful {

  override def login(gatewayContext: GatewayContext): Message = {
    val userNameArray = gatewayContext.getRequest.getQueryParams.get("userName")
    val passwordArray = gatewayContext.getRequest.getQueryParams.get("password")
    val (userName, password) = if(userNameArray != null && userNameArray.nonEmpty &&
      passwordArray != null && passwordArray.nonEmpty)
      (userNameArray.head, passwordArray.head)
    else if(StringUtils.isNotBlank(gatewayContext.getRequest.getRequestBody)){
      val json = BDPJettyServerHelper.gson.fromJson(gatewayContext.getRequest.getRequestBody, classOf[java.util.Map[String, Object]])
      (json.get("userName"), json.get("password"))
    } else (null, null)
    if(userName == null || StringUtils.isBlank(userName.toString)) {
      Message.error("Username can not be empty(用户名不能为空)！")
    } else if(password == null || StringUtils.isBlank(password.toString)) {
      Message.error("password can not be blank(密码不能为空)！")
    } else {
      //warn: For easy to useing linkis,Admin skip login
      if(GatewayConfiguration.ADMIN_USER.getValue.equals(userName.toString) && userName.toString.equals(password.toString)){
          GatewaySSOUtils.setLoginUser(gatewayContext, userName.toString)
          "login successful(登录成功)！".data("userName", userName)
            .data("isAdmin", true)
            .data("loginNum", 4)
            .data("lastLoginTime", System.currentTimeMillis)
      } else {
        val lowerCaseUserName = userName.toString.toLowerCase
        val message = login(lowerCaseUserName, password.toString)
        if(message.getStatus == 0) GatewaySSOUtils.setLoginUser(gatewayContext, lowerCaseUserName)
        message
      }
    }
  }

  protected def login(userName: String, password: String): Message

}