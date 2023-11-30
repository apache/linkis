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

package org.apache.linkis.metadata.query.service;

import org.apache.linkis.datasourcemanager.common.util.json.Json;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.reactor.IOReactorConfig;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

public class ElasticConnection implements Closeable {

  public static final String DEFAULT_TYPE_NAME = "type";

  private static final String DEFAULT_MAPPING_NAME = "mappings";
  private static final String DEFAULT_INDEX_NAME = "index";
  private static final String FIELD_PROPS = "properties";

  private RestClient restClient;

  public ElasticConnection(String[] endPoints, String username, String password)
      throws IOException {
    HttpHost[] httpHosts = new HttpHost[endPoints.length];
    for (int i = 0; i < endPoints.length; i++) {
      httpHosts[i] = HttpHost.create(endPoints[i]);
    }
    RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);
    CredentialsProvider credentialsProvider = null;
    if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
      credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(
          AuthScope.ANY, new UsernamePasswordCredentials(username, password));
    }
    // set only one thread
    CredentialsProvider finalCredentialsProvider = credentialsProvider;
    restClientBuilder.setHttpClientConfigCallback(
        httpClientBuilder -> {
          if (null != finalCredentialsProvider) {
            httpClientBuilder.setDefaultCredentialsProvider(finalCredentialsProvider);
          }
          return httpClientBuilder.setDefaultIOReactorConfig(
              IOReactorConfig.custom().setIoThreadCount(1).build());
        });
    this.restClient = restClientBuilder.build();
    // Try to test connection
    ping();
  }

  public List<String> getAllIndices() throws Exception {
    List<String> indices = new ArrayList<>();
    Request request = new Request("GET", "_cat/indices");
    request.addParameter("format", "JSON");
    Response response = restClient.performRequest(request);
    List<Map<String, Object>> list = Json.fromJson(response.getEntity().getContent(), Map.class);
    list.forEach(
        v -> {
          String index = String.valueOf(v.getOrDefault(DEFAULT_INDEX_NAME, ""));
          if (StringUtils.isNotBlank(index) && !index.startsWith(".")) {
            indices.add(index);
          }
        });
    return indices;
  }

  public List<String> getTypes(String index) throws Exception {
    List<String> types = new ArrayList<>();
    Request request = new Request("GET", index + "/_mappings");
    Response response = restClient.performRequest(request);
    Map<String, Map<String, Object>> result =
        Json.fromJson(response.getEntity().getContent(), Map.class);
    Map<String, Object> indexMap = result.get(index);
    Object props = indexMap.get(DEFAULT_MAPPING_NAME);
    if (props instanceof Map) {
      Set keySet = ((Map) props).keySet();
      for (Object v : keySet) {
        types.add(String.valueOf(v));
      }
    }
    return types;
  }

  public Map<Object, Object> getProps(String index, String type) throws Exception {
    Request request = new Request("GET", index + "/_mappings");
    Response response = restClient.performRequest(request);
    Map<String, Map<String, Object>> result =
        Json.fromJson(response.getEntity().getContent(), Map.class);
    Map mappings = (Map) result.get(index).get(DEFAULT_MAPPING_NAME);
    Map propsMap = mappings;
    if (mappings.containsKey(type)) {
      Object typeMap = mappings.get(type);
      if (typeMap instanceof Map) {
        propsMap = (Map) typeMap;
      }
    }
    Object props = propsMap.get(FIELD_PROPS);
    if (null != props && props instanceof Map) {
      return (Map) props;
    }
    return propsMap;
  }

  public void ping() throws IOException {
    Response response = restClient.performRequest(new Request("GET", "/"));
    int code = response.getStatusLine().getStatusCode();
    int successCode = 200;
    if (code != successCode) {
      throw new RuntimeException("Ping to ElasticSearch ERROR, response code: " + code);
    }
  }

  @Override
  public void close() throws IOException {
    this.restClient.close();
  }
}
