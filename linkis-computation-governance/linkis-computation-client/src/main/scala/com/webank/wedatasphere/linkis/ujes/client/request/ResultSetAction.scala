/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

/*
 * created by cooperyang on 2019/07/24.
 */

package com.webank.wedatasphere.linkis.ujes.client.request

import com.webank.wedatasphere.linkis.common.conf.Configuration
import com.webank.wedatasphere.linkis.httpclient.request.GetAction
import com.webank.wedatasphere.linkis.ujes.client.exception.UJESClientBuilderException

/**
  * created by cooperyang on 2019/5/23.
  */
class ResultSetAction private() extends GetAction with UJESJobAction {
  override def suffixURLs: Array[String] = Array("filesystem", "openFile")
}
object ResultSetAction {
  def builder(): Builder = new Builder
  class Builder private[ResultSetAction]() {
    private var user: String = _
    private var path: String = _
    private var page: Int = _
    private var pageSize: Int = _
    private var charset: String = Configuration.BDP_ENCODING.getValue

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

    def build(): ResultSetAction = {
      if(user == null) throw new UJESClientBuilderException("user is needed!")
      if(path == null) throw new UJESClientBuilderException("path is needed!")
      val resultSetAction = new ResultSetAction
      resultSetAction.setParameter("path", path)
      if(page > 0) resultSetAction.setParameter("page", page)
      if(pageSize > 0) resultSetAction.setParameter("pageSize", pageSize)
      resultSetAction.setParameter("charset", charset)
      resultSetAction.setUser(user)
      resultSetAction
    }

  }
}