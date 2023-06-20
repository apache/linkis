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

package org.apache.linkis.engineconnplugin.seatunnel.factory

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconnplugin.seatunnel.context.SeatunnelEngineConnContext
import org.apache.linkis.manager.engineplugin.common.creation.{
  ExecutorFactory,
  MultiExecutorEngineConnFactory
}
import org.apache.linkis.manager.label.entity.engine.EngineType
import org.apache.linkis.manager.label.entity.engine.EngineType.EngineType

class SeatunnelEngineConnFactory extends MultiExecutorEngineConnFactory with Logging {

  override def getExecutorFactories: Array[ExecutorFactory] = executorFactoryArray

  override protected def getDefaultExecutorFactoryClass: Class[_ <: ExecutorFactory] =
    classOf[SeatunnelSparkExecutorFactory]

  override protected def getEngineConnType: EngineType = EngineType.SEATUNNEL

  private val executorFactoryArray =
    Array[ExecutorFactory](
      new SeatunnelSparkExecutorFactory,
      new SeatunnelFlinkExecutorFactory,
      new SeatunnelZetaExecutorFactory
    )

  override protected def createEngineConnSession(
      engineCreationContext: EngineCreationContext
  ): Any = {
    val seatunnelEngineConnContext = new SeatunnelEngineConnContext()
    seatunnelEngineConnContext
  }

}
