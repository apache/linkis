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

import org.apache.linkis.common.utils.{LDAPUtils, Logging, Utils}
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.server._

class LDAPUserRestful extends UserPwdAbstractUserRestful with Logging {

  override def login(userName: String, password: String): Message = Utils.tryCatch {
    LDAPUtils.login(userName.toString, password.toString)
    "login successful(登录成功)！".data("userName", userName).data("isAdmin", false)
  } { t =>
    logger.warn("wrong user name or password(用户名或密码错误)！", t)
    Message.error("wrong user name or password(用户名或密码错误)！")
  }

  override def register(gatewayContext: GatewayContext): Message = {
    super.register(gatewayContext)
  }

}
