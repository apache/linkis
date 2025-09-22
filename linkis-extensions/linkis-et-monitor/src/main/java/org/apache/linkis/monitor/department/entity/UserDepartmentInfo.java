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

package org.apache.linkis.monitor.department.entity;

import java.util.Date;

public class UserDepartmentInfo {

  private String clusterCode;

  private String userType;
  private String userName;
  private String orgId;
  private String orgName;
  private String queueName;
  private String dbName;
  private String interfaceUser;
  private String isUnionAnalyse;
  private Date createTime;
  private String userItsmNo;

  // 构造函数、getter和setter方法
  public UserDepartmentInfo(
      String clusterCode,
      String userType,
      String userName,
      String orgId,
      String orgName,
      String queueName,
      String dbName,
      String interfaceUser,
      String isUnionAnalyse,
      Date createTime,
      String userItsmNo) {
    this.clusterCode = clusterCode;
    this.userType = userType;
    this.userName = userName;
    this.orgId = orgId;
    this.orgName = orgName;
    this.queueName = queueName;
    this.dbName = dbName;
    this.interfaceUser = interfaceUser;
    this.isUnionAnalyse = isUnionAnalyse;
    this.createTime = createTime;
    this.userItsmNo = userItsmNo;
  }

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

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getUserItsmNo() {
    return userItsmNo;
  }

  public void setUserItsmNo(String userItsmNo) {
    this.userItsmNo = userItsmNo;
  }
}
