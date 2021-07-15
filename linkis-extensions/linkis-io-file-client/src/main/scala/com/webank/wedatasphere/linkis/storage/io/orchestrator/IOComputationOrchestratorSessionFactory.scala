/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.storage.io.orchestrator

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.orchestrator.computation.ComputationOrchestratorSessionFactory
import com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.CodeConverterTransform
import com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.converter.ruler.JobReqParamCheckRuler
import com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.parser.DefaultCodeJobParserTransform
import com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.physical.{CacheExecTaskTransform, CodeExecTaskTransform, JobExecTaskTransform, StageExecTaskTransform}
import com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.reheater.PruneTaskRetryTransform
import com.webank.wedatasphere.linkis.orchestrator.computation.catalyst.validator.DefaultLabelRegularCheckRuler
import com.webank.wedatasphere.linkis.orchestrator.core.OrchestratorSessionBuilder
import com.webank.wedatasphere.linkis.orchestrator.extensions.CatalystExtensions.CatalystExtensionsBuilder
import com.webank.wedatasphere.linkis.orchestrator.extensions.CheckRulerExtensions.CheckRulerExtensionsBuilder
import com.webank.wedatasphere.linkis.orchestrator.extensions.OperationExtensions.OperationExtensionsBuilder
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.CheckRuler.{ConverterCheckRulerBuilder, ValidatorCheckRulerBuilder}
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst.Transform._
import com.webank.wedatasphere.linkis.orchestrator.extensions.catalyst._
import com.webank.wedatasphere.linkis.orchestrator.extensions.operation.CancelOperationBuilder
import com.webank.wedatasphere.linkis.orchestrator.extensions.{CatalystExtensions, CheckRulerExtensions, OperationExtensions}
import com.webank.wedatasphere.linkis.orchestrator.{Orchestrator, OrchestratorSession}

class IOComputationOrchestratorSessionFactory extends ComputationOrchestratorSessionFactory with Logging{
  private val codeConverterTransformBuilder = new ConverterTransformBuilder() {
    override def apply(v1: OrchestratorSession): ConverterTransform = new CodeConverterTransform
  }


  private val codeStageParserTransformBuilder = new ParserTransformBuilder() {
    override def apply(v1: OrchestratorSession): ParserTransform = new DefaultCodeJobParserTransform
  }

  //planner builder definition
  private val taskPlannerTransformBuilder = new PlannerTransformBuilder() {
    override def apply(v1: OrchestratorSession): PlannerTransform = new IOTaskPlannerTransform
  }


  private val jobExecTaskTransformBuilder = new PhysicalTransformBuilder() {
    override def apply(v1: OrchestratorSession): PhysicalTransform = new JobExecTaskTransform()
  }

  private val stageExecTaskTransformBuilder = new PhysicalTransformBuilder() {
    override def apply(v1: OrchestratorSession): PhysicalTransform = new StageExecTaskTransform()
  }

  private val cacheExecTaskTransformBuilder = new PhysicalTransformBuilder() {
    override def apply(v1: OrchestratorSession): PhysicalTransform = new CacheExecTaskTransform()
  }

  private val codeExecTaskTransformBuilder = new PhysicalTransformBuilder() {
    override def apply(v1: OrchestratorSession): PhysicalTransform = new CodeExecTaskTransform()
  }

  private val PruneTaskRetryTransformBuilder = new ReheaterTransformBuilder() {
    override def apply(v1: OrchestratorSession): ReheaterTransform = new PruneTaskRetryTransform()
  }

  private val catalystExtensionsBuilder: CatalystExtensionsBuilder = new CatalystExtensionsBuilder(){
    override def apply(v1: CatalystExtensions): Unit = {

      v1.injectConverterTransform(codeConverterTransformBuilder)


      v1.injectParserTransform(codeStageParserTransformBuilder)

      v1.injectPlannerTransform(taskPlannerTransformBuilder)

      v1.injectPhysicalTransform(jobExecTaskTransformBuilder)
      v1.injectPhysicalTransform(stageExecTaskTransformBuilder)
      v1.injectPhysicalTransform(cacheExecTaskTransformBuilder)
      v1.injectPhysicalTransform(codeExecTaskTransformBuilder)

      v1.injectReheaterTransform(PruneTaskRetryTransformBuilder)
    }
  }

  //convertCheckRuler
  private val jobReqCheckRulerBuilder = new ConverterCheckRulerBuilder(){
    override def apply(v1: OrchestratorSession): ConverterCheckRuler = {
      new JobReqParamCheckRuler
    }
  }


  private val labelRegularCheckRulerBuilder = new ValidatorCheckRulerBuilder(){
    override def apply(v1: OrchestratorSession): ValidatorCheckRuler = new DefaultLabelRegularCheckRuler
  }

  private val checkRulerExtensionsBuilder: CheckRulerExtensionsBuilder = new CheckRulerExtensionsBuilder(){
    override def apply(v1: CheckRulerExtensions): Unit = {

      v1.injectConverterCheckRuler(jobReqCheckRulerBuilder)
      v1.injectValidatorCheckRuler(labelRegularCheckRulerBuilder)

    }
  }

  //Operation
  private val cancelOperationBuilder = new CancelOperationBuilder


  private val operationExtensionsBuilder: OperationExtensionsBuilder = new OperationExtensionsBuilder(){
    override def apply(v1: OperationExtensions): Unit = {
      v1.injectOperation(cancelOperationBuilder)
    }
  }

  private val orchestrator: Orchestrator = Orchestrator.getOrchestrator
  orchestrator.initialize()

  private val orchestratorSessionMap: util.Map[String, OrchestratorSessionBuilder]  = new util.HashMap[String, OrchestratorSessionBuilder]()

  override def getOrCreateSession(id: String): OrchestratorSession = {
    if (! orchestratorSessionMap.containsKey(id)) synchronized {
      if (! orchestratorSessionMap.containsKey(id)){
        orchestratorSessionMap.put(id, createSessionBuilder(id))
      }
    }
    orchestratorSessionMap.get(id).getOrCreate()
  }

  override def getOrCreateSession(orchestratorSessionBuilder: OrchestratorSessionBuilder): OrchestratorSession = {
    val id = orchestratorSessionBuilder.getId()
    if (! orchestratorSessionMap.containsKey(id)) synchronized {
      if (! orchestratorSessionMap.containsKey(id)){
        orchestratorSessionMap.put(id, orchestratorSessionBuilder)
      }
    }
    orchestratorSessionMap.get(id).getOrCreate()
  }

  override def createSessionBuilder(id: String): OrchestratorSessionBuilder = {
    val builder = orchestrator.createOrchestratorSessionBuilder()
    builder.setId(id)
    builder.withCatalystExtensions(catalystExtensionsBuilder)
    builder.withCheckRulerExtensions(checkRulerExtensionsBuilder)
    builder.withOperationExtensions(operationExtensionsBuilder)
    builder
  }

  override def getOrchestrator(): Orchestrator = this.orchestrator
}
