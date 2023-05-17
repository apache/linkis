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

import org.apache.linkis.engineplugin.elasticsearch.conf.ElasticSearchConfiguration;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EncodingUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.sniff.Sniffer;

public class EsClientImpl extends EsClient {

  public EsClientImpl(String datasourceName, RestClient client, Sniffer sniffer) {
    super(datasourceName, client, sniffer);
  }

  @Override
  public Cancellable execute(
      String code, Map<String, String> options, ResponseListener responseListener) {
    Request request = createRequest(code, options);
    return client.performRequestAsync(request, responseListener);
  }

  private Request createRequest(String code, Map<String, String> options) {
    String endpoint = ElasticSearchConfiguration.ES_HTTP_ENDPOINT.getValue(options);
    String method = ElasticSearchConfiguration.ES_HTTP_METHOD.getValue(options);

    Request request = new Request(method, endpoint);
    request.setOptions(getRequestOptions(options));
    request.setJsonEntity(code);
    return request;
  }

  private RequestOptions getRequestOptions(Map<String, String> options) {
    RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();

    String username = ElasticSearchConfiguration.ES_USERNAME.getValue(options);
    String password = ElasticSearchConfiguration.ES_PASSWORD.getValue(options);

    // username / password convert to base auth
    if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
      Header authHeader =
          authenticate(
              new UsernamePasswordCredentials(username, password), StandardCharsets.UTF_8.name());
      builder.addHeader(authHeader.getName(), authHeader.getValue());
    }

    options.entrySet().stream()
        .filter(
            entry ->
                entry.getKey() != null
                    && entry.getValue() != null
                    && entry.getKey().startsWith(ElasticSearchConfiguration.ES_HTTP_HEADER_PREFIX))
        .forEach(entry -> builder.addHeader(entry.getKey(), entry.getValue()));

    return builder.build();
  }

  private Header authenticate(Credentials credentials, String charset) {
    StringBuilder tmp = new StringBuilder();
    tmp.append(credentials.getUserPrincipal().getName());
    tmp.append(":");
    tmp.append(credentials.getPassword() == null ? "null" : credentials.getPassword());

    byte[] base64password =
        Base64.encodeBase64(EncodingUtils.getBytes(tmp.toString(), charset), false);

    CharArrayBuffer buffer = new CharArrayBuffer(32);
    buffer.append(AUTH.WWW_AUTH_RESP);
    buffer.append(": Basic ");
    buffer.append(base64password, 0, base64password.length);

    return new BufferedHeader(buffer);
  }
}
