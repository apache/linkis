package com.webank.wedatasphere.linkis.manager.am.service.engine

import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineSwitchRequest
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import org.springframework.stereotype.Service


@Service
class DefaultEngineSwitchService extends EngineSwitchService {

  @Receiver
  override def switchEngine(engineSwitchRequest: EngineSwitchRequest): EngineNode = ???

}
