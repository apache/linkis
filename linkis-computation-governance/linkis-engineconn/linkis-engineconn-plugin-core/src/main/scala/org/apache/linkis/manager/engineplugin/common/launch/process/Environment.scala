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

package org.apache.linkis.manager.engineplugin.common.launch.process

import org.apache.commons.lang3.StringUtils

object Environment extends Enumeration {

  type Environment = Value

  val USER, ECM_HOME, PWD, PATH, SHELL, JAVA_HOME, CLASSPATH, HADOOP_HOME, HADOOP_CONF_DIR,
      HIVE_CONF_DIR, LOG_DIRS, TEMP_DIRS, ECM_HOST, ECM_PORT, RANDOM_PORT, SERVICE_DISCOVERY,
      PREFER_IP_ADDRESS, UDF_JARS, ENGINECONN_ENVKEYS = Value

  def variable(environment: Environment): String =
    LaunchConstants.EXPANSION_MARKER_LEFT + environment + LaunchConstants.EXPANSION_MARKER_RIGHT

}

object LaunchConstants {

  val CLASS_PATH_SEPARATOR = "<<CPS>>"
  val EXPANSION_MARKER_LEFT = "<<L"
  val EXPANSION_MARKER_RIGHT = "R>>"
  val LOG_DIRS_KEY = "LOG_DIRS"
  val TICKET_ID_KEY = "TICKET_ID"
  val ENGINE_CONN_CONF_DIR_NAME = "conf"
  val ENGINE_CONN_LIB_DIR_NAME = "lib"

  def addPathToClassPath(env: java.util.Map[String, String], value: String): Unit = {
    val v = if (env.containsKey(Environment.CLASSPATH.toString)) {
      env.get(Environment.CLASSPATH.toString) + CLASS_PATH_SEPARATOR + value
    } else value
    env.put(Environment.CLASSPATH.toString, v)
  }

  def addPathToUDFPath(env: java.util.Map[String, String], value: String): Unit = {
    if (StringUtils.isBlank(value)) return
    val v = if (env.containsKey(Environment.UDF_JARS.toString)) {
      env.get(Environment.UDF_JARS.toString) + "," + value
    } else value
    env.put(Environment.UDF_JARS.toString, v)
  }

}
