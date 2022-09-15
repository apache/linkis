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

package org.apache.linkis.manager.am.service.cache

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.governance.common.protocol.conf.{
  RemoveCacheConfRequest,
  RequestQueryEngineConfig,
  RequestQueryEngineConfigWithGlobalConfig,
  RequestQueryGlobalConfig
}
import org.apache.linkis.protocol.BroadcastProtocol
import org.apache.linkis.rpc.{BroadcastListener, Sender}
import org.apache.linkis.rpc.interceptor.common.CacheableRPCInterceptor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ConfCacheRemoveBroadcastListener extends BroadcastListener with Logging {

  @Autowired
  private var cacheableRPCInterceptor: CacheableRPCInterceptor = _

  override def onBroadcastEvent(protocol: BroadcastProtocol, sender: Sender): Unit =
    protocol match {
      case removeCacheConfRequest: RemoveCacheConfRequest =>
        if (removeCacheConfRequest.userCreatorLabel != null) {
          if (removeCacheConfRequest.engineTypeLabel != null) {
            val request = RequestQueryEngineConfig(
              removeCacheConfRequest.userCreatorLabel,
              removeCacheConfRequest.engineTypeLabel
            )
            val globalRequest = RequestQueryEngineConfigWithGlobalConfig(
              removeCacheConfRequest.userCreatorLabel,
              request.engineTypeLabel,
              null
            )
            cacheableRPCInterceptor.removeCache(request.toString)
            cacheableRPCInterceptor.removeCache(globalRequest.toString)
            logger.info(
              s"success to clear cache about configuration of ${removeCacheConfRequest.engineTypeLabel.getStringValue}-${removeCacheConfRequest.userCreatorLabel.getStringValue}"
            )
          } else {
            val request = RequestQueryGlobalConfig(removeCacheConfRequest.userCreatorLabel.getUser)
            cacheableRPCInterceptor.removeCache(request.toString)
            logger.info(
              s"success to clear cache about global configuration of ${removeCacheConfRequest.userCreatorLabel.getUser}"
            )
          }
        }
      case _ =>
    }

}
