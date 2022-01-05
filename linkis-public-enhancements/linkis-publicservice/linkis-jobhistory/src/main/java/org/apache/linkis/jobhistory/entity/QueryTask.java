/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.jobhistory.entity;

import java.util.Date;


public class QueryTask {
    private Long taskID;
    private String instance;
    private String execId;
    private String umUser;
    private String engineInstance;
    private String executionCode;
    private Float progress;
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
    private String sourceJson;
    private Date engineStartTime;
    private String taskResource;

    private String submitUser;

    private String labelJson;

    public Date getEngineStartTime() {
        return engineStartTime;
    }

    public void setEngineStartTime(Date engineStartTime) {
        this.engineStartTime = engineStartTime;
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

    public Float getProgress() {
        return progress;
    }

    public void setProgress(Float progress) {
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

    public String getLabelJson() {
        return labelJson;
    }

    public void setLabelJson(String labelJson) {
        this.labelJson = labelJson;
    }
}
