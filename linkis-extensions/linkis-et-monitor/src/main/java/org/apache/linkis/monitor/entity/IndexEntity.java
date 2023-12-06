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

package org.apache.linkis.monitor.entity;

import org.apache.linkis.monitor.constants.Constants;

public class IndexEntity {

  private final String subsystemId = Constants.ALERT_SUB_SYSTEM_ID();
  private String interfaceName;
  private String attrGroup;
  private String attrName;
  private String hostIp;
  private String metricValue;

  public String getSubsystemId() {
    return subsystemId;
  }

  public String getInterfaceName() {
    return interfaceName;
  }

  public void setInterfaceName(String interfaceName) {
    this.interfaceName = interfaceName;
  }

  public String getAttrGroup() {
    return attrGroup;
  }

  public void setAttrGroup(String attrGroup) {
    this.attrGroup = attrGroup;
  }

  public String getAttrName() {
    return attrName;
  }

  public void setAttrName(String attrName) {
    this.attrName = attrName;
  }

  public String getHostIp() {
    return hostIp;
  }

  public void setHostIp(String hostIp) {
    this.hostIp = hostIp;
  }

  public String getMetricValue() {
    return metricValue;
  }

  public void setMetricValue(String metricValue) {
    this.metricValue = metricValue;
  }

  public IndexEntity() {}

  public IndexEntity(
      String interfaceName, String attrGroup, String attrName, String hostIp, String metricValue) {
    this.interfaceName = interfaceName;
    this.attrGroup = attrGroup;
    this.attrName = attrName;
    this.hostIp = hostIp;
    this.metricValue = metricValue;
  }
}
