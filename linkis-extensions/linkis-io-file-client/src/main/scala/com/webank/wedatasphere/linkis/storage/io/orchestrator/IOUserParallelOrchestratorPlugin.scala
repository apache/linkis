/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.storage.io.orchestrator

import java.util

import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.orchestrator.conf.OrchestratorConfiguration
import com.webank.wedatasphere.linkis.orchestrator.plugin.UserParallelOrchestratorPlugin

class IOUserParallelOrchestratorPlugin extends UserParallelOrchestratorPlugin{

  private val MAX_RUNNER_TASK_SIZE = OrchestratorConfiguration.TASK_RUNNER_MAX_SIZE.getValue

  override def getUserMaxRunningJobs(user: String, labels: util.List[Label[_]]): Int = {
    MAX_RUNNER_TASK_SIZE
  }

  override def isReady: Boolean = true

  override def start(): Unit = {}

  override def close(): Unit = {
  }
}
