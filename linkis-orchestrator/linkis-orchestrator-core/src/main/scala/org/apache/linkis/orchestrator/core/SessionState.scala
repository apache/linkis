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

package org.apache.linkis.orchestrator.core

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.orchestrator.converter.Converter
import org.apache.linkis.orchestrator.execution.Execution
import org.apache.linkis.orchestrator.extensions.operation.Operation
import org.apache.linkis.orchestrator.listener.{
  OrchestratorAsyncListenerBus,
  OrchestratorSyncListenerBus
}
import org.apache.linkis.orchestrator.optimizer.Optimizer
import org.apache.linkis.orchestrator.parser.Parser
import org.apache.linkis.orchestrator.planner.Planner
import org.apache.linkis.orchestrator.reheater.Reheater
import org.apache.linkis.orchestrator.validator.Validator

/**
 */
trait SessionState {

  def getValue[T](commonVars: CommonVars[T]): T

  def getValue[T](key: String): T

  private[core] def setStringConf(key: String, value: String): Unit

  def createPlanBuilder(): PlanBuilder

  def getOperations: Array[Operation[_]]

  def getConverter: Converter

  def getParser: Parser

  def getValidator: Validator

  def getPlanner: Planner

  def getOptimizer: Optimizer

  def getExecution: Execution

  def getReheater: Reheater

  def getOrchestratorAsyncListenerBus: OrchestratorAsyncListenerBus

  def getOrchestratorSyncListenerBus: OrchestratorSyncListenerBus

}
