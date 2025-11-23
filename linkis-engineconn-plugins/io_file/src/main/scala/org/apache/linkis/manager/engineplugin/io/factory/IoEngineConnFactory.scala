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

package org.apache.linkis.manager.engineplugin.io.factory

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.computation.executor.creation.ComputationSingleExecutorEngineConnFactory
import org.apache.linkis.engineconn.executor.entity.LabelExecutor
import org.apache.linkis.manager.engineplugin.io.conf.IOEngineConnConfiguration
import org.apache.linkis.manager.engineplugin.io.executor.IoEngineConnExecutor
import org.apache.linkis.manager.label.entity.engine.{EngineType, EngineTypeLabel, RunType}
import org.apache.linkis.manager.label.entity.engine.EngineType.EngineType
import org.apache.linkis.manager.label.entity.engine.RunType.RunType

import org.apache.commons.lang3.StringUtils

class IoEngineConnFactory extends ComputationSingleExecutorEngineConnFactory with Logging {

  override protected def getEngineConnType: EngineType = EngineType.IO_ENGINE_FILE

  override protected def getRunType: RunType = RunType.IO_FILE

  override def newExecutor(
      id: Int,
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): LabelExecutor = {
    new IoEngineConnExecutor(id, IOEngineConnConfiguration.OUTPUT_LIMIT.getValue)
  }

  private def engineVersionMatch(
      initalLabel: EngineTypeLabel,
      engineCreationLabel: EngineTypeLabel
  ): Boolean = {
    if (StringUtils.isBlank(initalLabel.getVersion)) {
      true
    } else if (
        initalLabel.getVersion
          .equals("*") || initalLabel.getVersion.equalsIgnoreCase(engineCreationLabel.getVersion)
    ) {
      true
    } else {
      false
    }
  }

}
