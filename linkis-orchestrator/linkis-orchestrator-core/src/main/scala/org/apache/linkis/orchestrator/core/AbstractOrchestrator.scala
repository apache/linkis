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

import org.apache.linkis.orchestrator.{Orchestrator, OrchestratorContext, OrchestratorSession}
import org.apache.linkis.orchestrator.conf.OrchestratorConfiguration

import java.util.concurrent.atomic.AtomicReference

import scala.collection.JavaConversions.mapAsScalaMap

/**
 */
abstract class AbstractOrchestrator extends Orchestrator {

  private val activeOrchestratorSession = new InheritableThreadLocal[OrchestratorSession]
  private val defaultOrchestratorSession = new AtomicReference[OrchestratorSession]
  private var orchestratorContext: OrchestratorContext = _

  protected def createOrchestratorContext(): OrchestratorContext

  override def initialize(): Unit = {
    orchestratorContext = createOrchestratorContext()
  }

  override def version: String = OrchestratorConfiguration.ORCHESTRATOR_VERSION.getValue

  override def getOrchestratorContext: OrchestratorContext = orchestratorContext

  override def getActiveOrchestratorSession: OrchestratorSession = activeOrchestratorSession.get

  override def setActiveOrchestratorSession(orchestratorSession: OrchestratorSession): Unit =
    activeOrchestratorSession.set(orchestratorSession)

  override def getDefaultOrchestratorSession: OrchestratorSession = defaultOrchestratorSession.get

  override def setDefaultOrchestratorSession(orchestratorSession: OrchestratorSession): Unit =
    defaultOrchestratorSession.set(orchestratorSession)

  override val getName: String = s"${getClass.getSimpleName}-$version"

  override def createOrchestratorSessionBuilder(): OrchestratorSessionBuilder = {
    val builder = newOrchestratorSessionBuilder().setOrchestrator(this)
    if (getOrchestratorContext.getGlobalExtensions != null) {
      getOrchestratorContext.getGlobalExtensions.foreach(builder.withExtensions)
    }
    if (getOrchestratorContext.getGlobalConfigs != null) {
      getOrchestratorContext.getGlobalConfigs.foreach { case (k, v) => builder.config(k, v) }
    }
    builder
  }

  protected def newOrchestratorSessionBuilder(): OrchestratorSessionBuilder

  override def close(): Unit = {
    if (defaultOrchestratorSession.get() != null) defaultOrchestratorSession.get.close()
    getOrchestratorContext.close()
  }

}
