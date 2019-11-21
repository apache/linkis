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
package com.webank.wedatasphere.linkis.filesystem

import com.google.gson.Gson
import com.webank.wedatasphere.linkis.filesystem.action.OpenScriptFromBMLAction
import com.webank.wedatasphere.linkis.filesystem.conf.WorkspaceClientConf
import com.webank.wedatasphere.linkis.filesystem.request.{WorkspaceClient, WorkspaceHttpConf}
import com.webank.wedatasphere.linkis.filesystem.response.ScriptFromBMLResponse
import com.webank.wedatasphere.linkis.filesystem.result.ScriptFromBMLResult
import com.webank.wedatasphere.linkis.httpclient.authentication.AuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.config.{ClientConfig, ClientConfigBuilder}
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfig
import com.webank.wedatasphere.linkis.httpclient.response.Result


/**
  * Created by patinousward
  */
class WorkspaceClientImpl extends WorkspaceClient with WorkspaceHttpConf {

  def this(user: String, token: String,gatewayAddress:String) {
    this
    this.user = user
    this.token = token
    val maxConnection: Int = 100
    val readTimeout: Int = 10000
    val connectiontimeout = 30000
    authenticationStrategy = new TokenAuthenticationStrategy()
    clientConfig = ClientConfigBuilder.newBuilder().addUJESServerUrl( "http://" + gatewayAddress)
      .connectionTimeout(connectiontimeout).discoveryEnabled(false)
      .loadbalancerEnabled(false).maxConnectionSize(maxConnection)
      .retryEnabled(false).readTimeout(readTimeout)
      .setAuthenticationStrategy(authenticationStrategy).
      setAuthTokenKey(WorkspaceClientConf.tokenKey).setAuthTokenValue(this.token).build()
    dwsClientConfig = new DWSClientConfig(clientConfig)
    dwsClientConfig.setDWSVersion(WorkspaceClientConf.dwsVersion)
    dwsClientName = "Workspace-Client"
    dwsClient = new DWSHttpClient(dwsClientConfig, dwsClientName)
  }
  @throws[IllegalArgumentException]
  override def requestOpenScriptFromBML(resourceId: String, version: String, fileName: String): ScriptFromBMLResponse = {
    val action: OpenScriptFromBMLAction = new OpenScriptFromBMLAction()
    action.setUser(user)
    action.setParameter("resourceId", resourceId)
    action.setParameter("version", version)
    action.setParameter("fileName", fileName)
    val result: Result = dwsClient.execute(action)
    result match {
      case r: ScriptFromBMLResult => {
        if(r.getStatus!= 0) throw new IllegalArgumentException(r.getMessage)
        ScriptFromBMLResponse(r.scriptContent, r.metadata)
      }
      case _ => null
    }
  }

  override protected var user: String = _
  override protected var token: String = _
  override protected var authenticationStrategy: AuthenticationStrategy = _
  override protected var clientConfig: ClientConfig = _
  override protected var dwsClientConfig: DWSClientConfig = _
  override protected var dwsClientName: String = _
  override protected var dwsClient: DWSHttpClient = _
}
