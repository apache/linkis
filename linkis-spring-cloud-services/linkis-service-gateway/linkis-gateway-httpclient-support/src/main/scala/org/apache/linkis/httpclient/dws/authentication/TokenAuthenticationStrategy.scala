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
 
package org.apache.linkis.httpclient.dws.authentication

import java.util

import org.apache.linkis.httpclient.authentication._
import org.apache.linkis.httpclient.dws.exception.AuthenticationFailedException
import org.apache.linkis.httpclient.request.{Action, UserAction}
import org.apache.http.HttpResponse
import org.apache.http.cookie.Cookie

class TokenAuthenticationStrategy(override protected val sessionMaxAliveTime: Long) extends AbstractAuthenticationStrategy {

  def this() = this(-1)

  override def login(requestAction: Action, serverUrl: String): Authentication = requestAction match {
    case _: AuthenticationAction => null
    case action: UserAction => new HttpAuthentication {
      import TokenAuthenticationStrategy._

      import scala.collection.JavaConversions._
      override def authToCookies: Array[Cookie] = Array.empty

      override def authToHeaders: util.Map[String, String] = Map(TOKEN_USER_KEY -> action.getUser, TOKEN_KEY -> getClientConfig.getAuthTokenValue)

      override def authToMap: util.Map[String, String] = new util.HashMap[String, String]()

      override def getLastAccessTime: Long = System.currentTimeMillis

      override def updateLastAccessTime(): Unit = {}
    }
    case _ => throw new AuthenticationFailedException("cannot use token authentication, since no user is found to proxy.")
  }

  override protected def getAuthenticationAction(requestAction: Action, serverUrl: String): AuthenticationAction = null

  override def getAuthenticationResult(response: HttpResponse, requestAction: AuthenticationAction): AuthenticationResult = null

}
object TokenAuthenticationStrategy {
  val TOKEN_USER_KEY = "Token-User"
  val TOKEN_KEY = "Token-Code"
}