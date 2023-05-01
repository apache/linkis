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

package org.apache.linkis.manager.common.protocol.engine;

import org.apache.linkis.manager.common.protocol.OperateRequest;
import org.apache.linkis.protocol.message.RequestProtocol;

import java.util.Map;

public class EngineOperateRequest implements OperateRequest, RequestProtocol {

  private final String user;
  private final Map<String, Object> parameters;

  public EngineOperateRequest(String user, Map<String, Object> parameters) {
    this.user = user;
    this.parameters = parameters;
  }

  public String getUser() {
    return user;
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }
}
