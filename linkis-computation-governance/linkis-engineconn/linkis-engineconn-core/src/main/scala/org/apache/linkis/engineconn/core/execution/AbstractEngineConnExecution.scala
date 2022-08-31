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

package org.apache.linkis.engineconn.core.execution

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.common.execution.EngineConnExecution
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.executor.entity.Executor
import org.apache.linkis.manager.label.entity.engine.EngineConnMode._
import org.apache.linkis.manager.label.entity.engine.EngineConnModeLabel

import scala.collection.convert.decorateAsScala._

trait AbstractEngineConnExecution extends EngineConnExecution with Logging {

  protected def canExecute(engineCreationContext: EngineCreationContext): Boolean = {
    engineCreationContext.getLabels().asScala.exists {
      case engineConnModeLabel: EngineConnModeLabel =>
        val mode = toEngineConnMode(engineConnModeLabel.getEngineConnMode)
        logger.info(s"EngineConnMode is ${engineConnModeLabel.getEngineConnMode}.")
        getSupportedEngineConnModes.contains(mode)
      case _ => false
    }
  }

  protected def getSupportedEngineConnModes: Array[EngineConnMode]

  override def execute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {
    if (canExecute(engineCreationContext)) {
      logger.warn(s"${getClass.getName} is enabled, now step into it's execution.")
      doExecution(ExecutorManager.getInstance.getReportExecutor, engineCreationContext, engineConn)
    } else {
      logger.warn(s"${getClass.getName} is disabled, skip it's execution.")
    }
  }

  protected def doExecution(
      executor: Executor,
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit

  def returnAfterMeExecuted(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Boolean = {
    if (getReturnEngineConnModes.isEmpty) return false
    engineCreationContext.getLabels().asScala.exists {
      case engineConnModeLabel: EngineConnModeLabel =>
        val mode: EngineConnMode = engineConnModeLabel.getEngineConnMode
        getReturnEngineConnModes.contains(mode)
      case _ => false
    }
  }

  protected def getReturnEngineConnModes: Array[EngineConnMode] = Array.empty

}
