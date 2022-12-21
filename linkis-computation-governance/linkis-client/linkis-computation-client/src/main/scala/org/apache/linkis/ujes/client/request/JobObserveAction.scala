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

import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.request.{GetAction, POSTAction}
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.ujes.client.exception.UJESClientBuilderException

import java.util
import java.util.Map

class JobObserveAction private () extends POSTAction with UJESJobAction {

  override def suffixURLs: Array[String] = Array("jobhistory", "setting", "addObserveInfo")

  override def getRequestPayload: String =
    DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)

}

object JobObserveAction {
  def builder(): Builder = new Builder

  class Builder private[JobObserveAction] () {

    private var user: String = _
    private var taskId: String = _
    private var monitorLevel: String = _
    private var receiver: String = _
    private var subSystemId: String = _
    private var extra: util.Map[String, String] = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setTaskId(taskId: String): Builder = {
      this.taskId = taskId
      this
    }

    def setMonitorLevel(monitorLevel: String): Builder = {
      this.monitorLevel = monitorLevel
      this
    }

    def setReceiver(receiver: String): Builder = {
      this.receiver = receiver
      this
    }

    def setSubSystemId(subSystemId: String): Builder = {
      this.subSystemId = subSystemId
      this
    }

    def setExtra(extra: util.Map[String, String]): Builder = {
      this.extra = extra
      this
    }

    def build(): JobObserveAction = {
      val JobObserveAction = new JobObserveAction
      if (taskId == null) throw new UJESClientBuilderException("taskId is needed!")
      if (monitorLevel == null) throw new UJESClientBuilderException("monitorLevel is needed!")
      if (receiver == null) throw new UJESClientBuilderException("receiver is needed!")
      if (subSystemId == null) throw new UJESClientBuilderException("subSystemId is needed!")
      if (extra == null) throw new UJESClientBuilderException("extra is needed!")
      JobObserveAction.setUser(user)
      JobObserveAction.addRequestPayload("taskId", taskId)
      JobObserveAction.addRequestPayload(TaskConstant.MONITOR_LEVEL, monitorLevel)
      JobObserveAction.addRequestPayload(TaskConstant.RECEIVER, receiver)
      JobObserveAction.addRequestPayload(TaskConstant.SUB_SYSTEM_ID, subSystemId)
      JobObserveAction.addRequestPayload(TaskConstant.EXTRA, extra)
      JobObserveAction
    }

  }

}
