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
 
package org.apache.linkis.cli.common.entity.job;

import org.apache.linkis.cli.common.entity.execution.CommonSubExecutionType;
import org.apache.linkis.cli.common.entity.execution.SubExecutionType;

/**
 * @description Job should encapsulate all data needed for an execution(submit/job-management)
 */
public abstract class Job {
    /**
     * ID for client itself
     */
    private String cid;

    private String submitUser;
    private String proxyUser;

    private SubExecutionType subExecutionType = CommonSubExecutionType.NONE;
    private OutputWay outputWay;
    private String outputPath;

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getSubmitUser() {
        return submitUser;
    }

    public void setSubmitUser(String user) {
        this.submitUser = user;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String user) {
        this.proxyUser = user;
    }

    public SubExecutionType getSubExecutionType() {
        return subExecutionType;
    }

    public void setSubExecutionType(SubExecutionType subExecutionType) {
        this.subExecutionType = subExecutionType;
    }


    public OutputWay getOutputWay() {
        return outputWay;
    }

    public void setOutputWay(OutputWay outputWay) {
        this.outputWay = outputWay;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    //
//  public String getJobInfoStr(Map<String, String> extraInfo) {
//    StringBuilder sb = new StringBuilder();
//    sb.append("UserName: ").append(getUser()).append(", ")
//        .append("JobID: ").append(getJobIDWithPrefix()).append(", ")
//        .append("SyncKey: ").append(getSyncKey()).append(", ")
//        .append("StartTime: ").append(CommonUtils.formatTime(getStartTime())).append(", ")
//        .append("EndTime: ").append(CommonUtils.formatTime(getEndTime())).append(", ")
//        .append("JobExecutionType: ").append(getSubExecutionType()).append(", ")
//        .append("JobStatus: ").append(getStatus()).append(", ")
//        .append("JobProgress: ").append(getProgress()).append(", ")
//        .append("ErrorCode: ").append(getErrorCode()).append(", ")
//        .append("ErrorMsg: ").append(getErrorMsg()).append(", ")
//        .append("ServerAddr: ").append(getServerAddr()).append(", ")
//        .append("ClientAddr: ").append(getClientAddr()).append(", ")
//        .append("ClientVersion: ").append(getClientVersion()).append(", ")
//        .append("ClientPath: ").append(getClientPath()).append(",")
//        .append("Elapse: ").append(getEndTime() - getStartTime()).append(",")
//        .append("ExtraInfo: ").append(extraInfo.toString());
//    return sb.toString();
//  }
}
