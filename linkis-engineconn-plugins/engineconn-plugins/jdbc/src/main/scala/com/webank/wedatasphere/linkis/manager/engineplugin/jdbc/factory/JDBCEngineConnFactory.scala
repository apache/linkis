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

package com.webank.wedatasphere.linkis.manager.engineplugin.jdbc.factory

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.computation.executor.creation.ComputationSingleExecutorEngineConnFactory
import com.webank.wedatasphere.linkis.engineconn.executor.entity.LabelExecutor
import com.webank.wedatasphere.linkis.manager.engineplugin.jdbc.conf.JDBCConfiguration
import com.webank.wedatasphere.linkis.manager.engineplugin.jdbc.executer.JDBCEngineConnExecutor
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineType, RunType}
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineType.EngineType
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType.RunType

class JDBCEngineConnFactory extends ComputationSingleExecutorEngineConnFactory with Logging{

  override def newExecutor(id: Int, engineCreationContext: EngineCreationContext, engineConn: EngineConn): LabelExecutor = {
    new JDBCEngineConnExecutor(JDBCConfiguration.ENGINE_DEFAULT_LIMIT.getValue, id)
  }

  override protected def getEngineConnType: EngineType = EngineType.JDBC

  override protected def getRunType: RunType = RunType.JDBC
}
