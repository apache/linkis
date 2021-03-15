/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.ujes.client.request

import com.webank.wedatasphere.linkis.httpclient.request.GetAction
import com.webank.wedatasphere.linkis.ujes.client.exception.UJESClientBuilderException

class GetColumnsAction extends GetAction with UJESJobAction {
  override def suffixURLs: Array[String] = Array("datasource",  "columns")
}
object GetColumnsAction {
  def builder(): Builder = new Builder
  class Builder private[GetColumnsAction]() {

    private var user: String = _

    private var database: String = _

    private var table: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setDatabase(database: String): Builder = {
      this.database = database
      this
    }

    def setTable(table: String): Builder = {
      this.table = table
      this
    }

    def build(): GetColumnsAction = {
      if(user == null) throw new UJESClientBuilderException("user is needed!")
      if(database == null) throw new UJESClientBuilderException("database is needed!")
      val getColumnsAction = new GetColumnsAction
      getColumnsAction.setUser(user)
      getColumnsAction.setParameter("database", database)
      getColumnsAction.setParameter("table", table)
      getColumnsAction
    }
  }
}


