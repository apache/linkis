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

import org.apache.linkis.common.listener.{Event, EventListener}
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.server.{catchIt, BDPJettyServerHelper, Message}

import com.google.gson.Gson

abstract class ServerEventService extends EventListener with Logging {

  protected val gson: Gson = BDPJettyServerHelper.gson

  protected def sendMessage(id: Int, message: Message) =
    BDPJettyServerHelper.getControllerServer.sendMessage(id, message)

  protected def sendMessageToUser(user: String, message: Message): Unit =
    BDPJettyServerHelper.getControllerServer.sendMessageToUser(user, message)

  protected def sendMessageToAll(message: Message): Unit =
    BDPJettyServerHelper.getControllerServer.sendMessageToAll(message)

  val serviceName: String

  logger.info("add a socket ServerEventService: " + getClass.getName)
  BDPJettyServerHelper.addServerEventService(this)

  def onEvent(event: ServerEvent): Message

  def onEventError(event: Event, t: Throwable): Unit = event match {
    case e: SocketServerEvent => onEventError(e, t)
    case _ => logger.error(s"cannot recognize the event type $event.", t)
  }

  def onEventError(event: SocketServerEvent, t: Throwable): Unit = {
    val message = catchIt(throw t)
    event.socket.sendMessage(message << event.serverEvent.getMethod)
  }

}
