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

package com.webank.wedatasphere.linkis.entrance.orchestrator

import com.webank.wedatasphere.linkis.entrance.orchestrator.plugin.EntranceUserParallelOrchestratorPlugin
import com.webank.wedatasphere.linkis.orchestrator.OrchestratorSession
import com.webank.wedatasphere.linkis.orchestrator.computation.ComputationOrchestratorSessionFactory
import com.webank.wedatasphere.linkis.orchestrator.computation.operation.progress.ProgressOperationBuilder
import com.webank.wedatasphere.linkis.orchestrator.core.AbstractOrchestratorContext
import com.webank.wedatasphere.linkis.orchestrator.extensions.OperationExtensions
import com.webank.wedatasphere.linkis.orchestrator.extensions.OperationExtensions.OperationExtensionsBuilder


object EntranceOrchestrationFactory {

  val ENTRANCE_ORCHESTRATOR_DEFAULT_ID = "entranceOrchestrator"

  private lazy val orchestratorSession: OrchestratorSession = {
    val orchestratorSessionFactory = ComputationOrchestratorSessionFactory.getOrCreateExecutionFactory()
    val orchestratorSessionBuilder = orchestratorSessionFactory.createSessionBuilder(ENTRANCE_ORCHESTRATOR_DEFAULT_ID)
    val addOnOperation = new OperationExtensionsBuilder {
      override def apply(v1: OperationExtensions): Unit = {
        v1.injectOperation(new ProgressOperationBuilder())
      }
    }
    orchestratorSessionBuilder.withOperationExtensions(addOnOperation)
    val session = orchestratorSessionFactory.getOrCreateSession(orchestratorSessionBuilder)
    session.orchestrator.getOrchestratorContext match {
      case orchestratorContext: AbstractOrchestratorContext =>
        orchestratorContext.addGlobalPlugin(new EntranceUserParallelOrchestratorPlugin)
      case _ =>
    }
    session
  }

  def getOrchestrationSession(): OrchestratorSession = orchestratorSession


}
