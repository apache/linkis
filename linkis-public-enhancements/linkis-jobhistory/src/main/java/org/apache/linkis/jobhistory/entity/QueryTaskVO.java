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

import org.apache.linkis.governance.common.entity.job.SubJobDetail;

import java.util.Date;
import java.util.List;

public class QueryTaskVO {

  private Long taskID;
  private String instance;
  private String execId;
  /** submit User* */
  private String umUser;

  private String executeUser;
  private String engineInstance;
  private String executionCode;
  private String progress;
  private String logPath;
  private String resultLocation;
  private String status;
  private Date createdTime;
  private Date updatedTime;
  private String engineType;
  private Integer errCode;
  private String errDesc;
  private String executeApplicationName;
  private String requestApplicationName;
  private String runType;
  private String paramsJson;
  private Long costTime;
  private String strongerExecId;
  private String sourceJson;
  /** source字段:用来将sourceJson的value取出来进行拼接返回给前台展示 */
  private String sourceTailor;

  private Date engineStartTime;

  private List<String> labels;

  private boolean canRetry;

  private String observeInfo;
  private Boolean isReuse;
  private Date requestStartTime;
  private Date requestEndTime;
  private Long requestSpendTime;

  private String metrics;

  private String engineLogPath;
  private String ecmInstance;
  private String ticketId;

  public List<SubJobDetail> getSubJobs() {
    return subJobs;
  }

  public void setSubJobs(List<SubJobDetail> subJobs) {
    this.subJobs = subJobs;
  }

  private List<SubJobDetail> subJobs;

  public Date getEngineStartTime() {
    return engineStartTime;
  }

  public void setEngineStartTime(Date engineStartTime) {
    this.engineStartTime = engineStartTime;
  }

  public String getSourceTailor() {
    return sourceTailor;
  }

  public void setSourceTailor(String sourceTailor) {
    this.sourceTailor = sourceTailor;
  }

  public String getSourceJson() {
    return sourceJson;
  }

  public void setSourceJson(String sourceJson) {
    this.sourceJson = sourceJson;
  }

  public Long getTaskID() {
    return taskID;
  }

  public void setTaskID(Long taskID) {
    this.taskID = taskID;
  }

  public String getInstance() {
    return instance;
  }

  public void setInstance(String instance) {
    this.instance = instance;
  }

  public String getExecId() {
    return execId;
  }

  public void setExecId(String execId) {
    this.execId = execId;
  }

  public String getUmUser() {
    return umUser;
  }

  public void setUmUser(String umUser) {
    this.umUser = umUser;
  }

  public String getEngineInstance() {
    return engineInstance;
  }

  public void setEngineInstance(String engineInstance) {
    this.engineInstance = engineInstance;
  }

  public String getExecutionCode() {
    return executionCode;
  }

  public void setExecutionCode(String executionCode) {
    this.executionCode = executionCode;
  }

  public String getProgress() {
    return progress;
  }

  public void setProgress(String progress) {
    this.progress = progress;
  }

  public String getLogPath() {
    return logPath;
  }

  public void setLogPath(String logPath) {
    this.logPath = logPath;
  }

  public String getResultLocation() {
    return resultLocation;
  }

  public void setResultLocation(String resultLocation) {
    this.resultLocation = resultLocation;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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

  public String getEngineType() {
    return engineType;
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
  }

  public Integer getErrCode() {
    return errCode;
  }

  public void setErrCode(Integer errCode) {
    this.errCode = errCode;
  }

  public String getErrDesc() {
    return errDesc;
  }

  public void setErrDesc(String errDesc) {
    this.errDesc = errDesc;
  }

  public String getExecuteApplicationName() {
    return executeApplicationName;
  }

  public void setExecuteApplicationName(String executeApplicationName) {
    this.executeApplicationName = executeApplicationName;
  }

  public String getRequestApplicationName() {
    return requestApplicationName;
  }

  public void setRequestApplicationName(String requestApplicationName) {
    this.requestApplicationName = requestApplicationName;
  }

  public String getRunType() {
    return runType;
  }

  public void setRunType(String runType) {
    this.runType = runType;
  }

  public String getParamsJson() {
    return paramsJson;
  }

  public void setParamsJson(String paramsJson) {
    this.paramsJson = paramsJson;
  }

  public Long getCostTime() {
    return costTime;
  }

  public void setCostTime(Long costTime) {
    this.costTime = costTime;
  }

  public String getStrongerExecId() {
    return strongerExecId;
  }

  public void setStrongerExecId(String strongerExecId) {
    this.strongerExecId = strongerExecId;
  }

  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  public boolean isCanRetry() {
    return canRetry;
  }

  public void setCanRetry(boolean canRetry) {
    this.canRetry = canRetry;
  }

  public String getObserveInfo() {
    return observeInfo;
  }

  public void setObserveInfo(String observeInfo) {
    this.observeInfo = observeInfo;
  }

  public String getExecuteUser() {
    return executeUser;
  }

  public void setExecuteUser(String executeUser) {
    this.executeUser = executeUser;
  }

  public Boolean getIsReuse() {
    return isReuse;
  }

  public void setIsReuse(Boolean isReuse) {
    this.isReuse = isReuse;
  }

  public Date getRequestStartTime() {
    return requestStartTime;
  }

  public void setRequestStartTime(Date requestStartTime) {
    this.requestStartTime = requestStartTime;
  }

  public Date getRequestEndTime() {
    return requestEndTime;
  }

  public void setRequestEndTime(Date requestEndTime) {
    this.requestEndTime = requestEndTime;
  }

  public Long getRequestSpendTime() {
    return requestSpendTime;
  }

  public void setRequestSpendTime(Long requestSpendTime) {
    this.requestSpendTime = requestSpendTime;
  }

  public String getMetrics() {
    return metrics;
  }

  public void setMetrics(String metrics) {
    this.metrics = metrics;
  }

  public String getEngineLogPath() {
    return engineLogPath;
  }

  public void setEngineLogPath(String engineLogPath) {
    this.engineLogPath = engineLogPath;
  }

  public String getEcmInstance() {
    return ecmInstance;
  }

  public void setEcmInstance(String ecmInstance) {
    this.ecmInstance = ecmInstance;
  }

  public String getTicketId() {
    return ticketId;
  }

  public void setTicketId(String ticketId) {
    this.ticketId = ticketId;
  }
}
