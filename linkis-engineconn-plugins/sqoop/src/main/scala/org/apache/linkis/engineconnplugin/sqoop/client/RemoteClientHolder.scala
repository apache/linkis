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

package org.apache.linkis.engineconnplugin.sqoop.client

import org.apache.linkis.common.conf.{CommonVars, Configuration}
import org.apache.linkis.common.utils.{JsonUtils, Logging, Utils}
import org.apache.linkis.datasource.client.{AbstractRemoteClient, DataSourceRemoteClient}
import org.apache.linkis.datasource.client.impl.LinkisDataSourceRemoteClient
import org.apache.linkis.engineconnplugin.sqoop.exception.DataSourceRpcErrorException
import org.apache.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy
import org.apache.linkis.httpclient.dws.config.{DWSClientConfig, DWSClientConfigBuilder}
import org.apache.linkis.httpclient.request.{Action, UserAction}

import java.util.concurrent.TimeUnit

/**
 * To hold client for Linkis data source
 */
class RemoteClientHolder(user: String, system: String, clientConfig: DWSClientConfig)
    extends Logging {

  def this(user: String, system: String) = {
    this(user, system, RemoteClientHolder.CONFIG)
  }

  def getDataSourceClient: DataSourceRemoteClient = {
    Client.DATASOURCE
  }

  def executeDataSource[T](action: Action): T = {
    Utils.tryCatch {
      // Try to invoke "setSystem" method
      Utils.tryAndWarn {
        Option(action.getClass.getMethod("setSystem", classOf[String])).foreach(method => {
          method.setAccessible(true)
          method.invoke(action, system)
        })
      }
      action match {
        case action1: UserAction =>
          action1.setUser(user)
        case _ =>
      }
      getDataSourceClient.asInstanceOf[AbstractRemoteClient].execute(action).asInstanceOf[T]
    } { case e: Exception =>
      throw new DataSourceRpcErrorException(
        s"Fail to invoke action: " +
          s"${JsonUtils.jackson.writer().writeValueAsString(action)}",
        e
      )
    }
  }

  private object Client {
    lazy val DATASOURCE: DataSourceRemoteClient = new LinkisDataSourceRemoteClient(clientConfig)

  }

}

object RemoteClientHolder {

  /**
   * Default client configuration
   */
  val CONFIG: DWSClientConfig = DWSClientConfigBuilder
    .newBuilder()
    .addServerUrl(Configuration.getGateWayURL())
    .connectionTimeout(CommonVars[Long]("", 12).getValue)
    .discoveryEnabled(CommonVars[Boolean]("", false).getValue)
    .discoveryFrequency(CommonVars[Long]("", 1).getValue, TimeUnit.MINUTES)
    .loadbalancerEnabled(CommonVars[Boolean]("", false).getValue)
    .maxConnectionSize(CommonVars[Int]("", 1).getValue)
    .retryEnabled(CommonVars[Boolean]("", true).getValue)
    .readTimeout(CommonVars[Long]("", 12).getValue)
    .setAuthenticationStrategy(new TokenAuthenticationStrategy())
    .setAuthTokenKey(CommonVars[String]("", "").getValue)
    .setAuthTokenValue(CommonVars[String]("", "").getValue)
    .setDWSVersion(Configuration.LINKIS_WEB_VERSION.getValue)
    .build()

}
