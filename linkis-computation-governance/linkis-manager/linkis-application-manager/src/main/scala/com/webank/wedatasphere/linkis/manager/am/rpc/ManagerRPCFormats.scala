package com.webank.wedatasphere.linkis.manager.am.rpc

import com.webank.wedatasphere.linkis.manager.common.entity.resource.ResourceSerializer
import com.webank.wedatasphere.linkis.manager.common.serializer.NodeResourceSerializer
import com.webank.wedatasphere.linkis.resourcemanager.ResultResourceSerializer
import com.webank.wedatasphere.linkis.rpc.transform.RPCFormats
import org.json4s.Serializer
import org.springframework.stereotype.Component


@Component
class ManagerRPCFormats extends RPCFormats {

  override def getSerializers: Array[Serializer[_]] = Array(ResultResourceSerializer, ResourceSerializer, NodeResourceSerializer)
}