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

package org.apache.linkis.orchestrator.extensions.catalyst

import org.apache.linkis.orchestrator.OrchestratorSession
import org.apache.linkis.orchestrator.domain.{JobReq, Node}
import org.apache.linkis.orchestrator.plans.PlanContext
import org.apache.linkis.orchestrator.plans.ast.{ASTContext, ASTOrchestration}

/**
 */
trait CheckRuler[In <: Node, Context <: PlanContext] {

  def apply(in: In, context: Context): Unit

  def getName: String

}

trait ConverterCheckRuler extends CheckRuler[JobReq, ASTContext]

trait ValidatorCheckRuler extends CheckRuler[ASTOrchestration[_], ASTContext]

object CheckRuler {

  // type ConverterCheckRuler = CheckRuler[JobReq, ASTContext]
  // type ValidatorCheckRuler = CheckRuler[ASTOrchestration[_], ASTContext]

  type ConverterCheckRulerBuilder = OrchestratorSession => ConverterCheckRuler
  type ValidatorCheckRulerBuilder = OrchestratorSession => ValidatorCheckRuler

}
