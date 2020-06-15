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

package com.webank.wedatasphere.linkis.enginemanager.configuration

import com.webank.wedatasphere.linkis.enginemanager.EngineHook
import com.webank.wedatasphere.linkis.enginemanager.conf.EnvConfiguration._
import com.webank.wedatasphere.linkis.enginemanager.hook._
import com.webank.wedatasphere.linkis.resourcemanager.domain.ModuleInfo
import com.webank.wedatasphere.linkis.resourcemanager.{DriverAndYarnResource, LoadInstanceResource, ResourceRequestPolicy}
import com.webank.wedatasphere.linkis.rpc.Sender
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * Created by allenlliu on 2019/4/8.
  */
@Configuration
class SparkEngineManagerSpringConfiguration {

  @Bean(Array("resources"))
  def createResource(): ModuleInfo = {
    val totalResource = new DriverAndYarnResource(
      new LoadInstanceResource(ENGINE_MANAGER_MAX_MEMORY_AVAILABLE.getValue.toLong,
        ENGINE_MANAGER_MAX_CORES_AVAILABLE.getValue, ENGINE_MANAGER_MAX_CREATE_INSTANCES.getValue),
      null
    )

    val protectedResource = new DriverAndYarnResource(
      new LoadInstanceResource(ENGINE_MANAGER_PROTECTED_MEMORY.getValue.toLong, ENGINE_MANAGER_PROTECTED_CORES.getValue,
        ENGINE_MANAGER_PROTECTED_INSTANCES.getValue),
      null
    )

    ModuleInfo(Sender.getThisServiceInstance, totalResource, protectedResource, ResourceRequestPolicy.DriverAndYarn)
  }

  @Bean(name = Array("hooks"))
  def createEngineHook(): Array[EngineHook] = {
    Array(new ConsoleConfigurationEngineHook, new JarLoaderEngineHook)
  }
}

