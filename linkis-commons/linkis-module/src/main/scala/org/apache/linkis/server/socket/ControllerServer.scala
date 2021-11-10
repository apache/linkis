/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.server.socket

import java.util
import java.util.concurrent.atomic.AtomicInteger

import org.apache.linkis.common.conf.Configuration.DEFAULT_DATE_PATTERN
import org.apache.linkis.common.listener.Event
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.server.Message
import org.apache.linkis.server.conf.ServerConfiguration._
import org.apache.linkis.server.exception.BDPServerErrorException
import org.apache.linkis.server.socket.controller.{ServerListenerEventBus, SocketServerEvent}
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.commons.lang.time.DateFormatUtils
import org.eclipse.jetty.websocket.servlet._

import scala.collection.JavaConversions._


private[server] class ControllerServer(serverListenerEventBus: ServerListenerEventBus)
  extends WebSocketServlet with SocketListener
   with Event with Logging {

  private val socketList = new util.HashMap[Int, ServerSocket](BDP_SERVER_SOCKET_QUEUE_SIZE.getValue)
  private val idGenerator = new AtomicInteger(0)

  override def configure(webSocketServletFactory: WebSocketServletFactory): Unit = {
    webSocketServletFactory.setCreator(new WebSocketCreator {
      override def createWebSocket(servletUpgradeRequest: ServletUpgradeRequest,
                                   servletUpgradeResponse: ServletUpgradeResponse): AnyRef =
        ServerSocket(servletUpgradeRequest.getHttpServletRequest, ControllerServer.this)
    })
  }

  def sendMessage(id: Int, message: Message): Unit = {
    val socket = socketList.get(id)
    if(socket == null) throw new BDPServerErrorException(11004, s"ServerSocket($id) does not exist!(ServerSocket($id)不存在！)")
    socket.sendMessage(message)
  }

  def sendMessageToAll(message: Message): Unit =
    socketList.values().foreach(_.sendMessage(message))

  def sendMessageToUser(user: String, message: Message): Unit =
    socketList.values().filter(s => s != null && s.user.contains(user)).foreach(_.sendMessage(message))

  override def onClose(socket: ServerSocket, code: Int, message: String): Unit = {
    val date = DateFormatUtils.format(socket.createTime, DEFAULT_DATE_PATTERN.getValue)
    if(!socketList.containsKey(socket.id))
      warn(s"$socket created at $date has expired, ignore the close function!")
    else {
      info(s"$socket closed at $date with code $code and message: " + message)
      socketList synchronized {
        if(socketList.containsKey(socket.id)) socketList.remove(socket.id)
      }
    }
  }

  override def onOpen(socket: ServerSocket): Unit = socketList synchronized {
    val index = idGenerator.getAndIncrement()
    socket.id = index
    socketList.put(index, socket)
    info(s"open a new $socket with id $index for user ${socket.user.orNull}!")
  }

  override def onMessage(socket: ServerSocket, message: String): Unit = {
    if(StringUtils.isBlank(message)) {
      socket.sendMessage(Message.error("Empty message!"))
      return
    }
    val socketServerEvent = Utils.tryCatch(new SocketServerEvent(socket, message)){ t =>
      warn("parse message failed!", t)
      socket.sendMessage(Message.error(ExceptionUtils.getRootCauseMessage(t), t))
      return
    }
    if(socket.user.isEmpty && socketServerEvent.serverEvent.getMethod != BDP_SERVER_SOCKET_LOGIN_URI.getValue) {
      socket.sendMessage(Message.noLogin("You are not logged in, please login first!(您尚未登录，请先登录!)").data("websocketTag", socketServerEvent.serverEvent.getWebsocketTag) << socketServerEvent.serverEvent.getMethod)
    } else Utils.tryCatch(serverListenerEventBus.post(socketServerEvent)){
      case t: BDPServerErrorException => Message.error(t.getMessage, t).data("websocketTag", socketServerEvent.serverEvent.getWebsocketTag) << socketServerEvent.serverEvent.getMethod
    }
  }
}
