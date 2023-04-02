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

package org.apache.linkis.manager.common.entity.persistence;

import org.apache.linkis.manager.common.entity.resource.Resource;
import org.apache.linkis.manager.common.utils.ResourceUtils;

import java.util.Date;

public class ECResourceInfoRecord {

  private Integer id;

  private String createUser;

  private String labelValue;

  private String ticketId;

  private String serviceInstance;

  private String usedResource;

  private String requestResource;

  private String releasedResource;

  private String ecmInstance;

  private int requestTimes;
  private int usedTimes;
  private int releaseTimes;

  private Date usedTime;

  private Date createTime;

  private Date releaseTime;

  private String logDirSuffix;

  private String status;

  private String metrics;

  public ECResourceInfoRecord() {}

  public ECResourceInfoRecord(
      String labelValue,
      String createUser,
      String ticketId,
      Resource resource,
      String logDirSuffix) {
    this.labelValue = labelValue;
    this.ticketId = ticketId;
    this.createUser = createUser;
    if (null != resource) {
      this.requestResource = ResourceUtils.serializeResource(resource);
    }
    this.logDirSuffix = logDirSuffix;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getLabelValue() {
    return labelValue;
  }

  public void setLabelValue(String labelValue) {
    this.labelValue = labelValue;
  }

  public String getTicketId() {
    return ticketId;
  }

  public void setTicketId(String ticketId) {
    this.ticketId = ticketId;
  }

  public String getServiceInstance() {
    return serviceInstance;
  }

  public void setServiceInstance(String serviceInstance) {
    this.serviceInstance = serviceInstance;
  }

  public String getUsedResource() {
    return usedResource;
  }

  public void setUsedResource(String usedResource) {
    this.usedResource = usedResource;
  }

  public Date getUsedTime() {
    return usedTime;
  }

  public void setUsedTime(Date usedTime) {
    this.usedTime = usedTime;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getRequestResource() {
    return requestResource;
  }

  public void setRequestResource(String requestResource) {
    this.requestResource = requestResource;
  }

  public String getReleasedResource() {
    return releasedResource;
  }

  public void setReleasedResource(String releasedResource) {
    this.releasedResource = releasedResource;
  }

  public int getRequestTimes() {
    return requestTimes;
  }

  public void setRequestTimes(int requestTimes) {
    this.requestTimes = requestTimes;
  }

  public int getUsedTimes() {
    return usedTimes;
  }

  public void setUsedTimes(int usedTimes) {
    this.usedTimes = usedTimes;
  }

  public int getReleaseTimes() {
    return releaseTimes;
  }

  public void setReleaseTimes(int releaseTimes) {
    this.releaseTimes = releaseTimes;
  }

  public Date getReleaseTime() {
    return releaseTime;
  }

  public void setReleaseTime(Date releaseTime) {
    this.releaseTime = releaseTime;
  }

  public String getEcmInstance() {
    return ecmInstance;
  }

  public void setEcmInstance(String ecmInstance) {
    this.ecmInstance = ecmInstance;
  }

  public String getLogDirSuffix() {
    return logDirSuffix;
  }

  public void setLogDirSuffix(String logDirSuffix) {
    this.logDirSuffix = logDirSuffix;
  }

  public String getCreateUser() {
    return createUser;
  }

  public void setCreateUser(String createUser) {
    this.createUser = createUser;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getMetrics() {
    return metrics;
  }

  public void setMetrics(String metrics) {
    this.metrics = metrics;
  }

  @Override
  public String toString() {
    return "ECResourceInfoRecord{"
        + "createUser='"
        + createUser
        + '\''
        + ", ticketId='"
        + ticketId
        + '\''
        + ", serviceInstance='"
        + serviceInstance
        + '\''
        + '}';
  }
}
