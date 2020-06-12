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
import com.webank.wedatasphere.linkis.protocol.engine.EngineState.Idle
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState
import com.webank.wedatasphere.linkis.server.JMap

/**
  * Created by enjoyyin on 2018/9/27.
  */
class ReleaseEngineHook extends EngineHook with Logging {

  private var maxFreeTimeStr = ""
  private var maxFreeTime = 0l

  override def beforeCreateEngine(params: JMap[String, String]): JMap[String, String] = {
    val maxFreeTimeVar = EngineConfiguration.ENGINE_MAX_FREE_TIME.getValue(params)
    maxFreeTimeStr = maxFreeTimeVar.toString
    maxFreeTime = maxFreeTimeVar.toLong
    params
  }

  override def afterCreatedEngine(executor: EngineExecutor): Unit = {
    val period = math.min(math.max(maxFreeTime / 10, 30000), 120000)
    Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = if(ExecutorState.isCompleted(executor.state)) {
        error(s"$executor has completed with state ${executor.state}, now stop it.")
        System.exit(-1)
      } else if(executor.state == ExecutorState.ShuttingDown) {
        warn(s"$executor is ShuttingDown...")
        System.exit(-1)
      } else if(maxFreeTime > 0 && executor.state == Idle && System.currentTimeMillis - executor.getLastActivityTime > maxFreeTime) {
        warn(s"$executor has not been used for $maxFreeTimeStr, now try to shutdown it.")
        System.exit(-1)
      }
    }, 3 * 60 * 1000, period, TimeUnit.MILLISECONDS)
  }
}