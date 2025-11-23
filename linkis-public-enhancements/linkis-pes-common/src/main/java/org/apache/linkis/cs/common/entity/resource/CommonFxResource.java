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

public class CommonFxResource implements FxResource {

  private String createUser;
  private String udfName;
  private Integer udfType;
  private String path;
  private String registerFormat;
  private String useFormat;
  private String description;
  private Boolean isExpire;
  private Boolean isShared;
  private Long treeId;
  private Date createTime;
  private Date updateTime;
  private Boolean isLoad;

  @Override
  @KeywordMethod
  public String getCreateUser() {
    return createUser;
  }

  @Override
  public void setCreateUser(String createUser) {
    this.createUser = createUser;
  }

  @Override
  @KeywordMethod
  public String getUdfName() {
    return udfName;
  }

  @Override
  public void setUdfName(String udfName) {
    this.udfName = udfName;
  }

  @Override
  @KeywordMethod
  public Integer getUdfType() {
    return udfType;
  }

  @Override
  public void setUdfType(Integer udfType) {
    this.udfType = udfType;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String getRegisterFormat() {
    return registerFormat;
  }

  @Override
  public void setRegisterFormat(String registerFormat) {
    this.registerFormat = registerFormat;
  }

  @Override
  public String getUseFormat() {
    return useFormat;
  }

  @Override
  public void setUseFormat(String useFormat) {
    this.useFormat = useFormat;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
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
  public Boolean getShared() {
    return isShared;
  }

  @Override
  public void setShared(Boolean shared) {
    isShared = shared;
  }

  @Override
  public Long getTreeId() {
    return treeId;
  }

  @Override
  public void setTreeId(Long treeId) {
    this.treeId = treeId;
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
  public Date getUpdateTime() {
    return updateTime;
  }

  @Override
  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  @Override
  public Boolean getLoad() {
    return isLoad;
  }

  @Override
  public void setLoad(Boolean load) {
    isLoad = load;
  }
}
