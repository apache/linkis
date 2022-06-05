/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

public class ContextGetActionBuilder {

    private DefaultContextGetAction action;

    private Map<String, Object> requestParams = new HashMap<String, Object>(4);
    private Map<String, String> headerParams = new HashMap<>(2);

    public ContextGetActionBuilder(String url) {
        action = new DefaultContextGetAction(url);
    }

    public static ContextGetActionBuilder of(String url) {
        return new ContextGetActionBuilder(url);
    }

    public ContextGetActionBuilder with(ContextID contextID) throws ErrorException {
        String contextIDStr = SerializeHelper.serializeContextID(contextID);
        requestParams.put(ContextHTTPConstant.CONTEXT_ID_STR, contextIDStr);
        return this;
    }

    public ContextGetActionBuilder with(ContextHistory history) throws ErrorException {
        String historyStr = SerializeHelper.serializeContextHistory(history);
        requestParams.put(ContextHTTPConstant.CONTEXT_HISTORY_KEY, historyStr);
        return this;
    }

    public ContextGetActionBuilder with(String key, Object object) throws ErrorException {
        requestParams.put(key, object);
        return this;
    }

    public ContextGetActionBuilder addHeader(String key, String value) throws ErrorException {
        headerParams.put(key, value);
        return this;
    }

    public DefaultContextGetAction build() {
        StringBuilder paramBuilder = new StringBuilder("");
        if (!requestParams.isEmpty()) {
            boolean head = true;
            for (Map.Entry<String, Object> param : requestParams.entrySet()) {
                if (head) {
                    head = false;
                    paramBuilder.append("?");
                } else {
                    paramBuilder.append("&");
                }
                paramBuilder.append(param.getKey()).append("=");
                if (null == param.getValue()) {
                    paramBuilder.append("");
                } else {
                    paramBuilder.append(param.getValue());
                }
            }
        }
        action = new DefaultContextGetAction(action.url() + paramBuilder.toString());
        action.getHeaders().putAll(headerParams);
        return action;
    }
}
