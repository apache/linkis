/**
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
package com.webank.wedatasphere.linkis.entrance.configuration

import java.util.concurrent.TimeUnit

import com.facebook.presto.client.SocketChannelSocketFactory
import com.webank.wedatasphere.linkis.common.utils.{Logging, ShutdownUtils, Utils}
import com.webank.wedatasphere.linkis.entrance.annotation._
import com.webank.wedatasphere.linkis.entrance.configuration.PrestoConfiguration._
import com.webank.wedatasphere.linkis.entrance.execute._
import com.webank.wedatasphere.linkis.entrance.executor.PrestoEntranceEngineExecutorManager
import com.webank.wedatasphere.linkis.resourcemanager.client.ResourceManagerClient
import com.webank.wedatasphere.linkis.resourcemanager.domain.ModuleInfo
import com.webank.wedatasphere.linkis.resourcemanager.{InstanceAndPrestoResource, InstanceResource, ResourceRequestPolicy}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelScheduler
import com.webank.wedatasphere.linkis.scheduler.{Scheduler, SchedulerContext}
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration}


/**
 * Created by yogafire on 2020/5/13
 */
@Configuration
class PrestoSpringConfiguration extends Logging {

  @EntranceExecutorManagerBeanAnnotation
  def generateEntranceExecutorManager(@GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation groupFactory: GroupFactory,
                                      @EngineBuilderBeanAnnotation.EngineBuilderAutowiredAnnotation engineBuilder: EngineBuilder,
                                      @EngineRequesterBeanAnnotation.EngineRequesterAutowiredAnnotation engineRequester: EngineRequester,
                                      @EngineSelectorBeanAnnotation.EngineSelectorAutowiredAnnotation engineSelector: EngineSelector,
                                      @EngineManagerBeanAnnotation.EngineManagerAutowiredAnnotation engineManager: EngineManager,
                                      @Autowired entranceExecutorRulers: Array[EntranceExecutorRuler]): EntranceExecutorManager =
    new PrestoEntranceEngineExecutorManager(groupFactory, engineBuilder, engineRequester, engineSelector, engineManager, entranceExecutorRulers)

  @SchedulerBeanAnnotation
  def generateScheduler(@SchedulerContextBeanAnnotation.SchedulerContextAutowiredAnnotation schedulerContext: SchedulerContext): Scheduler = {
    val scheduler = new ParallelScheduler(schedulerContext)
    scheduler.init()
    scheduler.start()
    ShutdownUtils.addShutdownHook({
      info("scheduler shutdown...")
      scheduler.shutdown()
    })
    scheduler
  }

  @Bean(Array("resources"))
  def createResource(@Autowired rmClient: ResourceManagerClient): ModuleInfo = {
    Utils.tryQuietly(rmClient.unregister())
    Utils.addShutdownHook({
      info("rmClient shutdown, unregister resource..")
      rmClient.unregister()
    })
    val totalResource = new InstanceAndPrestoResource(new InstanceResource(ENTRANCE_MAX_JOB_INSTANCE.getValue), null)
    val protectResource = new InstanceAndPrestoResource(new InstanceResource(ENTRANCE_PROTECTED_JOB_INSTANCE.getValue), null)
    info(s"create presto entrance resources. totalResource is $totalResource, protectResource is $protectResource")
    ModuleInfo(Sender.getThisServiceInstance, totalResource, protectResource, ResourceRequestPolicy.InstanceAndPresto)
  }

  @EngineManagerBeanAnnotation
  def generateEngineManager(@Autowired resources: ModuleInfo) = new PrestoEntranceManager(resources)

  @Bean
  def okHttpClient(): OkHttpClient = {
    new OkHttpClient.Builder().socketFactory(new SocketChannelSocketFactory)
      .connectTimeout(PRESTO_HTTP_CONNECT_TIME_OUT.getValue, TimeUnit.SECONDS)
      .readTimeout(PRESTO_HTTP_READ_TIME_OUT.getValue, TimeUnit.SECONDS)
      .build()
  }

}
