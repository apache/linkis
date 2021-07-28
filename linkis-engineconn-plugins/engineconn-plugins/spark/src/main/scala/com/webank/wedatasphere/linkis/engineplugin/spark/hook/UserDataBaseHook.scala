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
import com.webank.wedatasphere.linkis.engineplugin.spark.exception.EngineErrorException

/**
  *
  */
class UserDataBaseHook extends EngineConnHook with Logging {
  self =>
  protected var creator: String = _
  protected var user: String = _
  protected var initSpecialCode: String = _
  val runType = "sql"

  @scala.throws[EngineErrorException]
  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {
    val params = engineCreationContext.getOptions
    creator = params.get("creator")
    user = params.get("user")
    initSpecialCode = "use " + user + "_ind;\n"
  }

  @scala.throws[EngineErrorException]
  override def beforeExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {

  }

  override def afterExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {
    /*info("Set default database for user , code: " + initSpecialCode)
    executor.execute(new ExecuteRequest with RunTypeExecuteRequest {
      override val code: String = initSpecialCode
      override val runType: String = self.runType
    })
    info("executed code: " + initSpecialCode)*/
  }

}
