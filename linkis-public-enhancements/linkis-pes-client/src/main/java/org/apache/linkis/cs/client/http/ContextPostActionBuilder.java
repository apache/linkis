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

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.cs.client.utils.SerializeHelper;
import org.apache.linkis.cs.common.entity.history.ContextHistory;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.protocol.ContextHTTPConstant;

import java.util.HashMap;
import java.util.Map;

public class ContextPostActionBuilder {

  private final DefaultContextPostAction action;

  private Map<String, Object> requestParams = new HashMap<String, Object>(4);
  private Map<String, String> headerParams = new HashMap<>(2);

  public ContextPostActionBuilder(String url) {
    action = new DefaultContextPostAction(url);
  }

  public static ContextPostActionBuilder of(String url) {
    return new ContextPostActionBuilder(url);
  }

  public ContextPostActionBuilder with(ContextID contextID) throws ErrorException {
    String contextIDStr = SerializeHelper.serializeContextID(contextID);
    requestParams.put(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr);
    return this;
  }

  public ContextPostActionBuilder with(ContextHistory history) throws ErrorException {
    String historyStr = SerializeHelper.serializeContextHistory(history);
    requestParams.put(ContextHTTPConstant.CONTEXT_HISTORY_KEY, historyStr);
    return this;
  }

  public ContextPostActionBuilder with(String key, Object object) throws ErrorException {
    requestParams.put(key, object);
    return this;
  }

  public ContextPostActionBuilder addHeader(String key, String value) throws ErrorException {
    headerParams.put(key, value);
    return this;
  }

  /*  public ContextPostActionBuilder with(ContextKey contextKey) {
      return this;
  }

  public ContextPostActionBuilder with(ContextKeyValue keyValue) {
      return this;
  }

  public ContextPostActionBuilder with(ContextValue value) {
      return this;
  }*/

  public DefaultContextPostAction build() {
    action.getRequestPayloads().putAll(requestParams);
    action.getHeaders().putAll(headerParams);
    return action;
  }
}
