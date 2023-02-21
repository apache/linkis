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
import org.apache.linkis.manager.common.entity.node.{EMNode, EngineNode, Node, ScoreServiceInstance}
import org.apache.linkis.manager.common.protocol.em.{ECMOperateRequest, ECMOperateResponse}
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest

trait EMNodeManager {

  def emRegister(emNode: EMNode): Unit

  def listEngines(emNode: EMNode): java.util.List[EngineNode]

  def listUserEngines(emNode: EMNode, user: String): java.util.List[EngineNode]

  def listUserNodes(user: String): java.util.List[Node]

  /**
   * Get detailed em information from the persistence
   *
   * @param scoreServiceInstances
   * @return
   */
  def getEMNodes(scoreServiceInstances: Array[ScoreServiceInstance]): Array[EMNode]

  def getEM(serviceInstance: ServiceInstance): EMNode

  def stopEM(emNode: EMNode): Unit

  def deleteEM(emNode: EMNode): Unit

  def pauseEM(serviceInstance: ServiceInstance): Unit

  /**
   *   1. request engineManager to launch engine 2. persist engine info
   *
   * @param engineConnLaunchRequest
   *   engine launch request
   * @param emNode
   *   ecm node
   * @return
   *   engine node
   */
  def createEngine(engineConnLaunchRequest: EngineConnLaunchRequest, emNode: EMNode): EngineNode

  def stopEngine(engineStopRequest: EngineStopRequest, emNode: EMNode): Unit

  def addEMNodeInstance(emNode: EMNode): Unit

  def initEMNodeMetrics(emNode: EMNode): Unit

  def executeOperation(ecmNode: EMNode, request: ECMOperateRequest): ECMOperateResponse
}
