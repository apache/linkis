package com.webank.wedatasphere.linkis.manager.am.service.em

import com.webank.wedatasphere.linkis.manager.common.protocol.em.RegisterEMRequest
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext

/**
  * @author peacewong
  * @date 2020/8/4 19:52
  */
trait EMRegisterService {


  /**
    * EM注册请求的第一个处理的请求，用于插入Instance信息
    *
    * @param emRegister
    */
  def addEMNodeInstance(emRegister: RegisterEMRequest, scm: ServiceMethodContext): Unit

  /**
    * EM注册插入的初始Metrics信息
    *
    * @param emRegister
    */
  def addEMNodeMetrics(emRegister: RegisterEMRequest): Unit

}
