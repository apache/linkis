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

package org.apache.linkis.engineconnplugin.sqoop.params

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.datasource.client.DataSourceRemoteClient
import org.apache.linkis.datasource.client.request.GetConnectParamsByDataSourceNameAction
import org.apache.linkis.datasource.client.response.GetConnectParamsByDataSourceNameResult
import org.apache.linkis.datasourcemanager.common.protocol.{DsInfoQueryRequest, DsInfoResponse}
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconnplugin.sqoop.client.RemoteClientHolder
import org.apache.linkis.engineconnplugin.sqoop.context.{
  SqoopEnvConfiguration,
  SqoopParamsConfiguration
}
import org.apache.linkis.engineconnplugin.sqoop.exception.DataSourceRpcErrorException
import org.apache.linkis.rpc.Sender

import org.apache.commons.lang3.StringUtils

import java.util

import scala.collection.JavaConverters._

/**
 * connect the data source manager instance
 */
class SqoopDataSourceParamsResolver extends SqoopParamsResolver with Logging {

  /**
   * main method fetch the connect params from datasource and then set into map
   * @param params
   *   input
   * @return
   */
  override def resolve(
      params: util.Map[String, String],
      context: EngineCreationContext
  ): util.Map[String, String] = {
    info(s"Invoke resolver: ${this.getClass.getSimpleName}")
    Option(params.get(SqoopParamsConfiguration.SQOOP_PARAM_DATA_SOURCE.getValue)).foreach(
      dataSourceName => {
        Option(rpcCall(dataSourceName)).foreach(response => {
          response.params.asScala.foreach {
            case (paramKey, paramValue) =>
              params.put(
                s"${SqoopParamsConfiguration.SQOOP_PARAM_PREFIX.getValue}$paramKey",
                String.valueOf(Option(paramValue).getOrElse(""))
              )
            case _ =>
          }
          if (response.params.isEmpty) {
            warn(
              s" Note: params from data source [$dataSourceName] is empty, have you published it ?"
            )
          }
          info(s"Fetch ${response.params.size} params from data source [${dataSourceName}]")
        })
      }
    )
    params
  }

  def clientCall(dataSource: String, user: String): util.Map[String, Any] = {
    val clientHolder = new RemoteClientHolder(user, "sqoop")
    val client: DataSourceRemoteClient = clientHolder.getDataSourceClient
    Utils.tryFinally {
      var result: GetConnectParamsByDataSourceNameResult = null
      try {
        // Fetch the connect params by data source and username
        val action = GetConnectParamsByDataSourceNameAction
          .builder()
          .setDataSourceName(dataSource)
          .setSystem("sqoop")
          .setUser(user)
          .build()
        result = clientHolder.executeDataSource(action)
      } catch {
        case e: Exception =>
          throw new DataSourceRpcErrorException(
            "Unable to access to the data source server in client",
            e
          )
      }
      result
    } { client.close() } match {
      case result: GetConnectParamsByDataSourceNameResult => result.connectParams
      case _ =>
        throw new DataSourceRpcErrorException(
          "Empty response from data source server in client",
          null
        )
    }
  }

  def rpcCall(dataSourceName: String): DsInfoResponse = {
    val sender = Sender.getSender(SqoopEnvConfiguration.LINKIS_DATASOURCE_SERVICE_NAME.getValue)
    var rpcResult: Any = null
    try {
      rpcResult = sender.ask(new DsInfoQueryRequest(null, dataSourceName, "sqoop"))
    } catch {
      case e: Exception =>
        throw new DataSourceRpcErrorException(
          s"Send rpc to data source service [${SqoopEnvConfiguration.LINKIS_DATASOURCE_SERVICE_NAME.getValue}] error",
          e
        )
    }
    rpcResult match {
      case response: DsInfoResponse =>
        if (!response.status) {
          throw new DataSourceRpcErrorException(
            "Exception happened in data source manager server, please check the log in instance",
            null
          )
        }
        val dsType = response.dsType
        if (StringUtils.isBlank(dsType)) {
          throw new DataSourceRpcErrorException(
            s"Data source type cannot be null for [$dataSourceName], creator:[${response.creator}]",
            null
          )
        }
        response
      case _ => null
    }
  }

}
