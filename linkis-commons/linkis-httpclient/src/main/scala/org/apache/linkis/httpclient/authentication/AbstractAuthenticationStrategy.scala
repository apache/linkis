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

package org.apache.linkis.httpclient.authentication

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.httpclient.Client
import org.apache.linkis.httpclient.config.ClientConfig
import org.apache.linkis.httpclient.request.{Action, UserAction}

import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpResponse

import java.util.concurrent.ConcurrentHashMap

abstract class AbstractAuthenticationStrategy extends AuthenticationStrategy with Logging {
  private var client: Client = _
  private val userNameToAuthentications = new ConcurrentHashMap[String, Authentication]()
  private var clientConfig: ClientConfig = _
  protected val sessionMaxAliveTime: Long

  def setClient(client: Client): Unit = this.client = client

  def getClient: Client = client

  protected def getKeyByUserAndURL(user: String, serverUrl: String): String =
    user + "@" + serverUrl

  protected def getUser(requestAction: Action): String = requestAction match {
    case _: AuthenticationAction => null
    case authAction: UserAction => authAction.getUser
    case _ =>
      if (StringUtils.isNotBlank(clientConfig.getAuthTokenKey)) clientConfig.getAuthTokenKey
      else null
  }

  protected def getKey(requestAction: Action, serverUrl: String): String = {
    val user = getUser(requestAction)
    if (user == null) return null
    getKeyByUserAndURL(user, serverUrl)
  }

  protected def getAuthenticationActionByKey(key: String): Authentication =
    userNameToAuthentications.get(key)

  def setClientConfig(clientConfig: ClientConfig): Unit = this.clientConfig = clientConfig

  def getClientConfig: ClientConfig = clientConfig

  def login(requestAction: Action, serverUrl: String): Authentication = {
    val key = getKey(requestAction, serverUrl)
    if (key == null) return null
    val oldAuth = getAuthenticationActionByKey(key)
    if (null != oldAuth && !isTimeout(oldAuth)) {
      val authenticationAction = oldAuth
      authenticationAction.updateLastAccessTime()
      authenticationAction
    } else {
      key.intern() synchronized {
        var authentication = getAuthenticationActionByKey(key)
        if (authentication == null || isTimeout(authentication)) {
          authentication = tryLogin(requestAction, serverUrl)
          putSession(key, authentication)
          logger.info(s"$key try reLogin")
        }
        authentication
      }
    }
  }

  def tryLogin(requestAction: Action, serverUrl: String): Authentication = {
    val action = getAuthenticationAction(requestAction, serverUrl)
    client.execute(action, 5000) match {
      case r: AuthenticationResult => r.getAuthentication
    }
  }

  /**
   * The static account password method, when the return code is determined to be 401, a mandatory
   * login is triggered
   * @param requestAction
   * @param serverUrl
   * @return
   */
  def enforceLogin(requestAction: Action, serverUrl: String): Authentication = {
    login(requestAction, serverUrl)
  }

  protected def getAuthenticationAction(
      requestAction: Action,
      serverUrl: String
  ): AuthenticationAction

  def getAuthenticationResult(
      response: HttpResponse,
      requestAction: AuthenticationAction
  ): AuthenticationResult

  def isTimeout(authentication: Authentication): Boolean =
    System.currentTimeMillis() - authentication.getLastAccessTime >= sessionMaxAliveTime

  def putSession(key: String, authentication: Authentication): Unit = {
    this.userNameToAuthentications.put(key, authentication)
  }

}
