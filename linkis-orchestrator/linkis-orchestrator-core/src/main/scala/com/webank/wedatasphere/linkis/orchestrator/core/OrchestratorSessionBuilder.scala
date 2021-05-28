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

package com.webank.wedatasphere.linkis.orchestrator.core

import com.webank.wedatasphere.linkis.orchestrator.extensions.CatalystExtensions.CatalystExtensionsBuilder
import com.webank.wedatasphere.linkis.orchestrator.extensions.CheckRulerExtensions.CheckRulerExtensionsBuilder
import com.webank.wedatasphere.linkis.orchestrator.extensions.Extensions
import com.webank.wedatasphere.linkis.orchestrator.extensions.OperationExtensions.OperationExtensionsBuilder
import com.webank.wedatasphere.linkis.orchestrator.{Orchestrator, OrchestratorSession}

/**
  *
  */
trait OrchestratorSessionBuilder {

  def setOrchestrator(orchestrator: Orchestrator): OrchestratorSessionBuilder

  def setId(id: String): OrchestratorSessionBuilder

  def getId(): String

  def config(key: String, value: Any): OrchestratorSessionBuilder


  def withExtensions(extensions: Extensions[_]): OrchestratorSessionBuilder

  def withCatalystExtensions(catalystExtensionsBuilder: CatalystExtensionsBuilder): OrchestratorSessionBuilder

  def withCheckRulerExtensions(CheckRulerExtensionsBuilder: CheckRulerExtensionsBuilder): OrchestratorSessionBuilder

  def withOperationExtensions(operationExtensionsBuilder: OperationExtensionsBuilder): OrchestratorSessionBuilder

  def getOrCreate(): OrchestratorSession

}