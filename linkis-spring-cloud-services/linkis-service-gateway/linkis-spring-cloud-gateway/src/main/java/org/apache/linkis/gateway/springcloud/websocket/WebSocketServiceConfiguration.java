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

package org.apache.linkis.gateway.springcloud.websocket;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.CommonVars$;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

@Configuration
public class WebSocketServiceConfiguration {

  public static final CommonVars<Integer> MAX_FRAME_LENGTH =
      CommonVars$.MODULE$.apply("wds.linkis.gateway.conf.max.frame.length", 655350);

  @Bean(name = "customWebSocketService")
  @Primary
  public WebSocketService webSocketService() {
    ReactorNettyRequestUpgradeStrategy strategy = new ReactorNettyRequestUpgradeStrategy();
    strategy.setMaxFramePayloadLength(MAX_FRAME_LENGTH.getValue());
    return new HandshakeWebSocketService(strategy);
  }
}
