package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineAskRequest
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext


trait EngineAskEngineService {

  def askEngine(engineAskRequest: EngineAskRequest, smc: ServiceMethodContext): Any

}
