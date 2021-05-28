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

package com.webank.wedatasphere.linkis.orchestrator

import java.io.Closeable

import com.webank.wedatasphere.linkis.orchestrator.core.SessionState
import com.webank.wedatasphere.linkis.orchestrator.domain.JobReq

/**
  *
  */
trait OrchestratorSession extends Closeable {

  val orchestrator: Orchestrator

  def initialize(conf: Map[String, Any]): Unit

  def isActive: Boolean

  def getOrchestratorSessionState: SessionState

  def cloneSession: OrchestratorSession

  def orchestrate(jobReq: JobReq): Orchestration

}