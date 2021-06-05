package com.webank.wedatasphere.linkis.manager.am.service.em

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.am.vo.EMNodeVo
import com.webank.wedatasphere.linkis.manager.common.entity.node.EMNode
import com.webank.wedatasphere.linkis.manager.common.protocol.em.GetEMInfoRequest


trait EMInfoService {

  def getEM(getEMInfoRequest: GetEMInfoRequest): EMNode

  def getEM(serviceInstance: ServiceInstance): EMNode

  def getAllEM(): Array[EMNode]

  def updateEMInfo(serviceInstance: ServiceInstance, healthyStatus:String)

}
