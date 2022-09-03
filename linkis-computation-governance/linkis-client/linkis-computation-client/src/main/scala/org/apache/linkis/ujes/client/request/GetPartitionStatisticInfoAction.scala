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

class GetPartitionStatisticInfoAction extends GetAction with UJESJobAction {
  override def suffixURLs: Array[String] = Array("datasource", "getPartitionStatisticInfo")
}

object GetPartitionStatisticInfoAction {
  def builder(): Builder = new Builder

  class Builder private[GetPartitionStatisticInfoAction] () {
    private var user: String = _
    private var database: String = _
    private var tableName: String = _
    private var partitionPath: String = _

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

    def setPartitionPath(partitionPath: String): Builder = {
      this.partitionPath = partitionPath
      this
    }

    def builder(): GetPartitionStatisticInfoAction = {
      if (StringUtils.isBlank(user)) throw new UJESClientBuilderException("user is needed!")
      if (StringUtils.isBlank(database)) {
        throw new UJESClientBuilderException("database is needed!")
      }
      if (StringUtils.isBlank(tableName)) throw new UJESClientBuilderException("table is needed!")
      if (StringUtils.isBlank(partitionPath)) {
        throw new UJESClientBuilderException("partitionPath is needed!")
      }
      val getPartitionStatisticInfoAction = new GetPartitionStatisticInfoAction
      getPartitionStatisticInfoAction.setUser(user)
      getPartitionStatisticInfoAction.setParameter("database", database)
      getPartitionStatisticInfoAction.setParameter("tableName", tableName)
      getPartitionStatisticInfoAction.setParameter("partitionPath", partitionPath)
      getPartitionStatisticInfoAction
    }

  }

}
