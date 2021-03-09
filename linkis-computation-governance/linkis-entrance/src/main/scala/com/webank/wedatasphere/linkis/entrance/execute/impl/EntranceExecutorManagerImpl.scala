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

package com.webank.wedatasphere.linkis.entrance.execute.impl

import com.webank.wedatasphere.linkis.entrance.execute._
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory

/**
  * Created by enjoyyin on 2018/9/26.
  */
class EntranceExecutorManagerImpl(groupFactory: GroupFactory,
                                  engineBuilder: EngineBuilder,
                                  engineRequester: EngineRequester,
                                  engineSelector: EngineSelector,
                                  engineManager: EngineManager,
                                  entranceExecutorRulers: Array[EntranceExecutorRuler]) extends EntranceExecutorManager(groupFactory) {

  engineManager.setEntranceExecutorManager(this)
  engineRequester.setEngineBuilder(engineBuilder)
  engineRequester.setExecutorListener(engineManager)
  private val _entranceExecutorRulers = entranceExecutorRulers.map(_.cloneNew())
  _entranceExecutorRulers.foreach(_.setEngineManager(engineManager))
  info("load entranceExecutorRules => " + _entranceExecutorRulers.toList)

  override def getOrCreateEngineBuilder(): EngineBuilder = engineBuilder

  override def getOrCreateEngineManager(): EngineManager = engineManager

  override def getOrCreateEngineRequester(): EngineRequester = engineRequester

  override def getOrCreateEngineSelector(): EngineSelector = engineSelector

  override def getOrCreateEntranceExecutorRulers(): Array[EntranceExecutorRuler] = _entranceExecutorRulers

  override def getOrCreateInterceptors(): Array[ExecuteRequestInterceptor] = Array(JobExecuteRequestInterceptor,
    LockExecuteRequestInterceptor, ReconnectExecuteRequestInterceptor, StorePathExecuteRequestInterceptor, RuntimePropertiesExecuteRequestInterceptor)
}