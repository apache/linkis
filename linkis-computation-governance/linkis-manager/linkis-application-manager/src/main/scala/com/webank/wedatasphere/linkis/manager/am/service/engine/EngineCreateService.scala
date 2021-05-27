package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.common.exception.LinkisRetryException
import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineCreateRequest
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext

/**
  * @author peacewong
  * @date 2020/6/12 15:52
  */
trait EngineCreateService {

  @throws[LinkisRetryException]
  def createEngine(engineCreateRequest: EngineCreateRequest, smc: ServiceMethodContext): EngineNode

}
