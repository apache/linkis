package com.webank.wedatasphere.linkis.manager.am.pointer

import com.webank.wedatasphere.linkis.common.exception.WarnException
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.protocol.node._
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.manager.service.common.pointer.NodePointer
import com.webank.wedatasphere.linkis.rpc.Sender


abstract class AbstractNodePointer extends NodePointer with Logging {


  protected def getSender: Sender = {
    Sender.getSender(getNode().getServiceInstance)
  }

  /**
    * 向对应的Node发送请求获取节点状态
    *
    * @return
    */
  override def getNodeStatus(): NodeStatus = {
    val sender = getSender
    sender.ask(new RequestNodeStatus) match {
      case responseStatus: ResponseNodeStatus =>
        responseStatus.getNodeStatus
      case warn: WarnException => throw warn
    }
  }

  /**
    * 向对应的Node发送请求获取节点心跳信息
    *
    * @return
    */
  override def getNodeHeartbeatMsg(): NodeHeartbeatMsg = {
    val sender = getSender
    sender.ask(new NodeHeartbeatRequest) match {
      case heartbeatMsg: NodeHeartbeatMsg =>
        heartbeatMsg
      case warn: WarnException => throw warn
    }
  }

  /**
    * 向对应的Node发送Kill 请求
    *
    * @return
    */
  override def stopNode(): Unit = {
    val sender = getSender
    sender.send(new StopNodeRequest)
  }

  /**
    * 向对应的Node Label 更新请求
    *
    * @return
    */
  override def updateLabels(labels: Array[Label[_]]): Unit = ???

}
