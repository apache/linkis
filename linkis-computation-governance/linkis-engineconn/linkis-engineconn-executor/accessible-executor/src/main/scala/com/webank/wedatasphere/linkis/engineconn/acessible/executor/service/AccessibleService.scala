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

package com.webank.wedatasphere.linkis.engineconn.acessible.executor.service

import com.webank.wedatasphere.linkis.engineconn.acessible.executor.listener.ExecutorStatusListener
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineSuicideRequest
import com.webank.wedatasphere.linkis.manager.common.protocol.node.{RequestNodeStatus, ResponseNodeStatus}
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext

trait AccessibleService extends ExecutorStatusListener {

  def stopExecutor: Unit

  def pauseExecutor: Unit

  def reStartExecutor: Boolean

  def dealEngineStopRequest(engineSuicideRequest: EngineSuicideRequest, smc: ServiceMethodContext): Unit



  def requestManagerReleaseExecutor(msg: String): Unit


  def dealRequestNodeStatus(requestNodeStatus: RequestNodeStatus): ResponseNodeStatus

}

