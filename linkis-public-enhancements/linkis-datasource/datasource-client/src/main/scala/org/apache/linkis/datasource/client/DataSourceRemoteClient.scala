package org.apache.linkis.datasource.client

import org.apache.linkis.datasource.client.request.{GetAllDataSourceTypesAction, GetConnectParamsByDataSourceIdAction, GetInfoByDataSourceIdAction, QueryDataSourceAction, QueryDataSourceEnvAction}
import org.apache.linkis.datasource.client.response.{GetAllDataSourceTypesResult, GetConnectParamsByDataSourceIdResult, GetInfoByDataSourceIdResult, QueryDataSourceEnvResult, QueryDataSourceResult}

trait DataSourceRemoteClient extends RemoteClient {
  def getAllDataSourceTypes(action:GetAllDataSourceTypesAction): GetAllDataSourceTypesResult
  def queryDataSourceEnv(action:QueryDataSourceEnvAction): QueryDataSourceEnvResult
  def getInfoByDataSourceId(action:GetInfoByDataSourceIdAction): GetInfoByDataSourceIdResult
  def queryDataSource(action:QueryDataSourceAction): QueryDataSourceResult
  def getConnectParams(action: GetConnectParamsByDataSourceIdAction): GetConnectParamsByDataSourceIdResult

}
