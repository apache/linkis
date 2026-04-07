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

package org.apache.linkis.entrance.protocol

import org.apache.linkis.protocol.BroadcastProtocol

/**
 * Entrance Group缓存清除广播消息
 *
 * 广播时机：Entrance实例offline时（通过/markoffline接口触发） 广播目的：通知所有其他Entrance实例清除本地Group缓存
 * 广播效果：下次任务提交时重新计算并发数，排除offline实例
 *
 * @param instance
 *   offline的Entrance实例标识
 * @param timestamp
 *   广播发送时间戳（毫秒）
 */
case class EntranceGroupCacheClearBroadcast(instance: String, timestamp: Long)
    extends BroadcastProtocol {

  // 不抛出任何异常，即使部分实例接收失败也不影响offline流程
  override val throwsIfAnyFailed: Boolean = false

}
