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

package org.apache.linkis.manager.engineplugin.python.hook

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.common.hook.EngineConnHook
import org.apache.linkis.manager.engineplugin.python.conf.PythonEngineConfiguration
import org.apache.linkis.manager.engineplugin.python.executor.PythonSession

import java.util

class PythonVersionEngineHook extends EngineConnHook with Logging {
  var _sparkpythonVersion: String = _
  var _sparkpythonExtraPackage: String = _
  var _pythonVersion: String = _
  var _pythonExtraPackage: String = _

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {
    val params =
      if (engineCreationContext.getOptions == null) new util.HashMap[String, String]()
      else engineCreationContext.getOptions
    _pythonVersion = params.getOrDefault("python.version", "python3")
    _pythonExtraPackage = params
      .getOrDefault("python.application.pyFiles", "file:///mnt/bdap/test/test/test.zip")
      .toString
    logger.info(s"python version => ${_pythonVersion}")
    logger.info(s"python version => ${_pythonExtraPackage}")
  }

  override def beforeExecutionExecute(
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn
  ): Unit = {
    logger.info("use python execute print cmd hello")
    engineConn.getEngineConnSession match {
      case pythonSession: PythonSession =>
        pythonSession.execute("print(1/2)")
        logger.info(s"print python version => ${PythonEngineConfiguration.PYTHON_VERSION.getValue}")
      case _ =>
        logger.error(s"Invalid pythonSession : ${engineConn.getEngineConnSession.getClass.getName}")
    }

  }

  override def afterExecutionExecute(
      engineCreationContext: _root_.org.apache.linkis.engineconn.common.creation.EngineCreationContext,
      engineConn: _root_.org.apache.linkis.engineconn.common.engineconn.EngineConn
  ): Unit = {}

}
