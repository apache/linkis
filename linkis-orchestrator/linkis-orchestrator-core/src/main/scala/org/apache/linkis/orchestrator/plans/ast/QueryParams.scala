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
 
package org.apache.linkis.orchestrator.plans.ast
import java.util
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.utils.TaskUtils

import scala.collection.JavaConverters.mapAsScalaMapConverter

/**
  *
  */
trait QueryParams {

  def getStartupParams: StartupParams

  def getRuntimeParams: RuntimeParams

  def getParams(key: String): Any

}

class QueryParamsImpl(params: java.util.Map[String, Any]) extends QueryParams {

  private var startupParams: StartupParams = _

  private var runtimeParams: RuntimeParams = _

  def init(): Unit = {
    var paramMap = params
    if (null == params) {
      paramMap = new util.HashMap[String, Any]()
    }
    val startUp = {
      val mapOri = TaskUtils.getStartupMap(paramMap)
      val map = new util.HashMap[String, AnyRef]()
      mapOri.asScala.foreach(kv => map.put(kv._1, kv._2.asInstanceOf[AnyRef]))
      map
    }
    startupParams = new StartupParamsImpl(startUp)
    val runtime = {
      val mapOri = TaskUtils.getRuntimeMap(paramMap)
      val map = new util.HashMap[String, AnyRef]()
      mapOri.asScala.foreach(kv => map.put(kv._1, kv._2.asInstanceOf[AnyRef]))
      map
    }
    val variable = {
      val mapOri = TaskUtils.getVariableMap(paramMap)
      val map = new util.HashMap[String, AnyRef]()
      mapOri.asScala.foreach(kv => map.put(kv._1, kv._2.asInstanceOf[AnyRef]))
      map
    }
    val special = {
      val mapOri = TaskUtils.getSpecialMap(paramMap)
      val map = new util.HashMap[String, AnyRef]()
      mapOri.asScala.foreach(kv => map.put(kv._1, kv._2.asInstanceOf[AnyRef]))
      map
    }
    runtimeParams = new RuntimeParamsImpl(runtime, variable, special)
  }

  init()

  override def getStartupParams: StartupParams = startupParams

  override def getRuntimeParams: RuntimeParams = runtimeParams

  override def getParams(key: String): Any = {
    if (null != params ) params.get(key)
    else null
  }
}

object QueryParams {

  val STARTUP_KEY = TaskConstant.PARAMS_CONFIGURATION_STARTUP

  val SPECIAL_KEY = TaskConstant.PARAMS_CONFIGURATION_SPECIAL

  val RUNTIME_KEY = TaskConstant.PARAMS_CONFIGURATION_RUNTIME

  val CONFIGURATION_KEY = TaskConstant.PARAMS_CONFIGURATION

  val VARIABLE_KEY = TaskConstant.PARAMS_VARIABLE

  val DATA_SOURCE_KEY = "dataSources"//TaskConstant.PARAMS_DATA_SOURCE

  val CONTEXT_KEY = "context"//TaskConstant.PARAMS_CONTEXT

  @Deprecated
  val CONTEXT_KEY_FOR_ID = "contextID"
  @Deprecated
  val CONTEXT_KEY_FOR_NODE_NAME = "nodeName"

  val JOB_KEY = "job" // in runtime map

}