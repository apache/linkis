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

package com.webank.wedatasphere.linkis.engineplugin.spark.hook

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.common.hook.EngineConnHook

/**
  *
  */
class SparkPythonVersionEngineHook extends EngineConnHook with Logging{
  var _sparkpythonVersion : String = _
  var _sparkpythonExtraPackage : String = _
  var _pythonVersion : String = _
  var _pythonExtraPackage:String = _
  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {
    val params = engineCreationContext.getOptions
    _sparkpythonVersion = params.getOrDefault("spark.python.version","python").toString
    _sparkpythonExtraPackage = params.getOrDefault("spark.application.pyFiles","file:///mnt/bdap/test/test/test.zip").toString
    logger.info(s"spark python version => ${_sparkpythonVersion}")
    logger.info(s"spark python Extra Package => ${_sparkpythonExtraPackage}")
    params
  }

  override def beforeExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {
    info("use python3 execute print cmd hello. check todo")
    /*executor.execute(new ExecuteRequest with RunTypeExecuteRequest with PythonExecuteRequest{
      override val code: String = "print('hello')"
      override val runType: String = "python"
      override val sparkPythonVersion: String = _sparkpythonVersion
      override val sparkPythonExtraPackage: String = _sparkpythonExtraPackage
      override val pythonVersion: String = _pythonVersion
      override val pythonExtraPackage: String = _pythonExtraPackage
    })*/
  }

  override def afterExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {}
}
