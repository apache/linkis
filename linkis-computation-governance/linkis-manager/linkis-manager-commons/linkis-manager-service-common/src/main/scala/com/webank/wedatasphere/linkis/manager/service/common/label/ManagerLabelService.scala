package com.webank.wedatasphere.linkis.manager.service.common.label

import java.util

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.label.entity.Label

/**
  * @author peacewong
  * @date 2020/8/5 14:38
  */
trait ManagerLabelService {

  def isEngine(serviceInstance: ServiceInstance): Boolean

  def isEngine(labels: util.List[Label[_]]): Boolean

  def isEM(serviceInstance: ServiceInstance): Boolean

}
