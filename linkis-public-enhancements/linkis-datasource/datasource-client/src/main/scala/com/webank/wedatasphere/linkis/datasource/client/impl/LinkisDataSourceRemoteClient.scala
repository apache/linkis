package com.webank.wedatasphere.linkis.datasource.client.impl

import com.webank.wedatasphere.linkis.datasource.client.request.{GetAllDataSourceTypesAction, GetInfoByDataSourceIdAction, GetKeyDefinitionsByTypeAction, QueryDataSourceAction, QueryDataSourceEnvAction}
import com.webank.wedatasphere.linkis.datasource.client.response.{GetAllDataSourceTypesResult, GetInfoByDataSourceIdResult, GetKeyDefinitionsByTypeResult, QueryDataSourceEnvResult, QueryDataSourceResult}
import com.webank.wedatasphere.linkis.datasource.client.{AbstractRemoteClient, DataSourceRemoteClient}
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfig


class LinkisDataSourceRemoteClient(clientConfig: DWSClientConfig) extends AbstractRemoteClient with DataSourceRemoteClient {
  protected override val dwsHttpClient = new DWSHttpClient(clientConfig, "DataSource-Client")

  override def getAllDataSourceTypes(action:GetAllDataSourceTypesAction): GetAllDataSourceTypesResult =  execute(action).asInstanceOf[GetAllDataSourceTypesResult]

  override def getKeyDefinitionsByType(action:GetKeyDefinitionsByTypeAction): GetKeyDefinitionsByTypeResult = execute(action).asInstanceOf[GetKeyDefinitionsByTypeResult]

  override def queryDataSourceEnv(action:QueryDataSourceEnvAction): QueryDataSourceEnvResult = execute(action).asInstanceOf[QueryDataSourceEnvResult]

  override def getInfoByDataSourceId(action: GetInfoByDataSourceIdAction): GetInfoByDataSourceIdResult = execute(action).asInstanceOf[GetInfoByDataSourceIdResult]

  override def queryDataSource(action: QueryDataSourceAction): QueryDataSourceResult = execute(action).asInstanceOf[QueryDataSourceResult]
}


