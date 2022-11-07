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

package org.apache.linkis.engineconn.acessible.executor.service

import org.apache.linkis.engineconn.acessible.executor.listener.ExecutorStatusListener
import org.apache.linkis.manager.common.protocol.engine.EngineSuicideRequest
import org.apache.linkis.manager.common.protocol.node.{RequestNodeStatus, ResponseNodeStatus}
import org.apache.linkis.rpc.Sender

trait AccessibleService extends ExecutorStatusListener {

  def stopExecutor: Unit

  def pauseExecutor: Unit

  def reStartExecutor: Boolean

  def dealEngineStopRequest(engineSuicideRequest: EngineSuicideRequest, sender: Sender): Unit

  @deprecated
  def requestManagerReleaseExecutor(msg: String): Unit

  def dealRequestNodeStatus(requestNodeStatus: RequestNodeStatus): ResponseNodeStatus

}
