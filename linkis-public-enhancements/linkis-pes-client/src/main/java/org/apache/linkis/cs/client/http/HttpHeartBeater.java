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

package org.apache.linkis.cs.client.http;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.listener.Event;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.cs.client.builder.ContextClientConfig;
import org.apache.linkis.cs.client.builder.HttpContextClientConfig;
import org.apache.linkis.cs.client.listener.ContextClientListener;
import org.apache.linkis.cs.client.listener.ContextClientListenerBus;
import org.apache.linkis.cs.client.listener.ContextClientListenerManager;
import org.apache.linkis.cs.client.listener.HeartBeater;
import org.apache.linkis.cs.client.utils.SerializeHelper;
import org.apache.linkis.cs.common.entity.source.CommonContextKeyValue;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.listener.callback.imp.ContextKeyValueBean;
import org.apache.linkis.cs.listener.event.enumeration.OperateType;
import org.apache.linkis.cs.listener.event.impl.DefaultContextKeyEvent;
import org.apache.linkis.httpclient.config.ClientConfig;
import org.apache.linkis.httpclient.dws.DWSHttpClient;
import org.apache.linkis.httpclient.dws.config.DWSClientConfig;
import org.apache.linkis.httpclient.response.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: heartbeater类的作用是为了csclient能够和csserver进行每秒钟交互的一个类，从server中获取内容， 然后封装成事件投递到
 * 事件总线，来让监听器进行消费
 */
public class HttpHeartBeater implements HeartBeater {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpHeartBeater.class);

  private ContextClientListenerBus<ContextClientListener, Event> contextClientListenerBus =
      ContextClientListenerManager.getContextClientListenerBus();

  private final String name = "ContextClientHTTPHeatBeater";

  // todo 要改成某一个微服务的标识
  private final String client_source = "TestClient";

  private DWSHttpClient dwsHttpClient;

  public HttpHeartBeater(ContextClientConfig contextClientConfig) {
    if (contextClientConfig instanceof HttpContextClientConfig) {
      HttpContextClientConfig httpContextClientConfig =
          (HttpContextClientConfig) contextClientConfig;
      ClientConfig clientConfig = httpContextClientConfig.getClientConfig();
      DWSClientConfig dwsClientConfig = new DWSClientConfig(clientConfig);
      dwsClientConfig.setDWSVersion(Configuration.LINKIS_WEB_VERSION().getValue());
      dwsHttpClient = new DWSHttpClient(dwsClientConfig, name);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void heartBeat() {
    ContextHeartBeatAction contextHeartBeatAction = new ContextHeartBeatAction(client_source);
    contextHeartBeatAction.getRequestPayloads().put("source", client_source);
    Result result = null;
    try {
      result = dwsHttpClient.execute(contextHeartBeatAction);
    } catch (Exception e) {
      LOGGER.error("执行heartbeat出现失败", e);
      return;
    }
    if (result instanceof ContextHeartBeatResult) {
      ContextHeartBeatResult contextHeartBeatResult = (ContextHeartBeatResult) result;
      Map<String, Object> data = contextHeartBeatResult.getData();
      Object object = data.get("ContextKeyValueBean");
      List<ContextKeyValueBean> kvBeans = new ArrayList<>();
      if (object instanceof List) {
        List<Object> list = (List<Object>) object;
        list.stream()
            .filter(Objects::nonNull)
            .map(Object::toString)
            .map(
                str -> {
                  try {
                    return SerializeHelper.deserializeContextKVBean(str);
                  } catch (ErrorException e) {
                    return null;
                  }
                })
            .filter(Objects::nonNull)
            .forEach(kvBeans::add);
      }
      if (kvBeans.size() > 0) {
        dealCallBack(kvBeans);
      }
    }
  }

  @Override
  public void dealCallBack(List<ContextKeyValueBean> kvs) {
    for (ContextKeyValueBean kv : kvs) {
      // todo 先忽略掉contextIDEvent
      ContextKeyValue contextKeyValue = new CommonContextKeyValue();
      contextKeyValue.setContextKey(kv.getCsKey());
      contextKeyValue.setContextValue(kv.getCsValue());
      DefaultContextKeyEvent event = new DefaultContextKeyEvent();
      event.setContextID(kv.getCsID());
      event.setOperateType(OperateType.UPDATE);
      event.setContextKeyValue(contextKeyValue);
      contextClientListenerBus.post(event);
    }
  }

  @Override
  public void start() {
    Utils.defaultScheduler()
        .scheduleAtFixedRate(
            new Runnable() {
              @Override
              public void run() {
                heartBeat();
              }
            },
            0,
            1,
            TimeUnit.HOURS);
  }

  @Override
  public void close() throws IOException {
    try {
      if (null != this.dwsHttpClient) {
        this.dwsHttpClient.close();
      }
    } catch (Exception e) {
      LOGGER.error("Failed to close httpContextClient", e);
      throw new IOException(e);
    }
  }
}
