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

package org.apache.linkis.engineconn.executor.service

import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.executor.conf.EngineConnExecutorConfiguration
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.engine.{
  ECCanKillRequest,
  ECCanKillResponse,
  EngineConnReleaseRequest
}
import org.apache.linkis.manager.common.protocol.node.NodeHeartbeatMsg
import org.apache.linkis.manager.common.protocol.resource.ResourceUsedProtocol
import org.apache.linkis.manager.label.entity.Label

trait ManagerService {

  def labelReport(labels: java.util.List[Label[_]]): Unit

  def statusReport(status: NodeStatus): Unit

  def requestReleaseEngineConn(engineConnReleaseRequest: EngineConnReleaseRequest): Unit

  def heartbeatReport(nodeHeartbeatMsg: NodeHeartbeatMsg): Unit

  def reportUsedResource(resourceUsedProtocol: ResourceUsedProtocol): Unit

  def ecCanKillRequest(ecCanKillRequest: ECCanKillRequest): ECCanKillResponse

}

object ManagerService {

  private val managerService: ManagerService = Utils.getClassInstance[ManagerService](
    EngineConnExecutorConfiguration.EXECUTOR_MANAGER_SERVICE_CLAZZ.getValue
  )

  def getManagerService: ManagerService = managerService
}
