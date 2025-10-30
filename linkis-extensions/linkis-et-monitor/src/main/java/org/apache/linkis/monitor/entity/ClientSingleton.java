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

package org.apache.linkis.monitor.entity;

import org.apache.linkis.bml.conf.BmlConfiguration;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy;
import org.apache.linkis.httpclient.dws.config.DWSClientConfig;
import org.apache.linkis.httpclient.dws.config.DWSClientConfigBuilder;
import org.apache.linkis.monitor.client.MonitorHTTPClient;
import org.apache.linkis.monitor.client.MonitorHTTPClientClientImpl;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ClientSingleton {
  private static MonitorHTTPClient instance;
  private static DWSClientConfig dwsClientConfig;

  private ClientSingleton() {}

  public static synchronized MonitorHTTPClient getInstance() {
    if (instance == null) {
      if (dwsClientConfig == null) {
        dwsClientConfig = createClientConfig(null, null);
      }
      instance = new MonitorHTTPClientClientImpl(dwsClientConfig);
    }
    return instance;
  }

  public static DWSClientConfig createClientConfig(String url, Map<String, Object> properties) {
    String realUrl = "";
    if (StringUtils.isBlank(url)) {
      realUrl = Configuration.getGateWayURL();
    } else {
      realUrl = url;
    }
    Map<String, Object> parms = new HashMap<>();
    if (MapUtils.isNotEmpty(properties)) {
      parms = properties;
    }
    int maxConnection =
        (int)
            parms.getOrDefault(
                BmlConfiguration.CONNECTION_MAX_SIZE_SHORT_NAME(),
                BmlConfiguration.CONNECTION_MAX_SIZE().getValue());
    int connectTimeout =
        (int)
            parms.getOrDefault(
                BmlConfiguration.CONNECTION_TIMEOUT_SHORT_NAME(),
                BmlConfiguration.CONNECTION_TIMEOUT().getValue());
    int readTimeout =
        (int)
            parms.getOrDefault(
                BmlConfiguration.CONNECTION_READ_TIMEOUT_SHORT_NAME(),
                BmlConfiguration.CONNECTION_READ_TIMEOUT().getValue());
    String tokenKey =
        (String)
            parms.getOrDefault(
                BmlConfiguration.AUTH_TOKEN_KEY_SHORT_NAME(),
                BmlConfiguration.AUTH_TOKEN_KEY().getValue());
    String tokenValue =
        (String)
            parms.getOrDefault(
                BmlConfiguration.AUTH_TOKEN_VALUE_SHORT_NAME(),
                BmlConfiguration.AUTH_TOKEN_VALUE().getValue());

    DWSClientConfig clientConfig =
        ((DWSClientConfigBuilder)
                (DWSClientConfigBuilder.newBuilder()
                    .addServerUrl(realUrl)
                    .connectionTimeout(connectTimeout)
                    .discoveryEnabled(false)
                    .discoveryFrequency(1, TimeUnit.MINUTES)
                    .loadbalancerEnabled(false)
                    .maxConnectionSize(maxConnection)
                    .retryEnabled(false)
                    .readTimeout(readTimeout)
                    .setAuthenticationStrategy(new TokenAuthenticationStrategy())
                    .setAuthTokenKey(tokenKey)
                    .setAuthTokenValue(tokenValue)))
            .setDWSVersion("v1")
            .build();

    return clientConfig;
  }
}
