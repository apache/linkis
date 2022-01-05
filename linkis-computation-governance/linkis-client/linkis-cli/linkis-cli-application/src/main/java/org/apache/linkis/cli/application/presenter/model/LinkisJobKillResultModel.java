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


public class LinkisJobKillResultModel implements PresenterModel {

    private String cid;
    private String jobId;
    private Boolean success;
    private String message;
    private String exception;
    private String cause;

    private String taskID;
    private String execID;
    private String user;
    private JobStatus jobStatus;

    public LinkisJobKillResultModel(String cid, String jobId, Boolean success, String message, String taskID, String execID, String user, JobStatus jobStatus) {
        this.cid = cid;
        this.jobId = jobId;
        this.success = success;
        this.message = message;
        this.taskID = taskID;
        this.execID = execID;
        this.user = user;
        this.jobStatus = jobStatus;
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
