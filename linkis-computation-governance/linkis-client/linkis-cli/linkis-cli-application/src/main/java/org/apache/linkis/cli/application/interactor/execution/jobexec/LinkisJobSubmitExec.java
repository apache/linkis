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

import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.ExecutorException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.execution.jobexec.JobSubmitExec;


public class LinkisJobSubmitExec extends JobSubmitExec {

    private String taskID;
    private String execID;
    private String user;

    private float progress = 0.0f;

    private String logPath; // remote path for job log
    private String resultLocation;
    private String[] resultSetPaths = null; // remote paths for job result set

    private Integer errCode = null;
    private String errDesc = null;


    public final String getTaskID() {
        return taskID;
    }

    public final void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public final String getExecID() {
        return execID;
    }

    public final void setExecID(String execID) {
        this.execID = execID;
    }

    public final String getUser() {
        return user;
    }

    public final void setUser(String user) {
        this.user = user;
    }

    public final float getJobProgress() {
        return progress;
    }

    public final void setJobProgress(float progress) {
        this.progress = progress;
    }

    public final String getLogPath() {
        return logPath;
    }

    public final void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public final String getResultLocation() {
        return resultLocation;
    }

    public final void setResultLocation(String resultLocation) {
        this.resultLocation = resultLocation;
    }

    public String[] getResultSetPaths() {
        return resultSetPaths;
    }

    public final void setResultSetPaths(String[] resultSetPaths) {
        this.resultSetPaths = resultSetPaths;
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

    @Override
    public void setCid(String cid) {
        super.setCid(cid);
    }

    @Override
    public void setJobID(String jobID) {
        super.setJobID(jobID);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public LinkisJobSubmitExec getCopy() {
        LinkisJobSubmitExec ret = null;
        try {
            ret = (LinkisJobSubmitExec) this.clone();
        } catch (Exception e) {
            new ExecutorException(this.getJobStatus(), "EXE0007", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Cannot get copy of LinkisJobSubmitExec", e);
        }
        return ret;
    }
}