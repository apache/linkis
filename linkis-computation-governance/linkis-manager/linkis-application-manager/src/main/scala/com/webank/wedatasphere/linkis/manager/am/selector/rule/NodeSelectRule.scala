package com.webank.wedatasphere.linkis.manager.am.selector.rule

import com.webank.wedatasphere.linkis.manager.common.entity.node.Node


trait NodeSelectRule {

  def ruleFiltering(nodes: Array[Node]): Array[Node]

}
