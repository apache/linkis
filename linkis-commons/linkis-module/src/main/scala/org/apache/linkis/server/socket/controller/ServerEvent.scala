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

import org.apache.linkis.common.listener.Event
import org.apache.linkis.server.BDPJettyServerHelper
import org.apache.linkis.server.socket.ServerSocket

import java.util

class ServerEvent() extends Event {
  private var id: Int = _
  private var method: String = _
  private var data: util.Map[String, Object] = _
  private var user: String = _
  private var websocketTag: String = _
  def setId(id: Int): Unit = this.id = id
  def getId: Int = id
  def setUser(user: String): Unit = this.user = user
  def setMethod(method: String): Unit = this.method = method
  def getMethod: String = method
  def setData(data: util.Map[String, Object]): Unit = this.data = data
  def getData: util.Map[String, Object] = data
  def getUser: String = user
  def setWebsocketTag(websocketTag: String): Unit = this.websocketTag = websocketTag
  def getWebsocketTag: String = websocketTag
}

class SocketServerEvent(private[controller] val socket: ServerSocket, val message: String)
    extends Event {
  val serverEvent: ServerEvent = SocketServerEvent.getServerEvent(message)
  socket.user.foreach(serverEvent.setUser)
  serverEvent.setId(socket.id)
}

object SocketServerEvent {

  def getServerEvent(message: String): ServerEvent =
    BDPJettyServerHelper.gson.fromJson(message, classOf[ServerEvent])

  def getMessageData(serverEvent: ServerEvent): String =
    BDPJettyServerHelper.gson.toJson(serverEvent.getData)

  def getMessageData(message: String): String = getMessageData(getServerEvent(message))
}
