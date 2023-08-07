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

package org.apache.linkis.cs.common.entity.resource;

import org.apache.linkis.cs.common.annotation.KeywordMethod;

import java.util.Date;

public class LinkisBMLResource implements BMLResource {

  private String resourceId;

  private String version;

  private Boolean isPrivate;

  private String resourceHeader;

  private String downloadedFileName;

  private String sys;

  private Date createTime;

  private Boolean isExpire;

  private String expireType;

  private String expireTime;

  private Date updateTime;

  private String updator;

  private Integer maxVersion;

  private String user;

  private String system;

  private Boolean enableFlag;

  @Override
  public Boolean getPrivate() {
    return isPrivate;
  }

  @Override
  public void setPrivate(Boolean aPrivate) {
    isPrivate = aPrivate;
  }

  @Override
  public String getResourceHeader() {
    return resourceHeader;
  }

  @Override
  public void setResourceHeader(String resourceHeader) {
    this.resourceHeader = resourceHeader;
  }

  @Override
  public String getDownloadedFileName() {
    return downloadedFileName;
  }

  @Override
  public void setDownloadedFileName(String downloadedFileName) {
    this.downloadedFileName = downloadedFileName;
  }

  @Override
  public String getSys() {
    return sys;
  }

  @Override
  public void setSys(String sys) {
    this.sys = sys;
  }

  @Override
  public Date getCreateTime() {
    return createTime;
  }

  @Override
  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  @Override
  public Boolean getExpire() {
    return isExpire;
  }

  @Override
  public void setExpire(Boolean expire) {
    isExpire = expire;
  }

  @Override
  public String getExpireType() {
    return expireType;
  }

  @Override
  public void setExpireType(String expireType) {
    this.expireType = expireType;
  }

  @Override
  public String getExpireTime() {
    return expireTime;
  }

  @Override
  public void setExpireTime(String expireTime) {
    this.expireTime = expireTime;
  }

  @Override
  public Date getUpdateTime() {
    return updateTime;
  }

  @Override
  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  @Override
  public String getUpdator() {
    return updator;
  }

  @Override
  public void setUpdator(String updator) {
    this.updator = updator;
  }

  @Override
  public Integer getMaxVersion() {
    return maxVersion;
  }

  @Override
  public void setMaxVersion(Integer maxVersion) {
    this.maxVersion = maxVersion;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public void setUser(String user) {
    this.user = user;
  }

  @Override
  public String getSystem() {
    return system;
  }

  @Override
  public void setSystem(String system) {
    this.system = system;
  }

  @Override
  public Boolean getEnableFlag() {
    return enableFlag;
  }

  @Override
  public void setEnableFlag(Boolean enableFlag) {
    this.enableFlag = enableFlag;
  }

  @Override
  @KeywordMethod
  public String getResourceId() {
    return this.resourceId;
  }

  @Override
  @KeywordMethod
  public String getVersion() {
    return this.version;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
