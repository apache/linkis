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

import org.apache.commons.lang3.StringUtils

class JobDeleteObserveAction extends GetAction with UJESJobAction {
  override def suffixURLs: Array[String] = Array("jobhistory", "setting", "deleteObserveInfo")
}

object JobDeleteObserveAction {
  def newBuilder(): Builder = new Builder

  class Builder private[JobDeleteObserveAction] () {
    private var user: String = _
    private var taskId: Long = _

    def setTaskId(taskId: Long): Builder = {
      this.taskId = taskId
      this
    }

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def build(): JobDeleteObserveAction = {
      val jobDeleteObserve = new JobDeleteObserveAction
      if (StringUtils.isNotBlank(user)) jobDeleteObserve.setUser(user)
      jobDeleteObserve.setParameter("taskId", taskId)
      jobDeleteObserve
    }

  }

}
