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
package com.webank.wedatasphere.linkis.bml.Entity;

import com.webank.wedatasphere.linkis.bml.common.Constant;
import com.webank.wedatasphere.linkis.bml.common.OperationEnum;
import com.webank.wedatasphere.linkis.bml.threading.TaskState;
import com.webank.wedatasphere.linkis.rpc.Sender;

import java.util.Date;
import java.util.Map;

/**
 * @author cooperyang
 * @date 2019-9-16
 */
public class ResourceTask {

    private long id;

    private String resourceId;

    private String version;

    /**
     * 操作类型.upload = 0, update = 1
     */
    private String operation;

    /**
     * 任务当前状态:Inited, Schduled, Running, Succeed, Failed,Cancelled
     */
    private String state;

    /**
     * 当前执行用户
     */
    private String submitUser;

    /**
     * 当前执行系统
     */
    private String system;

    /**
     * 物料库实例
     */
    private String instance;

    /**
     * 请求IP
     */
    private String clientIp;

    private String errMsg;

    private String extraParams;

    private Date startTime;

    private Date endTime;

    private Date lastUpdateTime;

    public static ResourceTask createUploadTask(String resourceId, String user,
        Map<String, Object> properties) {
        ResourceTask resourceTask = new ResourceTask();
        resourceTask.setResourceId(resourceId);
        resourceTask.setVersion(Constant.FIRST_VERSION);
        resourceTask.setOperation(OperationEnum.UPLOAD.getValue());
        resourceTask.setState(TaskState.SCHEDULED.getValue());
        resourceTask.setSubmitUser(user);
        resourceTask.setSystem((String)properties.get("system"));
        resourceTask.setClientIp((String)properties.get("clientIp"));
        resourceTask.setInstance(Sender.getThisInstance());
        resourceTask.setStartTime(new Date());
        resourceTask.setLastUpdateTime(new Date());
        return resourceTask;
    }

    public static ResourceTask createUpdateTask(String resourceId, String version, String user,
        String system, Map<String, Object> properties) {
        ResourceTask resourceTask = new ResourceTask();
        resourceTask.setResourceId(resourceId);
        resourceTask.setVersion(version);
        resourceTask.setOperation(OperationEnum.UPDATE.getValue());
        resourceTask.setState(TaskState.SCHEDULED.getValue());
        resourceTask.setSubmitUser(user);
        resourceTask.setClientIp((String)properties.get("clientIp"));
        resourceTask.setSystem(system);
        resourceTask.setInstance(Sender.getThisInstance());
        resourceTask.setStartTime(new Date());
        resourceTask.setLastUpdateTime(new Date());
        return resourceTask;
    }

  public static ResourceTask createDownloadTask(String resourceId, String version, String user,
      String system, String clientIp) {
    ResourceTask resourceTask = new ResourceTask();
    resourceTask.setResourceId(resourceId);
    resourceTask.setVersion(version);
    resourceTask.setOperation(OperationEnum.DOWNLOAD.getValue());
    resourceTask.setState(TaskState.RUNNING.getValue());
    resourceTask.setSubmitUser(user);
    resourceTask.setClientIp(clientIp);
    resourceTask.setSystem(system);
    resourceTask.setInstance(Sender.getThisInstance());
    resourceTask.setStartTime(new Date());
    resourceTask.setLastUpdateTime(new Date());
    return resourceTask;
  }

  public static ResourceTask createDeleteVersionTask(String resourceId, String version, String user,
      String system, String clientIp) {
    ResourceTask resourceTask = new ResourceTask();
    resourceTask.setResourceId(resourceId);
    resourceTask.setVersion(version);
    resourceTask.setOperation(OperationEnum.DELETE_VERSION.getValue());
    resourceTask.setState(TaskState.RUNNING.getValue());
    resourceTask.setSubmitUser(user);
    resourceTask.setClientIp(clientIp);
    resourceTask.setSystem(system);
    resourceTask.setInstance(Sender.getThisInstance());
    resourceTask.setStartTime(new Date());
    resourceTask.setLastUpdateTime(new Date());
    return resourceTask;
  }

  public static ResourceTask createDeleteResourceTask(String resourceId, String user, String system,
      String clientIp, String extraParams) {
    ResourceTask resourceTask = new ResourceTask();
    resourceTask.setResourceId(resourceId);
    resourceTask.setExtraParams(extraParams);
    resourceTask.setOperation(OperationEnum.DELETE_RESOURCE.getValue());
    resourceTask.setState(TaskState.RUNNING.getValue());
    resourceTask.setSubmitUser(user);
    resourceTask.setClientIp(clientIp);
    resourceTask.setSystem(system);
    resourceTask.setInstance(Sender.getThisInstance());
    resourceTask.setStartTime(new Date());
    resourceTask.setLastUpdateTime(new Date());
    return resourceTask;
  }

  public static ResourceTask createDeleteResourcesTask(String user, String system, String clientIp,
      String extraParams) {
    ResourceTask resourceTask = new ResourceTask();
    resourceTask.setExtraParams(extraParams);
    resourceTask.setOperation(OperationEnum.DELETE_RESOURCES.getValue());
    resourceTask.setState(TaskState.RUNNING.getValue());
    resourceTask.setSubmitUser(user);
    resourceTask.setClientIp(clientIp);
    resourceTask.setSystem(system);
    resourceTask.setInstance(Sender.getThisInstance());
    resourceTask.setStartTime(new Date());
    resourceTask.setLastUpdateTime(new Date());
    return resourceTask;
  }

  public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getSubmitUser() {
        return submitUser;
    }

    public void setSubmitUser(String submitUser) {
        this.submitUser = submitUser;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

  public String getExtraParams() {
    return extraParams;
  }

  public void setExtraParams(String extraParams) {
    this.extraParams = extraParams;
  }

  @Override
  public String toString() {
    return "ResourceTask{" + "id=" + id + ", resourceId='" + resourceId + '\'' + ", version='" +
        version + '\'' + ", operation='" + operation + '\'' + ", state='" + state + '\'' +
        ", submitUser='" + submitUser + '\'' + ", system='" + system + '\'' + ", instance='" +
        instance + '\'' + ", clientIp='" + clientIp + '\'' + ", errMsg='" + errMsg + '\'' +
        ", extraParams='" + extraParams + '\'' + ", startTime=" + startTime + ", endTime=" +
        endTime + ", lastUpdateTime=" + lastUpdateTime + '}';
  }
}
