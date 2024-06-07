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

package org.apache.linkis.engineconn.acessible.executor.info

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.acessible.executor.entity.AccessibleExecutor
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.manager.common.entity.enumeration.{NodeHealthy, NodeStatus}
import org.apache.linkis.manager.common.entity.metrics.NodeHealthyInfo

import org.springframework.stereotype.Component

trait NodeHealthyInfoManager {

  def getNodeHealthyInfo(): NodeHealthyInfo

  def setNodeHealthy(healthy: NodeHealthy): Unit

  def getNodeHealthy(): NodeHealthy

  def setByManager(setByManager: Boolean): Unit

}

@Component
class DefaultNodeHealthyInfoManager extends NodeHealthyInfoManager with Logging {

  private var healthy: NodeHealthy = NodeHealthy.Healthy

  private var setByManager: Boolean = false

  override def getNodeHealthyInfo(): NodeHealthyInfo = {
    val nodeHealthyInfo = new NodeHealthyInfo
    nodeHealthyInfo.setMsg("")

    /** If it is actively set by the manager, then the manager setting shall prevail */
    val newHealthy: NodeHealthy = if (this.setByManager) {
      this.healthy
    } else {
      NodeStatus.isEngineNodeHealthy(
        ExecutorManager.getInstance.getReportExecutor.asInstanceOf[AccessibleExecutor].getStatus
      )
    }
    logger.info("current node healthy status is {}", newHealthy)
    nodeHealthyInfo.setNodeHealthy(newHealthy)
    nodeHealthyInfo
  }

  override def setNodeHealthy(healthy: NodeHealthy): Unit = {
    this.healthy = healthy
  }

  override def setByManager(setByManager: Boolean): Unit = {
    this.setByManager = setByManager
  }

  override def getNodeHealthy(): NodeHealthy = this.healthy
}
