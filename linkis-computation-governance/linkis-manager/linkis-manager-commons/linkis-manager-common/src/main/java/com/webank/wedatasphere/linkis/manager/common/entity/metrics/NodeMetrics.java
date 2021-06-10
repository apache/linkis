package com.webank.wedatasphere.linkis.manager.common.entity.metrics;

import com.webank.wedatasphere.linkis.common.ServiceInstance;

import java.util.Date;


public interface NodeMetrics {

    ServiceInstance getServiceInstance();

    Integer getStatus();

    String getOverLoad();

    String getHeartBeatMsg();

    String getHealthy();

    void setHealthy(String healthy);

    Date getUpdateTime();

}
