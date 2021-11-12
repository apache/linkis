package com.webank.wedatasphere.linkis.datasource.client.impl

import com.webank.wedatasphere.linkis.datasource.client.request.{GetMetadataSourceAllDatabasesAction, GetMetadataSourceAllSizeAction}
import com.webank.wedatasphere.linkis.datasource.client.response.{GetMetadataSourceAllDatabasesResult, GetMetadataSourceAllSizeResult}
import com.webank.wedatasphere.linkis.datasource.client.{AbstractRemoteClient, MetadataSourceRemoteClient}
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfig

class LinkisMetadataSourceRemoteClient(clientConfig: DWSClientConfig) extends AbstractRemoteClient with MetadataSourceRemoteClient{
  override protected val dwsHttpClient: DWSHttpClient =  new DWSHttpClient(clientConfig, "MetadataSource-Client")

  override def getAllDBMetaDataSource(action: GetMetadataSourceAllDatabasesAction): GetMetadataSourceAllDatabasesResult = execute(action).asInstanceOf[GetMetadataSourceAllDatabasesResult]

  override def getAllSizeMetaDataSource(action: GetMetadataSourceAllSizeAction): GetMetadataSourceAllSizeResult = execute(action).asInstanceOf[GetMetadataSourceAllSizeResult]
}
