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

package com.webank.wedatasphere.linkis.engine.execute.hook

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engine.conf.EngineConfiguration
import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineHook}
import com.webank.wedatasphere.linkis.server.JMap

class MaxExecuteNumEngineHook extends EngineHook with Logging{
  private var maxExecuteNum = 0
  override def beforeCreateEngine(params: JMap[String, String]): JMap[String, String] = {
    maxExecuteNum = EngineConfiguration.ENGINE_MAX_EXECUTE_NUM.getValue(params)
    params
  }

  override def afterCreatedEngine(executor: EngineExecutor): Unit = if(maxExecuteNum > 0) {
    Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = if(executor.getExecutedNum >= maxExecuteNum) {
        error(s"$executor has reached max execute number $maxExecuteNum, now stop it.")
        System.exit(-1)
      }
    }, 30 * 1000, 1000 * maxExecuteNum, TimeUnit.MILLISECONDS)
  }
}