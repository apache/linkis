package org.apache.linkis.datasource.client.impl

import org.apache.linkis.datasource.client.request.{MetadataGetColumnsAction, MetadataGetDatabasesAction, MetadataGetPartitionsAction, MetadataGetTablePropsAction, MetadataGetTablesAction}
import org.apache.linkis.datasource.client.response.{MetadataGetColumnsResult, MetadataGetDatabasesResult, MetadataGetPartitionsResult, MetadataGetTablePropsResult, MetadataGetTablesResult}
import org.apache.linkis.datasource.client.{AbstractRemoteClient, MetaDataRemoteClient}
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.config.DWSClientConfig

class LinkisMetaDataRemoteClient(clientConfig: DWSClientConfig) extends AbstractRemoteClient with MetaDataRemoteClient {
  protected override val dwsHttpClient = new DWSHttpClient(clientConfig, "MetaData-Client")

  override def getDatabases(action: MetadataGetDatabasesAction): MetadataGetDatabasesResult = execute(action).asInstanceOf[MetadataGetDatabasesResult]

  override def getTables(action: MetadataGetTablesAction): MetadataGetTablesResult = execute(action).asInstanceOf[MetadataGetTablesResult]

  override def getTableProps(action: MetadataGetTablePropsAction): MetadataGetTablePropsResult = execute(action).asInstanceOf[MetadataGetTablePropsResult]

  override def getPartitions(action: MetadataGetPartitionsAction): MetadataGetPartitionsResult = execute(action).asInstanceOf[MetadataGetPartitionsResult]

  override def getColumns(action: MetadataGetColumnsAction): MetadataGetColumnsResult = execute(action).asInstanceOf[MetadataGetColumnsResult]
}
