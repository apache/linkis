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

package org.apache.linkis.engineplugin.spark.factory

import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.computation.executor.creation.ComputationExecutorFactory
import org.apache.linkis.engineconn.computation.executor.execute.ComputationExecutor
import org.apache.linkis.engineplugin.spark.entity.SparkEngineSession
import org.apache.linkis.engineplugin.spark.exception.NotSupportSparkDataCalcTypeException
import org.apache.linkis.engineplugin.spark.executor.SparkDataCalcExecutor
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.RunType
import org.apache.linkis.manager.label.entity.engine.RunType.RunType

class SparkDataCalcExecutorFactory extends ComputationExecutorFactory {

  override protected def newExecutor(
      id: Int,
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn,
      label: Array[Label[_]]
  ): ComputationExecutor = {
    engineConn.getEngineConnSession match {
      case sparkEngineSession: SparkEngineSession =>
        new SparkDataCalcExecutor(sparkEngineSession, id)
      case _ =>
        throw NotSupportSparkDataCalcTypeException(
          "Invalid EngineConn engine session obj, failed to create sparkSql executor"
        )
    }
  }

  override protected def getRunType: RunType = RunType.DATA_CALC
}
