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

package org.apache.linkis.engineplugin.elasticsearch.executor.client.impl;

import org.apache.linkis.engineplugin.elasticsearch.conf.ElasticSearchConfiguration;
import org.apache.linkis.engineplugin.elasticsearch.executor.client.*;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.lang.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchExecutorImpl implements ElasticSearchExecutor {

  private static final Logger logger = LoggerFactory.getLogger(ElasticSearchExecutorImpl.class);

  private EsClient client;
  private Cancellable cancelable;
  private String user;
  private String runType;
  private Map<String, String> properties;

  public ElasticSearchExecutorImpl(String runType, Map<String, String> properties) {
    this.runType = runType;
    this.properties = properties;
  }

  @Override
  public void open() throws Exception {
    this.client = EsClientFactory.getRestClient(properties);
    this.user =
        StringUtils.defaultString(
            properties.getOrDefault(TaskConstant.UMUSER, StorageUtils.getJvmUser()));
    switch (runType.trim().toLowerCase(Locale.getDefault())) {
      case "essql":
      case "sql":
        properties.putIfAbsent(
            ElasticSearchConfiguration.ES_HTTP_ENDPOINT.key(),
            ElasticSearchConfiguration.ES_HTTP_SQL_ENDPOINT.getValue(properties));
        break;
      default:
        break;
    }
  }

  @Override
  public ElasticSearchResponse executeLine(String code) {
    String realCode = code.trim();
    logger.info("es client begins to run {} code:\n {}", runType, realCode.trim());
    CountDownLatch countDown = new CountDownLatch(1);
    ElasticSearchResponse[] executeResponse = {
      new ElasticSearchErrorResponse("INCOMPLETE", null, null)
    };
    cancelable =
        client.execute(
            realCode,
            properties,
            new ResponseListener() {
              @Override
              public void onSuccess(Response response) {
                executeResponse[0] = convertResponse(response);
                countDown.countDown();
              }

              @Override
              public void onFailure(Exception exception) {
                executeResponse[0] =
                    new ElasticSearchErrorResponse(
                        "EsEngineExecutor execute fail. ", null, exception);
                countDown.countDown();
              }
            });
    try {
      countDown.await();
    } catch (InterruptedException e) {
      executeResponse[0] =
          new ElasticSearchErrorResponse("EsEngineExecutor execute interrupted. ", null, e);
    }
    return executeResponse[0];
  }

  // convert response to executeResponse
  private ElasticSearchResponse convertResponse(Response response) {
    try {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode >= 200 && statusCode < 300) {
        return ResponseHandler$.MODULE$.handle(response);
      } else {
        return new ElasticSearchErrorResponse(
            "EsEngineExecutor convert response fail. response code: " + statusCode, null, null);
      }
    } catch (Exception e) {
      return new ElasticSearchErrorResponse("EsEngineExecutor convert response error.", null, e);
    }
  }

  @Override
  public void close() {
    if (cancelable != null) {
      cancelable.cancel();
    }
  }
}
