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

package com.webank.wedatasphere.linkis.enginemanager.impl

import com.webank.wedatasphere.linkis.enginemanager._
import com.webank.wedatasphere.linkis.enginemanager.conf.EngineManagerConfiguration.ENGINE_SPRING_APPLICATION_NAME
import com.webank.wedatasphere.linkis.enginemanager.conf.EnvConfiguration._
import com.webank.wedatasphere.linkis.enginemanager.hook._
import com.webank.wedatasphere.linkis.enginemanager.process.{JavaProcessEngineBuilder, ProcessEngineBuilder}
import com.webank.wedatasphere.linkis.protocol.engine.RequestEngine
import com.webank.wedatasphere.linkis.resourcemanager.domain.ModuleInfo
import com.webank.wedatasphere.linkis.resourcemanager.{LoadInstanceResource, LoadResource, Resource, ResourceRequestPolicy}
import com.webank.wedatasphere.linkis.rpc.Sender
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * Created by johnnwang on 2018/10/15.
  */
@Configuration
class EngineManagerSpringConfiguration {

  @Bean(Array("engineCreator"))
  @ConditionalOnMissingBean
  def createEngineCreator(): EngineCreator = new AbstractEngineCreator {
    override protected def createProcessEngineBuilder(): ProcessEngineBuilder = new JavaProcessEngineBuilder {
      override protected def getExtractJavaOpts: String = null
      override protected def getAlias(request: RequestEngine): String = ENGINE_SPRING_APPLICATION_NAME.getValue
      override protected def getExtractClasspath: Array[String] = Array.empty
      override protected def classpathCheck(jarOrFiles: Array[String]): Unit = {}
      override protected val addApacheConfigPath: Boolean = true
    }
  }

  @Bean(Array("engineResourceFactory"))
  @ConditionalOnMissingBean
  def createEngineResourceFactory(): EngineResourceFactory = new AbstractEngineResourceFactory {
    override protected def getRequestResource(properties: java.util.Map[String, String]): Resource =
      new LoadResource(ENGINE_CLIENT_MEMORY.getValue(properties).toLong, 1)
  }

  @Bean(name = Array("hooks"))
  @ConditionalOnMissingBean
  def createEngineHook(): Array[EngineHook] = {
    Array(new ConsoleConfigurationEngineHook)// TODO
  }

  @Bean(name = Array("resources"))
  @ConditionalOnMissingBean
  def createResource(): ModuleInfo = {
    val maxResources = new LoadInstanceResource(ENGINE_MANAGER_MAX_MEMORY_AVAILABLE.getValue.toLong, ENGINE_MANAGER_MAX_CORES_AVAILABLE.getValue,
      ENGINE_MANAGER_MAX_CREATE_INSTANCES.getValue)
    ModuleInfo(Sender.getThisServiceInstance, maxResources,
      new LoadInstanceResource(ENGINE_MANAGER_PROTECTED_MEMORY.getValue.toLong, ENGINE_MANAGER_PROTECTED_CORES.getValue,
        ENGINE_MANAGER_PROTECTED_INSTANCES.getValue), ResourceRequestPolicy.LoadInstance)
  }
}
