package com.webank.wedatasphere.linkis.manager.common.protocol.resource

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.manager.common.entity.resource.NodeResource
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol

/**
  * @author peacewong
  * @date 2020/9/24 10:10
  */
trait ResourceProtocol extends RequestProtocol

case class ResourceUsedProtocol(serviceInstance: ServiceInstance, engineResource: NodeResource, ticketId: String = null) extends RequestProtocol


