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
package com.webank.wedatasphere.linkis.engineconnplugin.flink.launch

import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration._
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkResourceConfiguration
import com.webank.wedatasphere.linkis.manager.engineplugin.common.conf.EnvConfiguration
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.process.JavaProcessEngineConnLaunchBuilder


class FlinkEngineConnLaunchBuilder extends JavaProcessEngineConnLaunchBuilder {

  override protected def getCommands(implicit engineConnBuildRequest: EngineConnBuildRequest): Array[String] = {
    val properties = engineConnBuildRequest.engineConnCreationDesc.properties
    properties.put(EnvConfiguration.ENGINE_CONN_MEMORY.key, FlinkResourceConfiguration.LINKIS_FLINK_CLIENT_MEMORY.getValue(properties) + "G")
    super.getCommands
  }

  override protected def getNecessaryEnvironment(implicit engineConnBuildRequest: EngineConnBuildRequest): Array[String] =
    Array(FLINK_HOME_ENV, FLINK_CONF_DIR_ENV) ++: super.getNecessaryEnvironment

  override protected def ifAddHiveConfigPath: Boolean = true

}
