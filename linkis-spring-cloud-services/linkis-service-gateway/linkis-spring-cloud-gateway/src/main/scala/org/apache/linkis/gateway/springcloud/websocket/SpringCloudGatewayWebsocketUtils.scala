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
import org.apache.linkis.common.conf.{CommonVars, Configuration, TimeType}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.gateway.http.BaseGatewayContext
import org.apache.linkis.gateway.springcloud.http.SpringCloudGatewayHttpRequest

import org.springframework.cloud.gateway.filter.WebsocketRoutingFilter
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.http.server.reactive.AbstractServerHttpRequest
import org.springframework.web.reactive.socket.{WebSocketMessage, WebSocketSession}
import org.springframework.web.reactive.socket.adapter.AbstractWebSocketSession
import org.springframework.web.server.ServerWebExchange

import java.net.InetSocketAddress
import java.util
import java.util.concurrent.{ConcurrentHashMap, TimeUnit}

import scala.collection.JavaConverters._

import reactor.core.publisher.{Flux, Mono}

object SpringCloudGatewayWebsocketUtils extends Logging {

  val SPRING_CLOUD_GATEWAY_WEBSOCKET_HEARTBEAT =
    CommonVars("wds.linkis.gateway.websocket.heartbeat", new TimeType("5s")).getValue.toLong

  private val changeSchemeMethod = classOf[WebsocketRoutingFilter].getDeclaredMethod(
    "changeSchemeIfIsWebSocketUpgrade",
    classOf[ServerWebExchange]
  )

  private[websocket] val getDelegateMethod =
    classOf[AbstractWebSocketSession[_]].getDeclaredMethod("getDelegate")

  private val getHeadersFiltersMethod =
    classOf[WebsocketRoutingFilter].getDeclaredMethod("getHeadersFilters")

  private val cachedWebSocketSessions =
    new ConcurrentHashMap[String, GatewayWebSocketSessionConnection]

  changeSchemeMethod.setAccessible(true)
  getDelegateMethod.setAccessible(true)
  getHeadersFiltersMethod.setAccessible(true)

  Utils.defaultScheduler.scheduleAtFixedRate(
    new Runnable {

      override def run(): Unit = Utils.tryQuietly {
        cachedWebSocketSessions.asScala
          .filter { case (_, session) =>
            session.removeDeadProxySessions()
            session.canRelease
          }
          .foreach { case (key, session) =>
            logger.info(
              s"remove a dead webSocket connection $key from DWC-UI for user ${session.user}."
            )
            session.release()
            cachedWebSocketSessions.remove(key)
          }
        cachedWebSocketSessions.asScala.foreach(_._2.heartbeat())
      }

    },
    SPRING_CLOUD_GATEWAY_WEBSOCKET_HEARTBEAT,
    SPRING_CLOUD_GATEWAY_WEBSOCKET_HEARTBEAT,
    TimeUnit.MILLISECONDS
  )

  def removeAllGatewayWebSocketSessionConnection(user: String): Unit =
    cachedWebSocketSessions.asScala.filter(_._2.user == user).values.foreach { session =>
      session.release()
    }

  def removeGatewayWebSocketSessionConnection(inetSocketAddress: InetSocketAddress): Unit =
    cachedWebSocketSessions.asScala
      .find(_._2.getAddress == inetSocketAddress)
      .foreach { case (_, session) =>
        session.release()
      }

  private def getWebSocketSessionKey(webSocketSession: WebSocketSession): String =
    webSocketSession match {
      case gatewaySession: GatewayWebSocketSessionConnection =>
        getWebSocketSessionKey(gatewaySession.webSocketSession)
      case _ => webSocketSession.getId
    }

  def getProxyWebSocketSession(
      webSocketSession: WebSocketSession,
      serviceInstance: ServiceInstance
  ): WebSocketSession = {
    val key = getWebSocketSessionKey(webSocketSession)
    if (cachedWebSocketSessions.containsKey(key)) cachedWebSocketSessions synchronized {
      val webSocketSession = cachedWebSocketSessions.get(key)
      if (webSocketSession != null) {
        webSocketSession.getProxyWebSocketSession(serviceInstance).orNull
      } else null
    }
    else null
  }

  def getGatewayWebSocketSessionConnection(
      user: String,
      webSocketSession: WebSocketSession
  ): GatewayWebSocketSessionConnection = {
    val key = getWebSocketSessionKey(webSocketSession)
    if (!cachedWebSocketSessions.containsKey(key)) cachedWebSocketSessions synchronized {
      if (!cachedWebSocketSessions.containsKey(key)) {
        logger.info(s"receive a new webSocket connection $key from DWC-UI for user $user.")
        cachedWebSocketSessions.put(
          key,
          new GatewayWebSocketSessionConnection(webSocketSession, user)
        )
      }
    }
    cachedWebSocketSessions.get(key)
  }

  def setProxyWebSocketSession(
      user: String,
      serviceInstance: ServiceInstance,
      webSocketSession: WebSocketSession,
      proxySession: WebSocketSession
  ): Unit = {
    getGatewayWebSocketSessionConnection(user, webSocketSession).add(serviceInstance, proxySession)
  }

  def getHeadersFilters(
      websocketRoutingFilter: WebsocketRoutingFilter
  ): util.List[HttpHeadersFilter] =
    getHeadersFiltersMethod
      .invoke(websocketRoutingFilter)
      .asInstanceOf[util.List[HttpHeadersFilter]]

  def changeSchemeIfIsWebSocketUpgrade(
      websocketRoutingFilter: WebsocketRoutingFilter,
      exchange: ServerWebExchange
  ): Unit =
    changeSchemeMethod.invoke(websocketRoutingFilter, exchange)

  def getGatewayContext(exchange: ServerWebExchange): BaseGatewayContext = {
    val gatewayContext = new BaseGatewayContext
    gatewayContext.setWebSocketRequest()
    val request = new SpringCloudGatewayHttpRequest(
      exchange.getRequest.asInstanceOf[AbstractServerHttpRequest]
    )
    gatewayContext.setRequest(request)
    gatewayContext.setWebSocketRequest()
    gatewayContext.setResponse(new WebsocketGatewayHttpResponse)
    gatewayContext
  }

  def getWebSocketMessage(bufferFactory: DataBufferFactory, message: String): WebSocketMessage = {
    val dataBuffer = bufferFactory.wrap(message.getBytes(Configuration.BDP_ENCODING.getValue))
    new WebSocketMessage(WebSocketMessage.Type.TEXT, dataBuffer).retain()
  }

  def sendMsg(
      bufferFactory: DataBufferFactory,
      webSocketSession: WebSocketSession,
      message: String
  ): Mono[Void] = {
    val webSocketMessage = getWebSocketMessage(bufferFactory, message)
    webSocketSession.send(Flux.just(Array(webSocketMessage): _*))
  }

  def sendMsg(
      exchange: ServerWebExchange,
      webSocketSession: WebSocketSession,
      message: String
  ): Mono[Void] =
    sendMsg(exchange.getResponse.bufferFactory(), webSocketSession, message)

  def sendMsg(
      exchange: ServerWebExchange,
      webSocketSession: WebSocketSession,
      webSocketMessage: WebSocketMessage
  ): Mono[Void] =
    webSocketSession.send(Flux.just(Array(webSocketMessage.retain()): _*))

}
