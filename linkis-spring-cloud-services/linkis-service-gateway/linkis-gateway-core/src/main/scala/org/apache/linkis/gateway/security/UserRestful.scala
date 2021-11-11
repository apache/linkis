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

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util
import java.util.concurrent.TimeUnit
import java.util.{List, Random}

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import com.google.gson.Gson
import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.RSAUtils.keyPair
import org.apache.linkis.common.utils.{Logging, RSAUtils, Utils}
import org.apache.linkis.gateway.config.GatewayConfiguration
import org.apache.linkis.gateway.exception.GatewayErrorException
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.security.sso.SSOInterceptor
import org.apache.linkis.protocol.usercontrol.{RequestLogin, RequestRegister, RequestUserListFromWorkspace, RequestUserWorkspace, ResponseLogin, ResponseRegister, ResponseUserWorkspace, ResponseWorkspaceUserList}
import org.apache.linkis.rpc.Sender
import org.apache.linkis.server.conf.ServerConfiguration
import org.apache.linkis.server.security.SSOUtils
import org.apache.linkis.server.{Message, _}
import org.apache.commons.lang.StringUtils
import org.apache.commons.net.util.Base64
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods.parse
import scala.collection.JavaConversions._


trait UserRestful {

  def doUserRequest(gatewayContext: GatewayContext): Unit

}

abstract class AbstractUserRestful extends UserRestful with Logging {

  private var securityHooks: Array[SecurityHook] = Array.empty

  val dssProjectSender: Sender = Sender.getSender(GatewayConfiguration.DSS_QUERY_WORKSPACE_SERVICE_NAME.getValue)

  val configCache: LoadingCache[String, util.List[String]] = CacheBuilder.newBuilder().maximumSize(1000)
    .expireAfterAccess(1, TimeUnit.HOURS)
    .refreshAfterWrite(GatewayConfiguration.USER_WORKSPACE_REFLESH_TIME.getValue, TimeUnit.MINUTES)
    .build(new CacheLoader[String, util.List[String]]() {
      override def load(key: String): util.List[String] = {
        var userList: util.List[String] = new util.ArrayList[String]()
        if (GatewayConfiguration.REDIRECT_SWITCH_ON.getValue) {
          Utils.tryCatch {
            val controlIdStr = GatewayConfiguration.CONTROL_WORKSPACE_ID_LIST.getValue
            val controlIds = controlIdStr.split(",").toList.map(x => Integer.valueOf(x))

            userList = dssProjectSender.ask(new RequestUserListFromWorkspace(controlIds)).asInstanceOf[ResponseWorkspaceUserList].getUserNames
            info("Get user list from dss: "+ userList.toString)
          } {
            case e: Exception =>
              error(s"Call dss workspace rpc failed, ${e.getMessage}", e)
              throw new GatewayErrorException(40010, s"向DSS工程服务请求工作空间ID失败, ${e.getMessage}")
          }

        }
        userList
      }
    })


  def setSecurityHooks(securityHooks: Array[SecurityHook]): Unit = this.securityHooks = securityHooks

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
          Message.ok(loginUser + "Already logged in, please log out before signing in(已经登录，请先退出再进行登录)！").data("userName", loginUser)
        }(_ => login(gatewayContext))
      case "logout" => logout(gatewayContext)
      case "userInfo" => userInfo(gatewayContext)
      case "publicKey" => publicKey(gatewayContext)
      case "heartbeat" => heartbeat(gatewayContext)
      case "proxy" => proxy(gatewayContext)
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
      val lowerCaseUserName = proxyUser.toString.toLowerCase
      GatewaySSOUtils.setLoginUser(gatewayContext, lowerCaseUserName)
      "代理成功".data("proxyUser", proxyUser)
    } else {
      Message.error("Validation failed")
    }
  }

  def login(gatewayContext: GatewayContext): Message = {
    val message = tryLogin(gatewayContext)
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
    if (GatewayConfiguration.ENABLE_SSO_LOGIN.getValue) SSOInterceptor.getSSOInterceptor.logout(gatewayContext)
    if (securityHooks != null) securityHooks.foreach(_.preLogout(gatewayContext))
    "Logout successful(退出登录成功)！"
  }

  def userInfo(gatewayContext: GatewayContext): Message = {
    "get user information succeed!".data("userName", GatewaySSOUtils.getLoginUsername(gatewayContext))
  }

  def publicKey(gatewayContext: GatewayContext): Message = {
    val message = Message.ok("Gain success(获取成功)！").data("enableSSL", SSOUtils.sslEnable)
    if (GatewayConfiguration.LOGIN_ENCRYPT_ENABLE.getValue) {
      info(s"DEBUG: privateKey : " + RSAUtils.getDefaultPrivateKey())
      //      info(s"DEBUG: publicKey: " + RSAUtils.getDefaultPublicKey())
      val timeStamp = System.currentTimeMillis()
      info(s"DEBUG: time " + timeStamp)
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

  private val sender: Sender = Sender.getSender(GatewayConfiguration.USERCONTROL_SPRING_APPLICATION_NAME.getValue)
  private val LINE_DELIMITER = "</br>"
  private val USERNAME_STR = "userName"
  private val PASSWD_STR = "password"
  private val PASSWD_ENCRYPT_STR = "passwdEncrypt"
  private val httpClient = HttpClients.createDefault()

  override protected def tryLogin(gatewayContext: GatewayContext): Message = {
    val userNameArray = gatewayContext.getRequest.getQueryParams.get(USERNAME_STR)
    var passwordArray = gatewayContext.getRequest.getQueryParams.get(PASSWD_STR)
    val passwordArrayEncrypt = gatewayContext.getRequest.getQueryParams.get(PASSWD_ENCRYPT_STR)
    if (null == passwordArray || passwordArray.isEmpty || StringUtils.isBlank(passwordArray.head)) {
      passwordArray = passwordArrayEncrypt
    }
    val (userName, passwordEncrypt) = if (userNameArray != null && userNameArray.nonEmpty &&
      passwordArray != null && passwordArray.nonEmpty)
      (userNameArray.head, passwordArray.head)
    else if (StringUtils.isNotBlank(gatewayContext.getRequest.getRequestBody)) {
      val json = BDPJettyServerHelper.gson.fromJson(gatewayContext.getRequest.getRequestBody, classOf[java.util.Map[String, Object]])
      val tmpUsername = json.getOrDefault(USERNAME_STR, null)
      var tmpPasswd = json.getOrDefault(PASSWD_STR, null)
      if (null == tmpPasswd) {
        tmpPasswd = json.getOrDefault(PASSWD_ENCRYPT_STR, null)
      }
      (tmpUsername, tmpPasswd)
    } else (null, null)
    if (userName == null || StringUtils.isBlank(userName.toString)) {
      Message.error("Username can not be empty(用户名不能为空)！")
    } else if (passwordEncrypt == null || StringUtils.isBlank(passwordEncrypt.toString)) {
      Message.error("Password can not be blank(密码不能为空)！")
    } else {
      //warn: For easy to useing linkis,Admin skip login

      var password: String = null
      password = passwordEncrypt.asInstanceOf[String]

      if (GatewayConfiguration.LOGIN_ENCRYPT_ENABLE.getValue) {
        info(s"passwordEncrypt or : " + passwordEncrypt + ", username : " + userName)
        if (null != passwordEncrypt) {
          Utils.tryAndError({
            info("\npasswdEncrypt : " + passwordEncrypt + "\npublicKeyStr : " + RSAUtils.getDefaultPublicKey()
              + "\nprivateKeyStr : " + RSAUtils.getDefaultPrivateKey())
            val passwdOriObj = RSAUtils.decrypt(Base64.decodeBase64(passwordEncrypt.asInstanceOf[String].getBytes(StandardCharsets.UTF_8)))
            password = new String(passwdOriObj, StandardCharsets.UTF_8)
          })
        }
      }
      //      info("\npasswdOri :" + password)

      if (GatewayConfiguration.ADMIN_USER.getValue.equals(userName.toString) && GatewayConfiguration.ADMIN_PASSWORD.getValue.equals(password)) {
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
          if (GatewayConfiguration.REDIRECT_SWITCH_ON.getValue) {
            if (belongToOldUserFromDSS(userName.toString)) {
              val dataBytes: Array[Byte] = password.toString.getBytes(StandardCharsets.UTF_8)

              val decoded = Base64.decodeBase64(getPublicKeyFromOtherLinkis())
              val pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded))

              var passwdEncrypt = java.util.Base64.getEncoder().encodeToString(RSAUtils.encrypt(dataBytes, pubKey))
              logger.info("source passwdEncrypt: " + passwdEncrypt)
              passwdEncrypt = URLEncoder.encode(passwdEncrypt, Configuration.BDP_ENCODING.getValue)
              logger.info("Url encode passwdEncrypt: " + passwdEncrypt)
              val redirctUrl = GatewayConfiguration.REDIRECT_GATEWAY_URL.getValue + "api/rest_j/v1/user/relogin?userName=" + lowerCaseUserName + "&passwdEncrypt=" + passwdEncrypt
              message.data("redirectLinkisUrl", redirctUrl)
            } else {
              message = login(lowerCaseUserName, password.toString)
              //fakeLogin(lowerCaseUserName, password.toString)
              if (message.getStatus == 0) {
                GatewaySSOUtils.setLoginUser(gatewayContext, lowerCaseUserName)
              }
            }
          } else {
            message = login(lowerCaseUserName, password.toString)
            //fakeLogin(lowerCaseUserName, password.toString)
            if (message.getStatus == 0) {
              GatewaySSOUtils.setLoginUser(gatewayContext, lowerCaseUserName)
            }
          }
        }
        if (message.getData.containsKey("errmsg")) {
          message.setMessage(message.getMessage + LINE_DELIMITER + message.getData.get("errmsg").toString)
        }
        message
      }
    }
  }

  private def belongToOldUserFromDSS(userName: String): Boolean = {
    if (configCache.get("userList").contains(userName)) {
        logger.info("Belong to new dss user:" + userName)
        false
      } else {
        logger.info("Belong to old dss user:" + userName)
        true
      }

  }


  private def getPublicKeyFromOtherLinkis(): String = {
    val url = GatewayConfiguration.REDIRECT_GATEWAY_URL.getValue + "/api/rest_j/v1/user/publicKey";
    val httpGet = new HttpGet(url)
    httpGet.addHeader("Accept", "application/json")

    val response = httpClient.execute(httpGet)
    val resp = parse(EntityUtils.toString(response.getEntity()))
    logger.info("Get publickey resp is " + resp + ";url is " + url)

    val publicKey = (resp \ "data" \ "publicKey").asInstanceOf[JString].values

    logger.info("Get publickey  is " + publicKey)
    publicKey
  }

//  private def getWorkspaceIdFromDSS(userName: String): util.List[Integer] = {
//    val sender: Sender = Sender.getSender(GatewayConfiguration.DSS_QUERY_WORKSPACE_SERVICE_NAME.getValue)
//    val requestUserWorkspace: RequestUserWorkspace = new RequestUserWorkspace(userName)
//    var resp: Any = null
//    var workspaceId: util.List[Integer] = null
//    Utils.tryCatch {
//      resp = sender.ask(requestUserWorkspace)
//    } {
//      case e: Exception =>
//        error(s"Call dss workspace rpc failed, ${e.getMessage}", e)
//        throw new GatewayErrorException(40010, s"向DSS工程服务请求工作空间ID失败, ${e.getMessage}")
//    }
//    resp match {
//      case s: ResponseUserWorkspace => workspaceId = s.getUserWorkspaceIds
//      case _ =>
//        throw new GatewayErrorException(40012, s"向DSS工程服务请求工作空间ID返回值失败,")
//    }
//    logger.info("Get userWorkspaceIds  is " + workspaceId + ",and user is " + userName)
//    workspaceId
//  }

  protected def login(userName: String, password: String): Message

  private def getRandomProxyUser(): String = {
    var name = null.asInstanceOf[String]
    val userList = GatewayConfiguration.PROXY_USER_LIST
    val size = userList.size
    if (size <= 0) {
      warn(s"Invalid Gateway proxy user list")
    } else {
      val rand = new Random()
      name = userList(rand.nextInt(size))
    }
    name
  }

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
          message.setStatus(0)
          message.setMessage("Login successful(登录成功)")
          val proxyUser = getRandomProxyUser()
          if (StringUtils.isNotBlank(proxyUser)) {
            GatewaySSOUtils.setLoginUser(gatewayContext, proxyUser)
            message.setMessage("Login successful(登录成功)")
              .data("userName", proxyUser)
              .data("isAdmin", false)
          } else {
            message = Message.error("Invalid proxy user, please contact with administrator(代理用户无效，请联系管理员)")
          }

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
