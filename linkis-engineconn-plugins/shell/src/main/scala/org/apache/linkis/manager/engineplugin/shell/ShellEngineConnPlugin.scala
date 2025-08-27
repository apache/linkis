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

package org.apache.linkis.manager.engineplugin.shell

import org.apache.linkis.manager.engineplugin.common.EngineConnPlugin
import org.apache.linkis.manager.engineplugin.common.creation.EngineConnFactory
import org.apache.linkis.manager.engineplugin.common.launch.EngineConnLaunchBuilder
import org.apache.linkis.manager.engineplugin.common.resource.{
  EngineResourceFactory,
  GenericEngineResourceFactory
}
import org.apache.linkis.manager.engineplugin.shell.builder.ShellProcessEngineConnLaunchBuilder
import org.apache.linkis.manager.engineplugin.shell.factory.ShellEngineConnFactory
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.EngineType
import org.apache.linkis.manager.label.utils.EngineTypeLabelCreator

import java.util

class ShellEngineConnPlugin extends EngineConnPlugin {

  private val resourceLocker = new Object()

  private val engineLaunchBuilderLocker = new Object()

  private val engineFactoryLocker = new Object()

  private var engineResourceFactory: EngineResourceFactory = _

  private var engineLaunchBuilder: EngineConnLaunchBuilder = _

  private var engineFactory: EngineConnFactory = _

  private val defaultLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]()

  override def init(params: util.Map[String, AnyRef]): Unit = {
    val engineTypeLabel = EngineTypeLabelCreator.createEngineTypeLabel(EngineType.SHELL.toString)
    this.defaultLabels.add(engineTypeLabel)
  }

  override def getEngineResourceFactory: EngineResourceFactory = {
    if (null == engineResourceFactory) resourceLocker synchronized {
      engineResourceFactory = new GenericEngineResourceFactory
    }
    engineResourceFactory
  }

  override def getEngineConnLaunchBuilder: EngineConnLaunchBuilder = {
    new ShellProcessEngineConnLaunchBuilder
  }

  override def getEngineConnFactory: EngineConnFactory = {
    if (null == engineFactory) engineFactoryLocker synchronized {
      engineFactory = new ShellEngineConnFactory
    }
    engineFactory
  }

  override def getDefaultLabels: util.List[Label[_]] = this.defaultLabels

}
