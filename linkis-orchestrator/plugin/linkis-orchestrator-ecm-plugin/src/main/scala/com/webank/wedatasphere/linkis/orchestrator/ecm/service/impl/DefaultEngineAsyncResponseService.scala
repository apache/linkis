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

package com.webank.wedatasphere.linkis.orchestrator.ecm.service.impl

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.manager.common.protocol.RequestManagerUnlock
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.{EngineCreateError, EngineCreateSuccess}
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext
import com.webank.wedatasphere.linkis.orchestrator.ecm.cache.EngineAsyncResponseCache
import com.webank.wedatasphere.linkis.orchestrator.ecm.service.EngineAsyncResponseService
import com.webank.wedatasphere.linkis.rpc.Sender
import org.springframework.stereotype.Service

/**
  *
  *
  */
@Service
class DefaultEngineAsyncResponseService extends EngineAsyncResponseService with Logging {

  private val cacheMap = EngineAsyncResponseCache.getCache

  @Receiver
  override def onSuccess(engineCreateSuccess: EngineCreateSuccess, smc: ServiceMethodContext): Unit = {
    info(s"Success to create engine $engineCreateSuccess")
    Utils.tryCatch(cacheMap.put(engineCreateSuccess.id, engineCreateSuccess)) {
      t: Throwable =>
        error(s"client could be timeout, now to unlock engineNone", t)
        val requestManagerUnlock = RequestManagerUnlock(engineCreateSuccess.engineNode.getServiceInstance,
          engineCreateSuccess.engineNode.getLock, Sender.getThisServiceInstance)
        smc.send(requestManagerUnlock)
    }
  }

  @Receiver
  override def onError(engineCreateError: EngineCreateError, smc: ServiceMethodContext): Unit = {
    info(s"Failed to create engine ${engineCreateError.id}")
    cacheMap.put(engineCreateError.id, engineCreateError)
  }

}
