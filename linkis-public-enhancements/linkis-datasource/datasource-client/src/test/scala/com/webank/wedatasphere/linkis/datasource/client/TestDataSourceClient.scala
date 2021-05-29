package com.webank.wedatasphere.linkis.datasource.client

import com.webank.wedatasphere.linkis.datasource.client.impl.LinkisDataSourceRemoteClient
import com.webank.wedatasphere.linkis.datasource.client.request._
import com.webank.wedatasphere.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfigBuilder

import java.util.concurrent.TimeUnit

object TestDataSourceClient {
  def main(args: Array[String]): Unit = {
    val clientConfig = DWSClientConfigBuilder.newBuilder()
      .addServerUrl("http://192.168.10.201:9001")
      .connectionTimeout(30000)
      .discoveryEnabled(false)
      .discoveryFrequency(1,TimeUnit.MINUTES)
      .loadbalancerEnabled(true)
      .maxConnectionSize(5)
      .retryEnabled(false)
      .readTimeout(30000)
      .setAuthenticationStrategy(new StaticAuthenticationStrategy())
      .setAuthTokenKey("hadoop")
      .setAuthTokenValue("hadoop")
      .setDWSVersion("v1")
      .build()

    val dataSourceClient = new LinkisDataSourceRemoteClient(clientConfig)

    val getAllDataSourceTypesResult = dataSourceClient.getAllDataSourceTypes(GetAllDataSourceTypesAction.builder().setUser("hadoop").build()).getAllDataSourceType

    val queryDataSourceEnvResult = dataSourceClient.queryDataSourceEnv(
                                        QueryDataSourceEnvAction.builder()
                                        .setName("mysql")
                                        .setTypeId(2)
                                        .setCurrentPage(1)
                                        .setPageSize(1)
                                        .setUser("hadoop")
                                        .build() ).getDataSourceEnv

    val getInfoByDataSourceIdResult = dataSourceClient.getInfoByDataSourceId(
        GetInfoByDataSourceIdAction.builder().setDataSourceId(1).setSystem("xx").setUser("hadoop").build()
    ).getDataSource

    val queryDataSourceResult = dataSourceClient.queryDataSource(QueryDataSourceAction.builder()
                                      .setSystem("")
                                      .setName("mysql")
                                      .setTypeId(1)
                                      .setIdentifies("")
                                      .setCurrentPage(1)
                                      .setPageSize(10)
                                      .setUser("hadoop")
                                      .build()
                                ).getAllDataSource

    val getConnectParamsByDataSourceIdResult = dataSourceClient.getConnectParams(
      GetConnectParamsByDataSourceIdAction.builder().setDataSourceId(1).setSystem("xx").setUser("hadoop").build()
    )


//    val metadataClient = new LinkisMetaDataRemoteClient(clientConfig)
//    val metadataGetDatabasesResult = metadataClient.getDatabases(
//                                          MetadataGetDatabasesAction.builder()
//                                            .setSystem("xx")
//                                            .setDataSourceId(1)
//                                            .setUser("hadoop")
//                                            .build()
//                                     )
//    val metadataGetTablesResult = metadataClient.getTables(
//                                          MetadataGetTablesAction.builder()
//                                            .setSystem("xx")
//                                            .setDataSourceId(1)
//                                            .setDatabase("dss_test")
//                                            .setUser("hadoop").build()
//                                     )
//
//    val metadataGetTablePropsResult = metadataClient.getTableProps(
//                                          MetadataGetTablePropsAction.builder()
//                                            .setDataSourceId(3)
//                                            .setDatabase("dss_test")
//                                            .setTable("dss_appconn")
//                                            .setSystem("xx")
//                                            .setUser("hadoop")
//                                            .build()
//                                      )
////
////    val metadataGetPartitionsResult = metadataClient.getPartitions(
////                                          MetadataGetPartitionsAction.builder()
////                                            .setDataSourceId("")
////                                            .setDatabase("")
////                                            .setTable("")
////                                            .setSystem("")
////                                            .build()
////                                      )
//
//    val metadataGetColumnsResult = metadataClient.getColumns(
//                                          MetadataGetColumnsAction.builder()
//                                            .setDataSourceId(1)
//                                            .setDatabase("dss_test")
//                                            .setTable("dss_appconn")
//                                            .setSystem("xx")
//                                            .setUser("hadoop")
//                                            .build()
//                                      ).getAllColumns

    println("ss")


  }
}
