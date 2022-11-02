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

package org.apache.linkis.governance.common.entity.task;

import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.protocol.task.Task;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class RequestPersistTask implements Task {

  private Long taskID;
  /** instance 是指该task所在的统一入口的实例 ip + port */
  private String instance;

  private String execId;
  private String umUser;
  private String submitUser;

  /** engineInstance 是指task执行所请求的engine的实例信息，ip+port */
  private String engineInstance;

  private String executionCode;
  private Float progress;
  private String logPath;
  private String resultLocation;
  private String status;
  private Date createdTime;
  private Date updatedTime;
  private Integer errCode;
  private String errDesc;
  private String taskResource;
  /** executeApplicationName 参数指的是用户所寻求的服务，比如spark python R等等 */
  private String executeApplicationName;
  /** requestApplicationName 是creator的传参名，例如IDE或WTSS等 */
  private String requestApplicationName;
  /** source 存放脚本来源，scriptPath是其中一个参数用户采用传入执行脚本的方式，scriptPath就是脚本的存储地址 */
  private java.util.Map<String, String> source;
  /**
   * runType需要和executeApplicationName结合使用，如用户选择了Spark做为服务，他还需要指明使用哪种执行方式，比如pySpark RSpark等
   * runType和runType是同一个属性，为了兼容以前的代码
   */
  private String runType;

  private String engineType;
  private java.util.Map<String, Object> params;

  private Date engineStartTime;

  private List<Label<?>> labels;

  private String createService;

  private String description;

  public String getEngineType() {
    return engineType;
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
    this.runType = engineType;
  }

  public Date getEngineStartTime() {
    return engineStartTime;
  }

  public void setEngineStartTime(Date engineStartTime) {
    this.engineStartTime = engineStartTime;
  }

  public String getRunType() {
    return runType;
  }

  public void setRunType(String runType) {
    this.runType = runType;
    this.engineType = runType;
  }

  @Override
  public String getInstance() {
    return instance;
  }

  @Override
  public String getExecId() {
    return execId;
  }

  @Override
  public void setInstance(String instance) {
    this.instance = instance;
  }

  @Override
  public void setExecId(String execId) {
    this.execId = execId;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
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

  public Map<String, String> getSource() {
    return source;
  }

  public void setSource(Map<String, String> source) {
    this.source = source;
  }

  public Long getTaskID() {
    return taskID;
  }

  public void setTaskID(Long taskID) {
    this.taskID = taskID;
  }

  public String getUmUser() {
    return umUser;
  }

  public void setUmUser(String umUser) {
    this.umUser = umUser;
  }

  public Float getProgress() {
    return progress;
  }

  public void setProgress(Float progress) {
    this.progress = progress;
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

  public String getExecutionCode() {
    return executionCode;
  }

  public String getCode() {
    return this.getExecutionCode();
  }

  public void setExecutionCode(String executionCode) {
    this.executionCode = executionCode;
  }

  public String getLogPath() {
    return logPath;
  }

  public void setLogPath(String logPath) {
    this.logPath = logPath;
  }

  public String getEngineInstance() {
    return engineInstance;
  }

  public void setEngineInstance(String engineInstance) {
    this.engineInstance = engineInstance;
  }

  public String getTaskResource() {
    return taskResource;
  }

  public void setTaskResource(String taskResource) {
    this.taskResource = taskResource;
  }

  public String getSubmitUser() {
    return submitUser;
  }

  public void setSubmitUser(String submitUser) {
    this.submitUser = submitUser;
  }

  public List<Label<?>> getLabels() {
    return labels;
  }

  public void setLabels(List<Label<?>> labels) {
    this.labels = labels;
  }

  public String getCreateService() {
    return createService;
  }

  public void setCreateService(String createService) {
    this.createService = createService;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return "RequestPersistTask{"
        + "taskID="
        + taskID
        + ", instance='"
        + instance
        + '\''
        + ", execId='"
        + execId
        + '\''
        + ", umUser='"
        + umUser
        + '\''
        + ", engineInstance='"
        + engineInstance
        + '\''
        + ", executionCode='"
        + executionCode
        + '\''
        + ", progress="
        + progress
        + ", logPath='"
        + logPath
        + '\''
        + ", resultLocation='"
        + resultLocation
        + '\''
        + ", status='"
        + status
        + '\''
        + ", createdTime="
        + createdTime
        + ", updatedTime="
        + updatedTime
        + ", errCode="
        + errCode
        + ", errDesc='"
        + errDesc
        + '\''
        + ", executeApplicationName='"
        + executeApplicationName
        + '\''
        + ", requestApplicationName='"
        + requestApplicationName
        + '\''
        + ", source="
        + source
        + ", runType='"
        + runType
        + '\''
        + ", params="
        + params
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RequestPersistTask task = (RequestPersistTask) o;

    return new EqualsBuilder()
        .append(taskID, task.taskID)
        .append(instance, task.instance)
        .append(execId, task.execId)
        .append(umUser, task.umUser)
        .append(engineInstance, task.engineInstance)
        .append(executionCode, task.executionCode)
        .append(progress, task.progress)
        .append(logPath, task.logPath)
        .append(resultLocation, task.resultLocation)
        .append(status, task.status)
        .append(createdTime, task.createdTime)
        .append(updatedTime, task.updatedTime)
        .append(errCode, task.errCode)
        .append(errDesc, task.errDesc)
        .append(executeApplicationName, task.executeApplicationName)
        .append(requestApplicationName, task.requestApplicationName)
        .append(source, task.source)
        .append(runType, task.runType)
        .append(params, task.params)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(taskID)
        .append(instance)
        .append(execId)
        .append(umUser)
        .append(engineInstance)
        .append(executionCode)
        .append(progress)
        .append(logPath)
        .append(resultLocation)
        .append(status)
        .append(createdTime)
        .append(updatedTime)
        .append(errCode)
        .append(errDesc)
        .append(executeApplicationName)
        .append(requestApplicationName)
        .append(source)
        .append(runType)
        .append(params)
        .toHashCode();
  }
}
