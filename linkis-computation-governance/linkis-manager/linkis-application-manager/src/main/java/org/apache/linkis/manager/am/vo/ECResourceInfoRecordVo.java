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

package org.apache.linkis.manager.am.vo;

import java.util.Date;

public class ECResourceInfoRecordVo {

  private Integer id;

  private String createUser;

  private String labelValue;

  private String ticketId;

  private String serviceInstance;

  private ResourceVo usedResource;

  private ResourceVo requestResource;

  private ResourceVo releasedResource;

  private String ecmInstance;

  private int requestTimes;

  private int usedTimes;

  private int releaseTimes;

  private Date usedTime;

  private Date createTime;

  private Date releaseTime;

  private String logDirSuffix;

  private String engineType;

  private String status;

  public ECResourceInfoRecordVo() {}

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getCreateUser() {
    return createUser;
  }

  public void setCreateUser(String createUser) {
    this.createUser = createUser;
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

  public ResourceVo getUsedResource() {
    return usedResource;
  }

  public void setUsedResource(ResourceVo usedResource) {
    this.usedResource = usedResource;
  }

  public ResourceVo getRequestResource() {
    return requestResource;
  }

  public void setRequestResource(ResourceVo requestResource) {
    this.requestResource = requestResource;
  }

  public ResourceVo getReleasedResource() {
    return releasedResource;
  }

  public void setReleasedResource(ResourceVo releasedResource) {
    this.releasedResource = releasedResource;
  }

  public String getEcmInstance() {
    return ecmInstance;
  }

  public void setEcmInstance(String ecmInstance) {
    this.ecmInstance = ecmInstance;
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

  public Date getReleaseTime() {
    return releaseTime;
  }

  public void setReleaseTime(Date releaseTime) {
    this.releaseTime = releaseTime;
  }

  public String getLogDirSuffix() {
    return logDirSuffix;
  }

  public void setLogDirSuffix(String logDirSuffix) {
    this.logDirSuffix = logDirSuffix;
  }

  public String getEngineType() {
    return engineType;
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "ECRHistroryListVo{"
        + "id="
        + id
        + ", createUser='"
        + createUser
        + '\''
        + ", labelValue='"
        + labelValue
        + '\''
        + ", ticketId='"
        + ticketId
        + '\''
        + ", serviceInstance='"
        + serviceInstance
        + '\''
        + ", usedResource='"
        + usedResource
        + '\''
        + ", requestResource='"
        + requestResource
        + '\''
        + ", releasedResource='"
        + releasedResource
        + '\''
        + ", ecmInstance='"
        + ecmInstance
        + '\''
        + ", requestTimes="
        + requestTimes
        + ", usedTimes="
        + usedTimes
        + ", releaseTimes="
        + releaseTimes
        + ", usedTime="
        + usedTime
        + ", createTime="
        + createTime
        + ", releaseTime="
        + releaseTime
        + ", logDirSuffix='"
        + logDirSuffix
        + '\''
        + ", engineType='"
        + engineType
        + '\''
        + '}';
  }
}
