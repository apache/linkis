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

package org.apache.linkis.entrance.listener

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.entrance.scheduler.EntranceGroupFactory
import org.apache.linkis.protocol.BroadcastProtocol
import org.apache.linkis.protocol.label.EntranceGroupCacheClearBroadcast
import org.apache.linkis.rpc.BroadcastListener
import org.apache.linkis.rpc.Sender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Entrance Group缓存清除广播监听器
 *
 * 核心职责：
 *   1. 接收EntranceGroupCacheClearBroadcast广播消息 2. 检查功能开关是否启用 3. 调用EntranceGroupFactory清除所有Group缓存 4.
 *      记录清除日志，便于监控和排查
 *
 * 注意：只有在 linkis.entrance.group.cache.clear.enabled=true 时才会处理缓存清理广播
 */
@Service
class EntranceGroupCacheClearBroadcastListener extends BroadcastListener with Logging {

  @Autowired private var entranceGroupFactory: EntranceGroupFactory = _

  override def onBroadcastEvent(protocol: BroadcastProtocol, sender: Sender): Unit = {
    protocol match {
      case clear: EntranceGroupCacheClearBroadcast =>
        logger.info(s"Received cache clear broadcast from ${clear.instance} at ${clear.timestamp}")
        if (Configuration.ENTRANCE_GROUP_CACHE_CLEAR_ENABLED) {
          try {
            // 清除所有Group缓存
            entranceGroupFactory.clearAllGroupCache()
            logger.info(s"Successfully cleared all Group cache. Broadcast from: ${clear.instance}")
          } catch {
            case e: Exception =>
              logger.error(s"Failed to clear Group cache. Broadcast from: ${clear.instance}", e)
            // 不抛出异常，避免影响广播流程
          }
        } else {
          logger.info(
            s"Group cache clear feature is disabled, ignoring broadcast from ${clear.instance}"
          )
        }
      case _ =>
      // 忽略其他类型的广播消息
    }
  }

}
