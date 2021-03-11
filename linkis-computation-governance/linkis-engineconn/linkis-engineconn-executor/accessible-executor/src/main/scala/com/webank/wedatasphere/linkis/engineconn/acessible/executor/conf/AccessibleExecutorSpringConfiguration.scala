/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.engineconn.acessible.executor.conf

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.info.{DefaultNodeOverLoadInfoManager, NodeOverLoadInfoManager}
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.service.{EngineConnConcurrentLockService, EngineConnTimedLockService, LockService}
import com.webank.wedatasphere.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class AccessibleExecutorSpringConfiguration extends Logging {

  private val asyncListenerBusContext = ExecutorListenerBusContext.getExecutorListenerBusContext().getEngineConnAsyncListenerBus

  @Bean(Array("lockService"))
  @ConditionalOnMissingBean
  def createLockManager(): LockService = {

    val lockService = if (AccessibleExecutorConfiguration.ENGINECONN_SUPPORT_PARALLELISM.getValue) new EngineConnConcurrentLockService
    else new EngineConnTimedLockService
    asyncListenerBusContext.addListener(lockService)
    lockService
  }

  @Bean
  @ConditionalOnMissingBean(Array(classOf[NodeOverLoadInfoManager]))
  def createNodeOverLoadInfoManager(): NodeOverLoadInfoManager = {

    new DefaultNodeOverLoadInfoManager
  }


}
