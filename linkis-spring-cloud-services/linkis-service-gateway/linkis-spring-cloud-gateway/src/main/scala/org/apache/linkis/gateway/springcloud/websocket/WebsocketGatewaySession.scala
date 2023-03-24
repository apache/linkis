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

package org.apache.linkis.gateway.springcloud.websocket

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.gateway.exception.GatewayErrorException
import org.apache.linkis.gateway.springcloud.errorcode.GatewayErrorCodeSummary
import org.apache.linkis.gateway.springcloud.websocket.SpringCloudGatewayWebsocketUtils._
import org.apache.linkis.server.conf.ServerConfiguration

import org.springframework.core.io.buffer.NettyDataBufferFactory
import org.springframework.web.reactive.socket.{
  CloseStatus,
  HandshakeInfo,
  WebSocketMessage,
  WebSocketSession
}
import org.springframework.web.reactive.socket.adapter.ReactorNettyWebSocketSession
import org.springframework.web.reactive.socket.adapter.ReactorNettyWebSocketSession.WebSocketConnection

import java.net.InetSocketAddress
import java.util.function

import scala.collection.mutable.ArrayBuffer

import io.netty.handler.codec.http.websocketx.WebSocketFrame
import reactor.core.publisher.{Flux, Mono}
import reactor.netty.http.websocket.{WebsocketInbound, WebsocketOutbound}

class GatewayWebSocketSessionConnection(
    val webSocketSession: ReactorNettyWebSocketSession,
    val user: String,
    startTime: Long
) extends GatewayWebSocketSession(webSocketSession) {

  def this(webSocketSession: WebSocketSession, user: String) = this(
    webSocketSession.asInstanceOf[ReactorNettyWebSocketSession],
    user,
    System.currentTimeMillis
  )

  private val proxySessions = new ArrayBuffer[ProxyGatewayWebSocketSession](5)

  def add(serviceInstance: ServiceInstance, proxySession: WebSocketSession): Unit = synchronized {
    if (
        proxySessions.exists(session =>
          session.serviceInstance.getApplicationName == serviceInstance.getApplicationName && session.isAlive
        )
    ) {
      proxySession.close(CloseStatus.SERVER_ERROR)
      throw new GatewayErrorException(
        GatewayErrorCodeSummary.WEBSOCKET_CONNECT_ERROR.getErrorCode,
        s"Create a " +
          s"WebSocket connection for" +
          s" ${serviceInstance.getApplicationName} repeatedly!(重复地为${serviceInstance.getApplicationName}创建WebSocket连接！)"
      )
    }
    proxySession match {
      case reactorSession: ReactorNettyWebSocketSession =>
        logger.info(
          s"create a new ${serviceInstance.getApplicationName}-ProxySession(${proxySession.getId}) for the webSocket connection ${webSocketSession.getId} of user $user."
        )
        proxySessions += ProxyGatewayWebSocketSession(
          reactorSession,
          serviceInstance,
          System.currentTimeMillis
        )
    }
  }

  def getAddress: InetSocketAddress = webSocketSession.getHandshakeInfo.getRemoteAddress;

  def getProxyWebSocketSession(
      serviceInstance: ServiceInstance
  ): Option[ProxyGatewayWebSocketSession] = {
    val proxySession =
      proxySessions.find(_.serviceInstance.getApplicationName == serviceInstance.getApplicationName)
    proxySession.find(p =>
      if (!p.isAlive) {
        proxySessions synchronized proxySessions -= p
        false
      } else true
    )
  }

  def heartbeat(pingMsg: WebSocketMessage): Unit =
    proxySessions.filter(_.isAlive).foreach(_.heartbeat(pingMsg))

  def heartbeat(): Unit = proxySessions.filter(_.isAlive).foreach(_.heartbeat())

  def removeDeadProxySessions(): Unit =
    proxySessions synchronized proxySessions.filterNot(_.isAlive).foreach { proxySession =>
      logger.info(
        s"remove a dead ${proxySession.serviceInstance.getApplicationName}-ProxySession(${proxySession.webSocketSession.getId}) for webSocket connection ${webSocketSession.getId}."
      )
      proxySessions -= proxySession
    }

  def canRelease: Boolean = !isAlive && System.currentTimeMillis - startTime > 30000

  def release(): Unit = {
    proxySessions
      .filter(_.isAlive)
      .foreach(f => Utils.tryQuietly(f.webSocketSession.close().subscribe()))
    if (isAlive) Utils.tryQuietly(webSocketSession.close().subscribe())
  }

  override def close(): Mono[Void] = {
    proxySessions
      .filter(_.isAlive)
      .foreach(f => Utils.tryQuietly(f.webSocketSession.close().subscribe()))
    webSocketSession.close()
  }

  override def close(status: CloseStatus): Mono[Void] = {
    proxySessions
      .filter(_.isAlive)
      .foreach(f => Utils.tryQuietly(f.webSocketSession.close(status).subscribe()))
    webSocketSession.close(status)
  }

}

case class ProxyGatewayWebSocketSession(
    webSocketSession: ReactorNettyWebSocketSession,
    serviceInstance: ServiceInstance,
    startTime: Long
) extends GatewayWebSocketSession(webSocketSession) {
  private var lastPingTime = System.currentTimeMillis

  override def equals(obj: Any): Boolean = if (obj == null) false
  else {
    obj match {
      case w: ProxyGatewayWebSocketSession =>
        if (w.webSocketSession != null) webSocketSession.getId == w.webSocketSession.getId
        else false
      case _ => false
    }
  }

  def heartbeat(pingMsg: WebSocketMessage): Unit =
    if (System.currentTimeMillis - lastPingTime >= SPRING_CLOUD_GATEWAY_WEBSOCKET_HEARTBEAT) {
      webSocketSession.send(Flux.just(Array(pingMsg.retain()): _*)).subscribe()
      lastPingTime = System.currentTimeMillis
    }

  def heartbeat(): Unit = heartbeat(
    new WebSocketMessage(
      WebSocketMessage.Type.PING,
      webSocketSession.bufferFactory().wrap("".getBytes())
    )
  )

  override def hashCode(): Int = super.hashCode()
}

import org.apache.linkis.gateway.springcloud.websocket.GatewayWebSocketSession.getWebSocketConnection

class GatewayWebSocketSession private (
    inbound: WebsocketInbound,
    outbound: WebsocketOutbound,
    info: HandshakeInfo,
    bufferFactory: NettyDataBufferFactory
) extends ReactorNettyWebSocketSession(inbound, outbound, info, bufferFactory) {

  def this(webSocketSession: ReactorNettyWebSocketSession) = {
    this(
      webSocketSession.getInbound,
      webSocketSession.getOutbound,
      webSocketSession.getHandshakeInfo,
      webSocketSession.bufferFactory()
    )
    this.webSocketConnection = webSocketSession
  }

  protected var webSocketConnection: WebSocketConnection = _

  def isAlive: Boolean = !webSocketConnection.getInbound.receiveCloseStatus().subscribe().isDisposed

  override def receive(): Flux[WebSocketMessage] = webSocketConnection.getInbound
    .aggregateFrames(ServerConfiguration.BDP_SERVER_SOCKET_TEXT_MESSAGE_SIZE_MAX.getValue.toInt)
    .receiveFrames
    .map(new function.Function[WebSocketFrame, WebSocketMessage] {
      override def apply(t: WebSocketFrame): WebSocketMessage = toMessage(t)
    })

}

object GatewayWebSocketSession {

  implicit def getWebSocketConnection(
      webSocketSession: ReactorNettyWebSocketSession
  ): WebSocketConnection =
    getDelegateMethod.invoke(webSocketSession).asInstanceOf[WebSocketConnection]

}
