package com.webank.wedatasphere.linkis.rpc.eureka.conf

import com.netflix.discovery.EurekaClient
import com.webank.wedatasphere.linkis.rpc.interceptor.RPCServerLoader
import com.webank.wedatasphere.linkis.rpc.sender.eureka.EurekaRPCServerLoader
import org.springframework.boot.autoconfigure.condition.{ConditionalOnClass, ConditionalOnMissingBean}
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.{Bean, Configuration}


@Configuration
@EnableFeignClients
class RPCEurekaSpringConfiguration {

  @Bean(Array("rpcServerLoader"))
  @ConditionalOnClass(Array(classOf[EurekaClient]))
  @ConditionalOnMissingBean
  def createRPCServerLoader(): RPCServerLoader = new EurekaRPCServerLoader

}
