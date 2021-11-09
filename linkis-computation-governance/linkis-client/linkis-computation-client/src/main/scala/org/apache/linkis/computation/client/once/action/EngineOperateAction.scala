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

class EngineOperateAction extends GetEngineConnAction {

  override def suffixURLs: Array[String] = Array("linkisManager", "executeEngineOperation")

}

object EngineOperateAction {

  val OPERATOR_NAME_KEY = "__operator_name__"

  def newBuilder(): Builder = new Builder

  class Builder extends ServiceInstanceBuilder[EngineOperateAction] {

    private var operatorName = ""

    private var properties: util.Map[String, Any] = new util.HashMap[String, Any]

    def operatorName(operatorName: String): this.type  = {
      this.operatorName = operatorName
      this
    }

    def setProperties(properties: util.Map[String, Any]): this.type = {
      this.properties = properties
      this
    }

    def addProperty(key: String, value: Any): this.type = {
      if (this.properties == null) {
        this.properties = new util.HashMap[String, Any]
      }
      this.properties.put(key, value)
      this
    }

    override protected def createGetEngineConnAction(): EngineOperateAction = {
      val action = new EngineOperateAction
      addProperty(OPERATOR_NAME_KEY, operatorName)
      action.addRequestPayload("properties", properties)
      action
    }
  }

}

