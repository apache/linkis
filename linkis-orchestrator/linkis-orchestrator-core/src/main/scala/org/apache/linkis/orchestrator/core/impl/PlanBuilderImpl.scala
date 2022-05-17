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
 
package org.apache.linkis.orchestrator.core.impl

import org.apache.linkis.orchestrator.OrchestratorSession
import org.apache.linkis.orchestrator.core.PlanBuilder
import org.apache.linkis.orchestrator.plans.ast.ASTOrchestration
import org.apache.linkis.orchestrator.plans.logical.Task
import org.apache.linkis.orchestrator.plans.physical.ExecTask

/**
  *
  */
class PlanBuilderImpl extends PlanBuilder{

  private var orchestratorSession: OrchestratorSession = _
  private var astPlan: ASTOrchestration[_] = _
  private var logicalPlan: Task = _
  private var physicalPlan: ExecTask = _

  override def setOrchestratorSession(orchestratorSession: OrchestratorSession): PlanBuilder = {
    this.orchestratorSession = orchestratorSession
    this
  }

  override def setASTPlan(astPlan: ASTOrchestration[_]): PlanBuilder = {
    this.astPlan = astPlan
    this
  }

  override def setLogicalPlan(logicalPlan: Task): PlanBuilder = {
    this.logicalPlan = logicalPlan
    this
  }

  override def getLogicalPlan: Task = {
    if(logicalPlan == null) {
      astPlan = orchestratorSession.getOrchestratorSessionState.getParser.parse(astPlan)
      orchestratorSession.getOrchestratorSessionState.getValidator.validate(astPlan)
      logicalPlan = orchestratorSession.getOrchestratorSessionState.getPlanner.plan(astPlan)
    }
    logicalPlan
  }

  override def getBuiltPhysicalPlan: ExecTask = {
    if(physicalPlan == null) {
      physicalPlan = orchestratorSession.getOrchestratorSessionState.getOptimizer.optimize(getLogicalPlan)
    }
    physicalPlan
  }
}
