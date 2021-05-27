package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.common.exception.LinkisRetryException
import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineReuseRequest

/**
  * @author peacewong
  * @date 2020/6/12 16:05
  */
trait EngineReuseService {

  @throws[LinkisRetryException]
  def reuseEngine(engineReuseRequest: EngineReuseRequest): EngineNode

}
