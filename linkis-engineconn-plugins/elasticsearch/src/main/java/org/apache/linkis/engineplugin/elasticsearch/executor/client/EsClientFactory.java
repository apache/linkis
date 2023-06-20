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

package org.apache.linkis.engineplugin.elasticsearch.executor.client;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.engineplugin.elasticsearch.conf.ElasticSearchConfiguration;
import org.apache.linkis.engineplugin.elasticsearch.exception.EsParamsIllegalException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;

import java.util.*;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.sniff.Sniffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.engineplugin.elasticsearch.errorcode.EasticsearchErrorCodeSummary.CLUSTER_IS_BLANK;

public class EsClientFactory {

  private static final Logger logger = LoggerFactory.getLogger(EsClientFactory.class);

  public static EsClient getRestClient(Map<String, String> options) {
    String key = getDatasourceName(options);
    if (StringUtils.isBlank(key)) {
      return defaultClient;
    }

    if (!ES_CLIENT_MAP.containsKey(key)) {
      synchronized (ES_CLIENT_MAP) {
        if (!ES_CLIENT_MAP.containsKey(key)) {
          try {
            cacheClient(createRestClient(options));
          } catch (ErrorException e) {
            logger.error("es createRestClient failed, reason:", e);
          }
        }
      }
    }

    return ES_CLIENT_MAP.get(key);
  }

  private static int MAX_CACHE_CLIENT_SIZE = 20;

  private static Map<String, EsClient> ES_CLIENT_MAP =
      new LinkedHashMap<String, EsClient>() {

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, EsClient> eldest) {
          if (size() > MAX_CACHE_CLIENT_SIZE) {
            eldest.getValue().close();
            return true;
          } else {
            return false;
          }
        }
      };

  private static String getDatasourceName(Map<String, String> options) {
    return options.getOrDefault(ElasticSearchConfiguration.ES_DATASOURCE_NAME.key(), "");
  }

  private static void cacheClient(EsClient client) {
    ES_CLIENT_MAP.put(client.getDatasourceName(), client);
  }

  private static EsClient createRestClient(Map<String, String> options) throws ErrorException {
    String clusterStr = options.get(ElasticSearchConfiguration.ES_CLUSTER.key());

    if (StringUtils.isBlank(clusterStr)) {
      throw new EsParamsIllegalException(CLUSTER_IS_BLANK.getErrorDesc());
    }

    HttpHost[] cluster = getCluster(clusterStr);

    if (cluster.length == 0) {
      throw new EsParamsIllegalException(CLUSTER_IS_BLANK.getErrorDesc());
    }

    String username = options.get(ElasticSearchConfiguration.ES_USERNAME.key());
    String password = options.get(ElasticSearchConfiguration.ES_PASSWORD.key());

    if (ElasticSearchConfiguration.ES_AUTH_CACHE.getValue()) {
      setAuthScope(cluster, username, password);
    }

    HttpHost[] httpHosts =
        Arrays.stream(cluster)
            .map(item -> new HttpHost(item.getHostName(), item.getPort()))
            .toArray(HttpHost[]::new);
    RestClientBuilder builder = RestClient.builder(httpHosts);

    builder.setHttpClientConfigCallback(
        new RestClientBuilder.HttpClientConfigCallback() {
          @Override
          public HttpAsyncClientBuilder customizeHttpClient(
              HttpAsyncClientBuilder httpAsyncClientBuilder) {
            if (!ElasticSearchConfiguration.ES_AUTH_CACHE.getValue()) {
              httpAsyncClientBuilder.disableAuthCaching();
            }
            return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
          }
        });

    if (defaultHeaders != null) {
      builder.setDefaultHeaders(defaultHeaders);
    }

    RestClient restClient = builder.build();
    Sniffer sniffer =
        ElasticSearchConfiguration.ES_SNIFFER_ENABLE.getValue(options)
            ? Sniffer.builder(restClient).build()
            : null;

    return new EsClientImpl(getDatasourceName(options), restClient, sniffer);
  }

  private static CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

  private static EsClient defaultClient;

  static {
    String cluster = ElasticSearchConfiguration.ES_CLUSTER.getValue();
    if (StringUtils.isBlank(cluster)) {
      defaultClient = null;
    } else {
      Map<String, String> defaultOpts = new HashMap<>();
      defaultOpts.put(ElasticSearchConfiguration.ES_CLUSTER.key(), cluster);
      defaultOpts.put(
          ElasticSearchConfiguration.ES_DATASOURCE_NAME.key(),
          ElasticSearchConfiguration.ES_DATASOURCE_NAME.getValue());
      defaultOpts.put(
          ElasticSearchConfiguration.ES_USERNAME.key(),
          ElasticSearchConfiguration.ES_USERNAME.getValue());
      defaultOpts.put(
          ElasticSearchConfiguration.ES_PASSWORD.key(),
          ElasticSearchConfiguration.ES_PASSWORD.getValue());
      EsClient client = null;
      try {
        client = createRestClient(defaultOpts);
      } catch (ErrorException e) {
        logger.error("es createRestClient failed, reason:", e);
      }
      cacheClient(client);
      defaultClient = client;
    }
  }

  private static Header[] defaultHeaders =
      CommonVars.properties().entrySet().stream()
          .filter(
              entry ->
                  entry.getKey() != null
                      && entry.getValue() != null
                      && entry
                          .getKey()
                          .toString()
                          .startsWith(ElasticSearchConfiguration.ES_HTTP_HEADER_PREFIX))
          .map(entry -> new BasicHeader(entry.getKey().toString(), entry.getValue().toString()))
          .toArray(Header[]::new);

  private static HttpHost[] getCluster(String clusterStr) {
    if (StringUtils.isNotBlank(clusterStr)) {
      return Arrays.stream(clusterStr.split(","))
          .map(
              value -> {
                String[] arr = value.replace("http://", "").split(":");
                return new HttpHost(arr[0].trim(), Integer.parseInt(arr[1].trim()));
              })
          .toArray(HttpHost[]::new);
    } else {
      return new HttpHost[0];
    }
  }

  private static void setAuthScope(HttpHost[] cluster, String username, String password) {
    if (cluster != null
        && cluster.length > 0
        && StringUtils.isNotBlank(username)
        && StringUtils.isNotBlank(password)) {
      Arrays.stream(cluster)
          .forEach(
              host ->
                  credentialsProvider.setCredentials(
                      new AuthScope(
                          host.getHostName(),
                          host.getPort(),
                          AuthScope.ANY_REALM,
                          AuthScope.ANY_SCHEME),
                      new UsernamePasswordCredentials(username, password)));
    }
  }
}
