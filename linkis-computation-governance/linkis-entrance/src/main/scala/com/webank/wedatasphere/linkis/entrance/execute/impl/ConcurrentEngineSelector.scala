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

package com.webank.wedatasphere.linkis.entrance.execute.impl

import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.execute.{EngineSelector, EntranceEngine}
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState

/**
  * Created by enjoyyin on 2018/10/30.
  */
class ConcurrentEngineSelector extends EngineSelector {

  private val maxParallelismJobs = EntranceConfiguration.CONCURRENT_ENGINE_MAX_PARALLELISM.acquireNew
  protected val maxOverloadedJobs: Int = Math.min(maxParallelismJobs * 2, 50)

  protected def isEngineOverloaded(engine: EntranceEngine): Boolean = {
    val submittedTasks = engine.getConcurrentInfo.map(info => info.runningTasks + info.pendingTasks).getOrElse(0)
    submittedTasks >= maxOverloadedJobs
  }

  /**
    *
    * @param engines
    * @return
    */
  override def chooseEngine(engines: Array[EntranceEngine]): Option[EntranceEngine] = {
    val sortedEngines = engines.filter(e => ExecutorState.isAvailable(e.state) && e.getEngineReturns.length <= maxParallelismJobs)
      .sortBy(_.getConcurrentInfo.map(info => info.runningTasks + info.pendingTasks).getOrElse(0))
    if(sortedEngines.isEmpty) None
    else if(isEngineOverloaded(sortedEngines.head)) None
    else Option(sortedEngines.head)
  }

  override def lockEngine(engine: EntranceEngine): Option[String] = Option("OK")

  override def onEngineLocked(engine: EntranceEngine, lock: String): Unit = {}

  override def onEngineLockUsed(engine: EntranceEngine): Unit = {}
}
