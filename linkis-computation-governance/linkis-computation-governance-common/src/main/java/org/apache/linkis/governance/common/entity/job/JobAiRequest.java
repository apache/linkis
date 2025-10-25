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

package org.apache.linkis.governance.common.entity.job;

import org.apache.linkis.manager.label.entity.Label;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** linkis_ps_ai_job_history */
public class JobAiRequest {

  private Long id;
  private String jobHistoryId;
  private String submitUser;
  private String executeUser;
  private String submitCode;
  private String executionCode;
  private Map<String, Object> metrics = new HashMap<>();
  private Map<String, Object> params;
  private List<Label<?>> labels;
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

  public Map<String, Object> getMetrics() {
    return metrics;
  }

  public void setMetrics(Map<String, Object> metrics) {
    this.metrics = metrics;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

  public List<Label<?>> getLabels() {
    return labels;
  }

  public void setLabels(List<Label<?>> labels) {
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
    return "JobAiRequest{"
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
        + ", labels="
        + labels
        + ", params="
        + params
        + '}';
  }
}
