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

package com.webank.wedatasphere.linkis.engineconn.common.execution

import com.webank.wedatasphere.linkis.common.exception.FatalException
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.conf.EngineConnConf
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import org.apache.commons.lang.StringUtils


trait EngineExecution {

  def execute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit
}

object EngineExecution extends Logging {


  private val engineExecutions = initEngineExecutions

  private def initEngineExecutions: Array[EngineExecution] = {

    val executions = EngineConnConf.ENGINE_EXECUTIONS.getValue
    if (StringUtils.isNotBlank(executions)) {
      val clazzArr = executions.split(",")
      if (null != clazzArr && !clazzArr.isEmpty) {
        clazzArr.map { clazz =>
          Utils.getClassInstance[EngineExecution](clazz)
        }
      } else {
        throw new FatalException(0, "ENGINE_EXECUTIONS must be set")
      }
    } else {
      throw new FatalException(0, "ENGINE_EXECUTIONS must be set")
    }

  }

  def getEngineExecutions: Array[EngineExecution] = engineExecutions
}