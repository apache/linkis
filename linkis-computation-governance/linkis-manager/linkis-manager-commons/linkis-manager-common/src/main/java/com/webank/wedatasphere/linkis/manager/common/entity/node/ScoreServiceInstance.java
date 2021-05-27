package com.webank.wedatasphere.linkis.manager.common.entity.node;


import com.webank.wedatasphere.linkis.common.ServiceInstance;

/**
 * @author peacewong
 * @date 2020/6/7 21:12
 */
public interface ScoreServiceInstance  {


    double getScore() ;

    void setScore(double score);

    ServiceInstance getServiceInstance();

    void setServiceInstance(ServiceInstance serviceInstance);

}
