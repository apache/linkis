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

package org.apache.linkis.jobhistory.entity;

import java.util.Map;

public class MonitorVO {

  private Long taskId;
  private String monitorLevel;
  private String receiver;
  private String subSystemId;
  private Map<String, String> extra;

  public MonitorVO() {}

  public Long getTaskId() {
    return taskId;
  }

  public void setTaskId(Long taskId) {
    this.taskId = taskId;
  }

  public String getMonitorLevel() {
    return monitorLevel;
  }

  public void setMonitorLevel(String monitorLevel) {
    this.monitorLevel = monitorLevel;
  }

  public Map<String, String> getExtra() {
    return extra;
  }

  public void setExtra(Map<String, String> extra) {
    this.extra = extra;
  }

  public String getReceiver() {
    return receiver;
  }

  public void setReceiver(String receiver) {
    this.receiver = receiver;
  }

  public String getSubSystemId() {
    return subSystemId;
  }

  public void setSubSystemId(String subSystemId) {
    this.subSystemId = subSystemId;
  }

  @Override
  public String toString() {
    return "MonitorVO{"
        + "taskId="
        + taskId
        + ", monitorLevel='"
        + monitorLevel
        + '\''
        + ", receiver='"
        + receiver
        + '\''
        + ", subSystemId='"
        + subSystemId
        + '\''
        + ", extra="
        + extra
        + '}';
  }
}
