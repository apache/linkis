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

package org.apache.linkis.filesystem

import org.apache.linkis.filesystem.action.OpenScriptFromBMLAction
import org.apache.linkis.filesystem.conf.WorkspaceClientConf
import org.apache.linkis.filesystem.request.{WorkspaceClient, WorkspaceHttpConf}
import org.apache.linkis.filesystem.response.ScriptFromBMLResponse
import org.apache.linkis.filesystem.result.ScriptFromBMLResult
import org.apache.linkis.httpclient.authentication.AuthenticationStrategy
import org.apache.linkis.httpclient.config.{ClientConfig, ClientConfigBuilder}
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy
import org.apache.linkis.httpclient.dws.config.DWSClientConfig
import org.apache.linkis.httpclient.response.Result

class WorkspaceClientImpl extends WorkspaceClient with WorkspaceHttpConf {

  def this(user: String, token: String, gatewayAddress: String) {
    this
    this.user = user
    this.token = token
    val maxConnection: Int = 100
    val readTimeout: Int = 10 * 60 * 1000
    val connectiontimeout = 5 * 60 * 1000
    authenticationStrategy = new TokenAuthenticationStrategy()
    clientConfig = ClientConfigBuilder
      .newBuilder()
      .addServerUrl("http://" + gatewayAddress)
      .connectionTimeout(connectiontimeout)
      .discoveryEnabled(false)
      .loadbalancerEnabled(false)
      .maxConnectionSize(maxConnection)
      .retryEnabled(false)
      .readTimeout(readTimeout)
      .setAuthenticationStrategy(authenticationStrategy)
      .setAuthTokenKey(WorkspaceClientConf.tokenKey)
      .setAuthTokenValue(this.token)
      .build()
    dwsClientConfig = new DWSClientConfig(clientConfig)
    dwsClientConfig.setDWSVersion(WorkspaceClientConf.dwsVersion)
    dwsClientName = "Workspace-Client"
    dwsClient = new DWSHttpClient(dwsClientConfig, dwsClientName)
  }

  @throws[IllegalArgumentException]
  override def requestOpenScriptFromBML(
      resourceId: String,
      version: String,
      fileName: String
  ): ScriptFromBMLResponse = {
    val action: OpenScriptFromBMLAction = new OpenScriptFromBMLAction()
    action.setUser(user)
    action.setParameter("resourceId", resourceId)
    action.setParameter("version", version)
    action.setParameter("fileName", fileName)
    val result: Result = dwsClient.execute(action)
    result match {
      case r: ScriptFromBMLResult =>
        if (r.getStatus != 0) throw new IllegalArgumentException(r.getMessage)
        ScriptFromBMLResponse(r.scriptContent, r.metadata)
      case _ => null
    }
  }

  @throws[IllegalArgumentException]
  override def requestOpenScriptFromBML(
      resourceId: String,
      version: String,
      fileName: String,
      user: String
  ): ScriptFromBMLResponse = {
    val action: OpenScriptFromBMLAction = new OpenScriptFromBMLAction()
    action.setUser(user)
    action.setParameter("resourceId", resourceId)
    action.setParameter("version", version)
    action.setParameter("fileName", fileName)
    val result: Result = dwsClient.execute(action)
    result match {
      case r: ScriptFromBMLResult =>
        if (r.getStatus != 0) throw new IllegalArgumentException(r.getMessage)
        ScriptFromBMLResponse(r.scriptContent, r.metadata)
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
