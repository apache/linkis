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

package org.apache.linkis.ecm.server.spring

import org.apache.linkis.ecm.core.listener.ECMEventListener
import org.apache.linkis.ecm.server.context.{DefaultECMContext, ECMContext}
import org.apache.linkis.ecm.server.service.{EngineConnKillService, _}
import org.apache.linkis.ecm.server.service.impl._

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.core.env.Environment

@Configuration
class ECMSpringConfiguration {

  @Autowired
  private var env: Environment = _;

  @Bean
  @ConditionalOnMissingBean
  def getDefaultEngineConnManagerContext: ECMContext = {
    new DefaultECMContext
  }

  @Bean
  @ConditionalOnMissingBean
  def getBmlResourceLocalizationService(
      localDirsHandleService: LocalDirsHandleService
  ): ResourceLocalizationService = {
    val service: BmlResourceLocalizationService = new BmlResourceLocalizationService
    service.setLocalDirsHandleService(localDirsHandleService)
    service.setSpringEnv(env)
    service
  }

  @Bean
  @ConditionalOnMissingBean
  def getDefaultlocalDirsHandleService: LocalDirsHandleService = {
    new DefaultLocalDirsHandleService
  }

  @Bean
  @ConditionalOnMissingBean
  def getLinuxProcessEngineConnLaunchService(
      resourceLocalizationService: ResourceLocalizationService,
      localDirsHandleService: LocalDirsHandleService
  ): EngineConnLaunchService = {
    val service = new LinuxProcessEngineConnLaunchService
    service.setResourceLocalizationService(resourceLocalizationService)
    service.setLocalDirsHandleService(localDirsHandleService)
    service
  }

  @Bean
  @ConditionalOnMissingBean
  def getDefaultECMRegisterService(context: ECMContext): ECMRegisterService = {
    val service: DefaultECMRegisterService = new DefaultECMRegisterService
    registerSyncListener(context, service)
    service
  }

  @Bean
  @ConditionalOnMissingBean
  def getDefaultECMHealthService(context: ECMContext): ECMHealthService = {
    val service: DefaultECMHealthService = new DefaultECMHealthService
    registerSyncListener(context, service)
    service
  }

  @Bean
  @ConditionalOnMissingBean
  def getDefaultEngineConnKillService(
  ): EngineConnKillService = {
    val service = new DefaultEngineConnKillService
    service
  }

  @Bean
  @ConditionalOnMissingBean
  def getECMListenerService(
      engineConnKillService: EngineConnKillService,
      context: ECMContext
  ): ECMListenerService = {
    val service: ECMListenerService = new ECMListenerService
    service.setEngineConnKillService(engineConnKillService)
    registerASyncListener(context, service)
    service
  }

  private def registerSyncListener(context: ECMContext, listener: ECMEventListener): Unit = {
    context.getECMSyncListenerBus.addListener(listener)
  }

  private def registerASyncListener(context: ECMContext, listener: ECMEventListener): Unit = {
    context.getECMAsyncListenerBus.addListener(listener)
  }

}
