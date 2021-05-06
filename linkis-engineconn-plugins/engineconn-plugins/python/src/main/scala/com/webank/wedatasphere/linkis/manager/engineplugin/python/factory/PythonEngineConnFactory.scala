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

package com.webank.wedatasphere.linkis.manager.engineplugin.python.factory

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.{DefaultEngineConn, EngineConn}
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineconn.executor.entity.Executor
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.SingleExecutorEngineConnFactory
import com.webank.wedatasphere.linkis.manager.engineplugin.python.conf.PythonEngineConfiguration
import com.webank.wedatasphere.linkis.manager.engineplugin.python.exception.PythonSessionStartFailedExeception
import com.webank.wedatasphere.linkis.manager.engineplugin.python.executor.{PythonEngineConnExecutor, PythonSession}
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineRunTypeLabel, EngineType, RunType}


class PythonEngineConnFactory extends SingleExecutorEngineConnFactory with Logging {

  private var engineCreationContext: EngineCreationContext = _

  override def createExecutor(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Executor = {
    engineConn.getEngine match {
      case pythonSession: PythonSession =>
        this.engineCreationContext = engineCreationContext
        val id = ExecutorManager.getInstance().generateId()
        val executor = new PythonEngineConnExecutor(id, pythonSession, PythonEngineConfiguration.PYTHON_CONSOLE_OUTPUT_LINE_LIMIT.getValue)
        executor.getExecutorLabels().add(getDefaultEngineRunTypeLabel())
        executor
      case _ =>
        throw PythonSessionStartFailedExeception("Invalid python session.")
    }
  }

  override def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn = {
    val pythonSession = new PythonSession
    Utils.tryAndWarn(pythonSession.init())
    val engineConn = new DefaultEngineConn(engineCreationContext)
    engineConn.setEngineType(EngineType.PYTHON.toString)
    engineConn.setEngine(pythonSession)
    engineConn
  }

  override def getDefaultEngineRunTypeLabel(): EngineRunTypeLabel = {
    val runTypeLabel = new EngineRunTypeLabel
    runTypeLabel.setRunType(RunType.PYTHON.toString)
    runTypeLabel
  }
}
