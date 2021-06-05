package com.webank.wedatasphere.linkis.manager.common.entity.persistence;

import java.util.Date;


public class PersistenceNodeMetricsEntity {
    private String  instance;
    private String  name;

    private int status;
    private String overLoad;
    private String heartBeatMsg;
    private String healthy;
    private Date updateTime;
    private Date createTime;

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOverLoad() {
        return overLoad;
    }

    public void setOverLoad(String overLoad) {
        this.overLoad = overLoad;
    }

    public String getHeartBeatMsg() {
        return heartBeatMsg;
    }

    public void setHeartBeatMsg(String heartBeatMsg) {
        this.heartBeatMsg = heartBeatMsg;
    }

    public String getHealthy() {
        return healthy;
    }

    public void setHealthy(String healthy) {
        this.healthy = healthy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


}
