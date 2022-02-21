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

import org.apache.linkis.datasource.client.impl.LinkisDataSourceRemoteClient
import org.apache.linkis.datasource.client.request._
import org.apache.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import org.apache.linkis.httpclient.dws.config.DWSClientConfigBuilder

import java.util.concurrent.TimeUnit

object TestDataSourceClient {
  def main(args: Array[String]): Unit = {
    val clientConfig = DWSClientConfigBuilder.newBuilder()
      .addServerUrl("http://127.0.0.1:9001")
      .connectionTimeout(30000)
      .discoveryEnabled(false)
      .discoveryFrequency(1, TimeUnit.MINUTES)
      .loadbalancerEnabled(true)
      .maxConnectionSize(5)
      .retryEnabled(false)
      .readTimeout(30000)
      .setAuthenticationStrategy(new StaticAuthenticationStrategy())
      .setAuthTokenKey("hadoop")
      .setAuthTokenValue("hadoop")
      .setDWSVersion("v1")
      .build()

    val dataSourceClient = new LinkisDataSourceRemoteClient(clientConfig)

    val getAllDataSourceTypesResult = dataSourceClient.getAllDataSourceTypes(GetAllDataSourceTypesAction.builder().setUser("hadoop").build()).getAllDataSourceType

    val queryDataSourceEnvResult = dataSourceClient.queryDataSourceEnv(
                                        QueryDataSourceEnvAction.builder()
                                        .setName("mysql")
                                        .setTypeId(2)
                                        .setCurrentPage(1)
                                        .setPageSize(1)
                                        .setUser("hadoop")
                                        .build()).getDataSourceEnv

    val getInfoByDataSourceIdResult = dataSourceClient.getInfoByDataSourceId(
        GetInfoByDataSourceIdAction.builder().setDataSourceId(1).setSystem("xx").setUser("hadoop").build()
    ).getDataSource

    val queryDataSourceResult = dataSourceClient.queryDataSource(QueryDataSourceAction.builder()
                                      .setSystem("")
                                      .setName("mysql")
                                      .setTypeId(1)
                                      .setIdentifies("")
                                      .setCurrentPage(1)
                                      .setPageSize(10)
                                      .setUser("hadoop")
                                      .build()
                                ).getAllDataSource

    val getConnectParamsByDataSourceIdResult = dataSourceClient.getConnectParams(
      GetConnectParamsByDataSourceIdAction.builder().setDataSourceId(1).setSystem("xx").setUser("hadoop").build()
    )


  }
}
