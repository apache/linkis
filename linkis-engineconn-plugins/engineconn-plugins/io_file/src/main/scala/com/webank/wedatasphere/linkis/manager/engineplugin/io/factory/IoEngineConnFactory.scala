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

package com.webank.wedatasphere.linkis.manager.engineplugin.io.factory

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.{DefaultEngineConn, EngineConn}
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineconn.executor.entity.Executor
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.SingleExecutorEngineConnFactory
import com.webank.wedatasphere.linkis.manager.engineplugin.io.conf.IOEngineConnConfiguration
import com.webank.wedatasphere.linkis.manager.engineplugin.io.executor.IoEngineConnExecutor
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineRunTypeLabel, EngineType, EngineTypeLabel, RunType}
import org.apache.commons.lang.StringUtils

class IoEngineConnFactory extends SingleExecutorEngineConnFactory with Logging {

  private var engineCreationContext: EngineCreationContext = _

  override def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn = {
    val engineConn = new DefaultEngineConn(engineCreationContext)
    engineConn.setEngineType(EngineType.IO_ENGINE_FILE.toString)
    engineConn
  }

  override def createExecutor(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Executor = {
    this.engineCreationContext = engineCreationContext

    val id = ExecutorManager.getInstance().generateId()

    val executor = new IoEngineConnExecutor(id, IOEngineConnConfiguration.OUTPUT_LIMIT.getValue)

    val runTypeLabel = getDefaultEngineRunTypeLabel()
    executor.getExecutorLabels().add(runTypeLabel)
    executor
  }

  private def engineVersionMatch(initalLabel: EngineTypeLabel, engineCreationLabel: EngineTypeLabel): Boolean = {
    if (StringUtils.isBlank(initalLabel.getVersion)) {
      true
    } else if (initalLabel.getVersion.equals("*") || initalLabel.getVersion.equalsIgnoreCase(engineCreationLabel.getVersion)) {
      true
    } else {
      false
    }
  }

  override def getDefaultEngineRunTypeLabel(): EngineRunTypeLabel = {
    val runTypeLabel = new EngineRunTypeLabel
    runTypeLabel.setRunType(RunType.IO_FILE.toString)
    runTypeLabel
  }

}
