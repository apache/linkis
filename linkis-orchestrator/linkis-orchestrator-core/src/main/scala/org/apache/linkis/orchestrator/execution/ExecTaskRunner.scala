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

package org.apache.linkis.orchestrator.execution

import org.apache.linkis.common.utils.{ClassUtils, Logging}
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.orchestrator.conf.OrchestratorConfiguration
import org.apache.linkis.orchestrator.plans.physical.ExecTask
import org.apache.linkis.orchestrator.strategy.DefaultExecTaskRunnerFactory

import org.apache.commons.lang3.StringUtils

/**
 */
trait ExecTaskRunner extends Runnable {

  val task: ExecTask

  def getTaskResponse: TaskResponse

  def isCompleted: Boolean

  def isRunning: Boolean

  def isSucceed: Boolean

  def transientStatus(status: ExecutionNodeStatus): Unit

  def interrupt(): Unit

}

trait ExecTaskRunnerFactory {
  def createExecTaskRunner(task: ExecTask): ExecTaskRunner
}

object ExecTaskRunner extends Logging {

  private var execTaskRunnerFactory: ExecTaskRunnerFactory = _

  def getExecTaskRunnerFactory: ExecTaskRunnerFactory = {
    if (execTaskRunnerFactory == null) synchronized {
      if (execTaskRunnerFactory == null) {
        val factory =
          if (
              StringUtils.isNotBlank(OrchestratorConfiguration.EXEC_RUNNER_FACTORY_CLASS.getValue)
          ) {
            ClassUtils.getClassInstance[ExecTaskRunnerFactory](
              OrchestratorConfiguration.EXEC_RUNNER_FACTORY_CLASS.getValue
            )
          } else {
            new DefaultExecTaskRunnerFactory
          }
        logger.info("Use " + factory.getClass.getName + " to instance a new execTaskRunnerFactory.")
        execTaskRunnerFactory = factory
      }
    }
    execTaskRunnerFactory
  }

}
