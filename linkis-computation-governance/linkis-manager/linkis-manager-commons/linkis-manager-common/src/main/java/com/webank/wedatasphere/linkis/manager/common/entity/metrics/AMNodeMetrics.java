package com.webank.wedatasphere.linkis.manager.common.entity.metrics;

import com.webank.wedatasphere.linkis.common.ServiceInstance;

import java.util.Date;


public class AMNodeMetrics implements NodeMetrics {

    private Integer status;

    private String overLoad;

    private String heartBeatMsg;

    private String healthy;

    private ServiceInstance serviceInstance;

    private Date updateTime;


    @Override
    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
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
    public Date getUpdateTime() {
        return updateTime;
    }

    @Override
    public void setHealthy(String healthy) {
        this.healthy = healthy;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
