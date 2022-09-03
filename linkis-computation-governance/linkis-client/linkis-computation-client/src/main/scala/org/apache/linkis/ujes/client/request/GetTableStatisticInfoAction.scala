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

import org.apache.commons.lang3.StringUtils

class GetTableStatisticInfoAction extends GetAction with UJESJobAction {
  override def suffixURLs: Array[String] = Array("datasource", "getTableStatisticInfo")
}

object GetTableStatisticInfoAction {
  def builder(): Builder = new Builder

  class Builder private[GetTableStatisticInfoAction] () {
    private var user: String = _
    private var database: String = _
    private var tableName: String = _
    private var pageSize: Int = 100
    private var pageNow: Int = 1
    private var partitionSort: String = "desc"

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def getUser(): String = user

    def setDatabase(db: String): Builder = {
      this.database = db
      this
    }

    def setTable(table: String): Builder = {
      this.tableName = table
      this
    }

    def setPageSize(pageSize: Int): Builder = {
      this.pageSize = pageSize
      this
    }

    def setPageNow(pageNow: Int): Builder = {
      this.pageNow = pageNow
      this
    }

    def setPartitionSort(order: String): Builder = {
      this.partitionSort = order
      this
    }

    def builder(): GetTableStatisticInfoAction = {
      if (StringUtils.isBlank(user)) throw new UJESClientBuilderException("user is needed!")
      if (StringUtils.isBlank(database)) {
        throw new UJESClientBuilderException("database is needed!")
      }
      if (StringUtils.isBlank(tableName)) throw new UJESClientBuilderException("table is needed!")
      val getTableStatisticInfoAction = new GetTableStatisticInfoAction
      getTableStatisticInfoAction.setUser(user)
      getTableStatisticInfoAction.setParameter("database", database)
      getTableStatisticInfoAction.setParameter("tableName", tableName)
      getTableStatisticInfoAction.setParameter("pageNow", pageNow)
      getTableStatisticInfoAction.setParameter("pageSize", pageSize)
      getTableStatisticInfoAction.setParameter("partitionSort", partitionSort)
      getTableStatisticInfoAction
    }

  }

}
