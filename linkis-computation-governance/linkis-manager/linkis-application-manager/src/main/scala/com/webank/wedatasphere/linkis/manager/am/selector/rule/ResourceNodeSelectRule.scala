package com.webank.wedatasphere.linkis.manager.am.selector.rule

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.common.entity.node.{Node, RMNode}
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
  * @author peacewong
  * @date 2020/7/4 22:54
  */
@Component
@Order(5)
class ResourceNodeSelectRule extends NodeSelectRule with Logging {

  override def ruleFiltering(nodes: Array[Node]): Array[Node] = {
    if (null != nodes)
      nodes.sortWith(sortByResource)
    else
      nodes
  }

  /**
    * sort by label score
    *
    * @param nodeA
    * @param nodeB
    * @return
    */
  private def sortByResource(nodeA: Node, nodeB: Node): Boolean = {
    nodeA match {
      case node: RMNode if nodeB.isInstanceOf[RMNode] =>
        Utils.tryCatch {
          val nodeBRM = nodeB.asInstanceOf[RMNode]
          if (null == node.getNodeResource || null == node.getNodeResource.getLeftResource) {
            false
          } else if (null == nodeBRM.getNodeResource || null == nodeBRM.getNodeResource.getLeftResource) {
            true
          } else {
            node.getNodeResource.getLeftResource > nodeBRM.getNodeResource.getLeftResource
          }
        } {
          t: Throwable =>
            warn(s"Failed to Compare resource ${t.getMessage}")
            true
        }
      case _ => false
    }
  }


}
