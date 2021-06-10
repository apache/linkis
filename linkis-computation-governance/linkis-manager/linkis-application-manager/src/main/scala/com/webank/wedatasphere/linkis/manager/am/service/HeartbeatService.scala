package com.webank.wedatasphere.linkis.manager.am.service

import com.webank.wedatasphere.linkis.manager.common.protocol.node.NodeHeartbeatMsg


trait HeartbeatService {

  def heartbeatEventDeal(nodeHeartbeatMsg: NodeHeartbeatMsg): Unit

}
