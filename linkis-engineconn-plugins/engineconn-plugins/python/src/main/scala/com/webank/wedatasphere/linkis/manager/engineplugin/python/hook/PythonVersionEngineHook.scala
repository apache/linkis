/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.manager.engineplugin.python.hook

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.common.hook.EngineConnHook
import com.webank.wedatasphere.linkis.manager.engineplugin.python.conf.PythonEngineConfiguration
import com.webank.wedatasphere.linkis.manager.engineplugin.python.executor.PythonSession


class PythonVersionEngineHook  extends EngineConnHook with Logging{
  var _sparkpythonVersion : String = _
  var _sparkpythonExtraPackage : String = _
  var _pythonVersion : String = _
  var _pythonExtraPackage:String = _

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {
    val params = engineCreationContext.getOptions
    _pythonVersion = params.getOrDefault("python.version","python3").toString
    _pythonExtraPackage = params.getOrDefault("python.application.pyFiles","file:///mnt/bdap/test/test/test.zip").toString
    logger.info(s"python version => ${_pythonVersion}")
    logger.info(s"python version => ${_pythonExtraPackage}")
  }

  override def beforeExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {
    info("use python execute print cmd hello")
    engineConn.getEngineConnSession match {
      case pythonSession: PythonSession =>
        pythonSession.execute("print(1/2)")
          logger.info(s"print python version => ${PythonEngineConfiguration.PYTHON_VERSION.getValue}")
      case _ =>
        logger.error(s"Invalid pythonSession : ${engineConn.getEngineConnSession.getClass.getName}")
    }

  }

  override def afterExecutionExecute(engineCreationContext:  _root_.com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext, engineConn:  _root_.com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn): Unit = {

  }
}
