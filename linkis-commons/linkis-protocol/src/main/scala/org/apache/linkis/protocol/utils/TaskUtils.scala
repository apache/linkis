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

package org.apache.linkis.protocol.utils

import org.apache.linkis.protocol.constants.TaskConstant

import java.util

import scala.collection.JavaConverters._

object TaskUtils {

  def getMap(params: util.Map[String, AnyRef], key: String): util.Map[String, AnyRef] =
    if (params != null && params.containsKey(key)) {
      params.get(key) match {
        case map: util.Map[String, AnyRef] => map
        case _ => new util.HashMap[String, AnyRef]()
      }
    } else new util.HashMap[String, AnyRef]()

  private def addMap(
      params: util.Map[String, AnyRef],
      waitToAdd: util.Map[String, AnyRef],
      key: String
  ): Unit =
    if (params != null && params.containsKey(key)) {
      params.get(key) match {
        case map: util.Map[String, AnyRef] => map.putAll(waitToAdd)
        case _ => params.put(key, waitToAdd)
      }
    } else params.put(key, waitToAdd)

  private def clearMap(params: util.Map[String, AnyRef], key: String): Unit =
    if (params != null && params.containsKey(key)) {
      params.get(key) match {
        case map: util.Map[String, AnyRef] => map.clear()
        case _ => params.put(key, new util.HashMap[String, AnyRef]())
      }
    }

  private def getConfigurationMap(
      params: util.Map[String, AnyRef],
      key: String
  ): util.Map[String, AnyRef] = {
    val configurationMap = getMap(params, TaskConstant.PARAMS_CONFIGURATION)
    getMap(configurationMap, key)
  }

  def addConfigurationMap(
      params: util.Map[String, AnyRef],
      waitToAdd: util.Map[String, AnyRef],
      key: String
  ): Unit = {
    val configurationMap = getMap(params, TaskConstant.PARAMS_CONFIGURATION)
    if (configurationMap.isEmpty) params.put(TaskConstant.PARAMS_CONFIGURATION, configurationMap)
    addMap(configurationMap, waitToAdd, key)
  }

  def getVariableMap(params: util.Map[String, AnyRef]): util.Map[String, AnyRef] =
    getMap(params, TaskConstant.PARAMS_VARIABLE)

  def getStartupMap(params: util.Map[String, AnyRef]): util.Map[String, AnyRef] =
    getConfigurationMap(params, TaskConstant.PARAMS_CONFIGURATION_STARTUP)

  def getRuntimeMap(params: util.Map[String, AnyRef]): util.Map[String, AnyRef] =
    getConfigurationMap(params, TaskConstant.PARAMS_CONFIGURATION_RUNTIME)

  def getSpecialMap(params: util.Map[String, AnyRef]): util.Map[String, AnyRef] =
    getConfigurationMap(params, TaskConstant.PARAMS_CONFIGURATION_SPECIAL)

  def addVariableMap(
      params: util.Map[String, AnyRef],
      variableMap: util.Map[String, AnyRef]
  ): Unit =
    addMap(params, variableMap, TaskConstant.PARAMS_VARIABLE)

  def addStartupMap(params: util.Map[String, AnyRef], startupMap: util.Map[String, AnyRef]): Unit =
    addConfigurationMap(params, startupMap, TaskConstant.PARAMS_CONFIGURATION_STARTUP)

  def clearStartupMap(params: util.Map[String, AnyRef]): Unit = {
    val configurationMap = getMap(params, TaskConstant.PARAMS_CONFIGURATION)
    if (!configurationMap.isEmpty) {
      clearMap(configurationMap, TaskConstant.PARAMS_CONFIGURATION_STARTUP)
    }
  }

  def addRuntimeMap(params: util.Map[String, AnyRef], runtimeMap: util.Map[String, AnyRef]): Unit =
    addConfigurationMap(params, runtimeMap, TaskConstant.PARAMS_CONFIGURATION_RUNTIME)

  def addSpecialMap(params: util.Map[String, AnyRef], specialMap: util.Map[String, AnyRef]): Unit =
    addConfigurationMap(params, specialMap, TaskConstant.PARAMS_CONFIGURATION_SPECIAL)

  // todo
  def getLabelsMap(params: util.Map[String, AnyRef]): util.Map[String, AnyRef] =
    getMap(params, TaskConstant.LABELS)

  def addLabelsMap(params: util.Map[String, AnyRef], labels: util.Map[String, AnyRef]): Unit =
    addMap(params, labels, TaskConstant.LABELS)

  def isWithDebugInfo(params: util.Map[String, AnyRef]): Boolean = {
    val debug = getConfigurationMap(params, TaskConstant.PARAMS_CONFIGURATION_STARTUP).get(
      TaskConstant.DEBUG_ENBALE
    )
    if (debug != null && "true".equals(debug.toString)) {
      true
    } else {
      false
    }
  }

}
