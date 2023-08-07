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

package org.apache.linkis.udf.vo;

import java.util.Date;

public class UDFInfoVo {
  private Long id;
  private String createUser;
  private String udfName;
  private Integer udfType;
  private Boolean isExpire;
  private Boolean isShared;
  private Long treeId;
  private String sys;
  private String clusterName;
  private Date createTime;
  private Date updateTime;

  private Boolean isLoad;

  private String path; // 仅存储用户上一次上传的路径 作提示用
  private String registerFormat;
  private String useFormat;
  private String bmlResourceId;
  private String bmlResourceVersion;
  private String description;
  //    private String directory;  //分类，个人函数一级目录

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

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getRegisterFormat() {
    return registerFormat;
  }

  public void setRegisterFormat(String registerFormat) {
    this.registerFormat = registerFormat;
  }

  public String getUseFormat() {
    return useFormat;
  }

  public void setUseFormat(String useFormat) {
    this.useFormat = useFormat;
  }

  public String getBmlResourceId() {
    return bmlResourceId;
  }

  public void setBmlResourceId(String bmlResourceId) {
    this.bmlResourceId = bmlResourceId;
  }

  public String getBmlResourceVersion() {
    return bmlResourceVersion;
  }

  public void setBmlResourceVersion(String bmlResourceVersion) {
    this.bmlResourceVersion = bmlResourceVersion;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getLoad() {
    return isLoad;
  }

  public void setLoad(Boolean load) {
    isLoad = load;
  }
}
