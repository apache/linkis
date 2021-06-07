package com.webank.wedatasphere.linkis.manager.service.common.label

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.label.entity.Label


trait ManagerLabelService {

  def isEngine(serviceInstance: ServiceInstance): Boolean

  def isEngine(labels: util.List[Label[_]]): Boolean

  def isEM(serviceInstance: ServiceInstance): Boolean

}
