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

import java.util

class EngineConnOperateAction extends GetEngineConnAction {

  override def suffixURLs: Array[String] = Array("linkisManager", "executeEngineConnOperation")

}

object EngineConnOperateAction {

  val OPERATOR_NAME_KEY = "__operator_name__"

  def newBuilder(): Builder = new Builder

  class Builder extends ServiceInstanceBuilder[EngineConnOperateAction] {

    private var operatorName = ""

    private var parameters: util.Map[String, Any] = new util.HashMap[String, Any]

    def operatorName(operatorName: String): this.type = {
      this.operatorName = operatorName
      this
    }

    def setParameters(parameters: util.Map[String, Any]): this.type = {
      this.parameters = parameters
      this
    }

    def addParameter(key: String, value: Any): this.type = {
      if (this.parameters == null) {
        this.parameters = new util.HashMap[String, Any]
      }
      this.parameters.put(key, value)
      this
    }

    protected def newEngineConnOperateAction(): EngineConnOperateAction =
      new EngineConnOperateAction

    override protected def createGetEngineConnAction(): EngineConnOperateAction = {
      val action = newEngineConnOperateAction()
      addParameter(OPERATOR_NAME_KEY, operatorName)
      action.addRequestPayload("parameters", parameters)
      action
    }
  }

}

