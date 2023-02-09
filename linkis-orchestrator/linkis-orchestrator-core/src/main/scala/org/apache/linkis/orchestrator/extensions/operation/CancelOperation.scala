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

package org.apache.linkis.orchestrator.extensions.operation

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.orchestrator.{Orchestration, OrchestratorSession}
import org.apache.linkis.orchestrator.core.AbstractOrchestration
import org.apache.linkis.orchestrator.extensions.operation.Operation.OperationBuilder
import org.apache.linkis.orchestrator.listener.OrchestratorListenerBusContext
import org.apache.linkis.orchestrator.listener.task.KillRootExecTaskEvent

/**
 */
class CancelOperation extends Operation[Unit] with Logging {

  override def apply(orchestration: Orchestration): Unit = orchestration match {
    case abstractOrchestration: AbstractOrchestration =>
      if (null != abstractOrchestration.physicalPlan) {
        logger.info(
          "Orchestrator to kill job {} post kill event",
          abstractOrchestration.physicalPlan.getIDInfo()
        )
        orchestration.orchestratorSession.getOrchestratorSessionState.getOrchestratorSyncListenerBus
          .postToAll(KillRootExecTaskEvent(abstractOrchestration.physicalPlan))
      }
    case _ =>
  }

  override def getName: String = {
    Operation.CANCEL
  }

}

class CancelOperationBuilder extends OperationBuilder {

  override def apply(v1: OrchestratorSession): Operation[_] = {
    new CancelOperation
  }

}
