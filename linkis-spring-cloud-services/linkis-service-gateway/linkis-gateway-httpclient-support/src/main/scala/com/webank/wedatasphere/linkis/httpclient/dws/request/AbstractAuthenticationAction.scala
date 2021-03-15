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

package com.webank.wedatasphere.linkis.httpclient.dws.request

import com.webank.wedatasphere.linkis.httpclient.authentication.AuthenticationAction
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.request.POSTAction

abstract class AbstractAuthenticationAction(override val serverUrl: String) extends POSTAction with AuthenticationAction {
  private var user: String = _
  private var password: String = _

  override def setPassword(password: String): Unit = this.password = password

  override def getPassword: Option[String] = Option(password)

  override def getRequestBody: String = ""

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = user

  override def getRequestPayload: String = DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)
}

class DWSAuthenticationAction(override val serverUrl: String) extends AbstractAuthenticationAction(serverUrl) with DWSHttpAction {
  override def suffixURLs: Array[String] = Array("user", "login")
}