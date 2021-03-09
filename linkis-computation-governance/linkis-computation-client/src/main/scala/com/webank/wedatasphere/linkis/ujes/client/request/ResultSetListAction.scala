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

import com.webank.wedatasphere.linkis.httpclient.request.GetAction
import com.webank.wedatasphere.linkis.ujes.client.exception.UJESClientBuilderException
import com.webank.wedatasphere.linkis.ujes.client.response.JobInfoResult

/**
  * created by cooperyang on 2019/5/23.
  */
class ResultSetListAction extends GetAction with UJESJobAction {
  override def suffixURLs: Array[String] = Array("filesystem", "getDirFileTrees")
}
object ResultSetListAction {
  def builder(): Builder = new Builder
  class Builder private[ResultSetListAction]() {
    private var user: String = _
    private var path: String = _

    def set(jobInfoResult: JobInfoResult): Builder = {
      this.user = jobInfoResult.getRequestPersistTask.getUmUser
      this.path = jobInfoResult.getRequestPersistTask.getResultLocation
      this
    }

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setPath(path: String): Builder = {
      this.path = path
      this
    }

    def build(): ResultSetListAction = {
      if(user == null) throw new UJESClientBuilderException("user is needed!")
      if(path == null) throw new UJESClientBuilderException("path is needed!")
      val resultSetListAction = new ResultSetListAction
      resultSetListAction.setParameter("path", path)
      resultSetListAction.setUser(user)
      resultSetListAction
    }
  }
}