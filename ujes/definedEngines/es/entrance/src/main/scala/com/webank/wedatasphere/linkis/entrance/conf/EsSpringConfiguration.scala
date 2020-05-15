package com.webank.wedatasphere.linkis.entrance.conf

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.annotation._
import com.webank.wedatasphere.linkis.entrance.execute._
import com.webank.wedatasphere.linkis.entrance.execute.impl.EngineRequesterImpl
import com.webank.wedatasphere.linkis.resourcemanager.domain.ModuleInfo
import com.webank.wedatasphere.linkis.resourcemanager.{InstanceResource, ResourceRequestPolicy}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class EsSpringConfiguration extends Logging{

  @EntranceExecutorManagerBeanAnnotation
  def generateEntranceExecutorManager(@GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation groupFactory: GroupFactory,
                                      @EngineBuilderBeanAnnotation.EngineBuilderAutowiredAnnotation engineBuilder: EngineBuilder,
                                      @EngineRequesterBeanAnnotation.EngineRequesterAutowiredAnnotation engineRequester: EngineRequester,
                                      @EngineSelectorBeanAnnotation.EngineSelectorAutowiredAnnotation engineSelector: EngineSelector,
                                      @EngineManagerBeanAnnotation.EngineManagerAutowiredAnnotation engineManager: EngineManager,
                                      @Autowired entranceExecutorRulers: Array[EntranceExecutorRuler]): EntranceExecutorManager =
    new EsEntranceExecutorManager(groupFactory, engineBuilder, engineRequester, engineSelector, engineManager, entranceExecutorRulers)

  @EngineRequesterBeanAnnotation
  def generateEngineRequester(@GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation groupFactory: GroupFactory) = new EsEngineRequester(groupFactory)


  @Bean(Array("resources"))
  def createResource(): ModuleInfo = {
    val totalResource = new InstanceResource(EsEntranceConfiguration.ENTRANCE_MAX_JOB_INSTANCE.getValue)
    val protectResource = new InstanceResource(EsEntranceConfiguration.ENTRANCE_PROTECTED_JOB_INSTANCE.getValue)
    info(s"create resource for es engine totalResource is $totalResource, protectResource is $protectResource")
    ModuleInfo(Sender.getThisServiceInstance, totalResource, protectResource, ResourceRequestPolicy.LoadInstance)
  }

  @EngineManagerBeanAnnotation
  def generateEngineManager(@Autowired resources: ModuleInfo): EngineManager = new EsEngineManager(resources)

}
