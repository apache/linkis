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

package org.apache.linkis.monitor.until;

import org.apache.linkis.bml.conf.BmlConfiguration;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy;
import org.apache.linkis.httpclient.dws.config.DWSClientConfig;
import org.apache.linkis.httpclient.dws.config.DWSClientConfigBuilder;
import org.apache.linkis.monitor.client.MonitorHTTPClient;
import org.apache.linkis.monitor.client.MonitorHTTPClientClientImpl;
import org.apache.linkis.monitor.config.MonitorConfig;
import org.apache.linkis.monitor.entity.IndexEntity;
import org.apache.linkis.monitor.request.EmsListAction;
import org.apache.linkis.monitor.request.EntranceTaskAction;
import org.apache.linkis.monitor.response.EntranceTaskResult;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.ujes.client.response.EmsListResult;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.springframework.util.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsUntils {
  private static final Logger logger = LoggerFactory.getLogger(HttpsUntils.class);

  public static DWSClientConfig dwsClientConfig = createClientConfig(null, null);
  //        IOUtils.closeQuietly(client);
  public static MonitorHTTPClient client = new MonitorHTTPClientClientImpl(dwsClientConfig);
  public static final String localHost = Utils.getLocalHostname();

  public static Map<String, Object> sendHttp(String url, Map<String, Object> properties)
      throws IOException {
    if (null == dwsClientConfig) {
      dwsClientConfig = createClientConfig(url, properties);
    }
    if (null == client) {
      client = new MonitorHTTPClientClientImpl(dwsClientConfig);
    }
    EmsListAction build = EmsListAction.newBuilder().setUser("hadoop").build();
    EmsListResult result = client.list(build);
    return result.getResultMap();
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

  public static Map<String, Object> getEntranceTask(String url, String user, String Instance)
      throws IOException {
    if (null == dwsClientConfig) {
      dwsClientConfig = createClientConfig(null, null);
    }
    if (null == client) {
      client = new MonitorHTTPClientClientImpl(dwsClientConfig);
    }
    EntranceTaskAction build =
        EntranceTaskAction.newBuilder().setUser(user).setInstance(Instance).build();
    EntranceTaskResult result = client.entranList(build);
    return result.getResultMap();
  }

  public static void sendIndex(List<IndexEntity> list) throws IOException {
    Map<String, Object> parm = new HashMap<>();
    parm.put("userAuthKey", MonitorConfig.ECM_TASK_USER_AUTHKEY.getValue());
    parm.put("metricDataList", list);
    String json = BDPJettyServerHelper.gson().toJson(parm);

    RequestConfig requestConfig = RequestConfig.DEFAULT;
    StringEntity entity =
        new StringEntity(
            json, ContentType.create(ContentType.APPLICATION_JSON.getMimeType(), "UTF-8"));
    entity.setContentEncoding("UTF-8");

    HttpPost httpPost = new HttpPost(MonitorConfig.ECM_TASK_IMURL.getValue());
    httpPost.setConfig(requestConfig);
    httpPost.setEntity(entity);

    CloseableHttpClient httpClient = HttpClients.createDefault();
    CloseableHttpResponse execute = httpClient.execute(httpPost);
    String responseStr = EntityUtils.toString(execute.getEntity(), "UTF-8");
    Map<String, String> map = BDPJettyServerHelper.gson().fromJson(responseStr, Map.class);
    logger.info("send index response :{}", map);
    Assert.isTrue(!"0".equals(map.get("resultCode")), map.get("resultMsg"));
  }
}
