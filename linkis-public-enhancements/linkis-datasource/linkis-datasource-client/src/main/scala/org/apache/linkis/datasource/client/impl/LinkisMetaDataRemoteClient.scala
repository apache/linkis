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

package org.apache.linkis.datasource.client.impl

import org.apache.linkis.datasource.client.{AbstractRemoteClient, MetaDataRemoteClient}
import org.apache.linkis.datasource.client.request._
import org.apache.linkis.datasource.client.response._
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.config.DWSClientConfig

class LinkisMetaDataRemoteClient(clientConfig: DWSClientConfig)
    extends AbstractRemoteClient
    with MetaDataRemoteClient {

  protected override val dwsHttpClient = new DWSHttpClient(clientConfig, "MetaData-Client")

  override def getDatabases(action: MetadataGetDatabasesAction): MetadataGetDatabasesResult =
    execute(action).asInstanceOf[MetadataGetDatabasesResult]

  override def getTables(action: MetadataGetTablesAction): MetadataGetTablesResult =
    execute(action).asInstanceOf[MetadataGetTablesResult]

  override def getTableProps(action: MetadataGetTablePropsAction): MetadataGetTablePropsResult =
    execute(action).asInstanceOf[MetadataGetTablePropsResult]

  override def getPartitions(action: MetadataGetPartitionsAction): MetadataGetPartitionsResult =
    execute(action).asInstanceOf[MetadataGetPartitionsResult]

  override def getColumns(action: MetadataGetColumnsAction): MetadataGetColumnsResult = execute(
    action
  ).asInstanceOf[MetadataGetColumnsResult]

}
