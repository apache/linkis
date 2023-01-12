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
import org.apache.linkis.httpclient.request.POSTAction
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.ujes.client.exception.UJESClientBuilderException

import java.util

class JobSubmitAction private () extends POSTAction with UJESJobAction {
  override def suffixURLs: Array[String] = Array("entrance", "submit")

  override def getRequestPayload: String =
    DWSHttpClient.jacksonJson.writeValueAsString(getRequestPayloads)

}

object JobSubmitAction {
  def builder(): Builder = new Builder

  class Builder private[JobSubmitAction] () {
    private var user: String = _

    // TODO: remove executeUser in the future
    private var executeUser: String = _

    private var executionContent: util.Map[String, AnyRef] = _

    private var formatCode: Boolean = false

    private var labels: util.Map[String, AnyRef] = _

    private var params: util.Map[String, AnyRef] = _

    private var source: util.Map[String, AnyRef] = _

    def addExecuteCode(executeCode: String): Builder = {
      if (null == executionContent) executionContent = new util.HashMap[String, AnyRef]()
      executionContent.put("code", executeCode)
      this
    }

    def setRunTypeStr(runTypeStr: String): Builder = {
      if (null == executionContent) executionContent = new util.HashMap[String, AnyRef]()
      executionContent.put("runType", runTypeStr)
      this
    }

    def setUser(user: String): Builder = {
      this.user = user
      this
    }

    def addExecuteUser(executeUser: String): Builder = {
      this.executeUser = executeUser
      this
    }

    def enableFormatCode(): Builder = {
      this.formatCode = true
      this
    }

    def setExecutionContent(executionContent: util.Map[String, AnyRef]): Builder = {
      this.executionContent = executionContent
      this
    }

    def setLabels(labels: util.Map[String, AnyRef]): Builder = {
      this.labels = labels
      this
    }

    def setParams(params: util.Map[String, AnyRef]): Builder = {
      this.synchronized(this.params = params)
      this
    }

    def setSource(source: util.Map[String, AnyRef]): Builder = {
      this.synchronized(this.source = source)
      this
    }

    def setStartupParams(startupMap: util.Map[String, AnyRef]): Builder = {
      initParams
      TaskUtils.addStartupMap(this.params, startupMap)
      this
    }

    private def initParams(): Unit = {
      if (this.params == null) this synchronized {
        if (this.params == null) this.params = new util.HashMap[String, AnyRef]
      }
    }

    def setRuntimeParams(runtimeMap: util.Map[String, AnyRef]): Builder = {
      initParams
      TaskUtils.addRuntimeMap(this.params, runtimeMap)
      this
    }

    def setSpecialParams(specialMap: util.Map[String, AnyRef]): Builder = {
      initParams
      TaskUtils.addSpecialMap(this.params, specialMap)
      this
    }

    def setVariableMap(variableMap: util.Map[String, AnyRef]): Builder = {
      initParams
      TaskUtils.addVariableMap(this.params, variableMap)
      this
    }

    def build(): JobSubmitAction = {
      val submitAction = new JobSubmitAction
      submitAction.setUser(user)
      submitAction.addRequestPayload(TaskConstant.EXECUTE_USER, executeUser)
      if (formatCode) submitAction.addRequestPayload(TaskConstant.FORMATCODE, true)
      if (executionContent == null && params == null) {
        throw new UJESClientBuilderException("executionContent is needed!")
      }
      submitAction.addRequestPayload(TaskConstant.EXECUTION_CONTENT, executionContent)
      if (params == null) params = new util.HashMap[String, AnyRef]()
      submitAction.addRequestPayload(TaskConstant.PARAMS, params)
      if (this.source == null) this.source = new util.HashMap[String, AnyRef]()
      submitAction.addRequestPayload(TaskConstant.SOURCE, this.source)

      if (this.labels == null) this.labels = new util.HashMap[String, AnyRef]()
      submitAction.addRequestPayload(TaskConstant.LABELS, this.labels)
      submitAction
    }

  }

}
