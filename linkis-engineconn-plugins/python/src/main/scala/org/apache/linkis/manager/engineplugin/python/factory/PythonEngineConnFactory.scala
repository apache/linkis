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

package org.apache.linkis.manager.engineplugin.python.factory

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.{DefaultEngineConn, EngineConn}
import org.apache.linkis.engineconn.computation.executor.creation.ComputationSingleExecutorEngineConnFactory
import org.apache.linkis.engineconn.executor.entity.LabelExecutor
import org.apache.linkis.manager.engineplugin.python.conf.PythonEngineConfiguration
import org.apache.linkis.manager.engineplugin.python.errorcode.LinkisPythonErrorCodeSummary.INVALID_PYTHON_SESSION
import org.apache.linkis.manager.engineplugin.python.exception.PythonSessionStartFailedExeception
import org.apache.linkis.manager.engineplugin.python.executor.{
  PythonEngineConnExecutor,
  PythonSession
}
import org.apache.linkis.manager.label.entity.engine.{EngineType, RunType}
import org.apache.linkis.manager.label.entity.engine.EngineType.EngineType
import org.apache.linkis.manager.label.entity.engine.RunType.RunType

class PythonEngineConnFactory extends ComputationSingleExecutorEngineConnFactory with Logging {

  override def newExecutor(
      id: Int,
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): LabelExecutor = {
    engineConn.getEngineConnSession match {
      case pythonSession: PythonSession =>
        new PythonEngineConnExecutor(
          id,
          pythonSession,
          PythonEngineConfiguration.PYTHON_CONSOLE_OUTPUT_LINE_LIMIT.getValue
        )
      case _ =>
        throw new PythonSessionStartFailedExeception(INVALID_PYTHON_SESSION.getErrorDesc)
    }
  }

  override def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn = {
    val pythonSession = new PythonSession
    Utils.tryAndWarn(pythonSession.init())
    val engineConn = new DefaultEngineConn(engineCreationContext)
    engineConn.setEngineConnType(EngineType.PYTHON.toString)
    engineConn.setEngineConnSession(pythonSession)
    engineConn
  }

  override protected def getEngineConnType: EngineType = EngineType.PYTHON

  override protected def getRunType: RunType = RunType.PYTHON
}
