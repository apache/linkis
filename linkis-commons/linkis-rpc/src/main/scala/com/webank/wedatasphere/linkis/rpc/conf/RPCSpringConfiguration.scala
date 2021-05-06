/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.rpc.conf

import com.netflix.discovery.EurekaClient
import com.webank.wedatasphere.linkis.DataWorkCloudApplication
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.rpc.RPCReceiveRestful
import com.webank.wedatasphere.linkis.rpc.interceptor.RPCServerLoader
import com.webank.wedatasphere.linkis.rpc.sender.eureka.EurekaRPCServerLoader
import com.webank.wedatasphere.linkis.server.conf.ServerConfiguration
import org.apache.commons.lang.StringUtils
import org.springframework.boot.autoconfigure.condition.{ConditionalOnClass, ConditionalOnMissingBean}
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.context.event.EventListener


@Configuration
@EnableFeignClients
class RPCSpringConfiguration extends Logging {

  @Bean(Array("rpcServerLoader"))
  @ConditionalOnClass(Array(classOf[EurekaClient]))
  @ConditionalOnMissingBean
  def createRPCServerLoader(): RPCServerLoader = new EurekaRPCServerLoader

  @EventListener
  def completeInitialize(applicationPreparedEvent: ApplicationPreparedEvent): Unit = {
    val restfulClasses = ServerConfiguration.BDP_SERVER_RESTFUL_REGISTER_CLASSES.getValue
    val rpcRestfulName = applicationPreparedEvent.getApplicationContext.getBean(classOf[RPCReceiveRestful]).getClass.getName
    if(StringUtils.isEmpty(restfulClasses))
      DataWorkCloudApplication.setProperty(ServerConfiguration.BDP_SERVER_RESTFUL_REGISTER_CLASSES.key, rpcRestfulName)
    else
      DataWorkCloudApplication.setProperty(ServerConfiguration.BDP_SERVER_RESTFUL_REGISTER_CLASSES.key, restfulClasses +
        "," + rpcRestfulName)
    info("DataWorkCloud RPC need register RPCReceiveRestful, now add it to configuration.")
  }

}
