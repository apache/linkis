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

import org.apache.commons.lang3.StringUtils

class GetInfoPublishedByUserIpPortAction extends GetAction with DataSourceAction {
  private var datasourceTypeName: String = _
  private var ip: String = _
  private var port: String = _
  private var datasourceUser: String = _

  override def suffixURLs: Array[String] =
    Array(
      DATA_SOURCE_SERVICE_MODULE.getValue,
      "publishedInfo",
      datasourceTypeName,
      datasourceUser,
      ip,
      port
    )

  private var user: String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user
}

object GetInfoPublishedByUserIpPortAction {
  def builder(): Builder = new Builder

  class Builder private[GetInfoPublishedByUserIpPortAction] () {
    private var datasourceTypeName: String = _
    private var ip: String = _
    private var port: String = _
    private var system: String = _
    private var user: String = _
    private var datasourceUser: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setDatasourceUser(datasourceUser: String): Builder = {
      this.datasourceUser = datasourceUser
      this
    }

    def setDatasourceTypeName(datasourceTypeName: String): Builder = {
      this.datasourceTypeName = datasourceTypeName
      this
    }

    def setIp(ip: String): Builder = {
      this.ip = ip
      this
    }

    def setPort(port: String): Builder = {
      this.port = port
      this
    }

    def setSystem(system: String): Builder = {
      this.system = system
      this
    }

    def build(): GetInfoPublishedByUserIpPortAction = {
      if (datasourceTypeName == null) {
        throw new DataSourceClientBuilderException(DATASOURCE_NEEDED.getErrorDesc)
      }
      if (ip == null) {
        throw new DataSourceClientBuilderException(IP_NEEDED.getErrorDesc)
      }
      if (port == null) {
        throw new DataSourceClientBuilderException(PORT_NEEDED.getErrorDesc)
      }
      if (datasourceUser == null) {
        throw new DataSourceClientBuilderException(OWNER_NEEDED.getErrorDesc)
      }
      //      if (system == null) throw new DataSourceClientBuilderException(SYSTEM_NEEDED.getErrorDesc)
      if (user == null) throw new DataSourceClientBuilderException(USER_NEEDED.getErrorDesc)

      val GetInfoPublishedByUserIpPortAction = new GetInfoPublishedByUserIpPortAction
      GetInfoPublishedByUserIpPortAction.datasourceTypeName = this.datasourceTypeName
      GetInfoPublishedByUserIpPortAction.ip = this.ip
      GetInfoPublishedByUserIpPortAction.port = this.port
      GetInfoPublishedByUserIpPortAction.datasourceUser = this.datasourceUser
      if (StringUtils.isNotBlank(system)) {
        GetInfoPublishedByUserIpPortAction.setParameter("system", system)
      }
      GetInfoPublishedByUserIpPortAction.setUser(user)
      GetInfoPublishedByUserIpPortAction
    }

  }

}
