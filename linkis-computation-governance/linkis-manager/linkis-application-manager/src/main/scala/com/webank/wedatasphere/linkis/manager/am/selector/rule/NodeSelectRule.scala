package com.webank.wedatasphere.linkis.manager.am.selector.rule

import com.webank.wedatasphere.linkis.manager.common.entity.node.Node

/**
  * @author peacewong
  * @date 2020/6/30 22:48
  */
trait NodeSelectRule {

  def ruleFiltering(nodes: Array[Node]): Array[Node]

}
