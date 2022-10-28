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

package org.apache.linkis.manager.am.manager

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.node.{EngineNode, ScoreServiceInstance}
import org.apache.linkis.manager.common.protocol.engine.{
  EngineOperateRequest,
  EngineOperateResponse
}

trait EngineNodeManager {

  def listEngines(user: String): java.util.List[EngineNode]

  def getEngineNode(serviceInstance: ServiceInstance): EngineNode

  def getEngineNodeInfo(engineNode: EngineNode): EngineNode

  def getEngineNodeInfo(serviceInstance: ServiceInstance): EngineNode

  def getEngineNodeInfoByDB(engineNode: EngineNode): EngineNode

  /**
   * Get detailed engine information from the persistence
   *
   * @param scoreServiceInstances
   * @return
   */
  def getEngineNodes(scoreServiceInstances: Array[ScoreServiceInstance]): Array[EngineNode]

  def updateEngineStatus(
      serviceInstance: ServiceInstance,
      fromState: NodeStatus,
      toState: NodeStatus
  ): Unit

  /**
   * add info to persistence
   *
   * @param engineNode
   */
  def addEngineNode(engineNode: EngineNode): Unit

  def updateEngineNode(serviceInstance: ServiceInstance, engineNode: EngineNode): Unit

  def updateEngine(engineNode: EngineNode): Unit

  /**
   * delete info to persistence
   *
   * @param engineNode
   */
  def deleteEngineNode(engineNode: EngineNode): Unit

  def switchEngine(engineNode: EngineNode): EngineNode

  def reuseEngine(engineNode: EngineNode): EngineNode

  def useEngine(engineNode: EngineNode): EngineNode

  def useEngine(engineNode: EngineNode, timeout: Long): EngineNode

  def executeOperation(engineNode: EngineNode, request: EngineOperateRequest): EngineOperateResponse

}
