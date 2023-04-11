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

import java.util.Map;

public class EngineReuseRequest implements EngineRequest {

  private Map<String, Object> labels;

  private long timeOut;

  private int reuseCount;

  private String user;

  private Map<String, String> properties;

  public Map<String, Object> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, Object> labels) {
    this.labels = labels;
  }

  public long getTimeOut() {
    return timeOut;
  }

  public void setTimeOut(long timeOut) {
    this.timeOut = timeOut;
  }

  public int getReuseCount() {
    return reuseCount;
  }

  public void setReuseCount(int reuseCount) {
    this.reuseCount = reuseCount;
  }

  @Override
  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  @Override
  public String toString() {
    return "EngineReuseRequest{"
        + "timeOut="
        + timeOut
        + ", reuseCount="
        + reuseCount
        + ", user='"
        + user
        + '\''
        + '}';
  }
}
