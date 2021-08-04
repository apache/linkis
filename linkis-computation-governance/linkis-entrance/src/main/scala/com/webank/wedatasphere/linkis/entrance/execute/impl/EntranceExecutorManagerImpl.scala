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
import com.webank.wedatasphere.linkis.orchestrator.ecm.EngineConnManager
import com.webank.wedatasphere.linkis.scheduler.listener.ExecutorListener
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory


class EntranceExecutorManagerImpl(groupFactory: GroupFactory,
                                  engineConnManager: EngineConnManager) extends EntranceExecutorManager(groupFactory) {


  override def getOrCreateInterceptors(): Array[ExecuteRequestInterceptor] = Array(JobExecuteRequestInterceptor,
    LabelExecuteRequestInterceptor, ReconnectExecuteRequestInterceptor, StorePathExecuteRequestInterceptor, RuntimePropertiesExecuteRequestInterceptor)

  override def setExecutorListener(engineListener: ExecutorListener): Unit = {}
}