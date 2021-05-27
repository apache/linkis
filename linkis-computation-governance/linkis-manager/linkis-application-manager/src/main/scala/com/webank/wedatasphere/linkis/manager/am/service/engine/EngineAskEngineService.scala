package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineAskRequest
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext

/**
  * @author peacewong
  * @date 2020/6/12 15:58
  */
trait EngineAskEngineService {

  def askEngine(engineAskRequest: EngineAskRequest, smc: ServiceMethodContext): Any

}
