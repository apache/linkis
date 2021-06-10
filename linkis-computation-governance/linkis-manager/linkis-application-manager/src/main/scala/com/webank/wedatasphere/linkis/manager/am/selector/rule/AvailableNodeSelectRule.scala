package com.webank.wedatasphere.linkis.manager.am.selector.rule

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.manager.common.entity.node.{AMNode, Node}
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
  * require node state are idle or busy
  */
@Component
@Order(2)
class AvailableNodeSelectRule extends NodeSelectRule with Logging{

  override def ruleFiltering(nodes: Array[Node]): Array[Node] = {
    if (null != nodes) {
      nodes.filter {
        case amNode: AMNode =>
          if (! NodeStatus.isLocked(amNode.getNodeStatus) && NodeStatus.isAvailable(amNode.getNodeStatus) ) {
              true
            } else {
            info(s"engineConn ${amNode.getServiceInstance} cannot be reuse status: ${amNode.getNodeStatus}")
            false
          }
        case node: Node => NodeStatus.isAvailable(node.getNodeStatus)
      }
    } else {
      nodes
    }
  }


}
