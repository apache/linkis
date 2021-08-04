/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.message.builder;


import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol;
import com.webank.wedatasphere.linkis.rpc.Sender;
import scala.concurrent.duration.Duration;

import javax.servlet.http.HttpServletRequest;


public interface ServiceMethodContext {

    void putAttribute(String key, Object value);

    void putIfAbsent(String key, Object value);

    <T> T getAttribute(String key);

    <T> T getAttributeOrDefault(String key, T defaultValue);

    String getUser();

    HttpServletRequest getRequest();

    boolean notNull(String key);

    MessageJob publish(RequestProtocol requestProtocol);

    void send(Object message);

    Object ask(Object message);

    Object ask(Object message, Duration timeout);

    Sender getSender();

    void setTimeoutPolicy(MessageJobTimeoutPolicy policy);

    void setResult(Object result);

    <T> T getResult();

    boolean isInterrupted();

    boolean isCancel();

    boolean isSuccess();

}
