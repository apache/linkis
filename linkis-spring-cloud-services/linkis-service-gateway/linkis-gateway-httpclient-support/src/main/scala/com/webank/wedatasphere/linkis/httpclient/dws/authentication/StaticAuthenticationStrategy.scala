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

package com.webank.wedatasphere.linkis.httpclient.dws.authentication

import com.webank.wedatasphere.linkis.common.utils.ByteTimeUtils
import com.webank.wedatasphere.linkis.httpclient.authentication.{AbstractAuthenticationStrategy, AuthenticationAction, AuthenticationResult}
import com.webank.wedatasphere.linkis.httpclient.dws.exception.AuthenticationFailedException
import com.webank.wedatasphere.linkis.httpclient.dws.request.DWSAuthenticationAction
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSAuthenticationResult
import com.webank.wedatasphere.linkis.httpclient.request.{Action, UserAction, UserPwdAction}
import org.apache.commons.lang.StringUtils
import org.apache.http.HttpResponse

/**
  * created by cooperyang on 2019/5/22.
  */
class StaticAuthenticationStrategy(override protected val sessionMaxAliveTime: Long) extends AbstractAuthenticationStrategy {
   def this() = this(ByteTimeUtils.timeStringAsMs("2h"))

  override protected def getAuthenticationAction(requestAction: Action, serverUrl: String): AuthenticationAction = {
    val action = new DWSAuthenticationAction(serverUrl)
    def pwd: String = if(StringUtils.isNotBlank(getClientConfig.getAuthTokenValue)) getClientConfig.getAuthTokenValue
      else throw new AuthenticationFailedException("the value of authTokenValue in ClientConfig must be exists, since no password is found to login.")
    requestAction match {
      case userPwd: UserPwdAction =>
        action.addRequestPayload("userName", userPwd.getUser)
        action.addRequestPayload("password", userPwd.getPassword.getOrElse(pwd))
      case userAction: UserAction =>
        action.addRequestPayload("userName", userAction.getUser)
        action.addRequestPayload("password", pwd)
      case _ =>
        if(StringUtils.isBlank(getClientConfig.getAuthTokenKey))
          throw new AuthenticationFailedException("the value of authTokenKey in ClientConfig must be exists, since no user is found to login.")
        action.addRequestPayload("userName", getClientConfig.getAuthTokenKey)
        action.addRequestPayload("password", pwd)
    }
    action
  }

  override def getAuthenticationResult(response: HttpResponse, requestAction: AuthenticationAction): AuthenticationResult = {
    new DWSAuthenticationResult(response, requestAction.serverUrl)
  }
}