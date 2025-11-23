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

package org.apache.linkis.orchestrator

import org.apache.linkis.orchestrator.domain.AbstractJobReq
import org.apache.linkis.orchestrator.plans.unit.CodeLogicalUnit

/**
 */
object OrchestratorSuite extends App {

  private val orchestrator: Orchestrator = Orchestrator.getOrchestrator
  orchestrator.initialize()
  private val builder = orchestrator.createOrchestratorSessionBuilder()
  // builder.withExtensions()
  val orchestratorSession = builder.setId("test-Orchestrator").getOrCreate()
  orchestratorSession.initialize(Map.empty)

  val jobReq = new AbstractJobReq {
    override def getId: String = "0"
    override def getName: String = "testJobReq"
    val logicalUnit = CodeLogicalUnit("show tables", "sql")
  }

  val orchestration = orchestratorSession.orchestrate(jobReq)
  // scalastyle:off println
  println(orchestration.explain(true))
  orchestration.collectAndPrint()
  orchestratorSession.close()
  orchestrator.close()

}
