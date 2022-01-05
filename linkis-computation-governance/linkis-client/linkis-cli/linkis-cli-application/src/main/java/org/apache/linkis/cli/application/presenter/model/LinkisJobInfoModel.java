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
 
package org.apache.linkis.cli.application.presenter.model;


import org.apache.linkis.cli.common.entity.execution.jobexec.JobStatus;
import org.apache.linkis.cli.core.presenter.model.PresenterModel;

import java.util.Date;


public class LinkisJobInfoModel implements PresenterModel {

    private String cid;
    private String jobId;
    private Boolean success;
    private String message;
    private String exception;
    private String cause;

    private String taskID;
    private String instance;
    private String execId;
    private String strongerExecId;
    private String umUser;
    private String executionCode;
    private String logPath;
    private JobStatus status;
    private String engineType;
    private String runType;
    private Long costTime;
    private Date createdTime;
    private Date updatedTime;
    private Date engineStartTime;
    private Integer errCode;
    private String errMsg;
    private String executeApplicationName;
    private String requestApplicationName;
    private Double progress;

    public LinkisJobInfoModel(String cid, String jobId, Boolean success, String message, String taskID, String instance, String execId, String strongerExecId, String umUser, String executionCode, String logPath, JobStatus status, String engineType, String runType, Long costTime, Date createdTime, Date updatedTime, Date engineStartTime, Integer errCode, String errMsg, String executeApplicationName, String requestApplicationName, Double progress) {
        this.cid = cid;
        this.jobId = jobId;
        this.success = success;
        this.message = message;
        this.taskID = taskID;
        this.instance = instance;
        this.execId = execId;
        this.strongerExecId = strongerExecId;
        this.umUser = umUser;
        this.executionCode = executionCode;
        this.logPath = logPath;
        this.status = status;
        this.engineType = engineType;
        this.runType = runType;
        this.costTime = costTime;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.engineStartTime = engineStartTime;
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.executeApplicationName = executeApplicationName;
        this.requestApplicationName = requestApplicationName;
        this.progress = progress;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }
}
