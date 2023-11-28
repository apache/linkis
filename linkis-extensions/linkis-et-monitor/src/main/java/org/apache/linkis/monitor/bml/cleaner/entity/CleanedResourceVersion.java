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

package org.apache.linkis.monitor.bml.cleaner.entity;

import java.util.Date;

public class CleanedResourceVersion {

  private long id;

  private String resourceId;

  private String fileMd5;

  private String version;

  private long size;

  private String resource;

  private String oldResource;

  private String description;

  private String clientIp;

  private boolean enableFlag;

  private String user;

  private String system;

  private Date startTime;

  private Date endTime;

  private long startByte;

  private long endByte;

  private String updator;

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getSystem() {
    return system;
  }

  public void setSystem(String system) {
    this.system = system;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getOldResource() {
    return oldResource;
  }

  public void setOldResource(String oldResource) {
    this.oldResource = oldResource;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getFileMd5() {
    return fileMd5;
  }

  public void setFileMd5(String fileMd5) {
    this.fileMd5 = fileMd5;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getClientIp() {
    return clientIp;
  }

  public void setClientIp(String clientIp) {
    this.clientIp = clientIp;
  }

  public boolean isEnableFlag() {
    return enableFlag;
  }

  public void setEnableFlag(boolean enableFlag) {
    this.enableFlag = enableFlag;
  }

  public long getStartByte() {
    return startByte;
  }

  public void setStartByte(long startByte) {
    this.startByte = startByte;
  }

  public long getEndByte() {
    return endByte;
  }

  public void setEndByte(long endByte) {
    this.endByte = endByte;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public String getUpdator() {
    return updator;
  }

  public void setUpdator(String updator) {
    this.updator = updator;
  }

  public static CleanedResourceVersion copyFromResourceVersion(ResourceVersion resourceVersion) {
    CleanedResourceVersion cleanedResourceVersion = new CleanedResourceVersion();
    cleanedResourceVersion.setResourceId(resourceVersion.getResourceId());
    cleanedResourceVersion.setOldResource(resourceVersion.getResource());
    cleanedResourceVersion.setFileMd5(resourceVersion.getFileMd5());
    cleanedResourceVersion.setClientIp(resourceVersion.getClientIp());
    cleanedResourceVersion.setSize(resourceVersion.getSize());
    cleanedResourceVersion.setEnableFlag(resourceVersion.getEnableFlag());
    cleanedResourceVersion.setVersion(resourceVersion.getVersion());
    cleanedResourceVersion.setStartByte(resourceVersion.getStartByte());
    cleanedResourceVersion.setEndByte(resourceVersion.getEndByte());
    cleanedResourceVersion.setStartTime(resourceVersion.getStartTime());
    cleanedResourceVersion.setEndTime(resourceVersion.getEndTime());
    return cleanedResourceVersion;
  }
}
