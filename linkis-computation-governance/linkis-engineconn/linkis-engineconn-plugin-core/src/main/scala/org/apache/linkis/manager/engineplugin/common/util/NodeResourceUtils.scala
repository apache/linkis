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

package org.apache.linkis.manager.engineplugin.common.util

import org.apache.linkis.manager.common.entity.resource.LoadInstanceResource
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf

import org.apache.commons.lang3.StringUtils

import java.util
import java.util.Objects

object NodeResourceUtils {

  def appendMemoryUnitIfMissing(properties: util.Map[String, String]): Unit = {
    Objects.requireNonNull(properties);

    if (properties.containsKey(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)) {
      val settingClientMemory =
        properties.get(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)
      if (StringUtils.isBlank(settingClientMemory)) {
        properties.remove(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)
      } else if (!settingClientMemory.toLowerCase().endsWith("g")) {
        properties.put(
          EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key,
          settingClientMemory + "g"
        )
      }
    }
  }

  def applyAsLoadInstanceResource(properties: util.Map[String, String]): LoadInstanceResource = {
    appendMemoryUnitIfMissing(properties)

    new LoadInstanceResource(
      EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.getValue(properties).toLong,
      EngineConnPluginConf.JAVA_ENGINE_REQUEST_CORES.getValue(properties),
      EngineConnPluginConf.JAVA_ENGINE_REQUEST_INSTANCE
    )
  }

}
