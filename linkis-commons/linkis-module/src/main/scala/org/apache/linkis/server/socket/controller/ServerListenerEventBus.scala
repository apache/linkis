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

package org.apache.linkis.server.socket.controller

import org.apache.linkis.common.listener.ListenerEventBus
import org.apache.linkis.errorcode.LinkisModuleErrorCodeSummary._
import org.apache.linkis.server.exception.BDPServerErrorException

import org.apache.commons.lang3.StringUtils

class ServerListenerEventBus(
    eventQueueCapacity: Int,
    name: String,
    listenerConsumerThreadSize: Int,
    listenerThreadMaxFreeTime: Long
) extends ListenerEventBus[ServerEventService, SocketServerEvent](eventQueueCapacity, name)(
      listenerConsumerThreadSize,
      listenerThreadMaxFreeTime
    ) {

  /**
   * Post an event to the specified listener. `onPostEvent` is guaranteed to be called in the same
   * thread for all listeners.
   */
  override protected def doPostEvent(
      listener: ServerEventService,
      event: SocketServerEvent
  ): Unit = {
    val serverEvent = event.serverEvent
    if (StringUtils.isEmpty(serverEvent.getMethod)) {
      logger.info("ignore empty method with " + serverEvent.getData)
    } else if (serverEvent.getMethod.startsWith(listener.serviceName)) {
      val response = listener.onEvent(serverEvent)
      if (response != null) {
        response.setMethod(serverEvent.getMethod)
        event.socket.sendMessage(response)
      }
    }
  }

  override protected val dropEvent: DropEvent = new DropEvent {

    override def onDropEvent(event: SocketServerEvent): Unit = throw new BDPServerErrorException(
      WEBSOCKET_STOPPED.getErrorCode,
      WEBSOCKET_STOPPED.getErrorDesc
    )

    override def onBusStopped(event: SocketServerEvent): Unit = throw new BDPServerErrorException(
      WEBSOCKET_IS_FULL.getErrorCode,
      WEBSOCKET_IS_FULL.getErrorDesc
    )

  }

}
