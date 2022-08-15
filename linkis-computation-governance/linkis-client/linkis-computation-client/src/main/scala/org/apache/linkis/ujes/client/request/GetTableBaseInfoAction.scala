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

package org.apache.linkis.ujes.client.request

import org.apache.linkis.httpclient.request.GetAction
import org.apache.linkis.ujes.client.exception.UJESClientBuilderException

class GetTableBaseInfoAction extends GetAction with UJESJobAction {
  override def suffixURLs: Array[String] = Array("datasource", "getTableBaseInfo")
}

object GetTableBaseInfoAction {
  def builder(): Builder = new Builder

  class Builder private[GetTableBaseInfoAction] () {

    private var user: String = _

    private var database: String = _

    private var tablename: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setDatabase(database: String): Builder = {
      this.database = database
      this
    }

    def setTablename(tablename: String): Builder = {
      this.tablename = tablename
      this
    }

    def build(): GetTableBaseInfoAction = {
      if (user == null) throw new UJESClientBuilderException("user is needed!")
      if (database == null) throw new UJESClientBuilderException("database is needed!")
      if (tablename == null) throw new UJESClientBuilderException("tablename is needed!")
      val getTableBaseInfoAction = new GetTableBaseInfoAction
      getTableBaseInfoAction.setUser(user)
      getTableBaseInfoAction.setParameter("database", database)
      getTableBaseInfoAction.setParameter("tableName", tablename)
      getTableBaseInfoAction
    }

  }

}
