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

import java.util.Random

import com.google.gson.{Gson, JsonObject}
import com.webank.wedatasphere.linkis.common.utils.{Logging, RSAUtils, Utils}
import com.webank.wedatasphere.linkis.gateway.config.GatewayConfiguration
import com.webank.wedatasphere.linkis.gateway.http.GatewayContext
import com.webank.wedatasphere.linkis.gateway.security.sso.SSOInterceptor
import com.webank.wedatasphere.linkis.protocol.usercontrol.{RequestLogin, RequestRegister, ResponseLogin, ResponseRegister}
import com.webank.wedatasphere.linkis.rpc.Sender
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
abstract class AbstractUserRestful extends UserRestful with Logging {

  private var securityHooks: Array[SecurityHook] = Array.empty

  def setSecurityHooks(securityHooks: Array[SecurityHook]): Unit = this.securityHooks = securityHooks

  private val userRegex = {
    var userURI = ServerConfiguration.BDP_SERVER_USER_URI.getValue
    if(!userURI.endsWith("/")) userURI += "/"
    userURI
  }

  override def doUserRequest(gatewayContext: GatewayContext): Unit = {
    val path = gatewayContext.getRequest.getRequestURI.replace(userRegex, "")
    val message = path match {
      case "register" => register(gatewayContext)
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
        warn(s"Unknown request URI" + path)
        Message.error("unknown request URI " + path)
    }
    gatewayContext.getResponse.write(message)
    gatewayContext.getResponse.setStatus(Message.messageToHttpStatus(message))
    gatewayContext.getResponse.sendResponse()
  }

  def login(gatewayContext: GatewayContext): Message = {
    val message = tryLogin(gatewayContext)
    if(securityHooks != null) securityHooks.foreach(_.postLogin(gatewayContext))
    message
  }

  def register(gatewayContext: GatewayContext): Message = {
    val message = tryRegister(gatewayContext)
    message
  }

  protected def tryLogin(context: GatewayContext): Message

  def logout(gatewayContext: GatewayContext): Message = {
    GatewaySSOUtils.removeLoginUser(gatewayContext)
    if(GatewayConfiguration.ENABLE_SSO_LOGIN.getValue) SSOInterceptor.getSSOInterceptor.logout(gatewayContext)
    if(securityHooks != null) securityHooks.foreach(_.preLogout(gatewayContext))
    "Logout successful(退出登录成功)！"
  }

  def userInfo(gatewayContext: GatewayContext): Message = {
    "get user information succeed!".data("userName", GatewaySSOUtils.getLoginUsername(gatewayContext))
  }

  def publicKey(gatewayContext: GatewayContext): Message = {
    val message = Message.ok("Gain success(获取成功)！").data("enable", SSOUtils.sslEnable)
    if(SSOUtils.sslEnable) message.data("publicKey", RSAUtils.getDefaultPublicKey())
    message
  }

  def heartbeat(gatewayContext: GatewayContext): Message = Utils.tryCatch {
    GatewaySSOUtils.getLoginUsername(gatewayContext)
    "Maintain heartbeat success(维系心跳成功)！"
  }(t => Message.noLogin(t.getMessage))

  protected def tryRegister(context: GatewayContext): Message
}
abstract class UserPwdAbstractUserRestful extends AbstractUserRestful with Logging{

  private val sender: Sender = Sender.getSender(GatewayConfiguration.USERCONTROL_SPRING_APPLICATION_NAME.getValue)
  private val LINE_DELIMITER = "</br>"

  override protected def tryLogin(gatewayContext: GatewayContext): Message = {
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
      Message.error("Password can not be blank(密码不能为空)！")
    } else {
      //warn: For easy to useing linkis,Admin skip login
      if(GatewayConfiguration.ADMIN_USER.getValue.equals(userName.toString) && userName.toString.equals(password.toString)){
          GatewaySSOUtils.setLoginUser(gatewayContext, userName.toString)
          "login successful(登录成功)！".data("userName", userName)
            .data("isAdmin", true)
      } else {
        // firstly for test user
        var message = Message.ok()
        if (GatewayConfiguration.USERCONTROL_SWITCH_ON.getValue) {
          message = userControlLogin(userName.toString, password.toString, gatewayContext)
        } else {
          // standard login
          val lowerCaseUserName = userName.toString.toLowerCase
          message = login(lowerCaseUserName, password.toString)
          if(message.getStatus == 0) GatewaySSOUtils.setLoginUser(gatewayContext, lowerCaseUserName)
        }
        if (message.getData.containsKey("errmsg")) {
          message.setMessage(message.getMessage + LINE_DELIMITER + message.getData.get("errmsg").toString)
        }
        message
      }
    }
  }

  protected def login(userName: String, password: String): Message

  protected def register(gatewayContext: GatewayContext) : Message

  def userControlLogin(userName: String, password: String, gatewayContext: GatewayContext): Message = {
    var message = Message.ok()
    // usercontrol switch on(开启了用户控制开关)
          val requestLogin = new RequestLogin
    requestLogin.setUserName(userName.toString).setPassword(password.toString)
          Utils.tryCatch(sender.ask(requestLogin) match {
            case r: ResponseLogin =>
              message.setStatus(r.getStatus)
              if (StringUtils.isNotBlank(r.getErrMsg)) {
                message.data("errmsg", r.getErrMsg)
              }
              if (0 == r.getStatus) {
                GatewaySSOUtils.setLoginUser(gatewayContext, userName.toString)
                message.setStatus(0)
                message.setMessage("Login successful(登录成功)")
              } else {
                message = Message.error("Invalid username or password, please check and try again later(用户名或密码无效，请稍后再试)")
              }
          }) {
      t => {
              warn(s"Login rpc request error, err message ", t)
              message.setStatus(1)
              message.setMessage("System error, please try again later(系统异常，请稍后再试)")
              message.data("errmsg", t.getMessage)
          }
        }
        message
      }

  override def tryRegister(gatewayContext: GatewayContext): Message = {
    var message = Message.ok()
    if (GatewayConfiguration.USERCONTROL_SWITCH_ON.getValue) {
      message = userControlRegister(gatewayContext)
    } else {
      // TODO use normal register only when it's implemented(仅当实现了通用注册，才可以调注册接口)
      message = register(gatewayContext)
  }
    message
  }

  /**
    * userControl register(用户控制模块登录)
    * @param gatewayContext
    * @return
    */
  private def userControlRegister(gatewayContext: GatewayContext): Message = {
    val message = Message.ok()
    val gson = new Gson
    val requestRegister = new RequestRegister
    val requestBody: String = gatewayContext.getRequest.getRequestBody
    Utils.tryCatch({
      requestRegister.setParams(requestBody)
      sender.ask(requestRegister) match {
        case r: ResponseRegister =>
          message.setStatus(r.getStatus)
          message.setMessage(r.getMessage)
          var map = r.getData
          message.setData(map)
          message.setMethod(r.getMethod)
          info(s"Register rpc success. requestRegister=" + gson.toJson(requestRegister) + ", response=" + gson.toJson(r))
        }
    }) {
      e =>
       warn(s"Register rpc request error. err message ", e)
        message.setStatus(1)
        message.setMessage("System, please try again later(系统异常，请稍后再试)")
    }
    if (message.getData.containsKey("errmsg")) {
      // for frontend display
      message.setMessage(message.getMessage + LINE_DELIMITER + message.getData.get("errmsg").toString)
    }
    message
  }
}