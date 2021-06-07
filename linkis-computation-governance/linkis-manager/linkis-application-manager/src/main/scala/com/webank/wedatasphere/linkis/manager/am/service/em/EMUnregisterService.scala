package com.webank.wedatasphere.linkis.manager.am.service.em

import com.webank.wedatasphere.linkis.manager.common.protocol.em.{EMInfoClearRequest, StopEMRequest}
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext


trait EMUnregisterService {

  def stopEM(stopEMRequest: StopEMRequest, smc: ServiceMethodContext): Unit

  def clearEMInstanceInfo(emClearRequest: EMInfoClearRequest): Unit

}
