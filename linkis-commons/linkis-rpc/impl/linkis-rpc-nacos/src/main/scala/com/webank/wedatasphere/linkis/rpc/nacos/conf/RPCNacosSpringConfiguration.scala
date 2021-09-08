package com.webank.wedatasphere.linkis.rpc.nacos.conf

import com.alibaba.nacos.client.naming.NacosNamingService
import com.webank.wedatasphere.linkis.rpc.interceptor.RPCServerLoader
import com.webank.wedatasphere.linkis.rpc.nacos.sender.NacosRPCServerLoader
import org.springframework.boot.autoconfigure.condition.{ConditionalOnClass, ConditionalOnMissingBean}
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.{Bean, Configuration}


@Configuration
@EnableFeignClients
class RPCNacosSpringConfiguration {

  @Bean(Array("rpcServerLoader"))
  @ConditionalOnClass(Array(classOf[NacosNamingService]))
  @ConditionalOnMissingBean
  def createRPCServerLoader(): RPCServerLoader = new NacosRPCServerLoader

}
