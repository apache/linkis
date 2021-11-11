/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.orchestrator.execution

import org.apache.linkis.common.exception.FatalException
import org.apache.linkis.common.utils.{ClassUtils, Logging}
import org.apache.linkis.orchestrator.conf.OrchestratorConfiguration
import org.apache.linkis.orchestrator.core.SessionState
import org.apache.linkis.orchestrator.exception.OrchestratorErrorCodeSummary
import org.apache.commons.lang.StringUtils

/**
  *
  *
  */
trait ExecutionFactory {

  def createExecution(sessionState: SessionState): Execution

  protected def getTaskConsumer(sessionState: SessionState): TaskConsumer

  protected def getTaskManager(): TaskManager

  protected def getTaskScheduler(): TaskScheduler

}

object ExecutionFactory extends Logging {

  private var executionFactory: ExecutionFactory = _

  def getOrCreateExecutionFactory(): ExecutionFactory = {
    if (executionFactory == null) synchronized {
      if (executionFactory == null) {
        executionFactory = if (StringUtils.isNotBlank(OrchestratorConfiguration.ORCHESTRATOR_EXECUTION_FACTORY.getValue))
          ClassUtils.getClassInstance(OrchestratorConfiguration.ORCHESTRATOR_EXECUTION_FACTORY.getValue)
        else {
          throw new FatalException(OrchestratorErrorCodeSummary.EXECUTION_FATAL_CODE, s"Execution Factory class is null,please set ${OrchestratorConfiguration.ORCHESTRATOR_EXECUTION_FACTORY.key} ")
        }
      }
    }
    executionFactory
  }
}