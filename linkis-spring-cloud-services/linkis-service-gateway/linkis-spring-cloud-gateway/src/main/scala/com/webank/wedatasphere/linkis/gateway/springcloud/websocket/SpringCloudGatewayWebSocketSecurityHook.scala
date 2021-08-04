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

package com.webank.wedatasphere.linkis.gateway.springcloud.websocket

import com.webank.wedatasphere.linkis.gateway.http.GatewayContext
import com.webank.wedatasphere.linkis.gateway.security.SecurityHook
import org.springframework.stereotype.Component

@Component
class SpringCloudGatewayWebSocketSecurityHook extends SecurityHook {
  override def preLogout(gatewayContext: GatewayContext): Unit = {
    val inetAddress = gatewayContext.getRequest.getRemoteAddress
    SpringCloudGatewayWebsocketUtils.removeGatewayWebSocketSessionConnection(inetAddress)
  }

  override def postLogin(gatewayContext: GatewayContext): Unit = {}
}
