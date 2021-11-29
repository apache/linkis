package org.apache.linkis.datasource.client.impl

import org.apache.linkis.datasource.client.request.{GetAllDataSourceTypesAction, GetConnectParamsByDataSourceIdAction, GetInfoByDataSourceIdAction, QueryDataSourceAction, QueryDataSourceEnvAction}
import org.apache.linkis.datasource.client.response.{GetAllDataSourceTypesResult, GetConnectParamsByDataSourceIdResult, GetInfoByDataSourceIdResult, QueryDataSourceEnvResult, QueryDataSourceResult}
import org.apache.linkis.datasource.client.{AbstractRemoteClient, DataSourceRemoteClient}
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.config.DWSClientConfig


class LinkisDataSourceRemoteClient(clientConfig: DWSClientConfig) extends AbstractRemoteClient with DataSourceRemoteClient {
  protected override val dwsHttpClient = new DWSHttpClient(clientConfig, "DataSource-Client")

  override def getAllDataSourceTypes(action:GetAllDataSourceTypesAction): GetAllDataSourceTypesResult =  execute(action).asInstanceOf[GetAllDataSourceTypesResult]

  override def queryDataSourceEnv(action:QueryDataSourceEnvAction): QueryDataSourceEnvResult = execute(action).asInstanceOf[QueryDataSourceEnvResult]

  override def getInfoByDataSourceId(action: GetInfoByDataSourceIdAction): GetInfoByDataSourceIdResult = execute(action).asInstanceOf[GetInfoByDataSourceIdResult]

  override def queryDataSource(action: QueryDataSourceAction): QueryDataSourceResult = execute(action).asInstanceOf[QueryDataSourceResult]

  override def getConnectParams(action: GetConnectParamsByDataSourceIdAction): GetConnectParamsByDataSourceIdResult = execute(action).asInstanceOf[GetConnectParamsByDataSourceIdResult]
}


