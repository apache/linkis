/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.datasource.client.request

import org.apache.linkis.datasource.client.config.DatasourceClientConfig.DATA_SOURCE_SERVICE_MODULE
import org.apache.linkis.datasource.client.exception.DataSourceClientBuilderException
import org.apache.linkis.httpclient.request.GetAction


class QueryDataSourceAction extends GetAction with DataSourceAction{
  override def suffixURLs: Array[String] = Array(DATA_SOURCE_SERVICE_MODULE.getValue, "info")

  private var user: String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user
}

object QueryDataSourceAction {
  def builder(): Builder = new Builder

  class Builder private[QueryDataSourceAction]() {
    private var system: String = _
    private var name: String = _
    private var typeId: Long = _
    private var identifies: String = _
    private var currentPage: Integer = _
    private var pageSize: Integer = _
    private var user: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setSystem(system: String): Builder = {
      this.system = system
      this
    }

    def setName(name: String): Builder = {
      this.name = name
      this
    }

    def setTypeId(typeId: Long): Builder = {
      this.typeId = typeId
      this
    }

    def setIdentifies(identifies: String): Builder = {
      this.identifies = identifies
      this
    }

    def setCurrentPage(currentPage: Integer): Builder = {
      this.currentPage = currentPage
      this
    }

    def setPageSize(pageSize: Integer): Builder = {
      this.pageSize = pageSize
      this
    }

    def build(): QueryDataSourceAction = {
      if (system == null) throw new DataSourceClientBuilderException("system is needed!")
      if(name == null) throw new DataSourceClientBuilderException("name is needed!")
      if(typeId == null) throw new DataSourceClientBuilderException("typeId is needed!")
      if(identifies == null) throw new DataSourceClientBuilderException("identifies is needed!")
      if(currentPage == null) throw new DataSourceClientBuilderException("currentPage is needed!")
      if(pageSize == null) throw new DataSourceClientBuilderException("pageSize is needed!")
      if(user == null) throw new DataSourceClientBuilderException("user is needed!")

      val queryDataSourceAction = new QueryDataSourceAction
      queryDataSourceAction.setParameter("system", system)
      queryDataSourceAction.setParameter("name", name)
      queryDataSourceAction.setParameter("typeId", typeId)
      queryDataSourceAction.setParameter("identifies", identifies)
      queryDataSourceAction.setParameter("currentPage", currentPage)
      queryDataSourceAction.setParameter("pageSize", pageSize)
      queryDataSourceAction.setUser(user)

      queryDataSourceAction
    }
  }
}
