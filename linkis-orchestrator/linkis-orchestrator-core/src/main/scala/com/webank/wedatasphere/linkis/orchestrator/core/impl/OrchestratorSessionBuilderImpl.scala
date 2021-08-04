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

package com.webank.wedatasphere.linkis.orchestrator.core.impl

import com.webank.wedatasphere.linkis.orchestrator.OrchestratorSession
import com.webank.wedatasphere.linkis.orchestrator.core.{AbstractOrchestratorSessionBuilder, SessionState}
import com.webank.wedatasphere.linkis.orchestrator.extensions.Extensions
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.{CheckRuler, Transform}
import com.webank.wedatasphere.linkis.orchestrator.extensions.operation.Operation

/**
  *
  */
class OrchestratorSessionBuilderImpl extends AbstractOrchestratorSessionBuilder {

  override def createOrchestratorSession(createSessionState: OrchestratorSession => SessionState): OrchestratorSession = {
    val orchestratorSession = new OrchestratorSessionImpl(orchestrator, createSessionState)
    orchestratorSession
  }

  override protected def createSessionState(orchestratorSession: OrchestratorSession,
                                            transforms: Array[Transform[_, _, _]],
                                            checkRulers: Array[CheckRuler[_, _]],
                                            operations: Array[Operation[_]],
                                            extractExtensions: Array[Extensions[_]]): SessionState =
    new SessionStateImpl(orchestratorSession, transforms, checkRulers, operations, extractExtensions)

}
