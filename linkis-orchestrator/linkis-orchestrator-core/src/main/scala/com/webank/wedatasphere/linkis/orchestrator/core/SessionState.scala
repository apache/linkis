/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.orchestrator.core

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.orchestrator.converter.Converter
import com.webank.wedatasphere.linkis.orchestrator.execution.Execution
import com.webank.wedatasphere.linkis.orchestrator.extensions.operation.Operation
import com.webank.wedatasphere.linkis.orchestrator.listener.{OrchestratorAsyncListenerBus, OrchestratorSyncListenerBus}
import com.webank.wedatasphere.linkis.orchestrator.optimizer.Optimizer
import com.webank.wedatasphere.linkis.orchestrator.parser.Parser
import com.webank.wedatasphere.linkis.orchestrator.planner.Planner
import com.webank.wedatasphere.linkis.orchestrator.reheater.Reheater
import com.webank.wedatasphere.linkis.orchestrator.validator.Validator

/**
  *
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
