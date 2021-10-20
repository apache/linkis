/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.linkis.ecm.server.service._
import org.apache.linkis.ecm.server.service.impl._
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.{Bean, Configuration}


@Configuration
class ECMSpringConfiguration {

  @Bean
  @ConditionalOnMissingBean
  def getDefaultEngineConnManagerContext: ECMContext = {
    new DefaultECMContext
  }

  @Bean
  @ConditionalOnMissingBean
  def getDefaultYarnCallbackService: YarnCallbackService = {
    new DefaultYarnCallbackService
  }

  @Bean
  @ConditionalOnMissingBean
  def getBmlResourceLocalizationService(context: ECMContext, localDirsHandleService: LocalDirsHandleService): ResourceLocalizationService = {
    val service: BmlResourceLocalizationService = new BmlResourceLocalizationService
    service.setLocalDirsHandleService(localDirsHandleService)
    service
  }

  @Bean
  @ConditionalOnMissingBean
  def getDefaultLogCallbackService: LogCallbackService = {
    null
  }

  @Bean
  @ConditionalOnMissingBean
  def getDefaultlocalDirsHandleService: LocalDirsHandleService = {
    new DefaultLocalDirsHandleService
  }


  @Bean
  @ConditionalOnMissingBean
  def getDefaultEngineConnPidCallbackService: EngineConnPidCallbackService = {
    new DefaultEngineConnPidCallbackService
  }

  @Bean
  @ConditionalOnMissingBean
  def getDefaultEngineConnListService(context: ECMContext): EngineConnListService = {
    implicit val service: DefaultEngineConnListService = new DefaultEngineConnListService
    registerSyncListener(context)
    service
  }

  @Bean
  @ConditionalOnMissingBean
  def getLinuxProcessEngineConnLaunchService(resourceLocalizationService: ResourceLocalizationService): EngineConnLaunchService = {
    val service = new LinuxProcessEngineConnLaunchService
    service.setResourceLocalizationService(resourceLocalizationService)
    service
  }

  @Bean
  @ConditionalOnMissingBean
  def getDefaultECMRegisterService(context: ECMContext): ECMRegisterService = {
    implicit val service: DefaultECMRegisterService = new DefaultECMRegisterService
    registerSyncListener(context)
    service
  }


  @Bean
  @ConditionalOnMissingBean
  def getDefaultECMHealthService(context: ECMContext): ECMHealthService = {
    implicit val service: DefaultECMHealthService = new DefaultECMHealthService
    registerSyncListener(context)
    service
  }

  @Bean
  @ConditionalOnMissingBean
  def getDefaultEngineConnKillService(engineConnListService: EngineConnListService): EngineConnKillService = {
    val service = new DefaultEngineConnKillService
    service.setEngineConnListService(engineConnListService)
    service
  }

  private def registerSyncListener(context: ECMContext)(implicit listener: ECMEventListener): Unit = {
    context.getECMSyncListenerBus.addListener(listener)
  }

  private def registerASyncListener(context: ECMContext)(implicit listener: ECMEventListener): Unit = {
    context.getECMAsyncListenerBus.addListener(listener)
  }

}
