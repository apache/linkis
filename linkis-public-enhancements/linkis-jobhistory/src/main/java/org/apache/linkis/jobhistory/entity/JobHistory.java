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

import org.apache.linkis.common.utils.JobHistoryInfo;
import org.apache.linkis.jobhistory.util.QueryUtils;

import java.util.Date;

public class JobHistory {

  private Long id;
  private JobHistoryInfo jobHistoryInfo = new JobHistoryInfo();
  private String resultLocation;
  private String observeInfo;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public JobHistoryInfo getJobHistoryInfo() {
    return jobHistoryInfo;
  }

  public void setJobHistoryInfo(JobHistoryInfo jobHistoryInfo) {
    this.jobHistoryInfo = jobHistoryInfo;
  }

  public String getResultLocation() {
    return resultLocation;
  }

  public void setResultLocation(String resultLocation) {
    this.resultLocation = resultLocation;
  }

  public String getObserveInfo() {
    return observeInfo;
  }

  public void setObserveInfo(String observeInfo) {
    this.observeInfo = observeInfo;
  }

  public String getJobReqId() {
    return jobHistoryInfo.getJobReqId();
  }

  public void setJobReqId(String jobReqId) {
    jobHistoryInfo.setJobReqId(jobReqId);
  }

  public String getSubmitUser() {
    return jobHistoryInfo.getSubmitUser();
  }

  public void setSubmitUser(String submitUser) {
    jobHistoryInfo.setSubmitUser(submitUser);
  }

  public String getExecuteUser() {
    return jobHistoryInfo.getExecuteUser();
  }

  public void setExecuteUser(String executeUser) {
    jobHistoryInfo.setExecuteUser(executeUser);
  }

  public String getSource() {
    return jobHistoryInfo.getSource();
  }

  public void setSource(String source) {
    jobHistoryInfo.setSource(source);
  }

  public String getLabels() {
    return jobHistoryInfo.getLabels();
  }

  public void setLabels(String labels) {
    jobHistoryInfo.setLabels(labels);
  }

  public String getParams() {
    return jobHistoryInfo.getParams();
  }

  public void setParams(String params) {
    jobHistoryInfo.setParams(params);
  }

  public String getProgress() {
    return jobHistoryInfo.getProgress();
  }

  public void setProgress(String progress) {
    jobHistoryInfo.setProgress(progress);
  }

  public String getStatus() {
    return jobHistoryInfo.getStatus();
  }

  public void setStatus(String status) {
    jobHistoryInfo.setStatus(status);
  }

  public String getLogPath() {
    return jobHistoryInfo.getLogPath();
  }

  public void setLogPath(String logPath) {
    jobHistoryInfo.setLogPath(logPath);
  }

  public Integer getErrorCode() {
    return jobHistoryInfo.getErrorCode();
  }

  public void setErrorCode(Integer errorCode) {
    jobHistoryInfo.setErrorCode(errorCode);
  }

  public String getErrorDesc() {
    return jobHistoryInfo.getErrorDesc();
  }

  public void setErrorDesc(String errorDesc) {
    jobHistoryInfo.setErrorDesc(errorDesc);
  }

  public Date getCreatedTime() {
    return jobHistoryInfo.getCreatedTime();
  }

  public void setCreatedTime(Date createdTime) {
    jobHistoryInfo.setCreatedTime(createdTime);
  }

  public Date getUpdatedTime() {
    return jobHistoryInfo.getUpdatedTime();
  }

  public void setUpdatedTime(Date updatedTime) {
    jobHistoryInfo.setUpdatedTime(updatedTime);
  }

  public String getInstances() {
    return jobHistoryInfo.getInstances();
  }

  public void setInstances(String instances) {
    jobHistoryInfo.setInstances(instances);
  }

  public String getMetrics() {
    return jobHistoryInfo.getMetrics();
  }

  public void setMetrics(String metrics) {
    jobHistoryInfo.setMetrics(metrics);
  }

  public String getEngineType() {
    return jobHistoryInfo.getEngineType();
  }

  public void setEngineType(String engineType) {
    jobHistoryInfo.setEngineType(engineType);
  }

  public String getExecutionCode() {
    return jobHistoryInfo.getExecutionCode();
  }

  public void setExecutionCode(String executionCode) {
    jobHistoryInfo.setExecutionCode(executionCode);
  }

  public String getUpdateTimeMills() {
    return QueryUtils.dateToString(getUpdatedTime());
  }

  @Override
  public String toString() {
    return "JobHistory{"
        + "id="
        + id
        + ", jobReqId='"
        + jobHistoryInfo.getJobReqId()
        + '\''
        + ", labels='"
        + jobHistoryInfo.getLabels()
        + '\''
        + '}';
  }
}
