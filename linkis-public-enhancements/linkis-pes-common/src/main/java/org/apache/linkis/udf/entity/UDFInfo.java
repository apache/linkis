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

package org.apache.linkis.udf.entity;

import java.util.Date;

public class UDFInfo {
  private Long id;
  private String createUser;
  private String udfName;
  private Integer udfType;
  private Boolean isExpire;
  private Boolean isShared;
  private Long treeId;
  private Date createTime;
  private Date updateTime;
  private String sys;
  private String clusterName;

  private Boolean isLoad;
  private String description;

  public UDFInfo() {};

  public UDFInfo(
      Long id,
      String createUser,
      String udfName,
      Integer udfType,
      String path,
      String registerFormat,
      String useFormat,
      String description,
      Boolean isExpire,
      Boolean isShared,
      Long treeId,
      Date createTime,
      Date updateTime,
      Boolean isLoad) {
    this.id = id;
    this.createUser = createUser;
    this.udfName = udfName;
    this.udfType = udfType;
    this.isExpire = isExpire;
    this.isShared = isShared;
    this.treeId = treeId;
    this.createTime = createTime;
    this.updateTime = updateTime;
    this.isLoad = isLoad;
    this.description = description;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCreateUser() {
    return createUser;
  }

  public void setCreateUser(String createUser) {
    this.createUser = createUser;
  }

  public String getUdfName() {
    return udfName;
  }

  public void setUdfName(String udfName) {
    this.udfName = udfName;
  }

  public Integer getUdfType() {
    return udfType;
  }

  public void setUdfType(Integer udfType) {
    this.udfType = udfType;
  }

  public Boolean getExpire() {
    return isExpire;
  }

  public void setExpire(Boolean expire) {
    isExpire = expire;
  }

  public Boolean getShared() {
    return isShared;
  }

  public void setShared(Boolean shared) {
    isShared = shared;
  }

  public Long getTreeId() {
    return treeId;
  }

  public void setTreeId(Long treeId) {
    this.treeId = treeId;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public Boolean getLoad() {
    return isLoad;
  }

  public void setLoad(Boolean load) {
    isLoad = load;
  }

  public String getSys() {
    return sys;
  }

  public void setSys(String sys) {
    this.sys = sys;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
