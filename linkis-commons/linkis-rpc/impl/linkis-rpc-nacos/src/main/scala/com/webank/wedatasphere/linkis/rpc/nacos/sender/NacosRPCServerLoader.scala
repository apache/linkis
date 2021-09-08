package com.webank.wedatasphere.linkis.rpc.nacos.sender

import com.alibaba.cloud.nacos.NacosServiceInstance

import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.rpc.conf.RPCConfiguration
import com.webank.wedatasphere.linkis.rpc.interceptor.AbstractRPCServerLoader

import scala.concurrent.duration.Duration

class NacosRPCServerLoader extends AbstractRPCServerLoader {

  override val refreshMaxWaitTime: Duration = RPCConfiguration.BDP_RPC_NACOS_SERVICE_REFRESH_MAX_WAIT_TIME.getValue.toDuration

  override def refreshAllServers(): Unit = {

  }

  override def getDWCServiceInstance(serviceInstance: SpringCloudServiceInstance): ServiceInstance = serviceInstance match {
    case instance: NacosServiceInstance =>
      val applicationName = instance.getServiceId
      val host = instance.getHost
      val port = instance.getPort
      ServiceInstance(applicationName, s"$host:$port")
  }

}
