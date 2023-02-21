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

package org.apache.linkis.configuration.entity;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class TenantVo {

  @ApiModelProperty("id")
  private String id;

  @ApiModelProperty("user")
  private String user;

  @ApiModelProperty("creator")
  private String creator;

  @ApiModelProperty("tenantValue")
  private String tenantValue;

  @ApiModelProperty("createTime")
  private Date createTime;

  @ApiModelProperty("updateTime")
  private Date updateTime;

  @ApiModelProperty("desc")
  private String desc;

  @ApiModelProperty("bussinessUser")
  private String bussinessUser;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getTenantValue() {
    return tenantValue;
  }

  public void setTenantValue(String tenantValue) {
    this.tenantValue = tenantValue;
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

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public String getBussinessUser() {
    return bussinessUser;
  }

  public void setBussinessUser(String bussinessUser) {
    this.bussinessUser = bussinessUser;
  }

  @Override
  public String toString() {
    return "TenantVo{"
        + "id='"
        + id
        + '\''
        + ", user='"
        + user
        + '\''
        + ", creator='"
        + creator
        + '\''
        + ", tenantValue='"
        + tenantValue
        + '\''
        + ", createTime="
        + createTime
        + ", updateTime="
        + updateTime
        + ", desc='"
        + desc
        + '\''
        + ", bussinessUser='"
        + bussinessUser
        + '\''
        + '}';
  }
}
