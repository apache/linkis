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

package org.apache.linkis.orchestrator.ecm.service.impl

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.manager.common.protocol.RequestManagerUnlock
import org.apache.linkis.manager.common.protocol.engine.{EngineCreateError, EngineCreateSuccess}
import org.apache.linkis.orchestrator.ecm.cache.EngineAsyncResponseCache
import org.apache.linkis.orchestrator.ecm.service.EngineAsyncResponseService
import org.apache.linkis.rpc.Sender
import org.apache.linkis.rpc.message.annotation.Receiver

import org.springframework.stereotype.Service

/**
 */
@Service
class DefaultEngineAsyncResponseService extends EngineAsyncResponseService with Logging {

  private val cacheMap = EngineAsyncResponseCache.getCache

  @Receiver
  override def onSuccess(engineCreateSuccess: EngineCreateSuccess, sender: Sender): Unit = {
    logger.info(s"Success to create engine $engineCreateSuccess")
    Utils.tryCatch(cacheMap.put(engineCreateSuccess.getId, engineCreateSuccess)) { t: Throwable =>
      logger.error(s"client could be timeout, now to unlock engineNone", t)
      val requestManagerUnlock = new RequestManagerUnlock(
        engineCreateSuccess.getEngineNode.getServiceInstance,
        engineCreateSuccess.getEngineNode.getLock,
        Sender.getThisServiceInstance
      )
      sender.send(requestManagerUnlock)
    }
  }

  @Receiver
  override def onError(engineCreateError: EngineCreateError, sender: Sender): Unit = {
    logger.info(
      s"Failed to create engine ${engineCreateError.getId}, can retry ${engineCreateError.getRetry}"
    )
    cacheMap.put(engineCreateError.getId, engineCreateError)
  }

}
