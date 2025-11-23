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

package org.apache.linkis.computation.client.operator.impl

import org.apache.linkis.computation.client.once.action.EngineConnOperateAction
import org.apache.linkis.computation.client.once.result.EngineConnOperateResult
import org.apache.linkis.computation.client.operator.OnceJobOperator
import org.apache.linkis.ujes.client.exception.UJESJobException

import java.util

class EngineConnCommonOperator extends OnceJobOperator[util.Map[String, Any]] {

  private var operatorName: String = _
  private val parameters: util.Map[String, Any] = new util.HashMap[String, Any]

  def setRealOperatorName(operatorName: String): Unit = this.operatorName = operatorName

  def addParameter(key: String, value: Any): Unit = parameters.put(key, value)

  override protected def resultToObject(result: EngineConnOperateResult): util.Map[String, Any] =
    result.getResult

  override protected def addParameters(builder: EngineConnOperateAction.Builder): Unit = {
    if (operatorName == null) throw new UJESJobException(20310, "realOperatorName must be set!")
    builder.operatorName(operatorName)
    if (!parameters.isEmpty) builder.setParameters(parameters)
  }

  override def getName: String = EngineConnCommonOperator.OPERATOR_NAME

}

object EngineConnCommonOperator {
  val OPERATOR_NAME = "engineConnCommon"
}
