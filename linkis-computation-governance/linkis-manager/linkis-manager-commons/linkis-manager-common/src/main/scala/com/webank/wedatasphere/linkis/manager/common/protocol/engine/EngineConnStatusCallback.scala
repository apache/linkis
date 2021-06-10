package com.webank.wedatasphere.linkis.manager.common.protocol.engine

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus
import com.webank.wedatasphere.linkis.protocol.RetryableProtocol
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol


case class EngineConnStatusCallback(serviceInstance: ServiceInstance, ticketId: String, status: NodeStatus, initErrorMsg: String) extends RequestProtocol

/**
  * engineConnManager send to Manager
  */
case class EngineConnStatusCallbackToAM(serviceInstance: ServiceInstance, status: NodeStatus, initErrorMsg: String) extends RequestProtocol with RetryableProtocol
