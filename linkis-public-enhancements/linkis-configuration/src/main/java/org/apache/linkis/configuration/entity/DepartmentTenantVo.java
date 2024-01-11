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
public class DepartmentTenantVo {
  @ApiModelProperty("id")
  private String id;

  @ApiModelProperty("creator")
  private String creator;

  @ApiModelProperty("department")
  private String department;

  @ApiModelProperty("departmentId")
  private String departmentId;

  @ApiModelProperty("tenantValue")
  private String tenantValue;

  @ApiModelProperty("createTime")
  private Date createTime;

  @ApiModelProperty("updateTime")
  private Date updateTime;

  @ApiModelProperty("createBy")
  private String createBy;

  @ApiModelProperty("isValid")
  private String isValid;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getDepartmentId() {
    return departmentId;
  }

  public void setDepartmentId(String departmentId) {
    this.departmentId = departmentId;
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

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getIsValid() {
    return isValid;
  }

  public void setIsValid(String isValid) {
    this.isValid = isValid;
  }

  @Override
  public String toString() {
    return "DepartmentTenantVo{"
        + "id='"
        + id
        + '\''
        + ", creator='"
        + creator
        + '\''
        + ", department='"
        + department
        + '\''
        + ", departmentId='"
        + departmentId
        + '\''
        + ", tenantValue='"
        + tenantValue
        + '\''
        + ", createTime="
        + createTime
        + ", updateTime="
        + updateTime
        + ", bussinessUser='"
        + createBy
        + ", isValid="
        + isValid
        + '\''
        + '}';
  }
}
