/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.gateway.http

/**
  * created by cooperyang on 2019/1/9.
  */
trait GatewayContext {

  def getRequest: GatewayHttpRequest
  def setRequest(request: GatewayHttpRequest): Unit
  def getResponse: GatewayHttpResponse
  def setResponse(response: GatewayHttpResponse): Unit

  def setWebSocketRequest(): Unit
  def isWebSocketRequest: Boolean

  def setGatewayRoute(gatewayRoute: GatewayRoute): Unit
  def getGatewayRoute: GatewayRoute

}
class BaseGatewayContext extends GatewayContext {
  private var request: GatewayHttpRequest = _
  private var response: GatewayHttpResponse = _
  private var webSocketRequest: Boolean = false
  private var gatewayRoute: GatewayRoute = _

  override def getRequest: GatewayHttpRequest = request

  override def setRequest(request: GatewayHttpRequest): Unit = this.request = request

  override def getResponse: GatewayHttpResponse = response

  override def setResponse(response: GatewayHttpResponse): Unit = this.response = response

  override def setWebSocketRequest(): Unit = this.webSocketRequest = true

  override def isWebSocketRequest: Boolean = webSocketRequest

  override def setGatewayRoute(gatewayRoute: GatewayRoute): Unit = this.gatewayRoute = gatewayRoute

  override def getGatewayRoute: GatewayRoute = gatewayRoute
}