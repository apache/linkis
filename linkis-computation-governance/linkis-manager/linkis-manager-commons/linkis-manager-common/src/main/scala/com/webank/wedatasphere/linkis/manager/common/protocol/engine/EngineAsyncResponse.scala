package com.webank.wedatasphere.linkis.manager.common.protocol.engine

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol


trait EngineAsyncResponse extends RequestProtocol {
  def id(): String
}

case class EngineAskAsyncResponse(override val id: String, managerInstance: ServiceInstance) extends EngineAsyncResponse

case class EngineCreateSuccess(override val id: String, engineNode: EngineNode) extends EngineAsyncResponse

case class EngineCreateError(override val id: String, exception: String, retry: Boolean = false) extends EngineAsyncResponse

