package com.webank.wedatasphere.linkis.manager.am.selector

import com.webank.wedatasphere.linkis.manager.am.selector.rule.NodeSelectRule
import com.webank.wedatasphere.linkis.manager.common.entity.node.Node


trait NodeSelector {

  def choseNode(nodes: Array[Node]): Option[Node]

  def getNodeSelectRules(): Array[NodeSelectRule]

  def addNodeSelectRule(nodeSelectRule: NodeSelectRule): Unit

}
