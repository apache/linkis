package com.webank.wedatasphere.linkis.manager.am.service

import com.webank.wedatasphere.linkis.manager.common.protocol.node.NodeHeartbeatMsg

/**
  * @author peacewong
  * @date 2020/7/9 16:42
  */
trait HeartbeatService {

  def heartbeatEventDeal(nodeHeartbeatMsg: NodeHeartbeatMsg): Unit

}
