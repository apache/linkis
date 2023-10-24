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

package org.apache.linkis.basedatamanager.server.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** @TableName linkis_ps_udf_baseinfo */
@TableName(value = "linkis_ps_udf_baseinfo")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UdfBaseInfoEntity implements Serializable {

  @TableId(type = IdType.AUTO)
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

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UdfBaseInfoEntity that = (UdfBaseInfoEntity) o;
    return Objects.equals(id, that.id)
        && Objects.equals(createUser, that.createUser)
        && Objects.equals(udfName, that.udfName)
        && Objects.equals(udfType, that.udfType)
        && Objects.equals(isExpire, that.isExpire)
        && Objects.equals(isShared, that.isShared)
        && Objects.equals(treeId, that.treeId)
        && Objects.equals(createTime, that.createTime)
        && Objects.equals(updateTime, that.updateTime)
        && Objects.equals(sys, that.sys)
        && Objects.equals(clusterName, that.clusterName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        createUser,
        udfName,
        udfType,
        isExpire,
        isShared,
        treeId,
        createTime,
        updateTime,
        sys,
        clusterName);
  }

  @Override
  public String toString() {
    return "UdfBaseInfoEntity{"
        + "id="
        + id
        + ", createUser='"
        + createUser
        + '\''
        + ", udfName='"
        + udfName
        + '\''
        + ", udfType="
        + udfType
        + ", isExpire="
        + isExpire
        + ", isShared="
        + isShared
        + ", treeId="
        + treeId
        + ", createTime="
        + createTime
        + ", updateTime="
        + updateTime
        + ", sys='"
        + sys
        + '\''
        + ", clusterName='"
        + clusterName
        + '\''
        + '}';
  }
}
