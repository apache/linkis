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

package com.apache.wedatasphere.linkis.manager.engineplugin.shell.factory

import com.apache.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.apache.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.apache.wedatasphere.linkis.engineconn.computation.executor.creation.ComputationSingleExecutorEngineConnFactory
import com.apache.wedatasphere.linkis.engineconn.executor.entity.LabelExecutor
import com.apache.wedatasphere.linkis.manager.engineplugin.shell.executor.ShellEngineConnExecutor
import com.apache.wedatasphere.linkis.manager.label.entity.engine.EngineType.EngineType
import com.apache.wedatasphere.linkis.manager.label.entity.engine.RunType.RunType
import com.apache.wedatasphere.linkis.manager.label.entity.engine.{EngineType, RunType}

class ShellEngineConnFactory extends ComputationSingleExecutorEngineConnFactory {

  override protected def newExecutor(id: Int,
                                     engineCreationContext: EngineCreationContext,
                                     engineConn: EngineConn): LabelExecutor =
    new ShellEngineConnExecutor(id)

  override protected def getEngineConnType: EngineType = EngineType.SHELL

  override protected def getRunType: RunType = RunType.SHELL
}
