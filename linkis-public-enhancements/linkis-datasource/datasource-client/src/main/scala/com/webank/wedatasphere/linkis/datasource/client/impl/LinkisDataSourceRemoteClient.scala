package com.webank.wedatasphere.linkis.datasource.client.impl

import com.webank.wedatasphere.linkis.datasource.client.request.{CreateDataSourceAction, DataSourceTestConnectAction, DeleteDataSourceAction, ExpireDataSourceAction, GetAllDataSourceTypesAction, GetConnectParamsByDataSourceIdAction, GetDataSourceVersionsAction, GetInfoByDataSourceIdAction, GetKeyTypeDatasourceAction, PublishDataSourceVersionAction, QueryDataSourceAction, QueryDataSourceEnvAction, UpdateDataSourceAction, UpdateDataSourceParameterAction}
import com.webank.wedatasphere.linkis.datasource.client.response.{CreateDataSourceResult, DataSourceTestConnectResult, DeleteDataSourceResult, ExpireDataSourceResult, GetAllDataSourceTypesResult, GetConnectParamsByDataSourceIdResult, GetDataSourceVersionsResult, GetInfoByDataSourceIdResult, GetKeyTypeDatasourceResult, PublishDataSourceVersionResult, QueryDataSourceEnvResult, QueryDataSourceResult, UpdateDataSourceParameterResult, UpdateDataSourceResult}
import com.webank.wedatasphere.linkis.datasource.client.{AbstractRemoteClient, DataSourceRemoteClient}
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfig


class LinkisDataSourceRemoteClient(clientConfig: DWSClientConfig) extends AbstractRemoteClient with DataSourceRemoteClient {
  protected override val dwsHttpClient = new DWSHttpClient(clientConfig, "DataSource-Client")

  override def getAllDataSourceTypes(action:GetAllDataSourceTypesAction): GetAllDataSourceTypesResult =  execute(action).asInstanceOf[GetAllDataSourceTypesResult]

  override def queryDataSourceEnv(action:QueryDataSourceEnvAction): QueryDataSourceEnvResult = execute(action).asInstanceOf[QueryDataSourceEnvResult]

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


