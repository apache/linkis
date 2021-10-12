/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engineconnplugin.flink.factory

import com.webank.wedatasphere.linkis.engineconn.common.conf.EngineConnConf
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.computation.executor.creation.ComputationExecutorFactory
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.ComputationExecutor
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.executor.FlinkSQLComputationExecutor
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType
import com.webank.wedatasphere.linkis.manager.label.entity.engine.RunType.RunType
import org.apache.flink.yarn.configuration.YarnConfigOptions


class FlinkSQLExecutorFactory extends ComputationExecutorFactory {

  override protected def newExecutor(id: Int,
                           engineCreationContext: EngineCreationContext,
                           engineConn: EngineConn,
                           labels: Array[Label[_]]): ComputationExecutor = engineConn.getEngineConnSession match {
    case context: FlinkEngineConnContext =>
      context.getEnvironmentContext.getFlinkConfig.set(YarnConfigOptions.PROPERTIES_FILE_LOCATION, EngineConnConf.getWorkHome)
      val executor = new FlinkSQLComputationExecutor(id, context)
      executor
  }



  override protected def getRunType: RunType = RunType.SQL
}
