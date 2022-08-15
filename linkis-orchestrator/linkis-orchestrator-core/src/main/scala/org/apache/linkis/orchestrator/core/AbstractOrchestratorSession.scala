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

import org.apache.linkis.orchestrator.{Orchestration, Orchestrator, OrchestratorSession}
import org.apache.linkis.orchestrator.domain.JobReq
import org.apache.linkis.orchestrator.plans.ast.Job

/**
 */
abstract class AbstractOrchestratorSession(
    override val orchestrator: Orchestrator,
    sessionStateCreator: OrchestratorSession => SessionState
) extends OrchestratorSession {

  private var isInitialized = false

  private val sessionState: SessionState = sessionStateCreator(this)

  override def initialize(conf: Map[String, Any]): Unit = {
    conf.foreach {
      case (k, v: String) => getOrchestratorSessionState.setStringConf(k, v)
      case (k, v) if v != null => getOrchestratorSessionState.setStringConf(k, v.toString)
      case _ =>
    }
    isInitialized = true
  }

  override def isActive: Boolean = isInitialized && orchestrator.getOrchestratorContext.isActive

  override def getOrchestratorSessionState: SessionState = sessionState

  override def orchestrate(jobReq: JobReq): Orchestration = {
    val job = getOrchestratorSessionState.getConverter.convert(jobReq)
    createOrchestration(job)
  }

  protected def createOrchestration(job: Job): Orchestration

  override def close(): Unit = orchestrator.close()
}
