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

package org.apache.linkis.datasource.client

import java.util
import java.util.concurrent.TimeUnit

import org.apache.linkis.common.utils.JsonUtils
import org.apache.linkis.datasource.client.impl.{LinkisDataSourceRemoteClient, LinkisMetaDataRemoteClient}
import org.apache.linkis.datasource.client.request._
import org.apache.linkis.datasource.client.response._
import org.apache.linkis.datasourcemanager.common.domain.DataSource
import org.apache.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import org.apache.linkis.httpclient.dws.config.DWSClientConfigBuilder
import org.junit.jupiter.api.{Disabled, Test}

object TestHiveClient {
  val gatewayUrl = "http://127.0.0.1:9001"
  val clientConfig = DWSClientConfigBuilder.newBuilder
    .addServerUrl(gatewayUrl)
    .connectionTimeout(30000)
    .discoveryEnabled(false)
    .discoveryFrequency(1, TimeUnit.MINUTES)
    .loadbalancerEnabled(true)
    .maxConnectionSize(1)
    .retryEnabled(false)
    .readTimeout(30000)
    .setAuthenticationStrategy(new StaticAuthenticationStrategy)
    .setAuthTokenKey("hadoop")
    .setAuthTokenValue("xxxxx")
    .setDWSVersion("v1")

  val dataSourceclient = new LinkisDataSourceRemoteClient(clientConfig.build())

  val clientConfig2 = DWSClientConfigBuilder.newBuilder
    .addServerUrl(gatewayUrl)
    .connectionTimeout(30000)
    .discoveryEnabled(false)
    .discoveryFrequency(1, TimeUnit.MINUTES)
    .loadbalancerEnabled(true)
    .maxConnectionSize(1)
    .retryEnabled(false)
    .readTimeout(30000)
    .setAuthenticationStrategy(new StaticAuthenticationStrategy)
    .setAuthTokenKey("hadoop")
    .setAuthTokenValue("xxxxx")
    .setDWSVersion("v1")

  val metaDataClient = new LinkisMetaDataRemoteClient(clientConfig2.build())


  @Test
  @Disabled
  def testCreateDataSourceMysql: Unit = {
    val user = "hadoop"
    val system = "Linkis"
    val dataSourceName = "for-mysql-test"
    val dataSource = new DataSource();
    dataSource.setDataSourceName(dataSourceName)
    dataSource.setDataSourceDesc("this is for hive test")
    dataSource.setCreateSystem(system)
    dataSource.setDataSourceTypeId(4L)

    val map = JsonUtils.jackson.readValue(JsonUtils.jackson.writeValueAsString(dataSource), new util.HashMap[String, Any]().getClass)
    val createDataSourceAction: CreateDataSourceAction = CreateDataSourceAction.builder()
      .setUser(user)
      .addRequestPayloads(map)
      .build()
    val createDataSourceResult: CreateDataSourceResult = dataSourceclient.createDataSource(createDataSourceAction)
    val dataSourceId = createDataSourceResult.getInsertId

    // set connectParams
    val params = new util.HashMap[String, Any]
    val connectParams = new util.HashMap[String, Any]
    connectParams.put("envId", "3")
    params.put("connectParams", connectParams)
    params.put("comment", "init")

    val updateParameterAction: UpdateDataSourceParameterAction = UpdateDataSourceParameterAction.builder()
      .setUser(user)
      .setDataSourceId(dataSourceId)
      .addRequestPayloads(params)
      .build()
    val updateParameterResult: UpdateDataSourceParameterResult = dataSourceclient.updateDataSourceParameter(updateParameterAction)

    val version: Long = updateParameterResult.getVersion

    // publist dataSource version
    dataSourceclient.publishDataSourceVersion(
      PublishDataSourceVersionAction.builder()
        .setDataSourceId(dataSourceId)
        .setUser(user)
        .setVersion(version)
        .build())

    // example of use
    val metadataGetDatabasesAction: MetadataGetDatabasesAction = MetadataGetDatabasesAction.builder()
      .setUser(user)
      .setDataSourceName(dataSourceName)
      .setSystem(system)
      .build()
    val metadataGetDatabasesResult: MetadataGetDatabasesResult = metaDataClient.getDatabases(metadataGetDatabasesAction)

    val metadataGetTablesAction: MetadataGetTablesAction = MetadataGetTablesAction.builder()
      .setUser(user)
      .setDataSourceName(dataSourceName)
      .setDatabase("linkis_test_ind")
      .setSystem(system)
      .build()
    val metadataGetTablesResult: MetadataGetTablesResult = metaDataClient.getTables(metadataGetTablesAction)



    val metadataGetColumnsAction = MetadataGetColumnsAction.builder()
      .setUser(user)
      .setDataSourceName(dataSourceName)
      .setDatabase("linkis_test_ind")
      .setSystem(system)
      .setTable("test")
      .build()
    val metadataGetColumnsResult: MetadataGetColumnsResult = metaDataClient.getColumns(metadataGetColumnsAction)


  }
}




