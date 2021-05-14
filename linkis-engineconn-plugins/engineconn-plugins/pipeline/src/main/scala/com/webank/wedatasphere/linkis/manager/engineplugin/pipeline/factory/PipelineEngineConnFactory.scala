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

package com.webank.wedatasphere.linkis.manager.engineplugin.pipeline.factory

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.{DefaultEngineConn, EngineConn}
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineconn.executor.entity.Executor
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.SingleExecutorEngineConnFactory
import com.webank.wedatasphere.linkis.manager.engineplugin.pipeline.executor.PipelineEngineConnExecutor
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineRunTypeLabel, EngineType, RunType}

class PipelineEngineConnFactory extends SingleExecutorEngineConnFactory with Logging {

  override def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn = {
    val engineConn = new DefaultEngineConn(engineCreationContext)
    engineConn.setEngineType(EngineType.PIPELINE.toString)
    engineConn
  }
  override def createExecutor(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Executor = {

    val id = ExecutorManager.getInstance().generateId()
    val executor = new PipelineEngineConnExecutor(id)
    val runTypeLabel = getDefaultEngineRunTypeLabel()
    executor.getExecutorLabels().add(runTypeLabel)
    executor
  }

  override def getDefaultEngineRunTypeLabel(): EngineRunTypeLabel = {
    val runTypeLabel = new EngineRunTypeLabel
    runTypeLabel.setRunType(RunType.PIPELINE.toString)
    runTypeLabel
  }

}
