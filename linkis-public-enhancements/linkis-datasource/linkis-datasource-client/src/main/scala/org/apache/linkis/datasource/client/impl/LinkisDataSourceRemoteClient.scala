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

package org.apache.linkis.datasource.client.impl

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.datasource.client.{AbstractRemoteClient, DataSourceRemoteClient}
import org.apache.linkis.datasource.client.config.DatasourceClientConfig._
import org.apache.linkis.datasource.client.errorcode.DatasourceClientErrorCodeSummary.SERVERURL_CANNOT_NULL
import org.apache.linkis.datasource.client.exception.DataSourceClientBuilderException
import org.apache.linkis.datasource.client.request._
import org.apache.linkis.datasource.client.response._
import org.apache.linkis.httpclient.authentication.AuthenticationStrategy
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy
import org.apache.linkis.httpclient.dws.config.{DWSClientConfig, DWSClientConfigBuilder}

import org.apache.commons.lang3.StringUtils

import java.util.concurrent.TimeUnit

class LinkisDataSourceRemoteClient(clientConfig: DWSClientConfig, clientName: String)
    extends AbstractRemoteClient
    with DataSourceRemoteClient {
  def this() = this(null, null)

  def this(clientConfig: DWSClientConfig) = this(clientConfig, null)

  protected override val dwsHttpClient: DWSHttpClient = new DWSHttpClient(
    if (clientConfig != null) clientConfig else createClientConfig(),
    if (StringUtils.isEmpty(clientName)) DATA_SOURCE_SERVICE_CLIENT_NAME.getValue else clientName
  )

  def createClientConfig(): DWSClientConfig = {
    val serverUrl = Configuration.getGateWayURL()
    if (StringUtils.isEmpty(serverUrl)) {
      throw new DataSourceClientBuilderException(SERVERURL_CANNOT_NULL.getErrorDesc)
    }

    val maxConnection: Int = CONNECTION_MAX_SIZE.getValue
    val connectTimeout: Int = CONNECTION_TIMEOUT.getValue
    val readTimeout: Int = CONNECTION_READ_TIMEOUT.getValue
    val tokenKey: String = AUTH_TOKEN_KEY.getValue
    val tokenValue: String = AUTH_TOKEN_VALUE.getValue

    val authenticationStrategy: AuthenticationStrategy = new TokenAuthenticationStrategy()
    DWSClientConfigBuilder
      .newBuilder()
      .addServerUrl(serverUrl)
      .connectionTimeout(connectTimeout)
      .discoveryEnabled(false)
      .discoveryFrequency(1, TimeUnit.MINUTES)
      .loadbalancerEnabled(true)
      .maxConnectionSize(maxConnection)
      .retryEnabled(false)
      .readTimeout(readTimeout)
      .setAuthenticationStrategy(authenticationStrategy)
      .setAuthTokenKey(tokenKey)
      .setAuthTokenValue(tokenValue)
      .setDWSVersion(Configuration.LINKIS_WEB_VERSION.getValue)
      .build()
  }

  override def getAllDataSourceTypes(
      action: GetAllDataSourceTypesAction
  ): GetAllDataSourceTypesResult = execute(action).asInstanceOf[GetAllDataSourceTypesResult]

  override def queryDataSourceEnv(action: QueryDataSourceEnvAction): QueryDataSourceEnvResult =
    execute(action).asInstanceOf[QueryDataSourceEnvResult]

  override def getInfoByDataSourceId(
      action: GetInfoByDataSourceIdAction
  ): GetInfoByDataSourceIdResult = execute(action).asInstanceOf[GetInfoByDataSourceIdResult]

  override def getInfoByDataSourceName(
      action: GetInfoByDataSourceNameAction
  ): GetInfoByDataSourceNameResult = execute(action).asInstanceOf[GetInfoByDataSourceNameResult]

  override def getInfoPublishedByDataSourceName(
      action: GetInfoPublishedByDataSourceNameAction
  ): GetInfoPublishedByDataSourceNameResult =
    execute(action).asInstanceOf[GetInfoPublishedByDataSourceNameResult]

  override def queryDataSource(action: QueryDataSourceAction): QueryDataSourceResult = execute(
    action
  ).asInstanceOf[QueryDataSourceResult]

  override def getConnectParams(
      action: GetConnectParamsByDataSourceIdAction
  ): GetConnectParamsByDataSourceIdResult =
    execute(action).asInstanceOf[GetConnectParamsByDataSourceIdResult]

  override def getConnectParamsByName(
      action: GetConnectParamsByDataSourceNameAction
  ): GetConnectParamsByDataSourceNameResult =
    execute(action).asInstanceOf[GetConnectParamsByDataSourceNameResult]

  override def createDataSource(action: CreateDataSourceAction): CreateDataSourceResult = execute(
    action
  ).asInstanceOf[CreateDataSourceResult]

  override def getDataSourceTestConnect(
      action: DataSourceTestConnectAction
  ): DataSourceTestConnectResult = execute(action).asInstanceOf[DataSourceTestConnectResult]

  override def deleteDataSource(action: DeleteDataSourceAction): DeleteDataSourceResult = execute(
    action
  ).asInstanceOf[DeleteDataSourceResult]

  override def expireDataSource(action: ExpireDataSourceAction): ExpireDataSourceResult = execute(
    action
  ).asInstanceOf[ExpireDataSourceResult]

  override def getDataSourceVersions(
      action: GetDataSourceVersionsAction
  ): GetDataSourceVersionsResult = execute(action).asInstanceOf[GetDataSourceVersionsResult]

  override def publishDataSourceVersion(
      action: PublishDataSourceVersionAction
  ): PublishDataSourceVersionResult = execute(action).asInstanceOf[PublishDataSourceVersionResult]

  override def updateDataSource(action: UpdateDataSourceAction): UpdateDataSourceResult = execute(
    action
  ).asInstanceOf[UpdateDataSourceResult]

  override def updateDataSourceParameter(
      action: UpdateDataSourceParameterAction
  ): UpdateDataSourceParameterResult =
    execute(action).asInstanceOf[UpdateDataSourceParameterResult]

  override def getKeyDefinitionsByType(
      action: GetKeyTypeDatasourceAction
  ): GetKeyTypeDatasourceResult = execute(action).asInstanceOf[GetKeyTypeDatasourceResult]

}
