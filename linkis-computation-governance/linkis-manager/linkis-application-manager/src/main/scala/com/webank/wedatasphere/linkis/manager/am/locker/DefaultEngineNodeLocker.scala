package com.webank.wedatasphere.linkis.manager.am.locker

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.entity.node.{AMEngineNode, EngineNode}
import com.webank.wedatasphere.linkis.manager.common.protocol.{RequestEngineLock, RequestEngineUnlock, RequestManagerUnlock}
import com.webank.wedatasphere.linkis.manager.service.common.pointer.NodePointerBuilder
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * @author peacewong
  * @date 2020/7/16 21:05
  */
@Component
class DefaultEngineNodeLocker extends EngineNodeLocker with Logging {

  @Autowired
  private var nodeBuilder: NodePointerBuilder = _

  override def lockEngine(engineNode: EngineNode, timeout: Long): Option[String] = {
    //TODO 判断engine需要的锁类型进行不同的实例化
    nodeBuilder.buildEngineNodePointer(engineNode).lockEngine(RequestEngineLock(timeout))
  }


  override def releaseLock(engineNode: EngineNode, lock: String): Unit = {
    nodeBuilder.buildEngineNodePointer(engineNode).releaseLock(RequestEngineUnlock(lock))
  }

  @Receiver
  def releaseLock(requestManagerUnlock: RequestManagerUnlock): Unit = {
    info(s"client${requestManagerUnlock.clientInstance} Start to unlock engine ${requestManagerUnlock.engineInstance}")
    val engineNode = new AMEngineNode()
    engineNode.setServiceInstance(requestManagerUnlock.engineInstance)
    releaseLock(engineNode, requestManagerUnlock.lock)
    info(s"client${requestManagerUnlock.clientInstance} Finished to unlock engine ${requestManagerUnlock.engineInstance}")
  }

}
