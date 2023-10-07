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

package org.apache.linkis.datasource.client.request

import org.apache.linkis.datasource.client.config.DatasourceClientConfig.DATA_SOURCE_SERVICE_MODULE
import org.apache.linkis.datasource.client.errorcode.DatasourceClientErrorCodeSummary._
import org.apache.linkis.datasource.client.exception.DataSourceClientBuilderException
import org.apache.linkis.httpclient.request.GetAction

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.MessageFormat

/**
 * Get version parameters from data source
 */
class GetConnectParamsByDataSourceNameAction extends GetAction with DataSourceAction {
  private var dataSourceName: String = _

  private var user: String = _

  override def suffixURLs: Array[String] =
    Array(DATA_SOURCE_SERVICE_MODULE.getValue, "name", dataSourceName, "connect-params")

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = user
}

object GetConnectParamsByDataSourceNameAction {
  def builder(): Builder = new Builder

  class Builder private[GetConnectParamsByDataSourceNameAction] () {
    private var dataSourceName: String = _
    private var system: String = _
    private var user: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setDataSourceName(dataSourceName: String): Builder = {
      this.dataSourceName = dataSourceName
      this
    }

    def setSystem(system: String): Builder = {
      this.system = system
      this
    }

    def build(): GetConnectParamsByDataSourceNameAction = {
      if (dataSourceName == null) {
        throw new DataSourceClientBuilderException(DATASOURCENAME_NEEDED.getErrorDesc)
      }
      if (system == null) throw new DataSourceClientBuilderException(SYSTEM_NEEDED.getErrorDesc)
      if (user == null) throw new DataSourceClientBuilderException(USER_NEEDED.getErrorDesc)
      // Use URIEncoder to encode the datSourceName
      var requestDataSourceName = this.dataSourceName
      try {
        requestDataSourceName = URLEncoder.encode(dataSourceName, StandardCharsets.UTF_8.name())
      } catch {
        case e: Exception =>
          throw new DataSourceClientBuilderException(
            MessageFormat.format(CANNOT_SOURCE.getErrorDesc, dataSourceName),
            e
          )
      }
      val getConnectParamsByDataSourceNameAction = new GetConnectParamsByDataSourceNameAction
      getConnectParamsByDataSourceNameAction.dataSourceName = requestDataSourceName
      getConnectParamsByDataSourceNameAction.setParameter("system", system)
      getConnectParamsByDataSourceNameAction.setUser(user)
      getConnectParamsByDataSourceNameAction
    }

  }

}
