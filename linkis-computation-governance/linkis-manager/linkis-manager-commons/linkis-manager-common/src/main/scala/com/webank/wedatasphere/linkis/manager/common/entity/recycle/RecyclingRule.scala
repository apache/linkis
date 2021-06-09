package com.webank.wedatasphere.linkis.manager.common.entity.recycle

import com.webank.wedatasphere.linkis.common.ServiceInstance


trait RecyclingRule {
  val user: String
}

case class AssignNodeRule(serviceInstance: ServiceInstance, override val user: String) extends RecyclingRule

case class AssignUserRule(override val user: String) extends RecyclingRule

case class AssignEMNodeRule(serviceInstance: ServiceInstance, override val user: String) extends RecyclingRule
