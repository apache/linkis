package com.webank.wedatasphere.linkis.manager.common.protocol.engine

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.protocol.RetryableProtocol
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol

/**
  * @author peacewong
  * @date 2021/1/1 15:50
  */
/**
  * engineConn to ecm
  *
  * @param serviceInstance
  * @param status starting running
  * @param initErrorMsg
  */
case class EngineConnStatusCallback(serviceInstance: ServiceInstance, ticketId: String, status: NodeStatus, initErrorMsg: String) extends RequestProtocol

/**
  * engineConnManager send to Manager
  */
case class EngineConnStatusCallbackToAM(serviceInstance: ServiceInstance, status: NodeStatus, initErrorMsg: String) extends RequestProtocol with RetryableProtocol
