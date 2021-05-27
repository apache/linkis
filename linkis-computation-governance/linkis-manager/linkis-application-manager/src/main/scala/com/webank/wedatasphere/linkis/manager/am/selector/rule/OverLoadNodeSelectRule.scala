package com.webank.wedatasphere.linkis.manager.am.selector.rule

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.NodeOverLoadInfo
import com.webank.wedatasphere.linkis.manager.common.entity.node.{AMNode, Node}
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
  * @author peacewong
  * @date 2020/7/15 16:51
  */
@Component
@Order(3)
class OverLoadNodeSelectRule extends NodeSelectRule with Logging {

  override def ruleFiltering(nodes: Array[Node]): Array[Node] = {
    if (null != nodes)
      nodes.sortWith(sortByOverload)
    else
      nodes
  }

  /**
    * sort by sortByOverload
    *
    * @param nodeA
    * @param nodeB
    * @return
    */
  private def sortByOverload(nodeA: Node, nodeB: Node): Boolean = {
    nodeA match {
      case node: AMNode if nodeB.isInstanceOf[AMNode] =>
        Utils.tryCatch(getOverload(node.getNodeOverLoadInfo) < getOverload(nodeB.asInstanceOf[AMNode].getNodeOverLoadInfo)) {
          t: Throwable =>
            warn("Failed to Compare resource ", t)
            true
        }

      case _ => false
    }
  }

  private def getOverload(overloadInfo: NodeOverLoadInfo): Float = {
    if (overloadInfo == null) 0f else overloadInfo.getUsedMemory * 1f / overloadInfo.getMaxMemory
  }

}
