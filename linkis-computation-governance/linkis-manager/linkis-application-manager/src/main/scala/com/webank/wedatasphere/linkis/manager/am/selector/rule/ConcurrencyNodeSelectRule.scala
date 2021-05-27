package com.webank.wedatasphere.linkis.manager.am.selector.rule

import com.webank.wedatasphere.linkis.manager.common.entity.node.Node
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
  * @author peacewong
  * @date 2020/7/4 22:54
  */
@Component
@Order(1)
class ConcurrencyNodeSelectRule extends NodeSelectRule {

  override def ruleFiltering(nodes: Array[Node]): Array[Node] = {
    nodes
    //1. 并发选择规则只对Engine有效
    //2. TODO 通过标签判断engine是否支持并发，如果Engine为io,状态，当支持并发engine需要进行保留
  }

}
