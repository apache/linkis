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

package org.apache.linkis.engineconnplugin.flink.executor

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.computation.executor.execute.{
  ComputationExecutor,
  ConcurrentComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteResponse}

class FlinkManagerConcurrentExecutor(
    val id: Int,
    maxRunningNumber: Int,
    override protected val flinkEngineConnContext: FlinkEngineConnContext
) extends ConcurrentComputationExecutor
    with FlinkExecutor
    with Logging {

  override def executeLine(
      engineExecutorContext: EngineExecutionContext,
      code: String
  ): ExecuteResponse =
    ErrorExecuteResponse("FlinkManagerExecutor does not support executeLine", null)

  override def executeCompletely(
      engineExecutorContext: EngineExecutionContext,
      code: String,
      completedLine: String
  ): ExecuteResponse = executeLine(engineExecutorContext, code)

  override def progress(taskID: String): Float = 0.0f

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] = null

  override def getId: String = super.getId

  override def close(): Unit = {
    logger.info(s"FlinkManagerExecutor : ${getId} will close.")
    super.close()
  }

  override def getConcurrentLimit: Int = maxRunningNumber

  override def killAll(): Unit = {
    // TODO
  }

}
