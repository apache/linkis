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

package org.apache.linkis.engineconn.computation.executor.execute

import org.apache.linkis.DataWorkCloudApplication.getApplicationContext
import org.apache.linkis.engineconn.acessible.executor.info.DefaultNodeHealthyInfoManager
import org.apache.linkis.engineconn.acessible.executor.utils.AccessibleExecutorUtils.currentEngineIsUnHealthy
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.executor.entity.ConcurrentExecutor
import org.apache.linkis.manager.common.entity.enumeration.{NodeHealthy, NodeStatus}
import org.apache.linkis.manager.label.entity.entrance.ExecuteOnceLabel
import org.apache.linkis.scheduler.executer.ExecuteResponse

abstract class ConcurrentComputationExecutor(override val outputPrintLimit: Int = 1000)
    extends ComputationExecutor(outputPrintLimit)
    with ConcurrentExecutor {

  private val EXECUTOR_STATUS_LOCKER = new Object

  override def execute(engineConnTask: EngineConnTask): ExecuteResponse = {
    if (isBusy) {
      logger.error(
        s"Executor is busy but still got new task ! Running task num : ${getRunningTask}"
      )
    }
    if (getRunningTask >= getConcurrentLimit) EXECUTOR_STATUS_LOCKER.synchronized {
      if (getRunningTask >= getConcurrentLimit && NodeStatus.isIdle(getStatus)) {
        logger.info(
          s"running task: $getRunningTask > concurrent limit: $getConcurrentLimit, now to mark engine to busy"
        )
        transition(NodeStatus.Busy)
      }
    }
    logger.info(s"engineConnTask(${engineConnTask.getTaskId}) running task is ($getRunningTask) ")
    val response = super.execute(engineConnTask)
    if (getStatus == NodeStatus.Busy && getConcurrentLimit > getRunningTask) {
      EXECUTOR_STATUS_LOCKER.synchronized {
        if (getStatus == NodeStatus.Busy && getConcurrentLimit > getRunningTask) {
          logger.info(
            s"running task($getRunningTask) < concurrent limit:$getConcurrentLimit, now to mark engine to Unlock "
          )
          transition(NodeStatus.Unlock)
        }
      }
    }
    response
  }

  protected override def ensureOp[A](f: => A): A = f

  override def afterExecute(
      engineConnTask: EngineConnTask,
      executeResponse: ExecuteResponse
  ): Unit = {
    // execute once should try to shutdown
    if (engineConnTask.getLables.exists(_.isInstanceOf[ExecuteOnceLabel])) {
      if (!hasTaskRunning()) {
        logger.warn(
          s"engineConnTask(${engineConnTask.getTaskId}) is execute once, now to mark engine to Finished"
        )
        ExecutorManager.getInstance.getReportExecutor.tryShutdown()
      }
    }
    // unhealthy node should try to shutdown
    if (!hasTaskRunning() && currentEngineIsUnHealthy) {
      logger.info("no task running and ECNode is unHealthy, now to mark engine to Finished.")
      ExecutorManager.getInstance.getReportExecutor.tryShutdown()
    }
  }

  override def hasTaskRunning(): Boolean = {
    getRunningTask > 0
  }

  override def transition(toStatus: NodeStatus): Unit = {
    if (getRunningTask >= getConcurrentLimit && NodeStatus.Unlock == toStatus) {
      logger.info(
        s"running task($getRunningTask) > concurrent limit:$getConcurrentLimit, can not to mark EC to Unlock"
      )
      return
    }
    super.transition(toStatus)
  }

  override def getConcurrentLimit: Int = {
    var maxTaskNum = ComputationExecutorConf.ENGINE_CONCURRENT_THREAD_NUM.getValue - 5
    if (maxTaskNum <= 0) {
      logger.error(
        s"max task num  cannot ${maxTaskNum} < 0, should set linkis.engineconn.concurrent.thread.num > 6"
      )
      maxTaskNum = 1
    }
    logger.info(s"max task num $maxTaskNum")
    maxTaskNum
  }

}
