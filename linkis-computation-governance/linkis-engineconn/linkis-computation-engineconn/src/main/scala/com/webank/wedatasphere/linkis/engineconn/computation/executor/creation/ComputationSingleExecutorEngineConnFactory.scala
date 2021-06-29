/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.engineconn.computation.executor.creation

import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.ComputationExecutor
import com.webank.wedatasphere.linkis.engineconn.core.creation.{AbstractCodeLanguageLabelExecutorFactory, AbstractExecutorFactory}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.SingleLabelExecutorEngineConnFactory
import com.webank.wedatasphere.linkis.manager.label.entity.Label


trait ComputationSingleExecutorEngineConnFactory
  extends SingleLabelExecutorEngineConnFactory
    with AbstractCodeLanguageLabelExecutorFactory with AbstractExecutorFactory {

  protected override def newExecutor(id: Int,
                            engineCreationContext: EngineCreationContext,
                            engineConn: EngineConn,
                            labels: Array[Label[_]]): ComputationExecutor = null

  override def createExecutor(engineCreationContext: EngineCreationContext,
                              engineConn: EngineConn,
                              labels: Array[Label[_]]): ComputationExecutor =
    createExecutor(engineCreationContext, engineConn)


  override def createExecutor(engineCreationContext: EngineCreationContext,
                              engineConn: EngineConn): ComputationExecutor = {
    super.createExecutor(engineCreationContext, engineConn) match {
      case computationExecutor: ComputationExecutor =>
        computationExecutor.getExecutorLabels().add(getDefaultCodeLanguageLabel)
        computationExecutor
    }
  }

  override protected def createEngineConnSession(engineCreationContext: EngineCreationContext): Any = null

}
