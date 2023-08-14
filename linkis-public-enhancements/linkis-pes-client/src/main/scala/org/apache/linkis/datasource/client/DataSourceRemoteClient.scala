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

package org.apache.linkis.datasource.client

import org.apache.linkis.datasource.client.request._
import org.apache.linkis.datasource.client.response._

trait DataSourceRemoteClient extends RemoteClient {
  def getAllDataSourceTypes(action: GetAllDataSourceTypesAction): GetAllDataSourceTypesResult

  def queryDataSourceEnv(action: QueryDataSourceEnvAction): QueryDataSourceEnvResult
  def getInfoByDataSourceId(action: GetInfoByDataSourceIdAction): GetInfoByDataSourceIdResult

  def getInfoByDataSourceName(action: GetInfoByDataSourceNameAction): GetInfoByDataSourceNameResult

  def getInfoPublishedByDataSourceName(
      action: GetInfoPublishedByDataSourceNameAction
  ): GetInfoPublishedByDataSourceNameResult

  def queryDataSource(action: QueryDataSourceAction): QueryDataSourceResult

  def getConnectParams(
      action: GetConnectParamsByDataSourceIdAction
  ): GetConnectParamsByDataSourceIdResult

  def getConnectParamsByName(
      action: GetConnectParamsByDataSourceNameAction
  ): GetConnectParamsByDataSourceNameResult

  def createDataSource(action: CreateDataSourceAction): CreateDataSourceResult

  def getDataSourceTestConnect(action: DataSourceTestConnectAction): DataSourceTestConnectResult

  def deleteDataSource(action: DeleteDataSourceAction): DeleteDataSourceResult

  def expireDataSource(action: ExpireDataSourceAction): ExpireDataSourceResult

  def getDataSourceVersions(action: GetDataSourceVersionsAction): GetDataSourceVersionsResult

  def publishDataSourceVersion(
      action: PublishDataSourceVersionAction
  ): PublishDataSourceVersionResult

  def updateDataSource(action: UpdateDataSourceAction): UpdateDataSourceResult

  def updateDataSourceParameter(
      action: UpdateDataSourceParameterAction
  ): UpdateDataSourceParameterResult

  def getKeyDefinitionsByType(action: GetKeyTypeDatasourceAction): GetKeyTypeDatasourceResult
}
