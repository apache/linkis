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

class GetKeyTypeDatasourceAction extends GetAction with DataSourceAction {
  private var dataSourceTypeId: Long = _

  override def suffixURLs: Array[String] =
    Array(DATA_SOURCE_SERVICE_MODULE.getValue, "key-define", "type", dataSourceTypeId.toString)

  private var user: String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user
}

object GetKeyTypeDatasourceAction {
  def builder(): Builder = new Builder

  class Builder private[GetKeyTypeDatasourceAction] () {
    private var dataSourceTypeId: Long = _
    private var user: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setDataSourceTypeId(dataSourceTypeId: Long): Builder = {
      this.dataSourceTypeId = dataSourceTypeId
      this
    }

    def build(): GetKeyTypeDatasourceAction = {
      if (user == null) throw new DataSourceClientBuilderException(USER_NEEDED.getErrorDesc)

      val getKeyTypeDatasourceAction = new GetKeyTypeDatasourceAction
      getKeyTypeDatasourceAction.dataSourceTypeId = this.dataSourceTypeId
      getKeyTypeDatasourceAction.setUser(user)
      getKeyTypeDatasourceAction
    }

  }

}
