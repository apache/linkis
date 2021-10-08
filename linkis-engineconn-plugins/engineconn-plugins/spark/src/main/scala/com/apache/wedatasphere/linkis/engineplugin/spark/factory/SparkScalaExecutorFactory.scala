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

package com.apache.wedatasphere.linkis.engineplugin.spark.factory

import com.apache.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.apache.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.apache.wedatasphere.linkis.engineconn.computation.executor.creation.ComputationExecutorFactory
import com.apache.wedatasphere.linkis.engineconn.computation.executor.execute.ComputationExecutor
import com.apache.wedatasphere.linkis.engineplugin.spark.common.SparkKind
import com.apache.wedatasphere.linkis.engineplugin.spark.entity.SparkEngineSession
import com.apache.wedatasphere.linkis.engineplugin.spark.executor.SparkScalaExecutor
import com.apache.wedatasphere.linkis.manager.label.entity.Label
import com.apache.wedatasphere.linkis.manager.label.entity.engine.RunType
import com.apache.wedatasphere.linkis.manager.label.entity.engine.RunType.RunType

/**
  *
  */
class SparkScalaExecutorFactory extends ComputationExecutorFactory {

  override protected def getSupportRunTypes: Array[String] = Array(RunType.SCALA.toString,
    SparkKind.FUNCTION_MDQ_TYPE)

  override protected def newExecutor(id: Int,
                              engineCreationContext: EngineCreationContext,
                              engineConn: EngineConn,
                              labels: Array[Label[_]]): ComputationExecutor = {
    engineConn.getEngineConnSession match {
      case sparkEngineSession: SparkEngineSession =>
        new SparkScalaExecutor(sparkEngineSession, id)
    }
  }

  override protected def getRunType: RunType = RunType.SCALA
}
