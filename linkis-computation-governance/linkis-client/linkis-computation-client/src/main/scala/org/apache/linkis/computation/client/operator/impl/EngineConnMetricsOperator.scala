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

import org.apache.linkis.computation.client.once.result.EngineConnOperateResult
import org.apache.linkis.computation.client.operator.OnceJobOperator

import java.util

class EngineConnMetricsOperator extends OnceJobOperator[util.Map[String, AnyRef]] {

  override protected def resultToObject(
      result: EngineConnOperateResult
  ): util.Map[String, AnyRef] = {
    result.getAs("metrics")
  }

  override def getName: String = EngineConnMetricsOperator.OPERATOR_NAME
}

object EngineConnMetricsOperator {
  val OPERATOR_NAME = "engineConnMetrics"
}
