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

/**
 * @date 2021/3/5
 * @description
 */
public class JobRequest {

  private Long id;
  private String reqId;
  private String submitUser;
  private String executeUser;
  private Map<String, String> source;
  private String executionCode;
  private List<Label<?>> labels;
  private Map<String, Object> params;
  private String progress;
  private String status;
  private String logPath;
  private Integer errorCode;
  private String errorDesc;
  private Date createdTime;
  private Date updatedTime;
  private String instances;
  /** result location */
  private String resultLocation;

  private String observeInfo;

  private Map<String, Object> metrics = new HashMap<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getReqId() {
    return reqId;
  }

  public void setReqId(String reqId) {
    this.reqId = reqId;
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

  public Map<String, String> getSource() {
    return source;
  }

  public void setSource(Map<String, String> source) {
    this.source = source;
  }

  public String getExecutionCode() {
    return executionCode;
  }

  public void setExecutionCode(String executionCode) {
    this.executionCode = executionCode;
  }

  public List<Label<?>> getLabels() {
    return labels;
  }

  public void setLabels(List<Label<?>> labels) {
    this.labels = labels;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

  public String getProgress() {
    return progress;
  }

  public void setProgress(String progress) {
    this.progress = progress;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getLogPath() {
    return logPath;
  }

  public void setLogPath(String logPath) {
    this.logPath = logPath;
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

  public String getInstances() {
    return instances;
  }

  public void setInstances(String instances) {
    this.instances = instances;
  }

  public Map<String, Object> getMetrics() {
    return metrics;
  }

  public void setMetrics(Map<String, Object> metrics) {
    this.metrics = metrics;
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

  @Override
  public String toString() {
    return "JobRequest{"
        + "id="
        + id
        + ", reqId='"
        + reqId
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
        + ", status="
        + status
        + '}';
  }
}
