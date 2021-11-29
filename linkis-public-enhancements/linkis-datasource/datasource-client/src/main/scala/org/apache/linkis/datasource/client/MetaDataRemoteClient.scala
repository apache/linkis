package org.apache.linkis.datasource.client

import org.apache.linkis.datasource.client.request.{MetadataGetColumnsAction, MetadataGetDatabasesAction, MetadataGetPartitionsAction, MetadataGetTablePropsAction, MetadataGetTablesAction}
import org.apache.linkis.datasource.client.response.{MetadataGetColumnsResult, MetadataGetDatabasesResult, MetadataGetPartitionsResult, MetadataGetTablePropsResult, MetadataGetTablesResult}

trait MetaDataRemoteClient extends RemoteClient {
  def getDatabases(action:MetadataGetDatabasesAction): MetadataGetDatabasesResult
  def getTables(action:MetadataGetTablesAction): MetadataGetTablesResult
  def getTableProps(action:MetadataGetTablePropsAction): MetadataGetTablePropsResult
  def getPartitions(action:MetadataGetPartitionsAction): MetadataGetPartitionsResult
  def getColumns(action:MetadataGetColumnsAction): MetadataGetColumnsResult
}
