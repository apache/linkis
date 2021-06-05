package com.webank.wedatasphere.linkis.manager.am.recycle

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.recycle.{AssignNodeRule, RecyclingRule}
import org.springframework.stereotype.Component


@Component
class AssignNodeRuleExecutor extends RecyclingRuleExecutor {

  override def ifAccept(recyclingRule: RecyclingRule): Boolean = recyclingRule.isInstanceOf[AssignNodeRule]

  override def executeRule(recyclingRule: RecyclingRule): Array[ServiceInstance] = recyclingRule match {
    case AssignNodeRule(serviceInstance, user) =>
      Array(serviceInstance)
    case _ => null
  }

}
