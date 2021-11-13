/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.engineconn.computation.executor.execute

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.common.execution.EngineConnExecution
import org.apache.linkis.engineconn.core.execution.AbstractEngineConnExecution
import org.apache.linkis.engineconn.executor.conf.EngineConnExecutorConfiguration
import org.apache.linkis.engineconn.executor.entity.Executor
import org.apache.linkis.manager.label.entity.engine.EngineConnMode._
import org.apache.linkis.manager.label.entity.engine.EngineConnModeLabel


class ComputationEngineConnExecution extends AbstractEngineConnExecution {

  override protected def getSupportedEngineConnModes: Array[EngineConnMode] =
    ComputationEngineConnExecution.getSupportedEngineConnModes

  override protected def doExecution(executor: Executor,
                                     engineCreationContext: EngineCreationContext,
                                     engineConn: EngineConn): Unit = {
    info(s"Executor(${executor.getId}) is started. Now wait to be ready.")
  }

  /**
    * Usually, computation is in final.
    *
    * @return
    */
  override def getOrder: Int = 200
}

object ComputationEngineConnExecution {

  def getSupportedEngineConnModes: Array[EngineConnMode] = Array(Computation, Computation_With_Once)

}

import scala.collection.convert.decorateAsScala._
class ComputationExecutorManagerEngineConnExecution extends EngineConnExecution {
  override def execute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {
    var shouldSet = true
    engineCreationContext.getLabels().asScala.foreach {
      case engineConnModeLabel: EngineConnModeLabel =>
        val mode = toEngineConnMode(engineConnModeLabel.getEngineConnMode)
        shouldSet = ComputationEngineConnExecution.getSupportedEngineConnModes.contains(mode)
      case _ =>
    }
    if(shouldSet) DataWorkCloudApplication.setProperty(EngineConnExecutorConfiguration.EXECUTOR_MANAGER_CLASS.key,
      "org.apache.linkis.engineconn.computation.executor.creation.ComputationExecutorManagerImpl")
}

  /**
    * The smallest got the first execution opportunity.
    *
    * @return
    */
  override def getOrder: Int = 5
}