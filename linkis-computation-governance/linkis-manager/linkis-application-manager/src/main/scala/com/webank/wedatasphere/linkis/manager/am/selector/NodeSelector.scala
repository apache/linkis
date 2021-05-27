package com.webank.wedatasphere.linkis.manager.am.selector

import com.webank.wedatasphere.linkis.manager.am.selector.rule.NodeSelectRule
import com.webank.wedatasphere.linkis.manager.common.entity.node.Node

/**
  * @author peacewong
  * @date 2020/6/30 22:44
  */
trait NodeSelector {

  def choseNode(nodes: Array[Node]): Option[Node]

  def getNodeSelectRules(): Array[NodeSelectRule]

  def addNodeSelectRule(nodeSelectRule: NodeSelectRule): Unit

}
