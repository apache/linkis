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

import com.webank.wedatasphere.linkis.enginemanager.EngineHook
import com.webank.wedatasphere.linkis.enginemanager.conf.EnvConfiguration
import com.webank.wedatasphere.linkis.enginemanager.hook.{ConsoleConfigurationEngineHook, PyFunctionEngineHook, PyUdfEngineHook}
import com.webank.wedatasphere.linkis.resourcemanager.domain.ModuleInfo
import com.webank.wedatasphere.linkis.resourcemanager.{LoadInstanceResource, ResourceRequestPolicy}
import com.webank.wedatasphere.linkis.rpc.Sender
import org.springframework.context.annotation.{Bean, Configuration}
/**
  * Created by allenlliu on 2019/4/8.
  */

@Configuration
class PythonEngineManagerSpringConfiguration {
  @Bean(Array("resources"))
 def createResource(): ModuleInfo = {
   val totalresource =
     new LoadInstanceResource(EnvConfiguration.ENGINE_MANAGER_MAX_MEMORY_AVAILABLE.getValue.toLong ,
       EnvConfiguration.ENGINE_MANAGER_MAX_CORES_AVAILABLE.getValue, EnvConfiguration.ENGINE_MANAGER_MAX_CREATE_INSTANCES.getValue)

    val protectresource = new LoadInstanceResource(
      EnvConfiguration.ENGINE_MANAGER_PROTECTED_MEMORY.getValue.toLong,
      EnvConfiguration.ENGINE_MANAGER_PROTECTED_CORES.getValue, EnvConfiguration.ENGINE_MANAGER_PROTECTED_CORES.getValue
    )


    ModuleInfo(Sender.getThisServiceInstance, totalresource,protectresource,ResourceRequestPolicy.LoadInstance)


  }

  @Bean(name = Array("hooks"))
  def createEngineHook(): Array[EngineHook] = {
    Array(new ConsoleConfigurationEngineHook, new PyUdfEngineHook, new PyFunctionEngineHook)// TODO
  }


}
