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

import org.apache.linkis.orchestrator.{Orchestrator, OrchestratorSession}
import org.apache.linkis.orchestrator.extensions.CatalystExtensions.CatalystExtensionsBuilder
import org.apache.linkis.orchestrator.extensions.CheckRulerExtensions.CheckRulerExtensionsBuilder
import org.apache.linkis.orchestrator.extensions.Extensions
import org.apache.linkis.orchestrator.extensions.OperationExtensions.OperationExtensionsBuilder

/**
 */
trait OrchestratorSessionBuilder {

  def setOrchestrator(orchestrator: Orchestrator): OrchestratorSessionBuilder

  def setId(id: String): OrchestratorSessionBuilder

  def getId(): String

  def config(key: String, value: Any): OrchestratorSessionBuilder

  def withExtensions(extensions: Extensions[_]): OrchestratorSessionBuilder

  def withCatalystExtensions(
      catalystExtensionsBuilder: CatalystExtensionsBuilder
  ): OrchestratorSessionBuilder

  def withCheckRulerExtensions(
      CheckRulerExtensionsBuilder: CheckRulerExtensionsBuilder
  ): OrchestratorSessionBuilder

  def withOperationExtensions(
      operationExtensionsBuilder: OperationExtensionsBuilder
  ): OrchestratorSessionBuilder

  def getOrCreate(): OrchestratorSession

}
