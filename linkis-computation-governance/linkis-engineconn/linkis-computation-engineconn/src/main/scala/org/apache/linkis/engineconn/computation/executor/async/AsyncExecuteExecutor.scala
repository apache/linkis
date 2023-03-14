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

package org.apache.linkis.engineconn.computation.executor.async

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.common.exception.EngineConnException
import org.apache.linkis.engineconn.computation.executor.utlis.ComputationErrorCode
import org.apache.linkis.governance.common.utils.{JobUtils, LoggerUtils}
import org.apache.linkis.scheduler.executer._
import org.apache.linkis.scheduler.executer.ExecutorState.ExecutorState

class AsyncExecuteExecutor(executor: AsyncConcurrentComputationExecutor) extends Executor {

  override def getId: Long = {
    0
  }

  override def execute(executeRequest: ExecuteRequest): ExecuteResponse = {
    executeRequest match {
      case asyncExecuteRequest: AsyncExecuteRequest =>
        Utils.tryFinally {
          val jobId = JobUtils.getJobIdFromMap(asyncExecuteRequest.task.getProperties)
          LoggerUtils.setJobIdMDC(jobId)
          executor.asyncExecuteTask(
            asyncExecuteRequest.task,
            asyncExecuteRequest.engineExecutionContext
          )
        } {
          LoggerUtils.removeJobIdMDC()
        }
      case _ =>
        throw EngineConnException(
          ComputationErrorCode.ASYNC_EXECUTOR_ERROR_CODE,
          "Mismatched execution request"
        )
    }
  }

  override def state: ExecutorState = {
    ExecutorState.Idle
  }

  override def getExecutorInfo: ExecutorInfo = {
    null
  }

  override def close(): Unit = {}
}
