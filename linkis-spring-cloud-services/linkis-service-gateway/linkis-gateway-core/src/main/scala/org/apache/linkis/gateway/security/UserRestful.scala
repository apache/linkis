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

import org.apache.linkis.common.utils.{Logging, RSAUtils, Utils}
import org.apache.linkis.gateway.config.GatewayConfiguration
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.security.sso.SSOInterceptor
import org.apache.linkis.gateway.security.token.TokenAuthentication
import org.apache.linkis.protocol.usercontrol.{
  RequestLogin,
  RequestRegister,
  ResponseLogin,
  ResponseRegister
}
import org.apache.linkis.rpc.Sender
import org.apache.linkis.server.{Message, _}
import org.apache.linkis.server.conf.ServerConfiguration
import org.apache.linkis.server.security.SSOUtils

import org.apache.commons.lang3.StringUtils
import org.apache.commons.net.util.Base64

import java.nio.charset.StandardCharsets
import java.util.{Locale, Random}

import scala.collection.JavaConverters._

import com.google.gson.Gson

trait UserRestful {

  def doUserRequest(gatewayContext: GatewayContext): Unit

}

abstract class AbstractUserRestful extends UserRestful with Logging {

  private var securityHooks: Array[SecurityHook] = Array.empty

  val dssProjectSender: Sender =
    Sender.getSender(GatewayConfiguration.DSS_QUERY_WORKSPACE_SERVICE_NAME.getValue)

  def setSecurityHooks(securityHooks: Array[SecurityHook]): Unit = this.securityHooks =
    securityHooks

  private val userRegex = {
    var userURI = ServerConfiguration.BDP_SERVER_USER_URI.getValue
    if (!userURI.endsWith("/")) userURI += "/"
    userURI
  }

  override def doUserRequest(gatewayContext: GatewayContext): Unit = {
    val path = gatewayContext.getRequest.getRequestURI.replace(userRegex, "")
    val message = path match {
      case "register" => register(gatewayContext)
      case "login" =>
        Utils.tryCatch {
          val loginUser = GatewaySSOUtils.getLoginUsername(gatewayContext)
          Message
            .ok(loginUser + "Already logged in, please log out before signing in(已经登录，请先退出再进行登录)！")
            .data("userName", loginUser)
        }(_ => login(gatewayContext))
      case "token-login" =>
        if (!"POST".equalsIgnoreCase(gatewayContext.getRequest.getMethod)) {
          Message.error("Only support method POST")
        } else {
          TokenAuthentication.tokenAuth(gatewayContext, true)
          return
        }
      case "logout" => logout(gatewayContext)
      case "userInfo" => userInfo(gatewayContext)
      case "publicKey" => publicKey(gatewayContext)
      case "heartbeat" => heartbeat(gatewayContext)
      case "proxy" => proxy(gatewayContext)
      case "baseinfo" => baseinfo(gatewayContext)
      case _ =>
        Message.error("unknown request URI " + path)
    }
    gatewayContext.getResponse.write(message)
    gatewayContext.getResponse.setStatus(Message.messageToHttpStatus(message))
    gatewayContext.getResponse.sendResponse()
  }

  def proxy(gatewayContext: GatewayContext): Message = {
    val proxyUser = gatewayContext.getRequest.getQueryParams.get("proxyUser")(0)
    val validationCode = gatewayContext.getRequest.getQueryParams.get("validationCode")(0)
    // validate
    if (ProxyUserUtils.validate(proxyUser, validationCode)) {
      val lowerCaseUserName = proxyUser.toString.toLowerCase(Locale.getDefault)
      GatewaySSOUtils.setLoginUser(gatewayContext, lowerCaseUserName)
      "代理成功".data("proxyUser", proxyUser)
    } else {
      Message.error("Validation failed")
    }
  }

  def login(gatewayContext: GatewayContext): Message = {
    val message = tryLogin(gatewayContext)
    message
      .data("sessionTimeOut", SSOUtils.getSessionTimeOut())
      .data("enableWatermark", GatewayConfiguration.ENABLE_WATER_MARK.getValue)
    if (securityHooks != null) securityHooks.foreach(_.postLogin(gatewayContext))
    message
  }

  def register(gatewayContext: GatewayContext): Message = {
    val message = tryRegister(gatewayContext)
    message
  }

  protected def tryLogin(context: GatewayContext): Message

  def logout(gatewayContext: GatewayContext): Message = {
    GatewaySSOUtils.removeLoginUser(gatewayContext)
    if (GatewayConfiguration.ENABLE_SSO_LOGIN.getValue) {
      SSOInterceptor.getSSOInterceptor.logout(gatewayContext)
    }
    if (securityHooks != null) securityHooks.foreach(_.preLogout(gatewayContext))
    "Logout successful(退出登录成功)！"
  }

  def userInfo(gatewayContext: GatewayContext): Message = {
    "get user information succeed!".data(
      "userName",
      GatewaySSOUtils.getLoginUsername(gatewayContext)
    )
  }

  def baseinfo(gatewayContext: GatewayContext): Message = {
    Message
      .ok("get baseinfo success(获取成功)！")
      .data("resultSetExportEnable", GatewayConfiguration.IS_DOWNLOAD.getValue)
  }

  def publicKey(gatewayContext: GatewayContext): Message = {
    val message = Message.ok("Gain success(获取成功)！").data("enableSSL", SSOUtils.sslEnable)
    if (GatewayConfiguration.LOGIN_ENCRYPT_ENABLE.getValue) {
      logger.info(s"DEBUG: privateKey : " + RSAUtils.getDefaultPrivateKey())
      //      info(s"DEBUG: publicKey: " + RSAUtils.getDefaultPublicKey())
      val timeStamp = System.currentTimeMillis()
      logger.info(s"DEBUG: time " + timeStamp)
      message.data("debugTime", timeStamp)
      message.data("publicKey", RSAUtils.getDefaultPublicKey())
    }
    message.data("enableLoginEncrypt", GatewayConfiguration.LOGIN_ENCRYPT_ENABLE.getValue)
    message
  }

  def heartbeat(gatewayContext: GatewayContext): Message = Utils.tryCatch {
    GatewaySSOUtils.getLoginUsername(gatewayContext)
    val retMessage = Message.ok("Maintain heartbeat success(维系心跳成功)")
    retMessage.setStatus(0)
    retMessage
  }(t => Message.noLogin(t.getMessage))

  protected def tryRegister(context: GatewayContext): Message
}

abstract class UserPwdAbstractUserRestful extends AbstractUserRestful with Logging {

  private val sender: Sender =
    Sender.getSender(GatewayConfiguration.USERCONTROL_SPRING_APPLICATION_NAME.getValue)

  private val LINE_DELIMITER = "</br>"
  private val USERNAME_STR = "userName"
  private val PASSWD_STR = "password"
  private val PASSWD_ENCRYPT_STR = "passwdEncrypt"

  private def getUserNameAndPWD(gatewayContext: GatewayContext): (String, String) = {
    val userNameArray = gatewayContext.getRequest.getQueryParams.get(USERNAME_STR)
    var passwordArray = gatewayContext.getRequest.getQueryParams.get(PASSWD_STR)
    val passwordArrayEncrypt = gatewayContext.getRequest.getQueryParams.get(PASSWD_ENCRYPT_STR)
    if (null == passwordArray || passwordArray.isEmpty || StringUtils.isBlank(passwordArray.head)) {
      passwordArray = passwordArrayEncrypt
    }
    val (userName, passwordEncrypt) =
      if (
          userNameArray != null && userNameArray.nonEmpty &&
          passwordArray != null && passwordArray.nonEmpty
      ) {
        (userNameArray.head, passwordArray.head)
      } else if (StringUtils.isNotBlank(gatewayContext.getRequest.getRequestBody)) {
        val json = BDPJettyServerHelper.gson.fromJson(
          gatewayContext.getRequest.getRequestBody,
          classOf[java.util.Map[String, Object]]
        )
        val tmpUsername = json.getOrDefault(USERNAME_STR, null)
        var tmpPasswd = json.getOrDefault(PASSWD_STR, null)
        if (null == tmpPasswd) {
          tmpPasswd = json.getOrDefault(PASSWD_ENCRYPT_STR, null)
        }
        (tmpUsername, tmpPasswd)
      } else (null, null)

    if (null == userName || StringUtils.isBlank(userName.toString)) {
      return (null, null)
    }
    if (null == passwordEncrypt || StringUtils.isBlank(passwordEncrypt.toString)) {
      return (userName.toString, null)
    }

    val password: String = if (GatewayConfiguration.LOGIN_ENCRYPT_ENABLE.getValue) {
      logger.info(s"passwordEncrypt or : $passwordEncrypt username $userName")
      Utils.tryAndError({
        logger.info(
          "\npasswdEncrypt : " + passwordEncrypt + "\npublicKeyStr : " + RSAUtils
            .getDefaultPublicKey()
            + "\nprivateKeyStr : " + RSAUtils.getDefaultPrivateKey()
        )
        val passwdOriObj = RSAUtils.decrypt(
          Base64
            .decodeBase64(passwordEncrypt.asInstanceOf[String].getBytes(StandardCharsets.UTF_8))
        )
        new String(passwdOriObj, StandardCharsets.UTF_8)
      })
    } else {
      passwordEncrypt.asInstanceOf[String]
    }
    (userName.toString, password)
  }

  def clearExpireCookie(gatewayContext: GatewayContext): Unit = {
    val cookies =
      gatewayContext.getRequest.getCookies.values().asScala.flatMap(cookie => cookie).toArray
    val expireCookies = cookies.filter { cookie =>
      cookie.getName.equals(
        ServerConfiguration.LINKIS_SERVER_SESSION_TICKETID_KEY.getValue
      ) || cookie.getName.equals(
        ServerConfiguration.LINKIS_SERVER_SESSION_PROXY_TICKETID_KEY.getValue
      )
    }
    val host = gatewayContext.getRequest.getHeaders.get("Host")
    if (host != null && host.nonEmpty) {
      val maxDomainLevel = host.head.split("\\.").length
      for (level <- 1 to maxDomainLevel) {
        expireCookies
          .clone()
          .foreach(cookie => {
            cookie.setValue(null)
            cookie.setPath("/")
            cookie.setMaxAge(0)
            val domain = GatewaySSOUtils.getCookieDomain(host.head, level)
            cookie.setDomain(domain)
            gatewayContext.getResponse.addCookie(cookie)
            logger.info(
              s"success clear user cookie: ${getUserNameAndPWD(gatewayContext)._1}" +
                s"--${ServerConfiguration.LINKIS_SERVER_SESSION_TICKETID_KEY.getValue}" +
                s"--${domain}"
            )
          })
      }
    }
  }

  override protected def tryLogin(gatewayContext: GatewayContext): Message = {
    val (userName, password) = getUserNameAndPWD(gatewayContext)
    if (StringUtils.isBlank(userName)) {
      return Message.error("Username can not be empty(用户名不能为空)！")
    } else if (StringUtils.isBlank(password)) {
      return Message.error("Password can not be blank(密码不能为空)！")
    }
    if (
        GatewayConfiguration.ADMIN_USER.getValue.equals(
          userName
        ) && GatewayConfiguration.ADMIN_PASSWORD.getValue.equals(password)
    ) {
      GatewaySSOUtils.setLoginUser(gatewayContext, userName)
      "login successful(登录成功)！".data("userName", userName).data("isAdmin", true)
    } else {
      // firstly for test user
      var message = Message.ok()
      if (GatewayConfiguration.USERCONTROL_SWITCH_ON.getValue) {
        message = userControlLogin(userName, password, gatewayContext)
      } else {
        // standard login
        val lowerCaseUserName = userName.toLowerCase(Locale.getDefault())
        message = login(lowerCaseUserName, password)
        clearExpireCookie(gatewayContext)
        if (message.getStatus == 0) {
          GatewaySSOUtils.setLoginUser(gatewayContext, lowerCaseUserName)
        }
      }
      if (message.getData.containsKey("errmsg")) {
        message.setMessage(
          message.getMessage + LINE_DELIMITER + message.getData.get("errmsg").toString
        )
      }
      message
    }
  }

  protected def login(userName: String, password: String): Message

  private def getRandomProxyUser(): String = {
    var name = null.asInstanceOf[String]
    val userList = GatewayConfiguration.PROXY_USER_LIST
    val size = userList.size
    if (size <= 0) {
      logger.warn(s"Invalid Gateway proxy user list")
    } else {
      val rand = new Random()
      name = userList(rand.nextInt(size))
    }
    name
  }

  def userControlLogin(
      userName: String,
      password: String,
      gatewayContext: GatewayContext
  ): Message = {
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
          message.setStatus(0)
          message.setMessage("Login successful(登录成功)")
          val proxyUser = getRandomProxyUser()
          if (StringUtils.isNotBlank(proxyUser)) {
            GatewaySSOUtils.setLoginUser(gatewayContext, proxyUser)
            message
              .setMessage("Login successful(登录成功)")
              .data("userName", proxyUser)
              .data("isAdmin", false)
          } else {
            message =
              Message.error("Invalid proxy user, please contact with administrator(代理用户无效，请联系管理员)")
          }

        } else {
          message = Message.error(
            "Invalid username or password, please check and try again later(用户名或密码无效，请稍后再试)"
          )
        }
    }) { t =>
      {
        logger.warn(s"Login rpc request error, err message ", t)
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
      message = Message.error("请自行实现注册方法！")
    }
    message
  }

  /**
   * userControl register(用户控制模块登录)
   *
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
          logger.info(
            s"Register rpc success. requestRegister=" + gson
              .toJson(requestRegister) + ", response=" + gson.toJson(r)
          )
      }
    }) { e =>
      logger.warn(s"Register rpc request error. err message ", e)
      message.setStatus(1)
      message.setMessage("System, please try again later(系统异常，请稍后再试)")
    }
    if (message.getData.containsKey("errmsg")) {
      // for frontend display
      message.setMessage(
        message.getMessage + LINE_DELIMITER + message.getData.get("errmsg").toString
      )
    }
    message
  }

}
