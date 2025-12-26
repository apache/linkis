/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconnplugin.flink.config

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.service.{
  EngineConnConcurrentLockService,
  EngineConnTimedLockService,
  LockService
}
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext
import org.apache.linkis.engineconnplugin.flink.util.ManagerUtil

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class FlinkSrpingConfiguration extends Logging {

  private val asyncListenerBusContext =
    ExecutorListenerBusContext.getExecutorListenerBusContext().getEngineConnAsyncListenerBus

  @Bean(Array("lockService"))
  @ConditionalOnMissingBean
  def createLockManager(): LockService = {

    val lockService =
      if (ManagerUtil.isManager) {
        logger.info("Engine is manager, supports parallelism.")
        new EngineConnConcurrentLockService
      } else {
        logger.info("Engine is not manager, doesn't support parallelism.")
        new EngineConnTimedLockService
      }
    asyncListenerBusContext.addListener(lockService)
    FlinkLockerServiceHolder.registerLockService(lockService)
    lockService
  }

}

object FlinkLockerServiceHolder extends Logging {

  private var lockService: LockService = _

  def registerLockService(service: LockService): Unit = {
    Utils.tryAndError {
      if (null != service) {
        if (null == lockService) {
          logger.info(s"Will register lockService : ${service.getClass.getName}")
          lockService = service
        } else {
          logger.warn(
            s"Default lockService has been registered to ${lockService.getClass.getName}, will not register : ${service.getClass.getName}"
          )
        }
      } else {
        logger.warn("Cannot register null lockService")
      }
    }
  }

  def getDefaultLockService(): LockService = lockService

}
