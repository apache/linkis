package com.webank.wedatasphere.linkis.manager.am.pointer

import com.webank.wedatasphere.linkis.manager.common.entity.node.Node
import com.webank.wedatasphere.linkis.manager.common.protocol.{RequestEngineLock, RequestEngineUnlock, ResponseEngineLock}
import com.webank.wedatasphere.linkis.manager.service.common.pointer.EngineNodePointer


class DefaultEngineNodPointer(val node: Node) extends AbstractNodePointer with EngineNodePointer {


  /**
    * 与该远程指针关联的node信息
    *
    * @return
    */
  override def getNode(): Node = node

  override def lockEngine(requestEngineLock: RequestEngineLock): Option[String] = {
    getSender.ask(requestEngineLock) match {
      case ResponseEngineLock(lockStatus, lock, msg) =>
        if (lockStatus) {
          Some(lock)
        } else {
          error("Failed to get locker: " + msg)
          None
        }
      case _ => None
    }
  }

  override def releaseLock(requestEngineUnlock: RequestEngineUnlock): Unit = {
    getSender.send(requestEngineUnlock)
  }
}
