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

package com.webank.wedatasphere.linkis.gateway.config

import com.webank.wedatasphere.linkis.gateway.security.{LDAPUserRestful, SecurityFilter, SecurityHook, UserRestful}
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * created by cooperyang on 2019/1/9.
  */
@Configuration
class GatewaySpringConfiguration {

  @Autowired
  private var userRestful: UserRestful = _

  @PostConstruct
  def init(): Unit = {
    SecurityFilter.setUserRestful(userRestful)
  }

//  @Bean(Array("defaultGatewayParser"))
//  @ConditionalOnMissingBean
//  @Autowired(required = false)
//  def createGatewayParser(gatewayParsers: Array[GatewayParser]): DefaultGatewayParser =
//    new DefaultGatewayParser(gatewayParsers)
//
//  @Bean(Array("defaultGatewayRouter"))
//  @ConditionalOnMissingBean
//  def createGatewayRouter(): DefaultGatewayParser = new DefaultGatewayRouter

  @Bean(Array("userRestful"))
  @ConditionalOnMissingBean
  @Autowired(required = false)
  def createUserRestful(securityHooks: Array[SecurityHook]): UserRestful = {
    val userRestful = new LDAPUserRestful
    if(securityHooks != null) userRestful.setSecurityHooks(securityHooks)
    userRestful
  }

}
