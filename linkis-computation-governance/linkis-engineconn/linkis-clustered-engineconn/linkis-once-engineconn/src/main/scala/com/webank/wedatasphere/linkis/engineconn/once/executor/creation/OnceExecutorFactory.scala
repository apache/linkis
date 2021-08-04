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

package com.webank.wedatasphere.linkis.engineconn.once.executor.creation


import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.core.creation.AbstractCodeLanguageLabelExecutorFactory
import com.webank.wedatasphere.linkis.engineconn.once.executor.OnceExecutor
import com.webank.wedatasphere.linkis.engineconn.once.executor.execution.OnceEngineConnExecution
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineConnMode._
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineConnModeLabel


trait OnceExecutorFactory extends AbstractCodeLanguageLabelExecutorFactory {

  override protected def newExecutor(id: Int,
                                     engineCreationContext: EngineCreationContext,
                                     engineConn: EngineConn,
                                     labels: Array[Label[_]]): OnceExecutor

  override def canCreate(labels: Array[Label[_]]): Boolean =
    super.canCreate(labels) && labels.exists {
      case engineConnModeLabel: EngineConnModeLabel =>
        val mode: EngineConnMode = engineConnModeLabel.getEngineConnMode
        OnceEngineConnExecution.getSupportedEngineConnModes.contains(mode)
    case _ => false
  }

}
