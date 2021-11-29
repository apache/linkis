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
 
package org.apache.linkis.cli.application.interactor.execution.jobexec;

import org.apache.linkis.cli.common.entity.execution.jobexec.JobStatus;
import org.apache.linkis.cli.core.interactor.execution.jobexec.JobManExec;

import java.util.Date;


public class LinkisJobInfo extends JobManExec {
    private String taskID;
    private String instance;
    private String execId;
    private String strongerExecId;
    private String umUser;
    private String executionCode;
    private String logPath;
    private JobStatus jobStatus;
    private String engineType;
    private String runType;
    private long costTime;
    private Date createdTime;
    private Date updatedTime;
    private Date engineStartTime;
    private Integer errCode;
    private String errMsg;
    private String executeApplicationName;
    private String requestApplicationName;
    private double progress;

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
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

    public String getExecutionCode() {
        return executionCode;
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

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getRunType() {
        return runType;
    }

    public void setRunType(String runType) {
        this.runType = runType;
    }

    public Date getEngineStartTime() {
        return engineStartTime;
    }

    public void setEngineStartTime(Date engineStartTime) {
        this.engineStartTime = engineStartTime;
    }

    public Integer getErrCode() {
        return errCode;
    }

    public void setErrCode(Integer errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
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

    public String getStrongerExecId() {
        return strongerExecId;
    }

    public void setStrongerExecId(String strongerExecId) {
        this.strongerExecId = strongerExecId;
    }

    public long getCostTime() {
        return costTime;
    }

    public void setCostTime(long costTime) {
        this.costTime = costTime;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}
