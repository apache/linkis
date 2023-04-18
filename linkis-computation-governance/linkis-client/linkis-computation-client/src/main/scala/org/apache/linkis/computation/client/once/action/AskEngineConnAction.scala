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

package org.apache.linkis.computation.client.once.action

import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.request.POSTAction
import org.apache.linkis.ujes.client.exception.UJESJobException

import org.apache.commons.lang3.StringUtils

import java.util

class AskEngineConnAction extends POSTAction with LinkisManagerAction {

  override def getRequestPayload: String =
    DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)

  override def suffixURLs: Array[String] = Array("linkisManager", "askEngineConn")

}

object AskEngineConnAction {

  def newBuilder(): Builder = new Builder

  class Builder private[AskEngineConnAction] () {
    private var user: String = _
    private var properties: util.Map[String, String] = _
    private var labels: util.Map[String, String] = _
    private var maxSubmitTime: Long = _
    private var createService: String = _
    private var description: String = _

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def setProperties(properties: util.Map[String, String]): Builder = {
      this.properties = properties
      this
    }

    def setLabels(labels: java.util.Map[String, String]): Builder = {
      this.labels = labels
      this
    }

    def setMaxSubmitTime(maxSubmitTime: Long): Builder = {
      this.maxSubmitTime = maxSubmitTime
      this
    }

    def setCreateService(createService: String): Builder = {
      this.createService = createService
      this
    }

    def setDescription(description: String): Builder = {
      this.description = description
      this
    }

    def build(): AskEngineConnAction = {
      val action = new AskEngineConnAction()
      if (user == null) throw new UJESJobException("user is needed!")
      if (properties == null) properties = new java.util.HashMap[String, String]
      if (labels == null) throw new UJESJobException("labels is needed!")
      action.setUser(user)
      action.addRequestPayload("properties", properties)
      action.addRequestPayload("labels", labels)
      if (StringUtils.isNotBlank(createService)) {
        action.addRequestPayload("createService", createService)
      }
      if (null != maxSubmitTime) {
        action.addRequestPayload("timeOut", maxSubmitTime)
      }
      if (StringUtils.isNotBlank(description)) {
        action.addRequestPayload("description", description)
      }
      action
    }

  }

}
