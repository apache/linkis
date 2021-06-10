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