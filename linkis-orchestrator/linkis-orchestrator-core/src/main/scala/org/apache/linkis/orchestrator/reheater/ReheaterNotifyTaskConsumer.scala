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

package org.apache.linkis.orchestrator.reheater

import org.apache.linkis.orchestrator.execution.ExecTaskRunner
import org.apache.linkis.orchestrator.execution.impl.{DefaultTaskManager, NotifyTaskConsumer}
import org.apache.linkis.orchestrator.plans.physical.ExecTask

/**
 */
abstract class ReheaterNotifyTaskConsumer extends NotifyTaskConsumer {

  val reheater: Reheater

  protected def reheatIt(execTask: ExecTask): Boolean = {
    val key = getReheatableKey(execTask.getId)
    def compareAndSet(lastRunning: String): Boolean = {
      val thisRunning =
        getExecution.taskManager.getCompletedTasks(execTask).map(_.task.getId).mkString(",")
      if (thisRunning != lastRunning) {
        logger.debug(
          s"${execTask.getIDInfo()} Try to reheat this $thisRunning. lastRunning: $lastRunning"
        )
        val reheaterStatus = reheater.reheat(execTask)
        execTask.getPhysicalContext.set(key, thisRunning)
        reheaterStatus
      } else {
        false
      }
    }
    execTask.getPhysicalContext.get(key) match {
      case lastRunning: String =>
        compareAndSet(lastRunning)
      case _ if getExecution.taskManager.getCompletedTasks(execTask).nonEmpty =>
        compareAndSet(null)
      case _ => false

    }

  }

  protected def getReheatableKey(id: String): String =
    ReheaterNotifyTaskConsumer.REHEAT_KEY_PREFIX + id

  override protected def beforeFetchLaunchTask(): Array[ExecTaskRunner] =
    getExecution.taskManager match {
      case taskManager: DefaultTaskManager =>
        val (executionTasks, runnableExecTasks) = taskManager.getRunnableExecutionTasksAndExecTask
        val reheaterRootExecTasks =
          executionTasks.map(_.getRootExecTask).filter(reheatIt).map(_.getId)
        runnableExecTasks.filterNot(task =>
          reheaterRootExecTasks.contains(task.task.getPhysicalContext.getRootTask.getId)
        )
      case _ => null
    }

}

object ReheaterNotifyTaskConsumer {

  /**
   * Prefix of key in physical context for reheater
   */
  val REHEAT_KEY_PREFIX = "reheatable_"

}
