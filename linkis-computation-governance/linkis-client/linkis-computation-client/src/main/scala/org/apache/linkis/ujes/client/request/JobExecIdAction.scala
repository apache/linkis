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

class JobExecIdAction private () extends GetAction with UJESJobAction {

  private var execId: String = _
  private var jobServiceType: JobExecIdAction.JobServiceType.JobServiceType = _

  override def suffixURLs: Array[String] = Array("entrance", execId, jobServiceType.toString)
}

object JobExecIdAction {
  def builder(): Builder = new Builder

  class Builder private[JobExecIdAction] () {
    private var user: String = _
    private var execId: String = _
    private var jobServiceType: JobServiceType.JobServiceType = _

    def setJobServiceType(jobServiceType: JobServiceType.JobServiceType): Builder = {
      this.jobServiceType = jobServiceType
      this
    }

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setExecId(execId: String): Builder = {
      this.execId = execId
      this
    }

    def build(): JobExecIdAction = {
      val jobStatusAction = new JobExecIdAction
      if (execId == null) throw new UJESClientBuilderException("execId is needed!")
      if (user == null) throw new UJESClientBuilderException("user is needed!")
      if (jobServiceType == null) {
        throw new UJESClientBuilderException("jobServiceType is needed!")
      }
      jobStatusAction.execId = execId
      jobStatusAction.setUser(user)
      jobStatusAction.jobServiceType = jobServiceType
      jobStatusAction
    }

  }

  object JobServiceType extends Enumeration {
    type JobServiceType = Value
    val JobStatus = Value("status")
    val JobProgress = Value("progress")
    val JobKill = Value("kill")
    val JobPause = Value("pause")
    val JobRuntimeTuning = Value("runtimeTuning")
  }

}
