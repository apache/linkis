package com.webank.wedatasphere.linkis.entrance.conf

import com.webank.wedatasphere.linkis.common.utils.{Logging, ShutdownUtils, Utils}
import com.webank.wedatasphere.linkis.entrance.annotation._
import com.webank.wedatasphere.linkis.entrance.execute._
import com.webank.wedatasphere.linkis.resourcemanager.client.ResourceManagerClient
import com.webank.wedatasphere.linkis.resourcemanager.domain.ModuleInfo
import com.webank.wedatasphere.linkis.resourcemanager.{InstanceResource, ResourceRequestPolicy}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.{Scheduler, SchedulerContext}
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelScheduler
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
  def generateEngineRequester(@GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation groupFactory: GroupFactory,
                              @Autowired rmClient: ResourceManagerClient) = {
    new EsEngineRequester(groupFactory, rmClient)
  }

  @Bean(Array("resources"))
  def createResource(@Autowired rmClient: ResourceManagerClient): ModuleInfo = {
    // Clean up resources before creating resources to prevent dirty data when exiting abnormally (创造资源之前进行资源清理，防止异常退出时产生了脏数据)
    Utils.tryQuietly(rmClient.unregister())
    Utils.addShutdownHook({
      info("rmClient shutdown, unregister resource...")
      rmClient.unregister
    })
    val totalResource = new InstanceResource(EsEntranceConfiguration.ENTRANCE_MAX_JOB_INSTANCE.getValue)
    val protectResource = new InstanceResource(EsEntranceConfiguration.ENTRANCE_PROTECTED_JOB_INSTANCE.getValue)
    info(s"create resource for es engine totalResource is $totalResource, protectResource is $protectResource")
    ModuleInfo(Sender.getThisServiceInstance, totalResource, protectResource, ResourceRequestPolicy.Instance)
  }

  @SchedulerBeanAnnotation
  def generateScheduler(@SchedulerContextBeanAnnotation.SchedulerContextAutowiredAnnotation schedulerContext: SchedulerContext): Scheduler = {
    val scheduler = new ParallelScheduler(schedulerContext)
    scheduler.init()
    scheduler.start()
    Utils.addShutdownHook({
      info("scheduler shutdown...")
      scheduler.shutdown
    })
    scheduler
  }

  @EngineManagerBeanAnnotation
  def generateEngineManager(@Autowired resources: ModuleInfo) = new EsEngineManager(resources)

}
