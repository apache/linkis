package com.webank.wedatasphere.linkis.manager.am.selector.rule

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.common.entity.node.{Node, ScoreServiceInstance}
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
  * @author peacewong
  * @date 2020/7/4 22:54
  */
@Component
@Order(0)
class ScoreNodeSelectRule extends NodeSelectRule with Logging {

  override def ruleFiltering(nodes: Array[Node]): Array[Node] = {
    if (null != nodes)
      nodes.sortWith(sortByScore)
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
  private def sortByScore(nodeA: Node, nodeB: Node): Boolean = {
    nodeA match {
      case instance: ScoreServiceInstance if nodeB.isInstanceOf[ScoreServiceInstance] =>
        Utils.tryCatch(instance.getScore > nodeB.asInstanceOf[ScoreServiceInstance].getScore) {
          t: Throwable =>
            warn("Failed to Compare resource ", t)
            true
        }

      case _ => false
    }
  }

}
