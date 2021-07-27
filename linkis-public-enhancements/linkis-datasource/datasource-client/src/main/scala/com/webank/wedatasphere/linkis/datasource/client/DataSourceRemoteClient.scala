package com.webank.wedatasphere.linkis.datasource.client

import com.webank.wedatasphere.linkis.datasource.client.request.{GetAllDataSourceTypesAction, GetInfoByDataSourceIdAction, GetKeyDefinitionsByTypeAction, QueryDataSourceAction, QueryDataSourceEnvAction}
import com.webank.wedatasphere.linkis.datasource.client.response.{GetAllDataSourceTypesResult, GetInfoByDataSourceIdResult, GetKeyDefinitionsByTypeResult, QueryDataSourceEnvResult, QueryDataSourceResult}

trait DataSourceRemoteClient extends RemoteClient {
  def getAllDataSourceTypes(action:GetAllDataSourceTypesAction): GetAllDataSourceTypesResult
  def getKeyDefinitionsByType(action:GetKeyDefinitionsByTypeAction): GetKeyDefinitionsByTypeResult
  def queryDataSourceEnv(action:QueryDataSourceEnvAction): QueryDataSourceEnvResult
  def getInfoByDataSourceId(action:GetInfoByDataSourceIdAction): GetInfoByDataSourceIdResult
  def queryDataSource(action:QueryDataSourceAction): QueryDataSourceResult
}
