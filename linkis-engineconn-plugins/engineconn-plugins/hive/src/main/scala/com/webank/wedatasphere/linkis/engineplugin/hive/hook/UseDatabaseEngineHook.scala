/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.engineplugin.hive.hook

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.common.hook.EngineConnHook
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import com.webank.wedatasphere.linkis.engineconn.core.executor.ExecutorManager
import com.webank.wedatasphere.linkis.engineplugin.hive.executor.HiveEngineConnExecutor
import org.apache.commons.lang.StringUtils

class UseDatabaseEngineHook extends EngineConnHook with Logging {

  private val USER: String = "user"

  override def beforeCreateEngineConn(engineCreationContext: EngineCreationContext): Unit = {}

  override def beforeExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = {}

  override def afterExecutionExecute(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Unit = Utils.tryAndError {
    val options = engineCreationContext.getOptions
    val user: String = if (options.containsKey(USER)) options.get(USER) else {
      Utils.getJvmUser
    }
    val database = if (StringUtils.isNotEmpty(user)) {
      user + "_ind"
    } else {
      "default"
    }
    val useDataBaseSql = "use " + database
    info(s"hive client begin to run init_code $useDataBaseSql")
    ExecutorManager.getInstance().getDefaultExecutor match {
      case executor: HiveEngineConnExecutor =>
        executor.executeLine(new EngineExecutionContext(executor), useDataBaseSql)
      case _ =>
    }

  }
}
