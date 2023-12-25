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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class DepartmentVo {

  @ApiModelProperty("cluster_code")
  private String clusterCode;

  @ApiModelProperty("user_type")
  private String userType;

  @ApiModelProperty("user_name")
  private String userName;

  @ApiModelProperty("org_id")
  private String orgId;

  @ApiModelProperty("org_name")
  private String orgName;

  @ApiModelProperty("queue_name")
  private String queueName;

  @ApiModelProperty("db_name")
  private String dbName;

  @ApiModelProperty("interface_user")
  private String interfaceUser;

  @ApiModelProperty("is_union_analyse")
  private String isUnionAnalyse;

  @ApiModelProperty("create_time")
  private String createTime;

  @ApiModelProperty("user_itsm_no")
  private String userItsmNo;

  public String getClusterCode() {
    return clusterCode;
  }

  public void setClusterCode(String clusterCode) {
    this.clusterCode = clusterCode;
  }

  public String getUserType() {
    return userType;
  }

  public void setUserType(String userType) {
    this.userType = userType;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getOrgName() {
    return orgName;
  }

  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  public String getQueueName() {
    return queueName;
  }

  public void setQueueName(String queueName) {
    this.queueName = queueName;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public String getInterfaceUser() {
    return interfaceUser;
  }

  public void setInterfaceUser(String interfaceUser) {
    this.interfaceUser = interfaceUser;
  }

  public String getIsUnionAnalyse() {
    return isUnionAnalyse;
  }

  public void setIsUnionAnalyse(String isUnionAnalyse) {
    this.isUnionAnalyse = isUnionAnalyse;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getUserItsmNo() {
    return userItsmNo;
  }

  public void setUserItsmNo(String userItsmNo) {
    this.userItsmNo = userItsmNo;
  }

  @Override
  public String toString() {
    return "DepartmentVo{"
        + "clusterCode='"
        + clusterCode
        + '\''
        + ", userType='"
        + userType
        + '\''
        + ", userName='"
        + userName
        + '\''
        + ", orgId='"
        + orgId
        + '\''
        + ", orgName='"
        + orgName
        + '\''
        + ", queueName='"
        + queueName
        + '\''
        + ", dbName='"
        + dbName
        + '\''
        + ", interfaceUser='"
        + interfaceUser
        + '\''
        + ", isUnionAnalyse='"
        + isUnionAnalyse
        + '\''
        + ", createTime='"
        + createTime
        + '\''
        + ", userItsmNo='"
        + userItsmNo
        + '\''
        + '}';
  }
}
