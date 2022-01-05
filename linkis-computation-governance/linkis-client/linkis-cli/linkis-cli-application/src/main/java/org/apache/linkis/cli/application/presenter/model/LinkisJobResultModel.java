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


import org.apache.linkis.cli.core.presenter.model.JobExecModel;


public class LinkisJobResultModel extends JobExecModel {
    private String taskID;
    private String execID;
    private String user;
    private Integer totalPage = 1;
    private String logPath = null;
    private String resultLocation = null;
    private String[] resultSetPaths = null;
    private Object resultMetaData = null;
    private Object resultContent = null;
    private Integer errCode = null;
    private String errDesc = null;


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

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public String getResultLocation() {
        return resultLocation;
    }

    public void setResultLocation(String resultLocation) {
        this.resultLocation = resultLocation;
    }

    public String[] getResultSetPaths() {
        return resultSetPaths;
    }

    public void setResultSetPaths(String[] resultSetPaths) {
        this.resultSetPaths = resultSetPaths;
    }

    public Object getResultMetaData() {
        return resultMetaData;
    }

    public void setResultMetaData(Object resultMetaData) {
        this.resultMetaData = resultMetaData;
    }

    public Object getResultContent() {
        return resultContent;
    }

    public void setResultContent(Object resultContent) {
        this.resultContent = resultContent;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
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
}