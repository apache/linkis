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

package org.apache.linkis.server.socket

import org.apache.linkis.common.collection.BlockingLoopArray
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.server.security.SecurityFilter

import javax.servlet.http.HttpServletRequest

import java.util.concurrent.TimeUnit

import org.eclipse.jetty.websocket.api.{Session, WebSocketAdapter}

case class ServerSocket(
    request: HttpServletRequest,
    socketListener: SocketListener,
    protocol: String = ""
) extends WebSocketAdapter {
  private var session: Session = _
  private[socket] var id: Int = _
  val createTime = System.currentTimeMillis
  def user: Option[String] = SecurityFilter.getLoginUser(request)
  // Add a queue to do buffering, can not directly sendMessage back, will lead to the connection can not stand
  // 加一个队列做缓冲，不能直接sendMessage回去，会导致连接受不住
  private val cacheMessages = new BlockingLoopArray[String](100)

  Utils.defaultScheduler.scheduleAtFixedRate(
    new Runnable {

      override def run(): Unit = {
        var message = cacheMessages.poll()
        while (message.isDefined) {
          message.foreach(session.getRemote.sendString)
          message = cacheMessages.poll()
        }
      }

    },
    1000,
    1000,
    TimeUnit.MILLISECONDS
  )

  override def onWebSocketClose(statusCode: Int, reason: String): Unit =
    socketListener.onClose(this, statusCode, reason)

  override def onWebSocketConnect(sess: Session): Unit = {
    session = sess
    socketListener.onOpen(this)
  }

  override def onWebSocketText(message: String): Unit = socketListener.onMessage(this, message)

  def sendMessage(message: String): Unit = {
    cacheMessages.put(message)
  }

  override def toString: String = s"ServerSocket($id, $user)"
}
