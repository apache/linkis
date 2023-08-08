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

package org.apache.linkis.cs.client.builder;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.cs.client.utils.ContextClientConf;
import org.apache.linkis.httpclient.authentication.AuthenticationStrategy;
import org.apache.linkis.httpclient.config.ClientConfig;
import org.apache.linkis.httpclient.config.ClientConfigBuilder;
import org.apache.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy;

/** Description: 以http的方式和cs-server进行交互的配置,包括http的诸多配置 */
public class HttpContextClientConfig extends ContextClientConfig {

  private ClientConfig clientConfig;

  private static final int CS_CONNECTION_TIMEOUT =
      CommonVars.apply("wds.linkis.cs.connection.timeout", 3 * 60 * 1000).getValue();
  private static final int CS_READ_TIMEOUT =
      CommonVars.apply("wds.linkis.cs.read.timeout", 3 * 60 * 1000).getValue();
  private static final int CS_MAX_CONNECTION =
      CommonVars.apply("wds.linkis.cs.max.connection", 50).getValue();

  public HttpContextClientConfig() {
    // 初始化clientConfig
    String gatewayUrl = Configuration.getGateWayURL();
    AuthenticationStrategy authenticationStrategy = new TokenAuthenticationStrategy();
    clientConfig =
        ClientConfigBuilder.newBuilder()
            .addServerUrl(gatewayUrl)
            .connectionTimeout(CS_CONNECTION_TIMEOUT)
            .discoveryEnabled(false)
            .loadbalancerEnabled(false)
            .maxConnectionSize(CS_MAX_CONNECTION)
            .retryEnabled(false)
            .readTimeout(CS_READ_TIMEOUT)
            .setAuthenticationStrategy(authenticationStrategy)
            .setAuthTokenKey(ContextClientConf.CONTEXT_CLIENT_AUTH_KEY().getValue())
            .setAuthTokenValue(ContextClientConf.CONTEXT_CLIENT_AUTH_VALUE().getValue())
            .build();
  }

  /** @return 返回一个的 */
  public ClientConfig getClientConfig() {
    return this.clientConfig;
  }
}
