package com.webank.wedatasphere.linkis.manager.common.entity.persistence;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.common.entity.metrics.NodeMetrics;

import java.util.Date;


public class PersistenceNodeMetrics implements NodeMetrics {

    private String  instance;

    private int status;
    private String overLoad;
    private String heartBeatMsg;
    private String healthy;

    private Date updateTime;
    private Date createTime;


    private ServiceInstance serviceInstance;


    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }


    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }


    @Override
    public ServiceInstance getServiceInstance() {
        return this.serviceInstance;
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String getOverLoad() {
        return overLoad;
    }

    public void setOverLoad(String overLoad) {
        this.overLoad = overLoad;
    }

    @Override
    public String getHeartBeatMsg() {
        return heartBeatMsg;
    }

    public void setHeartBeatMsg(String heartBeatMsg) {
        this.heartBeatMsg = heartBeatMsg;
    }

    @Override
    public String getHealthy() {
        return healthy;
    }

    @Override
    public void setHealthy(String healthy) {
        this.healthy = healthy;
    }

    @Override
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
