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

package org.apache.linkis.manager.am.service.engine

import org.apache.linkis.manager.am.vo.ResourceVo
import org.apache.linkis.manager.common.entity.node.EngineNode
import org.apache.linkis.manager.common.protocol.engine.{
  EngineConnReleaseRequest,
  EngineStopRequest,
  EngineSuicideRequest
}
import org.apache.linkis.rpc.Sender

import java.util

import scala.collection.mutable

trait EngineStopService {

  def stopEngine(engineStopRequest: EngineStopRequest, sender: Sender): Unit

  /**
   * Asyn stop all unlock ec node of a specified ecm
   *
   * @param ecmInstance
   *   the specified ecm.
   * @param operatorName
   *   the username who request this operation
   * @return
   *   Map
   */
  def stopUnlockEngineByECM(ecmInstance: String, operatorName: String): java.util.Map[String, Any]

  def asyncStopEngine(engineStopRequest: EngineStopRequest): Unit

  /**
   * Async stop a ec node with change node status
   *
   * @param engineStopRequest
   * @return
   */
  def asyncStopEngineWithUpdateMetrics(engineStopRequest: EngineStopRequest): Unit

  def engineSuicide(engineSuicideRequest: EngineSuicideRequest, sender: Sender): Unit

  def dealEngineRelease(engineConnReleaseRequest: EngineConnReleaseRequest, sender: Sender): Unit

  def engineConnInfoClear(ecNode: EngineNode): Unit

}

object EngineStopService {

  def askEngineToSuicide(engineSuicideRequest: EngineSuicideRequest): Unit = {
    if (null == engineSuicideRequest.getServiceInstance) return
    Sender.getSender(engineSuicideRequest.getServiceInstance).send(engineSuicideRequest)
  }

}
