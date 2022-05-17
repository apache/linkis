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

package org.apache.linkis.datasource.client.impl

import org.apache.linkis.datasource.client.request._
import org.apache.linkis.datasource.client.response._
import org.apache.linkis.datasource.client.{AbstractRemoteClient, DataSourceRemoteClient}
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.config.DWSClientConfig


class LinkisDataSourceRemoteClient(clientConfig: DWSClientConfig) extends AbstractRemoteClient with DataSourceRemoteClient {
  protected override val dwsHttpClient = new DWSHttpClient(clientConfig, "DataSource-Client")

  override def getAllDataSourceTypes(action: GetAllDataSourceTypesAction): GetAllDataSourceTypesResult = execute(action).asInstanceOf[GetAllDataSourceTypesResult]

  override def queryDataSourceEnv(action: QueryDataSourceEnvAction): QueryDataSourceEnvResult = execute(action).asInstanceOf[QueryDataSourceEnvResult]

  override def getInfoByDataSourceId(action: GetInfoByDataSourceIdAction): GetInfoByDataSourceIdResult = execute(action).asInstanceOf[GetInfoByDataSourceIdResult]

  override def queryDataSource(action: QueryDataSourceAction): QueryDataSourceResult = execute(action).asInstanceOf[QueryDataSourceResult]

  override def getConnectParams(action: GetConnectParamsByDataSourceIdAction): GetConnectParamsByDataSourceIdResult = execute(action).asInstanceOf[GetConnectParamsByDataSourceIdResult]

  override def createDataSource(action: CreateDataSourceAction): CreateDataSourceResult = execute(action).asInstanceOf[CreateDataSourceResult]

  override def getDataSourceTestConnect(action: DataSourceTestConnectAction): DataSourceTestConnectResult = execute(action).asInstanceOf[DataSourceTestConnectResult]

  override def deleteDataSource(action: DeleteDataSourceAction): DeleteDataSourceResult = execute(action).asInstanceOf[DeleteDataSourceResult]

  override def expireDataSource(action: ExpireDataSourceAction): ExpireDataSourceResult = execute(action).asInstanceOf[ExpireDataSourceResult]

  override def getDataSourceVersions(action: GetDataSourceVersionsAction): GetDataSourceVersionsResult = execute(action).asInstanceOf[GetDataSourceVersionsResult]

  override def publishDataSourceVersion(action: PublishDataSourceVersionAction): PublishDataSourceVersionResult = execute(action).asInstanceOf[PublishDataSourceVersionResult]

  override def updateDataSource(action: UpdateDataSourceAction): UpdateDataSourceResult = execute(action).asInstanceOf[UpdateDataSourceResult]

  override def updateDataSourceParameter(action: UpdateDataSourceParameterAction): UpdateDataSourceParameterResult = execute(action).asInstanceOf[UpdateDataSourceParameterResult]

  override def getKeyDefinitionsByType(action: GetKeyTypeDatasourceAction): GetKeyTypeDatasourceResult = execute(action).asInstanceOf[GetKeyTypeDatasourceResult]
}


