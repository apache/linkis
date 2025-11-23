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

package org.apache.linkis.jobhistory.entity;

import java.util.Date;

public class JobAiHistory {
  private Long id;
  private String jobHistoryId;
  private String submitUser;
  private String executeUser;
  private String submitCode;
  private String executionCode;
  private String metrics;
  private String params;
  private String labels;
  private Integer errorCode;
  private String errorDesc;
  private String engineInstances;
  private String engineType;
  private Date changeTime;
  private Date createdTime;
  private Date updatedTime;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getJobHistoryId() {
    return jobHistoryId;
  }

  public void setJobHistoryId(String jobHistoryId) {
    this.jobHistoryId = jobHistoryId;
  }

  public String getSubmitUser() {
    return submitUser;
  }

  public void setSubmitUser(String submitUser) {
    this.submitUser = submitUser;
  }

  public String getExecuteUser() {
    return executeUser;
  }

  public void setExecuteUser(String executeUser) {
    this.executeUser = executeUser;
  }

  public String getSubmitCode() {
    return submitCode;
  }

  public void setSubmitCode(String submitCode) {
    this.submitCode = submitCode;
  }

  public String getExecutionCode() {
    return executionCode;
  }

  public void setExecutionCode(String executionCode) {
    this.executionCode = executionCode;
  }

  public String getMetrics() {
    return metrics;
  }

  public void setMetrics(String metrics) {
    this.metrics = metrics;
  }

  public String getParams() {
    return params;
  }

  public void setParams(String params) {
    this.params = params;
  }

  public String getLabels() {
    return labels;
  }

  public void setLabels(String labels) {
    this.labels = labels;
  }

  public Integer getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(Integer errorCode) {
    this.errorCode = errorCode;
  }

  public String getErrorDesc() {
    return errorDesc;
  }

  public void setErrorDesc(String errorDesc) {
    this.errorDesc = errorDesc;
  }

  public String getEngineInstances() {
    return engineInstances;
  }

  public void setEngineInstances(String engineInstances) {
    this.engineInstances = engineInstances;
  }

  public String getEngineType() {
    return engineType;
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
  }

  public Date getChangeTime() {
    return changeTime;
  }

  public void setChangeTime(Date changeTime) {
    this.changeTime = changeTime;
  }

  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }

  public Date getUpdatedTime() {
    return updatedTime;
  }

  public void setUpdatedTime(Date updatedTime) {
    this.updatedTime = updatedTime;
  }

  @Override
  public String toString() {
    return "JobAiHistory{"
        + "id="
        + id
        + ", jobHistoryId='"
        + jobHistoryId
        + '\''
        + ", submitUser='"
        + submitUser
        + '\''
        + ", executeUser='"
        + executeUser
        + '\''
        + ", submitCode='"
        + submitCode
        + '\''
        + ", labels="
        + labels
        + ", errorDesc='"
        + errorDesc
        + '\''
        + ", engineInstances='"
        + engineInstances
        + '\''
        + ", engineType='"
        + engineType
        + '\''
        + ", changeTime="
        + changeTime
        + ", createdTime="
        + createdTime
        + ", updatedTime="
        + updatedTime
        + '}';
  }
}
