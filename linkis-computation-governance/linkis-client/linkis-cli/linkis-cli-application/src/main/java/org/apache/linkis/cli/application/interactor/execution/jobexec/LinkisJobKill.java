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


public class LinkisJobKill extends JobManExec {

    private String taskID;
    private String execID;
    private String user;
    private JobStatus jobStatus;

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getExecID() {
        return execID;
    }

    public void setExecID(String execID) {
        this.execID = execID;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public final void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public final boolean isJobSubmitted() {
        return !(this.getJobStatus() == JobStatus.UNSUBMITTED || this.getJobStatus() == JobStatus.SUBMITTING);
    }

    public final boolean isJobCompleted() {
        return this.isJobSuccess() || this.isJobFailure() || this.isJobCancelled() || this.isJobTimeout();
    }

    public final boolean isJobSuccess() {
        return this.getJobStatus() == JobStatus.SUCCEED;
    }

    public final boolean isJobFailure() {
        return this.getJobStatus() == JobStatus.FAILED;
    }

    public final boolean isJobCancelled() {
        return this.getJobStatus() == JobStatus.CANCELLED;
    }

    public final boolean isJobTimeout() {
        return this.getJobStatus() == JobStatus.TIMEOUT;
    }

    public final boolean isJobAbnormalStatus() {
        return this.getJobStatus() == JobStatus.UNKNOWN;
    }


}
