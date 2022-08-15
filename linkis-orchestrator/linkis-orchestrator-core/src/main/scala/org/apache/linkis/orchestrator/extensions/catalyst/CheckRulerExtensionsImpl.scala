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

package org.apache.linkis.orchestrator.extensions.catalyst

import org.apache.linkis.orchestrator.OrchestratorSession
import org.apache.linkis.orchestrator.extensions.CheckRulerExtensions
import org.apache.linkis.orchestrator.extensions.catalyst.CheckRuler.{
  ConverterCheckRulerBuilder,
  ValidatorCheckRulerBuilder
}

import scala.collection.mutable.ArrayBuffer

/**
 */
class CheckRulerExtensionsImpl extends CheckRulerExtensions {

  private val converterCheckRulerBuilders = new ArrayBuffer[ConverterCheckRulerBuilder]
  private val validatorCheckRulerBuilders = new ArrayBuffer[ValidatorCheckRulerBuilder]

  override def injectConverterCheckRuler(
      converterCheckRulerBuilder: ConverterCheckRulerBuilder
  ): Unit =
    converterCheckRulerBuilders += converterCheckRulerBuilder

  override def injectValidatorCheckRuler(
      validatorCheckRulerBuilder: ValidatorCheckRulerBuilder
  ): Unit =
    validatorCheckRulerBuilders += validatorCheckRulerBuilder

  override def build(orchestratorSession: OrchestratorSession): Array[CheckRuler[_, _]] =
    Array(converterCheckRulerBuilders, validatorCheckRulerBuilders)
      .flatMap(_.map(_(orchestratorSession)))
      .toArray

}
