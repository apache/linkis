/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineStopRequest, EngineSuicideRequest}
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext
import com.webank.wedatasphere.linkis.rpc.Sender


trait EngineStopService {


  def stopEngine(engineStopRequest: EngineStopRequest, smc: ServiceMethodContext): Unit

  def engineSuicide(engineSuicideRequest: EngineSuicideRequest, smc: ServiceMethodContext): Unit

}


object EngineStopService {
  def askEngineToSuicide(engineSuicideRequest: EngineSuicideRequest): Unit = {
    if (null == engineSuicideRequest.getServiceInstance) return
    Sender.getSender(engineSuicideRequest.getServiceInstance).send(engineSuicideRequest)
  }
}