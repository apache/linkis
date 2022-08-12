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
import org.apache.linkis.httpclient.request.GetAction

class GetAllDataSourceTypesAction extends GetAction with DataSourceAction {

  override def suffixURLs: Array[String] =
    Array(DATA_SOURCE_SERVICE_MODULE.getValue, "type", "all")

  private var user: String = _

  override def setUser(user: String): Unit = this.user = user

  override def getUser: String = this.user
}

object GetAllDataSourceTypesAction {
  def builder(): Builder = new Builder

  class Builder private[GetAllDataSourceTypesAction] () {
    private var user: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def build(): GetAllDataSourceTypesAction = {
      val action = new GetAllDataSourceTypesAction
      action.setUser(user)

      action
    }

  }

}
