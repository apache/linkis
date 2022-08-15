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
 
package org.apache.linkis.orchestrator.extensions.catalyst

import org.apache.linkis.orchestrator.OrchestratorSession
import org.apache.linkis.orchestrator.extensions.CatalystExtensions
import org.apache.linkis.orchestrator.extensions.catalyst.Transform._

import scala.collection.mutable.ArrayBuffer

/**
  *
  */
class CatalystExtensionsImpl extends CatalystExtensions {

  private val converterTransformBuilders = new ArrayBuffer[ConverterTransformBuilder]

  private val parserTransformBuilders = new ArrayBuffer[ParserTransformBuilder]

  private val plannerTransformBuilders = new ArrayBuffer[PlannerTransformBuilder]

  private val optimizerTransformBuilders = new ArrayBuffer[OptimizerTransformBuilder]

  private val physicalTransformBuilders = new ArrayBuffer[PhysicalTransformBuilder]

  private val reheaterTransformBuilders = new ArrayBuffer[ReheaterTransformBuilder]

  override def injectConverterTransform(converterTransformBuilder: ConverterTransformBuilder): Unit =
    converterTransformBuilders += converterTransformBuilder

  override def injectParserTransform(parserTransformBuilder: ParserTransformBuilder): Unit =
    parserTransformBuilders += parserTransformBuilder

  override def injectPlannerTransform(plannerTransformBuilder: PlannerTransformBuilder): Unit =
    plannerTransformBuilders += plannerTransformBuilder

  override def injectOptimizerTransform(optimizerTransformBuilder: OptimizerTransformBuilder): Unit =
    optimizerTransformBuilders += optimizerTransformBuilder

  override def injectPhysicalTransform(physicalTransformBuilder: PhysicalTransformBuilder): Unit =
    physicalTransformBuilders += physicalTransformBuilder

  override def injectReheaterTransform(reheaterTransformBuilder: ReheaterTransformBuilder): Unit =
    reheaterTransformBuilders += reheaterTransformBuilder

  override def build(orchestratorSession: OrchestratorSession): Array[Transform[_, _, _]] =
    Array(converterTransformBuilders, parserTransformBuilders, plannerTransformBuilders, optimizerTransformBuilders,
      physicalTransformBuilders, reheaterTransformBuilders).flatMap(_.map(_(orchestratorSession))).toArray
}
