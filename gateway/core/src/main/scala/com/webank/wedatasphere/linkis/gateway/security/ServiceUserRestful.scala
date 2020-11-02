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

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.gateway.http.GatewayContext
import com.webank.wedatasphere.linkis.server._
import scalaj.http._
import org.json4s.JsonAST.JBool
import com.google.gson.GsonBuilder
import com.webank.wedatasphere.linkis.common.conf.CommonVars
import org.apache.commons.lang.StringUtils

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

/**
  * created by chongchuanbing on 2020/6/30.
  */
class ServiceUserRestful extends UserPwdAbstractUserRestful with Logging {

  val loginAuthVerifyUrl = CommonVars("login.auth.verify.url", "").getValue
  
  private implicit val executor = ExecutionContext.global
  private val gson = new GsonBuilder().create

  override def login(userName: String, password: String): Message = Utils.tryCatch{
    authenticate(userName.toString, password.toString)
    "login successful(登录成功)！".data("userName", userName).data("isAdmin", false)
  }{ t =>
    warn("wrong user name or password(用户名或密码错误)！", t)
    Message.error("wrong user name or password(用户名或密码错误)！")
  }

  override def register(gatewayContext: GatewayContext): Message = {
    Message.error("please implements the register method(请自行实现注册方法)")
  }

  case class UserInfo(account: String, password: String)

  case class NcRespose(code: Int, success: JBool)
  
  def authenticate(userName: String, password: String): Unit = {
    if (StringUtils.isEmpty(loginAuthVerifyUrl)) {
      error(s"authenticate url not config.")
      throw new Exception("authenticate failed")
    }
    val jsonStr = Http(loginAuthVerifyUrl)
      .postData(gson.toJson(UserInfo(userName, password)))
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .asString
      .body

    val respose = gson.fromJson(jsonStr, classOf[NcRespose])    

    if (!respose.success.value) {
      throw new Exception("authenticate failed")
    }
  }

}