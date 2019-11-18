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
import com.webank.wedatasphere.linkis.ujes.client.response.JobExecuteResult
import org.apache.commons.lang.StringUtils

/**
  * created by cooperyang on 2019/5/23.
  */
class JobInfoAction extends GetAction with UJESJobAction {
  private var taskId: String = _
  override def suffixURLs: Array[String] = Array("jobhistory", taskId, "get")
}
object JobInfoAction {
  def builder(): Builder = new Builder
  class Builder private[JobInfoAction]() {
    private var taskId: String = _
    private var user: String = _
    def setTaskId(taskId: String): Builder = {
      this.taskId = taskId
      this
    }
    def setUser(user: String): Builder = {
      this.user = user
      this
    }
    def setTaskId(jobExecuteResult: JobExecuteResult): Builder = {
      this.user = jobExecuteResult.getUser
      setTaskId(jobExecuteResult.getTaskID)
    }
    def build(): JobInfoAction = {
      if(StringUtils.isBlank(taskId)) throw new UJESClientBuilderException("taskId is needed!")
      if(StringUtils.isBlank(user)) throw new UJESClientBuilderException("user is needed!")
      val jobInfoAction = new JobInfoAction
      jobInfoAction.taskId = taskId
      jobInfoAction.setUser(user)
      jobInfoAction
    }
  }
}
