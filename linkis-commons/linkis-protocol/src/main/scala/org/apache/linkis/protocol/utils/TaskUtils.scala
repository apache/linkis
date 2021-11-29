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
 
package org.apache.linkis.protocol.utils

import java.util

import org.apache.linkis.protocol.constants.TaskConstant

import scala.collection.JavaConversions._

object TaskUtils {

  def getMap(params: util.Map[String, Any], key: String): util.Map[String, Any] =
    if(params != null && params.containsKey(key))
      params.get(key) match {
        case map: util.Map[String, Any] => map
        case map: util.Map[String, Object] =>
          val resultMap = new util.HashMap[String, Any]
          map.keySet().foreach { k => resultMap.put(k, map.get(k))}
          resultMap
        case _ => new util.HashMap[String, Any]()
      }
    else new util.HashMap[String, Any]()

  private def addMap(params: util.Map[String, Any], waitToAdd: util.Map[String, Any], key: String): Unit =
    if(params != null && params.containsKey(key))
      params.get(key) match {
        case map: util.Map[String, Any] => map.putAll(waitToAdd)
        case map: util.Map[String, _] =>
          val resultMap = new util.HashMap[String, Any]
          map.keySet().foreach { k => resultMap.put(k, map.get(k))}
          resultMap.putAll(waitToAdd)
          params.put(key, resultMap)
        case _ => params.put(key, waitToAdd)
      }
    else params.put(key, waitToAdd)

  private def getConfigurationMap(params: util.Map[String, Any], key: String) = {
    val configurationMap = getMap(params, TaskConstant.PARAMS_CONFIGURATION)
    getMap(configurationMap, key)
  }

  def addConfigurationMap(params: util.Map[String, Any], waitToAdd: util.Map[String, Any], key: String): Unit = {
    val configurationMap = getMap(params, TaskConstant.PARAMS_CONFIGURATION)
    if(configurationMap.isEmpty) params.put(TaskConstant.PARAMS_CONFIGURATION, configurationMap)
    addMap(configurationMap, waitToAdd, key)
  }

  def getVariableMap(params: util.Map[String, Any]) = getMap(params, TaskConstant.PARAMS_VARIABLE)

  def getStartupMap(params: util.Map[String, Any]) = getConfigurationMap(params, TaskConstant.PARAMS_CONFIGURATION_STARTUP)

  def getRuntimeMap(params: util.Map[String, Any]) = getConfigurationMap(params, TaskConstant.PARAMS_CONFIGURATION_RUNTIME)

  def getSpecialMap(params: util.Map[String, Any]) = getConfigurationMap(params, TaskConstant.PARAMS_CONFIGURATION_SPECIAL)

  def addVariableMap(params: util.Map[String, Any], variableMap: util.Map[String, Any]) = addMap(params, variableMap, TaskConstant.PARAMS_VARIABLE)

  def addStartupMap(params: util.Map[String, Any], startupMap: util.Map[String, Any]) =
    addConfigurationMap(params, startupMap, TaskConstant.PARAMS_CONFIGURATION_STARTUP)

  def addRuntimeMap(params: util.Map[String, Any], runtimeMap: util.Map[String, Any]) =
    addConfigurationMap(params, runtimeMap, TaskConstant.PARAMS_CONFIGURATION_RUNTIME)

  def addSpecialMap(params: util.Map[String, Any], specialMap: util.Map[String, Any]) =
    addConfigurationMap(params, specialMap, TaskConstant.PARAMS_CONFIGURATION_SPECIAL)

  // tdoo
  def getLabelsMap(params: util.Map[String, Any]) = getMap(params, TaskConstant.LABELS)

  def addLabelsMap(params: util.Map[String, Any], labels: util.Map[String, Any]): Unit = addMap(params, labels, TaskConstant.LABELS)

}
