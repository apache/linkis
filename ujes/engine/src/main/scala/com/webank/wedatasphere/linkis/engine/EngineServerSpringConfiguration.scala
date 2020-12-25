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

package com.webank.wedatasphere.linkis.engine

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engine.annotation.EngineSchedulerContextBeanAnnotation
import com.webank.wedatasphere.linkis.engine.conf.EngineConfiguration.ENGINE_SUPPORT_PARALLELISM
import com.webank.wedatasphere.linkis.engine.execute.hook._
import com.webank.wedatasphere.linkis.engine.execute.{CodeParser, CommonEngineJob, EngineExecutorContext, EngineHook}
import com.webank.wedatasphere.linkis.engine.lock.{EngineConcurrentLockManager, EngineTimedLockManager}
import com.webank.wedatasphere.linkis.protocol.engine.RequestTask
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext
import com.webank.wedatasphere.linkis.scheduler.queue.Job
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * Created by enjoyyin on 2018/10/15.
  */
@Configuration
class EngineServerSpringConfiguration extends Logging {

  @Bean(Array("engineName"))
  @ConditionalOnMissingBean
  def createEngineName(): String = Sender.getThisServiceInstance.getApplicationName

  @Bean(Array("engineParser"))
  @ConditionalOnMissingBean
  def createEngineParser(): EngineParser = new EngineParser {
    override def parseToJob(request: RequestTask): Job = {
      val job = new CommonEngineJob
      job.setRequestTask(request)
      job
    }
  }

  @Bean(Array("codeParser"))
  @ConditionalOnMissingBean
  def createCodeParser(): CodeParser = new CodeParser {
    override def parse(code: String, engineExecutorContext: EngineExecutorContext): Array[String] = Array(code)
  }

  @Bean(Array("engineHooks"))
  @ConditionalOnMissingBean
  def createEngineHooks(): Array[EngineHook] = Array(new ReleaseEngineHook, new MaxExecuteNumEngineHook)

  @Bean(Array("lockManager"))
  @ConditionalOnMissingBean
  def createLockManager(@EngineSchedulerContextBeanAnnotation.EngineSchedulerContextAutowiredAnnotation schedulerContext: SchedulerContext): LockManager =
    if(ENGINE_SUPPORT_PARALLELISM.getValue) new EngineConcurrentLockManager(schedulerContext)
    else new EngineTimedLockManager(schedulerContext)
}