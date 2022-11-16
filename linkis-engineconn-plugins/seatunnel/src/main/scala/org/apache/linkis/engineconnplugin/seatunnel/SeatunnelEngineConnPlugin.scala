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

package org.apache.linkis.engineconnplugin.seatunnel

import org.apache.linkis.engineconnplugin.seatunnel.factory.SeatunnelEngineConnFactory
import org.apache.linkis.engineconnplugin.seatunnel.launch.SeatunnelEngineConnLaunchBuilder
import org.apache.linkis.manager.engineplugin.common.EngineConnPlugin
import org.apache.linkis.manager.engineplugin.common.creation.EngineConnFactory
import org.apache.linkis.manager.engineplugin.common.launch.EngineConnLaunchBuilder
import org.apache.linkis.manager.engineplugin.common.resource.{
  EngineResourceFactory,
  GenericEngineResourceFactory
}
import org.apache.linkis.manager.label.entity.Label

class SeatunnelEngineConnPlugin extends EngineConnPlugin {
  private val resourceLocker = new Object()

  private val engineLaunchBuilderLocker = new Object()

  private val engineFactoryLocker = new Object()

  private var engineResourceFactory: EngineResourceFactory = _
  private var engineConnLaunchBuilder: EngineConnLaunchBuilder = _
  private var engineConnFactory: EngineConnFactory = _
  override def init(params: java.util.Map[String, Any]): Unit = {}

  override def getEngineResourceFactory: EngineResourceFactory = {

    resourceLocker.synchronized {
      if (null == engineResourceFactory) {
        engineResourceFactory = new GenericEngineResourceFactory
      }
      engineResourceFactory
    }
  }

  override def getEngineConnLaunchBuilder: EngineConnLaunchBuilder = {
    engineLaunchBuilderLocker.synchronized {
      if (null == engineConnLaunchBuilder) {
        engineConnLaunchBuilder = new SeatunnelEngineConnLaunchBuilder()
      }
      engineConnLaunchBuilder
    }
  }

  override def getEngineConnFactory: EngineConnFactory = {
    engineFactoryLocker.synchronized {
      if (null == engineConnFactory) {
        engineConnFactory = new SeatunnelEngineConnFactory
      }
      engineConnFactory
    }
  }

  override def getDefaultLabels: java.util.List[Label[_]] = new java.util.ArrayList[Label[_]]
}
