package com.webank.wedatasphere.linkis.manager.common.entity.node;


import com.webank.wedatasphere.linkis.common.ServiceInstance;


public interface ScoreServiceInstance  {


    double getScore() ;

    void setScore(double score);

    ServiceInstance getServiceInstance();

    void setServiceInstance(ServiceInstance serviceInstance);

}
