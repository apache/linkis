package com.webank.wedatasphere.linkis.shell.enginemanager.conf

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.enginemanager.EngineHook
import com.webank.wedatasphere.linkis.enginemanager.conf.EnvConfiguration
import com.webank.wedatasphere.linkis.enginemanager.hook.{ConsoleConfigurationEngineHook, JarLoaderEngineHook}
import com.webank.wedatasphere.linkis.resourcemanager.{LoadInstanceResource, ResourceRequestPolicy}
import com.webank.wedatasphere.linkis.resourcemanager.domain.ModuleInfo
import com.webank.wedatasphere.linkis.rpc.Sender
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * created by cooperyang on 2019/5/14
  * Description:
  */
@Configuration
class ShellEngineManagerConfiguration extends Logging {

  @Bean(Array("resources"))
  def createResource(): ModuleInfo = {
    val totalResource = new LoadInstanceResource(EnvConfiguration.ENGINE_MANAGER_MAX_MEMORY_AVAILABLE.getValue.toLong ,
      EnvConfiguration.ENGINE_MANAGER_MAX_CORES_AVAILABLE.getValue, EnvConfiguration.ENGINE_MANAGER_MAX_CREATE_INSTANCES.getValue)


    val protectResource = new LoadInstanceResource(EnvConfiguration.ENGINE_MANAGER_PROTECTED_MEMORY.getValue.toLong,
      EnvConfiguration.ENGINE_MANAGER_PROTECTED_CORES.getValue, EnvConfiguration.ENGINE_MANAGER_PROTECTED_CORES.getValue)
    info(s"create resource for shell engine totalResource is $totalResource, protectResource is $protectResource")
    ModuleInfo(Sender.getThisServiceInstance, totalResource, protectResource, ResourceRequestPolicy.LoadInstance)
  }

  @Bean(name = Array("hooks"))
  def createEngineHook(): Array[EngineHook] = {
    Array(new ConsoleConfigurationEngineHook)// TODO
  }

}
