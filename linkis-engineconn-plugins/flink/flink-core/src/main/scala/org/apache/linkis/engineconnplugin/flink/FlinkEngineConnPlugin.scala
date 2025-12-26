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

package org.apache.linkis.engineconnplugin.flink

import org.apache.linkis.engineconnplugin.flink.factory.FlinkEngineConnFactory
import org.apache.linkis.engineconnplugin.flink.launch.FlinkEngineConnLaunchBuilder
import org.apache.linkis.engineconnplugin.flink.resource.FlinkEngineConnResourceFactory
import org.apache.linkis.manager.engineplugin.common.EngineConnPlugin
import org.apache.linkis.manager.engineplugin.common.creation.EngineConnFactory
import org.apache.linkis.manager.engineplugin.common.launch.EngineConnLaunchBuilder
import org.apache.linkis.manager.engineplugin.common.resource.EngineResourceFactory
import org.apache.linkis.manager.label.entity.Label

class FlinkEngineConnPlugin extends EngineConnPlugin {

  private var engineResourceFactory: EngineResourceFactory = _

  private var engineConnFactory: EngineConnFactory = _

  private val resourceLocker = new Array[Byte](0)

  private val engineFactoryLocker = new Array[Byte](0)

  override def init(params: java.util.Map[String, AnyRef]): Unit = {}

  override def getEngineResourceFactory: EngineResourceFactory = {
    if (null == engineResourceFactory) resourceLocker.synchronized {
      if (null == engineResourceFactory) {
        engineResourceFactory = new FlinkEngineConnResourceFactory
      }
    }
    engineResourceFactory
  }

  override def getEngineConnLaunchBuilder: EngineConnLaunchBuilder = {
    new FlinkEngineConnLaunchBuilder()
  }

  override def getEngineConnFactory: EngineConnFactory = {
    if (null == engineConnFactory) engineFactoryLocker.synchronized {
      if (null == engineConnFactory) {
        engineConnFactory = new FlinkEngineConnFactory
      }
    }
    engineConnFactory
  }

  override def getDefaultLabels: java.util.List[Label[_]] = new java.util.ArrayList[Label[_]]
}
