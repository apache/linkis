package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.common.exception.LinkisRetryException
import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineSwitchRequest

/**
  * @author peacewong
  * @date 2020/6/12 16:00
  */
trait EngineSwitchService {

  @throws[LinkisRetryException]
  def switchEngine(engineSwitchRequest: EngineSwitchRequest): EngineNode

}
