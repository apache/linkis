package com.webank.wedatasphere.linkis.manager.service.common.pointer

import com.webank.wedatasphere.linkis.manager.common.protocol.{RequestEngineLock, RequestEngineUnlock}


trait EngineNodePointer extends NodePointer {

  def lockEngine(requestEngineLock: RequestEngineLock): Option[String]


  def releaseLock(requestEngineUnlock: RequestEngineUnlock): Unit

}
