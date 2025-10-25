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

package org.apache.linkis.manager.engineplugin.shell.configuration

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.acessible.executor.info.{
  DefaultNodeOverLoadInfoManager,
  NodeOverLoadInfoManager
}
import org.apache.linkis.engineconn.acessible.executor.service.{
  EngineConnConcurrentLockService,
  EngineConnTimedLockService,
  LockService
}
import org.apache.linkis.engineconn.executor.listener.ExecutorListenerBusContext

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class AccessibleExecutorConfiguration extends Logging {

  @Bean(Array("lockService"))
  def createLockManager(): LockService = {
    val lockService = new EngineConnConcurrentLockService
    logger.info("use shell engine conn configuration to create concurrent lockService")
    lockService
  }

}
