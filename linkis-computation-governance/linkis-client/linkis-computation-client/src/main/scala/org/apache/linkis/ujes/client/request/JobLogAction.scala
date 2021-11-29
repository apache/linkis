/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.linkis.ujes.client.response.JobLogResult

class JobLogAction private() extends GetAction with UJESJobAction {

  private var execId: String = _

  override def suffixURLs: Array[String] = Array("entrance", execId, "log")

}

object JobLogAction {
  def builder(): Builder = new Builder
  class Builder private[JobLogAction]() {
    private var user: String = _
    private var execId: String = _
    private var fromLine: Int = _
    private var size: Int = _

    def setFromLine(fromLine: Int): Builder = {
      this.fromLine = fromLine
      this
    }

    def setFromLine(jobLogResult: JobLogResult): Builder = {
      this.fromLine = jobLogResult.getFromLine
      this
    }

    def setSize(size: Int): Builder = {
      this.size = size
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

    def build(): JobLogAction = {
      val jobLogAction = new JobLogAction
      if(execId == null) throw new UJESClientBuilderException("execId is needed!")
      if(user == null) throw new UJESClientBuilderException("user is needed!")
      jobLogAction.execId = execId
      jobLogAction.setUser(user)
      if(fromLine > 0) jobLogAction.setParameter("fromLine", fromLine)
      if(size > 0) jobLogAction.setParameter("size", size)
      jobLogAction
    }
  }

}
