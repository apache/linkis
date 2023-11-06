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

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.httpclient.request.GetAction
import org.apache.linkis.ujes.client.exception.UJESClientBuilderException

class ResultSetAction private () extends GetAction with UJESJobAction {
  override def suffixURLs: Array[String] = Array("filesystem", "openFile")
}

object ResultSetAction {
  def builder(): Builder = new Builder

  class Builder private[ResultSetAction] () {
    private var user: String = _
    private var path: String = _
    private var page: Int = _
    private var pageSize: Int = _
    private var charset: String = Configuration.BDP_ENCODING.getValue

    // default value is :org.apache.linkis.storage.domain.Dolphin.LINKIS_NULL
    private var nullValue: String = "LINKIS_NULL"

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setPath(path: String): Builder = {
      this.path = path
      this
    }

    def setPage(page: Int): Builder = {
      this.page = page
      this
    }

    def setPageSize(pageSize: Int): Builder = {
      this.pageSize = pageSize
      this
    }

    def setCharset(charset: String): Builder = {
      this.charset = charset
      this
    }

    def setNullValue(nullValue: String): Builder = {
      this.nullValue = nullValue
      this
    }

    def build(): ResultSetAction = {
      if (user == null) throw new UJESClientBuilderException("user is needed!")
      if (path == null) throw new UJESClientBuilderException("path is needed!")
      val resultSetAction = new ResultSetAction
      resultSetAction.setParameter("path", path)
      if (page > 0) resultSetAction.setParameter("page", page)
      if (pageSize > 0) resultSetAction.setParameter("pageSize", pageSize)
      resultSetAction.setParameter("charset", charset)
      resultSetAction.setParameter("nullValue", nullValue)
      resultSetAction.setUser(user)
      resultSetAction
    }

  }

}
