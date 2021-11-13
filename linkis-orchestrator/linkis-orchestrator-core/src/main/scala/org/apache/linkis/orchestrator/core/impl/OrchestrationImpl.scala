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

import org.apache.linkis.common.io.{Fs, FsPath}
import org.apache.linkis.orchestrator.core.{AbstractOrchestration, OrchestrationResponse, PlanBuilder}
import org.apache.linkis.orchestrator.exception.{OrchestratorErrorCodeSummary, OrchestratorErrorException}
import org.apache.linkis.orchestrator.plans.ast.ASTOrchestration
import org.apache.linkis.orchestrator.plans.logical.Task
import org.apache.linkis.orchestrator.{Orchestration, OrchestratorSession}

/**
  *
  */
class OrchestrationImpl(orchestratorSession: OrchestratorSession, planBuilder: PlanBuilder)
  extends AbstractOrchestration(orchestratorSession, planBuilder) {

  def this(orchestratorSession: OrchestratorSession, astPlan: ASTOrchestration[_]) = this(orchestratorSession,
    orchestratorSession.getOrchestratorSessionState.createPlanBuilder()
      .setOrchestratorSession(orchestratorSession).setASTPlan(astPlan))

  def this(orchestratorSession: OrchestratorSession, logicalPlan: Task) = this(orchestratorSession,
    orchestratorSession.getOrchestratorSessionState.createPlanBuilder()
      .setOrchestratorSession(orchestratorSession).setLogicalPlan(logicalPlan))

  override protected def collectResultSet(orchestrationResponse: OrchestrationResponse): String = throw new OrchestratorErrorException(OrchestratorErrorCodeSummary.ORCHESTRATION_FOR_OPERATION_NOT_SUPPORT_ERROR_CODE,
    s"Not support to read result from $orchestrationResponse.")

  override protected def getFileSystem(fsPath: FsPath): Fs =
    throw new OrchestratorErrorException(OrchestratorErrorCodeSummary.ORCHESTRATION_FOR_OPERATION_NOT_SUPPORT_ERROR_CODE,
      s"Not support to read result from $fsPath.")

  override protected def createOrchestration(logicalPlan: Task): Orchestration = new OrchestrationImpl(orchestratorSession, logicalPlan)

}
