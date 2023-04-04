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
import java.util.{Locale, Objects}
import java.util.regex.Pattern

import org.slf4j.LoggerFactory

object NodeResourceUtils {

  private var LOG = LoggerFactory.getLogger(getClass)

  val JAVA_MEMORY_UNIT_G = "G"
  val JAVA_MEMORY_UNIT_M = "M"
  val JAVA_MEMORY_UNIT_K = "K"
  val JAVA_MEMORY_UNIT_B = "B"

  var JAVA_MEMORY_REGEX = "^[1-9]\\d*[gGmMkK]?[bB]?$"

  def appendMemoryUnitIfMissing(properties: util.Map[String, String]): Unit = {
    Objects.requireNonNull(properties);

    if (properties.containsKey(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)) {
      val settingClientMemory =
        properties.get(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)
      if (StringUtils.isBlank(settingClientMemory)) {
        properties.remove(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)
      } else {
        properties.put(
          EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key,
          formatJavaOptionMemoryWithDefaultUnitG(settingClientMemory)
        )
      }
    }
  }

  def formatJavaOptionMemory(memory: String, defaultUnit: String): String = {
    assert(StringUtils.isNotBlank(memory))
    val memoryTmp = memory.toUpperCase(Locale.getDefault).trim()
    if (!Pattern.matches(JAVA_MEMORY_REGEX, memoryTmp)) {
      LOG.error("the java option memory: '{}' format error, use default 1G replaced", memory)
      1 + JAVA_MEMORY_UNIT_G
    } else if (
        memoryTmp.endsWith(JAVA_MEMORY_UNIT_G) || memoryTmp.endsWith(
          JAVA_MEMORY_UNIT_M
        ) || memoryTmp.endsWith(JAVA_MEMORY_UNIT_K)
    ) {
      memoryTmp
    } else if (memoryTmp.endsWith(JAVA_MEMORY_UNIT_B)) {
      memoryTmp.substring(0, memoryTmp.length - 1)
    } else {
      memoryTmp + defaultUnit
    }
  }

  def formatJavaOptionMemoryWithDefaultUnitG(memory: String): String = {
    formatJavaOptionMemory(memory, JAVA_MEMORY_UNIT_G)
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
