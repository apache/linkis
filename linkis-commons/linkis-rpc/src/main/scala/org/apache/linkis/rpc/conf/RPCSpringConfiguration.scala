/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.rpc.conf

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.rpc.RPCReceiveRestful
import org.apache.linkis.rpc.interceptor.RPCServerLoader
import org.apache.linkis.rpc.sender.spring.SpringRPCServerLoader
import org.apache.linkis.server.conf.ServerConfiguration

import org.apache.commons.lang3.StringUtils

import org.springframework.boot.autoconfigure.condition.{
  ConditionalOnClass,
  ConditionalOnMissingBean
}
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.context.event.EventListener

@Configuration
@EnableFeignClients
class RPCSpringConfiguration extends Logging {

  @Bean(Array("rpcServerLoader"))
  @ConditionalOnClass(Array(classOf[DiscoveryClient]))
  @ConditionalOnMissingBean
  def createRPCServerLoader(): RPCServerLoader = new SpringRPCServerLoader

  @EventListener
  def completeInitialize(applicationPreparedEvent: ApplicationPreparedEvent): Unit = {
    val restfulClasses = ServerConfiguration.BDP_SERVER_RESTFUL_REGISTER_CLASSES.getValue
    val rpcRestfulName = applicationPreparedEvent.getApplicationContext
      .getBean(classOf[RPCReceiveRestful])
      .getClass
      .getName
    if (StringUtils.isEmpty(restfulClasses)) {
      DataWorkCloudApplication.setProperty(
        ServerConfiguration.BDP_SERVER_RESTFUL_REGISTER_CLASSES.key,
        rpcRestfulName
      )
    } else {
      DataWorkCloudApplication.setProperty(
        ServerConfiguration.BDP_SERVER_RESTFUL_REGISTER_CLASSES.key,
        restfulClasses +
          "," + rpcRestfulName
      )
    }
    logger.info("DataWorkCloud RPC need register RPCReceiveRestful, now add it to configuration.")
  }

}
