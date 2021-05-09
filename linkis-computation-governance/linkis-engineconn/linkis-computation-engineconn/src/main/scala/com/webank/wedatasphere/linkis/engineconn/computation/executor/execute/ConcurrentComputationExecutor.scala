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

package com.webank.wedatasphere.linkis.engineconn.computation.executor.execute

import com.webank.wedatasphere.linkis.engineconn.computation.executor.entity.EngineConnTask
import com.webank.wedatasphere.linkis.engineconn.executor.entity.ConcurrentExecutor
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse
import scala.collection.JavaConverters._


abstract class ConcurrentComputationExecutor(override val outputPrintLimit: Int = 1000) extends ComputationExecutor(outputPrintLimit) with ConcurrentExecutor {

  override def execute(engineConnTask: EngineConnTask): ExecuteResponse = {
    if (getRunningTask >= getConcurrentLimit) synchronized {
      if (getRunningTask >= getConcurrentLimit) {
        info(s"running task($getRunningTask) > concurrent limit $getConcurrentLimit, now to mark engine to busy ")
        transition(NodeStatus.Busy)
      }
    }
    val response = super.execute(engineConnTask)
    if (getStatus == NodeStatus.Busy && getConcurrentLimit > getRunningTask) synchronized {
      if (getStatus == NodeStatus.Busy && getConcurrentLimit > getRunningTask) {
        info(s"running task($getRunningTask) < concurrent limit $getConcurrentLimit, now to mark engine to Unlock ")
        transition(NodeStatus.Unlock)
      }
    }
    response
  }

  protected override  def ensureOp[A](f: => A): A = if (!isEngineInitialized)
    f
  else ensureIdle(f, false)

  override def killAll(): Unit = {
    taskCache.asMap().keySet().asScala.foreach(killTask(_))
  }

  override def close(): Unit = {
    killAll()
    super.close()
  }
}
