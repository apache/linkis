/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.protocol.query;

import com.webank.wedatasphere.linkis.protocol.task.Task;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Date;
import java.util.Map;

/**
 * Created by enjoyyin on 2018/9/30.
 */
public class RequestPersistTask  implements Task, QueryProtocol {
    private Long taskID;
    /**
     * Instance is an instance of the unified entry where the task is located. ip + port
     * instance 是指该task所在的统一入口的实例 ip + port
     */
    private String instance;
    private String execId;
    private String umUser;
    /**
     * EngineInstance is the instance information of the engine that the task executes, ip+port
     * engineInstance 是指task执行所请求的engine的实例信息，ip+port
     */
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
    /**
     * The executeApplicationName parameter refers to the service the user is looking for, such as spark python R, etc.
     * executeApplicationName 参数指的是用户所寻求的服务，比如spark python R等等
     */
    private String executeApplicationName;
    /**
     * requestApplicationName is the name of the creator, such as IDE or WTSS
     * requestApplicationName 是creator的传参名，例如IDE或WTSS等
     */
    private String requestApplicationName;
    /**
     * The user adopts the way of executing the script, and the scriptPath is the storage address of the script.
     * 用户采用传入执行脚本的方式，scriptPath就是脚本的存储地址
     */
    private String scriptPath;
    private java.util.Map<String, String> source;
    /**
     * The runType needs to be used in conjunction with the executeApplicationName. If the user selects Spark as the service, he also needs to specify which execution mode to use, such as pySpark RSpark.
     * runType and runType are the same attribute, in order to be compatible with the previous code
     * runType需要和executeApplicationName结合使用，如用户选择了Spark做为服务，他还需要指明使用哪种执行方式，比如pySpark RSpark等
     * runType和runType是同一个属性，为了兼容以前的代码
     */
    private String runType;
    private java.util.Map<String, Object> params;




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

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public Map<String, String> getSource() {
        return source;
    }

    public void setSource(Map<String, String> source) {
        this.source = source;
    }

    public String getRunType() {
        return runType;
    }

    public void setRunType(String runType) {
        this.runType = runType;
        this.engineType = runType;
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

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
        this.runType = engineType;
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

    public String getCode(){
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

    @Override
    public String toString() {
        return "RequestPersistTask{" +
                "taskID=" + taskID +
                ", instance='" + instance + '\'' +
                ", execId='" + execId + '\'' +
                ", umUser='" + umUser + '\'' +
                ", engineInstance='" + engineInstance + '\'' +
                ", executionCode='" + executionCode + '\'' +
                ", progress=" + progress +
                ", logPath='" + logPath + '\'' +
                ", resultLocation='" + resultLocation + '\'' +
                ", status='" + status + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                ", engineType='" + engineType + '\'' +
                ", errCode=" + errCode +
                ", errDesc='" + errDesc + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RequestPersistTask that = (RequestPersistTask) o;

        return new EqualsBuilder()
                .append(taskID, that.taskID)
                .append(instance, that.instance)
                .append(execId, that.execId)
                .append(umUser, that.umUser)
                .append(engineInstance, that.engineInstance)
                .append(executionCode, that.executionCode)
                .append(progress, that.progress)
                .append(logPath, that.logPath)
                .append(resultLocation, that.resultLocation)
                .append(status, that.status)
                .append(createdTime, that.createdTime)
                .append(updatedTime, that.updatedTime)
                .append(engineType, that.engineType)
                .append(errCode, that.errCode)
                .append(errDesc, that.errDesc)
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
                .append(engineType)
                .append(errCode)
                .append(errDesc)
                .toHashCode();
    }
}
