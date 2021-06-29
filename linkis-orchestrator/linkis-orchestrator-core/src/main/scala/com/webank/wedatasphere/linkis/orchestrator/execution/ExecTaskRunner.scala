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

package com.webank.wedatasphere.linkis.orchestrator.execution

import com.webank.wedatasphere.linkis.common.utils.{ClassUtils, Logging}
import com.webank.wedatasphere.linkis.governance.common.entity.ExecutionNodeStatus
import com.webank.wedatasphere.linkis.orchestrator.conf.OrchestratorConfiguration
import com.webank.wedatasphere.linkis.orchestrator.plans.physical.ExecTask
import com.webank.wedatasphere.linkis.orchestrator.strategy.DefaultExecTaskRunnerFactory
import org.apache.commons.lang.StringUtils

/**
  *
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
    if(execTaskRunnerFactory == null) synchronized {
      if(execTaskRunnerFactory == null) {
        val factory = if(StringUtils.isNotBlank(OrchestratorConfiguration.EXEC_RUNNER_FACTORY_CLASS.getValue))
          ClassUtils.getClassInstance[ExecTaskRunnerFactory](OrchestratorConfiguration.EXEC_RUNNER_FACTORY_CLASS.getValue)
        else new DefaultExecTaskRunnerFactory
        info("Use " + factory.getClass.getName + " to instance a new execTaskRunnerFactory.")
        execTaskRunnerFactory = factory
      }
    }
    execTaskRunnerFactory
  }

}