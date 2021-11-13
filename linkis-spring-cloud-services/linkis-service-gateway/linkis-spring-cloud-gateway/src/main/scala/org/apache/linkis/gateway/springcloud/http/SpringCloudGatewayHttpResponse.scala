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
 
package org.apache.linkis.gateway.springcloud.http

import java.util.function.BiFunction

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.gateway.http.GatewayHttpResponse
import javax.servlet.http.Cookie
import org.reactivestreams.Publisher
import org.springframework.http.server.reactive.{AbstractServerHttpResponse, ServerHttpResponse}
import org.springframework.http.{HttpStatus, ResponseCookie}
import reactor.core.publisher.{Flux, Mono}
import reactor.netty.http.server.HttpServerResponse
import reactor.netty.http.websocket.{WebsocketInbound, WebsocketOutbound}

class SpringCloudGatewayHttpResponse(response: ServerHttpResponse) extends GatewayHttpResponse {

  private val cachedHTTPResponseMsg = new StringBuilder
  private val cachedWebSocketResponseMsg = new StringBuilder
  private val cachedRedirectUrlMsg = new StringBuilder
  private var responseMono: Mono[Void] = _

  override def addCookie(cookie: Cookie): Unit = {
    val responseCookie = ResponseCookie.from(cookie.getName, cookie.getValue)
    responseCookie.maxAge(cookie.getMaxAge)
    responseCookie.secure(cookie.getSecure)
    responseCookie.path(cookie.getPath)
    responseCookie.domain(cookie.getDomain)
    responseCookie.httpOnly(cookie.isHttpOnly)
    response.addCookie(responseCookie.build())
  }

  override def setHeader(key: String, value: String): Unit = response.getHeaders.add(key, value)

  override def setStatus(status: Int): Unit = response.setStatusCode(HttpStatus.valueOf(status))

  override def write(message: String): Unit = cachedHTTPResponseMsg.append(message)

  override def sendResponse(): Unit = if(responseMono == null) synchronized {
    if(responseMono != null) return
    if(cachedRedirectUrlMsg.nonEmpty) {
      if(response.getStatusCode == null || (response.getStatusCode != null && !response.getStatusCode.is3xxRedirection()))
        response.setStatusCode(HttpStatus.TEMPORARY_REDIRECT)
      response.getHeaders.set("Location", cachedRedirectUrlMsg.toString)
      responseMono = response.setComplete()
      return
    }
    setHeader("Content-Type", "application/json;charset=UTF-8")
    if(cachedHTTPResponseMsg.nonEmpty) {
      val dataBuffer = response.bufferFactory().wrap(cachedHTTPResponseMsg.toString.getBytes(Configuration.BDP_ENCODING.getValue))
      val messageFlux = Flux.just(Array(dataBuffer): _*)
      responseMono = response.writeWith(messageFlux)
    } else if(cachedWebSocketResponseMsg.nonEmpty) {
      response match {
        case abstractResponse: AbstractServerHttpResponse =>
          val nativeResponse = abstractResponse.getNativeResponse.asInstanceOf[HttpServerResponse]
          responseMono = nativeResponse.sendWebsocket(new BiFunction[WebsocketInbound, WebsocketOutbound, Publisher[Void]] {
            override def apply(in: WebsocketInbound, out: WebsocketOutbound): Publisher[Void] = {
              val dataBuffer = response.bufferFactory().wrap(cachedWebSocketResponseMsg.toString.getBytes(Configuration.BDP_ENCODING.getValue))
              SpringCloudHttpUtils.sendWebSocket(out, dataBuffer)
            }
          })
        case _ =>
      }
    }
  }

  override def isCommitted: Boolean = responseMono != null

  def getResponseMono: Mono[Void] = responseMono

  override def writeWebSocket(message: String): Unit = cachedWebSocketResponseMsg.append(message)

  override def redirectTo(url: String): Unit = cachedRedirectUrlMsg.append(url)
}
